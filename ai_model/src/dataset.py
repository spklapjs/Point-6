import os
import torch
from torch.utils.data import Dataset, DataLoader, random_split

class Point6Dataset(Dataset):
    def __init__(self, processed_dir):
        self.features = torch.load(os.path.join(processed_dir, 'features.pt'))
        self.labels = torch.load(os.path.join(processed_dir, 'labels.pt')).long()

    def __len__(self):
        return len(self.features)

    def __getitem__(self, idx):
        return self.features[idx], self.labels[idx]

def get_data_loaders(processed_dir, batch_size=32):
    dataset = Point6Dataset(processed_dir)
    
    total_size = len(dataset)
    train_size = int(0.7 * total_size)
    val_size = int(0.15 * total_size)
    test_size = total_size - train_size - val_size
    
    train_dataset, val_dataset, test_dataset = random_split(
        dataset, [train_size, val_size, test_size]
    )
    
    train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True)
    val_loader = DataLoader(val_dataset, batch_size=batch_size, shuffle=False)
    test_loader = DataLoader(test_dataset, batch_size=batch_size, shuffle=False)
    
    return train_loader, val_loader, test_loader
