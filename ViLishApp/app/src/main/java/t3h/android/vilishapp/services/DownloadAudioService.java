package t3h.android.vilishapp.services;

import android.app.Notification;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.Scheduler;

import java.util.List;

public class MainDownloadService extends DownloadService {
    private static final int JOB_ID = 1;
    private static final int FOREGROUND_NOTIFICATION_ID = 1;

    protected MainDownloadService(int foregroundNotificationId, long foregroundNotificationUpdateInterval, @Nullable String channelId, int channelNameResourceId, int channelDescriptionResourceId) {
        super(FOREGROUND_NOTIFICATION_ID, DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL, channelId, channelNameResourceId, 0);
    }

    @Override
    protected DownloadManager getDownloadManager() {
        return null;
    }

    @Nullable
    @Override
    protected Scheduler getScheduler() {
        return null;
    }

    @Override
    protected Notification getForegroundNotification(List<Download> downloads, int notMetRequirements) {
        return null;
    }
}
