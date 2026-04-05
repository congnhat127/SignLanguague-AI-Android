package com.signlanguage.data.model

/**
 * Lớp dữ liệu đại diện cho kết quả nhận diện
 */
data class Recognition(
    val label: String,
    val confidence: Float
) {
    override fun toString(): String {
        return "$label / " + String.format("%.1f%%", confidence * 100.0f)
    }
}
