package t3h.android.vilishapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import t3h.android.vilishapp.models.Audio;
import t3h.android.vilishapp.models.DownloadedAudio;

@Dao
public interface DownloadedAudioDao {
    @Query("select * from downloaded_audio")
    LiveData<List<Audio>> getDownloadedAudioList();

    @Query("select id from downloaded_audio")
    LiveData<List<String>> getDownloadedAudioIds();

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = DownloadedAudio.class)
    void insertDownloadedAudio(Audio audio);

    @Query("delete from downloaded_audio where id = :id")
    int deleteDownloadedAudioById(String id);

    @Query("delete from downloaded_audio")
    void deleteAllDownloadedAudios();
}
