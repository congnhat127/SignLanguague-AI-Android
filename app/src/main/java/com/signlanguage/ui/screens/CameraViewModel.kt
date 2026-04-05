package com.signlanguage.ui.screens

import androidx.lifecycle.ViewModel
import com.signlanguage.data.model.Recognition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel quản lý trạng thái của màn hình Camera
 */
class CameraViewModel : ViewModel() {

    // Trạng thái nhận diện hiện tại (từ đơn)
    private val _currentRecognition = MutableStateFlow<Recognition?>(null)
    val currentRecognition: StateFlow<Recognition?> = _currentRecognition.asStateFlow()

    // Cả câu đã dịch xong
    private val _fullSentence = MutableStateFlow("")
    val fullSentence: StateFlow<String> = _fullSentence.asStateFlow()

    // Tốc độ khung hình (FPS)
    private val _fps = MutableStateFlow(0)
    val fps: StateFlow<Int> = _fps.asStateFlow()

    // Trạng thái hệ thống (đang quét, lỗi, vv)
    private val _status = MutableStateFlow("Đang sẵn sàng...")
    val status: StateFlow<String> = _status.asStateFlow()

    fun updateRecognition(recognition: Recognition?) {
        _currentRecognition.value = recognition
        if (recognition != null) {
            _status.value = "Đang nhận diện..."
        } else {
            _status.value = "Đang quét cử chỉ tay..."
        }
    }

    fun updateFps(newFps: Int) {
        _fps.value = newFps
    }

    fun addToSentence(word: String) {
        if (_fullSentence.value.isEmpty()) {
            _fullSentence.value = word
        } else {
            _fullSentence.value += " $word"
        }
    }

    fun clearSentence() {
        _fullSentence.value = ""
    }
}
