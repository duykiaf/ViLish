package t3h.android.vilishapp.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import t3h.android.vilishapp.R;
import t3h.android.vilishapp.helpers.AppConstant;
import t3h.android.vilishapp.models.Audio;
import t3h.android.vilishapp.repositories.DownloadedAudioRepository;

public class DownloadAudioService extends Service {
    private ThreadPoolExecutor executor;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private int completedDownloads, urlsSize;
    private HashMap<String, Audio> audioSelectedList = new HashMap<>();
    private DownloadedAudioRepository downloadedAudioRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        executor = new ThreadPoolExecutor(AppConstant.CORE_POOL_SIZE, AppConstant.MAX_DOWNLOAD_FILES,
                AppConstant.KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
        );
        sendNotification();
        downloadedAudioRepository = new DownloadedAudioRepository(getApplication());
    }

    private void sendNotification() {
        // Khởi tạo NotificationManager
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Khởi tạo NotificationCompat.Builder
        notificationBuilder = new NotificationCompat.Builder(this, AppConstant.CHANNEL_ID)
                .setSmallIcon(R.drawable.elife_logo)
                .setContentTitle(AppConstant.DOWNLOADING_TITLE)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        startForeground(AppConstant.NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendNotification();
        if (intent.getSerializableExtra(AppConstant.AUDIO_SELECTED_LIST) != null) {
            StringBuilder completedDownloadMessage = new StringBuilder();
            audioSelectedList = (HashMap<String, Audio>) intent.getSerializableExtra(AppConstant.AUDIO_SELECTED_LIST);
            executor.execute(() -> {
                urlsSize = audioSelectedList.size();

                for (Map.Entry<String, Audio> entry : audioSelectedList.entrySet()) {
                    downloadAudio(entry.getValue());
                }

                // tải xong thì dừng foreground trước
                stopForeground(true);
                // tạo thông báo mới
                completedDownloadMessage.append(completedDownloads).append("/").append(urlsSize);
                notificationBuilder.setContentTitle(AppConstant.DOWNLOAD_COMPLETE)
                        .setContentText(completedDownloadMessage.toString())
                        .setProgress(0, 0, false);
                notificationManager.notify(AppConstant.NOTIFICATION_ID, notificationBuilder.build());
                // đặt lại completedDownloads
                completedDownloads = 0;

                // gửi receiver thông báo đã hoàn tất quá trình tải
                Intent downloadCompletedIntent = new Intent(AppConstant.DOWNLOAD_COMPLETED_BROADCAST);
                downloadCompletedIntent.putExtra(AppConstant.DOWNLOAD_COMPLETE, true);
                sendBroadcast(downloadCompletedIntent);
            });
        }
        return START_NOT_STICKY;
    }

    // Thực hiện tải âm thanh từ audioUrl
    private void downloadAudio(Audio audio) {
        try {
            // thứ tự file đang tải
            StringBuilder currentFileDownloading = new StringBuilder();
            currentFileDownloading.append(completedDownloads + 1).append("/").append(urlsSize);

            String fileName = audio.getAudioFileName();

            // Tạo kết nối đến URL của tệp âm thanh trên Firebase Storage
            URL url = new URL(audio.getAudioFileFromFirebase());
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
                        .setContentTitle(AppConstant.DOWNLOADING_TITLE)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(currentFileDownloading.toString()))
                        .setProgress(100, progress, false);
                notificationManager.notify(AppConstant.NOTIFICATION_ID, notificationBuilder.build());
            }

            // tăng biến đếm khi đã tải xong 1 file
            completedDownloads++;

            // Đóng các luồng
            inputStream.close();
            outputStream.close();
            connection.disconnect();

            // lưu audio tải xuống vào cơ sở dữ liệu
            downloadedAudioRepository.insertDownloadedAudio(audio);
        } catch (IOException e) {
            e.printStackTrace();
        }
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