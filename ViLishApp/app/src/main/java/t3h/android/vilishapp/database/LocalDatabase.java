package t3h.android.vilishapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import t3h.android.vilishapp.dao.BookmarksDao;
import t3h.android.vilishapp.models.Audio;

@Database(entities = {Audio.class}, version = 1, exportSchema = false)
public abstract class LocalDatabase extends RoomDatabase {
    public abstract BookmarksDao bookmarksDao();

    private static LocalDatabase INSTANCE;

    public static synchronized LocalDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, LocalDatabase.class, "vilish_db.db").build();
        }
        return INSTANCE;
    }
}
