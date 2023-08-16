package t3h.android.vilishapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AudioViewModel extends ViewModel {
    private MutableLiveData<String> audioLyrics = new MutableLiveData<>();
    private MutableLiveData<String> audioTranslations = new MutableLiveData<>();

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
}
