package com.app.gdl.utils

enum class SlotType(val code: String, val label: String) {
    TBL("tbls", "Top Banner Left Side"),
    TBR("tbrs", "Top Banner Right Side"),
    MS("ms", "Middle Section"),
    BBLS("bbls", "Bottom Banner Left Side"),
    BBRS("bbrs", "Bottom Banner Right Side"),
    UNKNOWN("unknown", "Unknown");

    companion object {
        fun fromCode(code: String?): SlotType {
            return values().find { it.code == code } ?: UNKNOWN
        }
    }
}
