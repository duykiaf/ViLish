package t3h.android.vilishapp.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;

public class AudioViewModel extends AndroidViewModel {
    private MutableLiveData<String> audioTitle = new MutableLiveData<>();
    private MutableLiveData<String> audioLyrics = new MutableLiveData<>();
    private MutableLiveData<String> audioTranslations = new MutableLiveData<>();
    private ExoPlayer player;
    private MutableLiveData<String> topicIdLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> exoplayerStop = new MutableLiveData<>(true);
    private MutableLiveData<Boolean> onBottomControlClick = new MutableLiveData<>(false);

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
}
