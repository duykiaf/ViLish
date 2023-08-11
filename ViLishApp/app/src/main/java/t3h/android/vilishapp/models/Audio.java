package t3h.android.vilishapp.models;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@IgnoreExtraProperties
@Entity(tableName = "bookmarks")
public class Audio implements Serializable {
    @PrimaryKey
    @SerializedName("id")
    private int intId;

    @SerializedName("audio_str_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("audio_file_from_device")
    private String audioFileFromDevice;

    @SerializedName("lyrics")
    private String lyrics;

    @SerializedName("translations")
    private String translations;

    @SerializedName("topic_id")
    private String topicId;

    @SerializedName("topic_name")
    private String topicName;

    public Audio() {
    }

    public Audio(String audioStrId, String name, String audioFileFromDevice, String lyrics,
                 String translations, String topicId, String topicName) {
        id = audioStrId;
        this.name = name;
        this.audioFileFromDevice = audioFileFromDevice;
        this.lyrics = lyrics;
        this.translations = translations;
        this.topicId = topicId;
        this.topicName = topicName;
    }

    public int getIntId() {
        return intId;
    }

    public void setIntId(int intId) {
        this.intId = intId;
    }

    public String getId() {
        return id;
    }

    public void setId(String audioStrId) {
        id = audioStrId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return name.equalsIgnoreCase(audio.getName());
    }
}
