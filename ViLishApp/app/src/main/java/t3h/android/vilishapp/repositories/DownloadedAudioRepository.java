package t3h.android.vilishapp.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import t3h.android.vilishapp.dao.DownloadedAudioDao;
import t3h.android.vilishapp.database.LocalDatabase;
import t3h.android.vilishapp.models.Audio;
import t3h.android.vilishapp.models.DownloadedAudio;

public class DownloadedAudioRepository {
    private DownloadedAudioDao downloadedAudioDao;

    public DownloadedAudioRepository() {
    }

    public DownloadedAudioRepository(Application application) {
        downloadedAudioDao = LocalDatabase.getInstance(application).downloadedAudioDao();
    }

    public LiveData<List<Audio>> getDownloadedAudioList() {
        return downloadedAudioDao.getDownloadedAudioList();
    }

    public LiveData<List<String>> getDownloadedAudioIds() {
        return downloadedAudioDao.getDownloadedAudioIds();
    }

    public void insertDownloadedAudio(Audio audio) {
        downloadedAudioDao.insertDownloadedAudio(audio);
    }

    public int deleteSingleDownloadedAudio(Audio audio) {
        return downloadedAudioDao.deleteSingleDownloadedAudio(audio);
    }

    public void deleteAllDownloadedAudios() {
        downloadedAudioDao.deleteAllDownloadedAudios();
    }
}
