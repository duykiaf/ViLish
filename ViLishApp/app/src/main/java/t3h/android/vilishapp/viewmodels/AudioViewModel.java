package t3h.android.vilishapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.exoplayer2.ExoPlayer;

public class AudioViewModel extends ViewModel {
    private MutableLiveData<String> audioLyrics = new MutableLiveData<>();
    private MutableLiveData<String> audioTranslations = new MutableLiveData<>();
    private MutableLiveData<ExoPlayer> exoPlayerMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> bottomControlsVisible = new MutableLiveData<>(false);

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

    public void setExoPlayerMutableLiveData(ExoPlayer player) {
        exoPlayerMutableLiveData.setValue(player);
    }

    public LiveData<ExoPlayer> getExoplayer() {
        return exoPlayerMutableLiveData;
    }

    public void setBottomControlsVisible(Boolean isVisible) {
        bottomControlsVisible.setValue(isVisible);
    }

    public LiveData<Boolean> getBottomControlsVisibleValue() {
        return bottomControlsVisible;
    }
}
