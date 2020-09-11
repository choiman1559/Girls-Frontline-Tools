package com.fqxd.gftools.features.noti;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.toolbox.JsonObjectRequest;
import com.fqxd.gftools.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MessageSendClass extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String TOPIC = "/topics/" + getSharedPreferences("com.fqxd.gftools_preferences", MODE_PRIVATE).getString("uid", "");
        Intent intent = getIntent();
        String Package = intent.getStringExtra("package");
        String DEVICE_ID = intent.getStringExtra("device_id");
        String DEVICE_NAME = intent.getStringExtra("device_name");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("원격 실핼");
        builder.setMessage("원격으로 소전을 실행하시겠습니까?\n패키지 : " + Package + "\n기기명 : " + DEVICE_NAME);
        builder.setPositiveButton("원격 실행", (dialog, which) -> {
            JSONObject notificationHead = new JSONObject();
            JSONObject notifcationBody = new JSONObject();
            try {
                notifcationBody.put("Package", Package);
                notifcationBody.put("type", "reception");
                notifcationBody.put("device_name", DEVICE_NAME);
                notifcationBody.put("device_id",  DEVICE_ID);
                notifcationBody.put("from_name", Build.MANUFACTURER + " " + Build.MODEL);

                notificationHead.put("to", TOPIC);
                notificationHead.put("data", notifcationBody);
            } catch (JSONException e) {
                Log.e("Noti", "onCreate: " + e.getMessage());
            }
            sendNotification(notificationHead);
            finish();
        });
        builder.setNegativeButton("취소", (dialog, which) -> finish());
        builder.show();
    }

    private void sendNotification(JSONObject notification) {
        final String FCM_API = "https://fcm.googleapis.com/fcm/send";
        final String serverKey = "key=" + getString(R.string.serverKey);
        final String contentType = "application/json";
        final String TAG = "NOTIFICATION TAG";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                response -> Log.i(TAG, "onResponse: " + response.toString()),
                error -> {
                    Toast.makeText(MessageSendClass.this, "Request error", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "onErrorResponse: Didn't work");
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }
}
