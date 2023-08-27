package t3h.android.vilishapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import t3h.android.vilishapp.models.Audio;

@Dao
public interface BookmarksDao {
    @Query("select * from bookmarks")
    LiveData<List<Audio>> getAllList();

    @Query("select id from bookmarks")
    LiveData<List<String>> getBookmarkAudioIds();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addBookmark(Audio audio);

    @Delete
    int deleteBookmark(Audio audio);
}
