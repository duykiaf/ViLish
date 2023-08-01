package t3h.android.admin.model;

import androidx.annotation.Nullable;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class Audio implements Serializable {
    private String id;
    private String name;
    private String audioFileFromFirebase;
    private String audioFileFromDevice;
    private String lyrics;
    private String translations;
    private int status;
    private String topicId;
    private String topicName;

    public Audio() {
    }

    public Audio(String id, String name, String audioFileFromFirebase, String audioFileFromDevice,
                 String lyrics, String translations, int status, String topicId, String topicName) {
        this.id = id;
        this.name = name;
        this.audioFileFromFirebase = audioFileFromFirebase;
        this.audioFileFromDevice = audioFileFromDevice;
        this.lyrics = lyrics;
        this.translations = translations;
        this.status = status;
        this.topicId = topicId;
        this.topicName = topicName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAudioFileFromFirebase() {
        return audioFileFromFirebase;
    }

    public void setAudioFileFromFirebase(String audioFileFromFirebase) {
        this.audioFileFromFirebase = audioFileFromFirebase;
    }

    public String getAudioFileFromDevice() {
        return audioFileFromDevice;
    }

    public void setAudioFileFromDevice(String audioFileFromDevice) {
        this.audioFileFromDevice = audioFileFromDevice;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getTranslations() {
        return translations;
    }

    public void setTranslations(String translations) {
        this.translations = translations;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Audio audio = (Audio) obj;
        return name.equalsIgnoreCase(audio.getName()) && status == audio.getStatus();
    }
}
