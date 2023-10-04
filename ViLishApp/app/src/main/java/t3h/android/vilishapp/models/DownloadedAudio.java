package t3h.android.vilishapp.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
@Entity(tableName = "downloaded_audio")
public class DownloadedAudio implements Serializable {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "audio_file_name")
    private String audioFileName;

    @ColumnInfo(name = "audio_file_from_device")
    private String audioFileFromDevice;

    @ColumnInfo(name = "audio_file_from_firebase")
    private String audioFileFromFirebase;

    @ColumnInfo(name = "lyrics")
    private String lyrics;

    @ColumnInfo(name = "translations")
    private String translations;

    @ColumnInfo(name = "topic_id")
    private String topicId;

    @ColumnInfo(name = "topic_name")
    private String topicName;

    @NonNull
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

    public String getAudioFileName() {
        return audioFileName;
    }

    public void setAudioFileName(String audioFileName) {
        this.audioFileName = audioFileName;
    }

    public String getAudioFileFromDevice() {
        return audioFileFromDevice;
    }

    public void setAudioFileFromDevice(String audioFileFromDevice) {
        this.audioFileFromDevice = audioFileFromDevice;
    }

    public String getAudioFileFromFirebase() {
        return audioFileFromFirebase;
    }

    public void setAudioFileFromFirebase(String audioFileFromFirebase) {
        this.audioFileFromFirebase = audioFileFromFirebase;
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

    public DownloadedAudio() {
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        DownloadedAudio audio = (DownloadedAudio) obj;
        return name.equalsIgnoreCase(audio.getName());
    }
}
