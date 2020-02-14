package com.fqxd.gftools.features.noti;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.application.isradeleon.notify.Notify;
import com.fqxd.gftools.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import static com.crashlytics.android.core.CrashlyticsCore.TAG;

@Deprecated
public class FireStoreListener extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        startService(new Intent(getApplicationContext(),FireStoreListener.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(getSharedPreferences("NotiPrefs",MODE_PRIVATE).getBoolean("Enabled",false)) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            final DocumentReference docRef = db.collection(getSharedPreferences("NotiPrefs", MODE_PRIVATE).getString("uid", "error")).document("data");
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        String packagename = snapshot.getString("package");
                        CharSequence title = snapshot.getString("title");
                        CharSequence text = snapshot.getString("text");
                        CharSequence subtext = snapshot.getString("subtext");

                        Notify.create(FireStoreListener.this)
                                .setTitle(title.toString())
                                .setContent(text.toString())
                                .setLargeIcon(R.drawable.gf_icon)
                                .setImportance(Notify.NotificationImportance.MAX)
                                .setSmallIcon(R.drawable.start_xd)
                                .enableVibration(true)
                                .setAutoCancel(true)
                                .show();
                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                }
            });
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }
}
