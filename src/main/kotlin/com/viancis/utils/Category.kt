package com.viancis.utils


enum class Category(val colorCode: String) {
    ERROR("\u001B[31m"),  // Красный цвет
    INFO("\u001B[32m"),    // Зеленый цвет
    WARNING("\u001B[33m"), // Желтый цвет
    DEBUG("\u001B[35m"),  // Херовый цвет
    SUCCESS("\u001B[36m"),  // Херовый цвет
    CONVENTION("\u001B[34m"),  // Херовый цвет
    RESET("\u001B[0m");    // Сброс цвета
}