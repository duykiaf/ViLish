package t3h.android.vilishapp.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;

import java.util.HashMap;

import t3h.android.vilishapp.models.Audio;

public class AudioViewModel extends AndroidViewModel {
    private MutableLiveData<String> audioTitle = new MutableLiveData<>();
    private MutableLiveData<String> audioLyrics = new MutableLiveData<>();
    private MutableLiveData<String> audioTranslations = new MutableLiveData<>();
    private ExoPlayer player;
    private MutableLiveData<String> topicIdLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> exoplayerStop = new MutableLiveData<>(true);
    private MutableLiveData<Boolean> onBottomControlClick = new MutableLiveData<>(false);
    private MutableLiveData<Integer> itemDownloadSelectedCounter = new MutableLiveData<>(0);
    private MutableLiveData<HashMap<String, Audio>> audioSelectedLiveData = new MutableLiveData<>();
    private MutableLiveData<HashMap<String, Integer>> audioCheckedPosListLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isAudioListScreen = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isBookmarksScreen = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isAudioDownloadedScreen = new MutableLiveData<>(false);
    private MutableLiveData<Integer> bookmarkDeletedPos = new MutableLiveData<>(-1);
    private MutableLiveData<Integer> downloadAudioDeletedPos = new MutableLiveData<>(-1);
    private MutableLiveData<Boolean> isDownloading = new MutableLiveData<>(false);

    public AudioViewModel(@NonNull Application application) {
        super(application);
        player = new ExoPlayer.Builder(application.getApplicationContext()).build();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
    }

    public void setAudioTitle(String audioTitle) {
        this.audioTitle.setValue(audioTitle);
    }

    public LiveData<String> getAudioTitle() {
        return audioTitle;
    }

    public void setAudioLyrics(String lyrics) {
        audioLyrics.setValue(lyrics);
    }

    public LiveData<String> getAudioLyrics() {
        return audioLyrics;
    }

    public void setAudioTranslations(String translations) {
        audioTranslations.setValue(translations);
    }

    public LiveData<String> getAudioTranslations() {
        return audioTranslations;
    }

    public ExoPlayer getExoplayer() {
        return player;
    }

    public void stopExoplayer() {
        if (player.isPlaying()) {
            player.stop();
        }
    }

    public void setTopicIdLiveData(String topicId) {
        topicIdLiveData.setValue(topicId);
    }

    public String getTopicIdLiveData() {
        return topicIdLiveData.getValue();
    }

    public void setStopState(Boolean isStopped) {
        exoplayerStop.setValue(isStopped);
    }

    public LiveData<Boolean> getExoplayerState() {
        return exoplayerStop;
    }

    public void setBottomControlClick(Boolean clicked) {
        onBottomControlClick.setValue(clicked);
    }

    public Boolean getBottomControlClickListener() {
        return onBottomControlClick.getValue();
    }

    public void setItemDownloadSelectedCounter(int counter) {
        itemDownloadSelectedCounter.setValue(counter);
    }

    public LiveData<Integer> getItemDownloadSelectedCounter() {
        return itemDownloadSelectedCounter;
    }

    public void setAudioSelected(HashMap<String, Audio> audioSelected) {
        audioSelectedLiveData.setValue(audioSelected);
    }

    public LiveData<HashMap<String, Audio>> getAudioSelected() {
        return audioSelectedLiveData;
    }

    public void setAudioCheckedPosListLiveData(HashMap<String, Integer> audioCheckedPosList) {
        audioCheckedPosListLiveData.setValue(audioCheckedPosList);
    }

    public LiveData<HashMap<String, Integer>> getAudioCheckedPosList() {
        return audioCheckedPosListLiveData;
    }

    public void setIsAudioListScreen(Boolean isAudioListScreen) {
        this.isAudioListScreen.setValue(isAudioListScreen);
    }

    public LiveData<Boolean> audioListScreenFlag() {
        return isAudioListScreen;
    }

    public void setIsBookmarksScreen(Boolean isBookmarksScreen) {
        this.isBookmarksScreen.setValue(isBookmarksScreen);
    }

    public LiveData<Boolean> bookmarksScreenFlag() {
        return isBookmarksScreen;
    }

    public void setIsAudioDownloadedScreen(Boolean isAudioDownloadedScreen) {
        this.isAudioDownloadedScreen.setValue(isAudioDownloadedScreen);
    }

    public LiveData<Boolean> audioDownloadedScreenFlag() {
        return isAudioDownloadedScreen;
    }

    public void setBookmarkDeletedPos(Integer bookmarkDeletedPos) {
        this.bookmarkDeletedPos.setValue(bookmarkDeletedPos);
    }

    public Integer getBookmarkDeletedPos() {
        return bookmarkDeletedPos.getValue();
    }

    public void setDownloadAudioDeletePos(Integer downloadAudioDeletePos) {
        this.downloadAudioDeletedPos.setValue(downloadAudioDeletePos);
    }

    public Integer getDownloadAudioDeletePos() {
        return downloadAudioDeletedPos.getValue();
    }

    public void setIsDownloading(Boolean isDownloading) {
        this.isDownloading.setValue(isDownloading);
    }

    public Boolean getDownloadingFlag() {
        return isDownloading.getValue();
    }
}
