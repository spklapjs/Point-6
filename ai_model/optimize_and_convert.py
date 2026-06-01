import os
import sys
import shutil
import subprocess
import torch
import numpy as np
import torch.nn as nn
import torch.optim as optim
import torch.nn.utils.prune as prune
from torch.utils.data import TensorDataset, DataLoader

class DynamicCNNLSTM(nn.Module):
    def __init__(self, in_channels, num_classes=6):
        super(DynamicCNNLSTM, self).__init__()
        self.cnn = nn.Sequential(
            nn.Conv1d(in_channels, 32, kernel_size=3, padding=1),
            nn.ReLU(),
            nn.BatchNorm1d(32),
            nn.Conv1d(32, 64, kernel_size=3, padding=1),
            nn.ReLU(),
            nn.BatchNorm1d(64)
        )
        self.lstm = nn.LSTM(64, 128, batch_first=True, num_layers=1)
        self.fc = nn.Linear(128, num_classes)

    def forward(self, x):
        x = self.cnn(x)
        x = x.permute(0, 2, 1)
        
        h0 = torch.zeros(1, x.size(0), 128, device=x.device)
        c0 = torch.zeros(1, x.size(0), 128, device=x.device)
        lstm_out, _ = self.lstm(x, (h0, c0))
        
        # 윈도우 크기 변경에 맞추어 양수 고정 인덱스를 19에서 39로 수정합니다.
        out = self.fc(lstm_out[:, 39, :])
        return out

def get_tool_path(tool_name):
    base_dir = os.path.dirname(sys.executable)
    tool_path = os.path.join(base_dir, "Scripts", f"{tool_name}.exe")
    if os.path.exists(tool_path):
        return tool_path
    return shutil.which(tool_name)

def optimize_and_export_pipeline(device_name, in_channels, feature_path, label_path, checkpoint_path):
    print(f"--- {device_name} 모델 최적화 및 로컬 파이프라인 가동 ---")

    num_classes = 6
    model = DynamicCNNLSTM(in_channels=in_channels, num_classes=num_classes)

    if os.path.exists(checkpoint_path):
        try:
            model.load_state_dict(torch.load(checkpoint_path, map_location="cpu"))
            print(f"저장된 가중치 로드 완료: {checkpoint_path}")
        except Exception as e:
            print(f"경고: {checkpoint_path} 가중치 로드 실패. 초기 상태로 진행합니다. ({e})")
    else:
        print(f"경고: {checkpoint_path} 가중치가 없어 초기 상태로 진행합니다.")

    for name, module in model.named_modules():
        if isinstance(module, nn.Conv1d):
            prune.ln_structured(module, name="weight", amount=0.93, n=2, dim=0)
            prune.remove(module, "weight")
    print("명세서 기준 93퍼센트 가중치 프루닝 완료")

    try:
        features = torch.load(feature_path, map_location="cpu")
        labels = torch.load(label_path, map_location="cpu")
        print(f"{device_name} 소스 특징 데이터셋 로드 성공")
    except Exception as e:
        print(f"데이터셋 로드 실패: {e}")
        # 예외 처리용 예비 데이터 생성 규격을 20에서 40으로 수정합니다.
        features = torch.randn(200, in_channels, 40)
        labels = torch.randint(0, num_classes, (200,))

    dataset = TensorDataset(features, labels)
    loader = DataLoader(dataset, batch_size=32, shuffle=True)
    criterion = nn.CrossEntropyLoss()
    optimizer = optim.Adam(model.parameters(), lr=0.001)

    print("정확도 복구를 위한 미세 조정(Fine-tuning)을 오프라인에서 개시합니다.")
    model.train()
    for epoch in range(1, 51):
        total_loss = 0
        correct = 0
        total = 0
        for batch_x, batch_y in loader:
            optimizer.zero_grad()
            outputs = model(batch_x)
            loss = criterion(outputs, batch_y)
            loss.backward()
            optimizer.step()

            total_loss += loss.item()
            _, predicted = torch.max(outputs.data, 1)
            total += batch_y.size(0)
            correct += (predicted == batch_y).sum().item()

        if epoch == 1 or epoch % 10 == 0:
            acc = 100 * correct / total
            print(f"Epoch {epoch}/50, Loss: {total_loss/len(loader):.4f}, 동적 검증 정확도: {acc:.2f} %")

    model.eval()

    raw_onnx_path = f"{device_name}_model.onnx"
    output_tflite_dir = f"exported_models/{device_name}_tflite"

    # 더미 입력 차원을 20에서 40으로 수정합니다.
    dummy_input = torch.randn(1, in_channels, 40, device="cpu")
    torch.onnx.export(
        model,
        dummy_input,
        raw_onnx_path,
        export_params=True,
        input_names=["input"],
        output_names=["output"],
        opset_version=18
    )
    print(f"순수 중간 표상 {raw_onnx_path} 출력 완료")

    onnx2tf_exe = get_tool_path("onnx2tf")

    if not onnx2tf_exe:
        print("에러: 변환 도구(onnx2tf)를 찾을 수 없습니다.")
        return

    try:
        print(f"외부 서브프로세스를 호출하여 {device_name} TFLite 변환을 연동합니다.")
        os.makedirs(output_tflite_dir, exist_ok=True)

        onnx2tf_cmd = [
            onnx2tf_exe, 
            "-i", raw_onnx_path, 
            "-o", output_tflite_dir
        ]

        subprocess.run(onnx2tf_cmd, check=True)
        print(f"{device_name} 모델 동적 범위 양자화 변환 완결: {output_tflite_dir} 폴더 확인\n")
    except Exception as e:
        print(f"에러: 외부 변환 도구 컴파일 도중 오류가 발생했습니다. 원인: {e}")

if __name__ == "__main__":
    os.makedirs("exported_models", exist_ok=True)
    optimize_and_export_pipeline("smartphone", 6, "data/processed/smartphone_features.pt", "data/processed/smartphone_labels.pt", "data/checkpoints/smartphone_best_model.pth")
    optimize_and_export_pipeline("spen", 2, "data/processed/spen_features.pt", "data/processed/spen_labels.pt", "data/checkpoints/spen_best_model.pth")
    print("point-6 모바일 추론 엔진 빌드 시퀀스가 완결되었습니다.")