package t3h.android.vilishapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import t3h.android.vilishapp.dao.BookmarksDao;
import t3h.android.vilishapp.dao.DownloadedAudioDao;
import t3h.android.vilishapp.models.Audio;
import t3h.android.vilishapp.models.DownloadedAudio;

@Database(entities = {Audio.class, DownloadedAudio.class}, version = 1, exportSchema = false)
public abstract class LocalDatabase extends RoomDatabase {
    public abstract BookmarksDao bookmarksDao();
    public abstract DownloadedAudioDao downloadedAudioDao();

    private static LocalDatabase INSTANCE;

    public static synchronized LocalDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, LocalDatabase.class, "vilish_db.db").build();
        }
        return INSTANCE;
    }
}
