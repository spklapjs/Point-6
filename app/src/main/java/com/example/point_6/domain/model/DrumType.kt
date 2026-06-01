package com.example.point_6.domain.model

enum class DrumType(val index: Int) {
    CYMBAL1(0),
    TOM1(1),
    CYMBAL2(2),
    HI_HAT(3),
    SNARE(4),
    TOM2(5);

    companion object {
        fun fromIndex(index: Int): DrumType {
            return values().firstOrNull { it.index == index } ?: SNARE
        }
    }
}