package com.signlanguage.ui.screens

import androidx.lifecycle.ViewModel
import com.signlanguage.data.model.Recognition
import com.signlanguage.data.remote.SignLanguageWebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraViewModel : ViewModel() {

    private val _currentRecognition = MutableStateFlow<Recognition?>(null)
    val currentRecognition: StateFlow<Recognition?> = _currentRecognition.asStateFlow()

    private val _fullSentence = MutableStateFlow("")
    val fullSentence: StateFlow<String> = _fullSentence.asStateFlow()

    private val _fps = MutableStateFlow(0)
    val fps: StateFlow<Int> = _fps.asStateFlow()

    private val _status = MutableStateFlow("Đang kết nối server...")
    val status: StateFlow<String> = _status.asStateFlow()

    private var webSocketClient: SignLanguageWebSocketClient? = null
    
    // Logic to avoid adding same word multiple times too quickly
    private var lastAddedWord = ""
    private var frameCounter = 0
    private val CONFIRMATION_THRESHOLD = 15 

    fun initWebSocket(url: String) {
        webSocketClient = SignLanguageWebSocketClient(url, object : SignLanguageWebSocketClient.WebSocketCallback {
            override fun onPredictionReceived(label: String, confidence: Float) {
                val recognition = Recognition(label, confidence)
                _currentRecognition.value = recognition
                _status.value = "Đang nhận diện..."
                
                // Logic to build sentence
                if (label != lastAddedWord && confidence > 0.8f) {
                    frameCounter++
                    if (frameCounter >= CONFIRMATION_THRESHOLD) {
                        addToSentence(label)
                        lastAddedWord = label
                        frameCounter = 0
                    }
                } else if (label == lastAddedWord) {
                    frameCounter = 0
                }
            }

            override fun onConnected() {
                _status.value = "Đã kết nối Server"
            }

            override fun onDisconnected() {
                _status.value = "Mất kết nối Server"
            }

            override fun onError(error: String?) {
                _status.value = "Lỗi: $error"
            }
        })
        webSocketClient?.connect()
    }

    fun sendImageToServer(imageBytes: ByteArray) {
        webSocketClient?.sendImage(imageBytes)
    }

    fun updateFps(newFps: Int) {
        _fps.value = newFps
    }

    private fun addToSentence(word: String) {
        if (_fullSentence.value.isEmpty()) {
            _fullSentence.value = word
        } else {
            _fullSentence.value += " $word"
        }
    }

    fun clearSentence() {
        _fullSentence.value = ""
        lastAddedWord = ""
    }

    override fun onCleared() {
        super.onCleared()
        webSocketClient?.disconnect()
    }
}
