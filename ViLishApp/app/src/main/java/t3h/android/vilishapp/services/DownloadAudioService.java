package t3h.android.vilishapp.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import t3h.android.vilishapp.R;
import t3h.android.vilishapp.helpers.AppConstant;

public class DownloadAudioService extends Service {
    private ThreadPoolExecutor executor;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    List<String> urls = new ArrayList<>();
    private int index, completedDownloads;

    @Override
    public void onCreate() {
        super.onCreate();

        // Khởi tạo ThreadPoolExecutor với số lượng luồng tùy chọn
        executor = new ThreadPoolExecutor(AppConstant.CORE_POOL_SIZE, AppConstant.MAX_DOWNLOAD_FILES,
                10, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
        );

        // Khởi tạo NotificationManager
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Khởi tạo NotificationCompat.Builder
        notificationBuilder = new NotificationCompat.Builder(this, AppConstant.CHANNEL_ID)
                .setSmallIcon(R.drawable.play_circle_blue_outline_ic)
                .setContentTitle("Downloading Audio")
                .setContentText("2/10")
                .setProgress(100, 0, false)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        startForeground(AppConstant.NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        urls.add("https://firebasestorage.googleapis.com/v0/b/vi-lish.appspot.com/o/Audios%2F1690899210731?alt=media&token=18934ea0-d6f9-4a75-b281-fc3a14b19822");
        urls.add("https://firebasestorage.googleapis.com/v0/b/vi-lish.appspot.com/o/Audios%2F1690899527587?alt=media&token=d1e3e524-d174-4ec4-b138-c969174260c9");
        urls.add("https://firebasestorage.googleapis.com/v0/b/vi-lish.appspot.com/o/Audios%2F1691763383466?alt=media&token=127abe36-df30-4915-8820-cbd395121a46");
        urls.add("https://firebasestorage.googleapis.com/v0/b/vi-lish.appspot.com/o/Audios%2F1691762330831?alt=media&token=f8e4b9a0-716d-4db6-a830-33d6fda3da34");
        urls.add("https://firebasestorage.googleapis.com/v0/b/vi-lish.appspot.com/o/Audios%2F1691762397930?alt=media&token=d6defe84-9c2b-476c-b90c-395f126d4e89");

        executor.execute(() -> {
            for (String url : urls) {
                index++;
                downloadAudio(url, "audio_" + index + ".mp3");
            }
        });
        return START_NOT_STICKY;
    }

    private void downloadAudio(String audioUrl, String fileName) {
        executor.execute(() -> {
            // Thực hiện tải âm thanh từ audioUrl
            try {
                // Tạo kết nối đến URL của tệp âm thanh trên Firebase Storage
                URL url = new URL(audioUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int totalBytes = connection.getContentLength();

                // Mở luồng đọc dữ liệu từ kết nối
                InputStream inputStream = connection.getInputStream();

                // Tạo một tệp trên internal storage để lưu trữ tệp âm thanh
                File file = new File(this.getFilesDir(), fileName);
                FileOutputStream outputStream = new FileOutputStream(file);

                // Đọc dữ liệu từ luồng đầu vào và ghi vào tệp trên internal storage
                byte[] buffer = new byte[1024];
                int bytesRead;
                int downloadedBytes = 0;
                int progress;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);

                    // Cập nhật tiến trình dựa trên số byte đã tải xuống
                    downloadedBytes += bytesRead;
                    progress = downloadedBytes * 100 / totalBytes;

                    notificationBuilder
                            .setContentTitle("Downloading Audio").setProgress(100, progress, false);
                    notificationManager.notify(AppConstant.NOTIFICATION_ID, notificationBuilder.build());
                }

                // Increment the completedDownloads count
                completedDownloads++;

                // If all downloads are complete, update the notification and stop the service
                if (completedDownloads == urls.size()) {
                    notificationBuilder.setContentText("Download Complete")
                            .setProgress(0, 0, false);
                    notificationManager.notify(AppConstant.NOTIFICATION_ID, notificationBuilder.build());
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(this::stopSelf, 4000);
                }

                // Đóng các luồng
                inputStream.close();
                outputStream.close();
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}