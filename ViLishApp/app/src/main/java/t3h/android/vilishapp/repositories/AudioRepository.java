package t3h.android.vilishapp.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import t3h.android.vilishapp.dao.BookmarksDao;
import t3h.android.vilishapp.database.LocalDatabase;
import t3h.android.vilishapp.models.Audio;

public class AudioRepository {
    private BookmarksDao bookmarksDao;

    public AudioRepository() {
    }

    public AudioRepository(Application application) {
        bookmarksDao = LocalDatabase.getInstance(application).bookmarksDao();
    }

    public LiveData<List<Audio>> getBookmarkList() {
        return bookmarksDao.getAllList();
    }

    public LiveData<List<String>> getBookmarkAudioIds() {
        return bookmarksDao.getBookmarkAudioIds();
    }

    public void addBookmark(Audio audio) {
        bookmarksDao.addBookmark(audio);
    }

    public int deleteBookmark(Audio audio) {
        if (audio != null) {
            return bookmarksDao.deleteBookmark(audio);
        }
        return 0;
    }

    public void deleteAllBookmarks() {
        bookmarksDao.deleteAllBookmark();
    }
}
