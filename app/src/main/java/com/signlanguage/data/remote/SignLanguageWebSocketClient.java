package com.signlanguage.data.remote;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class SignLanguageWebSocketClient {
    private static final String TAG = "SignLanguageWS";
    private WebSocket webSocket;
    private final OkHttpClient client;
    private final WebSocketCallback callback;
    private final String serverUrl;

    public interface WebSocketCallback {
        void onPredictionReceived(String label, float confidence);
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }

    public SignLanguageWebSocketClient(String serverUrl, WebSocketCallback callback) {
        this.serverUrl = serverUrl;
        this.callback = callback;
        // Cấu hình timeout dài để tránh ngắt kết nối khi mạng chậm
        this.client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public void connect() {
        if (webSocket != null) {
            return;
        }
        
        Request request = new Request.Builder().url(serverUrl).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                Log.d(TAG, "Connected to server");
                SignLanguageWebSocketClient.this.webSocket = webSocket;
                if (callback != null) callback.onConnected();
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                try {
                    JSONObject json = new JSONObject(text);
                    // Dựa vào format README: {"type": "prediction", "data": {"top_class": "...", "confidence": ...}}
                    if (json.has("type") && json.getString("type").equals("prediction")) {
                        JSONObject data = json.getJSONObject("data");
                        String label = data.getString("top_class");
                        double confidence = data.getDouble("confidence");
                        if (callback != null) callback.onPredictionReceived(label, (float) confidence);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON: " + text, e);
                }
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                webSocket.close(1000, null);
                SignLanguageWebSocketClient.this.webSocket = null;
                if (callback != null) callback.onDisconnected();
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                Log.e(TAG, "WebSocket Failure: " + t.getMessage());
                SignLanguageWebSocketClient.this.webSocket = null;
                if (callback != null) callback.onError(t.getMessage());
            }
            
            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                SignLanguageWebSocketClient.this.webSocket = null;
                if (callback != null) callback.onDisconnected();
            }
        });
    }

    public void sendImage(byte[] imageBytes) {
        if (webSocket != null) {
            String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
            try {
                JSONObject request = new JSONObject();
                request.put("type", "predict");
                request.put("image_base64", base64Image);
                webSocket.send(request.toString());
            } catch (JSONException e) {
                Log.e(TAG, "Error creating request", e);
            }
        } else {
            Log.w(TAG, "WebSocket is not connected. Cannot send image.");
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "App closed");
            webSocket = null;
        }
    }
}
