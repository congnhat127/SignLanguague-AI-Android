package com.signlanguage.data.ml

import android.content.Context
import android.graphics.Bitmap
import com.signlanguage.data.model.Recognition
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

/**
 * Lớp xử lý nhận diện ngôn ngữ ký hiệu sử dụng TFLite Task Library
 */
class SignLanguageClassifier(private val context: Context) {

    private var classifier: ImageClassifier? = null

    init {
        setupClassifier()
    }

    private fun setupClassifier() {
        val options = ImageClassifier.ImageClassifierOptions.builder()
            .setMaxResults(1) // Lấy kết quả tốt nhất
            .setScoreThreshold(0.5f) // Chỉ lấy kết quả có độ tin cậy > 50%
            .build()

        try {
            classifier = ImageClassifier.createFromFileAndOptions(
                context,
                "sign_language_model.tflite",
                options
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun classify(bitmap: Bitmap): List<Recognition> {
        if (classifier == null) return emptyList()

        // Chuyển đổi Bitmap sang TensorImage
        val image = TensorImage.fromBitmap(bitmap)
        
        // Chạy inference
        val results = classifier?.classify(image)

        // Chuyển đổi kết quả sang định dạng Recognition của chúng ta
        return results?.flatMap { classification ->
            classification.categories.map { category ->
                Recognition(
                    label = category.label,
                    confidence = category.score
                )
            }
        } ?: emptyList()
    }

    fun close() {
        classifier?.close()
        classifier = null
    }
}
