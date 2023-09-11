package t3h.android.vilishapp.helpers;

import androidx.media3.database.DatabaseProvider;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.cache.Cache;
import androidx.media3.exoplayer.offline.DownloadManager;
import androidx.media3.exoplayer.offline.DownloadNotificationHelper;

import java.io.File;

public class DownloadManagerHelper {
    public static final String DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel";
    private static final boolean USE_CRONET_FOR_NETWORKING = true;
    private static final String TAG = "DownloadManagerHelper";
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";
    private static DataSource.Factory dataSourceFactory;
    private static DataSource.Factory httpDataSourceFactory;
    private static DatabaseProvider databaseProvider;
    private static File downloadDirectory;
    private static Cache downloadCache;
    private static DownloadManager downloadManager;
//    private static DownloadTracker downloadTracker;
    private static DownloadNotificationHelper downloadNotificationHelper;


}
