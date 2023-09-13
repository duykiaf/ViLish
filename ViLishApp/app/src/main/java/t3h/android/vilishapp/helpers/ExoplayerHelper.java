package t3h.android.vilishapp.helpers;

import android.os.Bundle;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

import java.util.ArrayList;
import java.util.List;

import t3h.android.vilishapp.models.Audio;

public class ExoplayerHelper {
    public static List<MediaItem> getMediaItems(List<Audio> audioList) {
        List<MediaItem> mediaItems = new ArrayList<>();
        for (Audio audio : audioList) {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(audio.getAudioFileFromFirebase())
                    .setMediaMetadata(getMediaMetadata(audio))
                    .build();
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }

    private static MediaMetadata getMediaMetadata(Audio audio) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstant.AUDIO_ID, audio.getId());
        bundle.putString(AppConstant.AUDIO_TRANSLATIONS, audio.getTranslations());
        return new MediaMetadata.Builder()
                .setExtras(bundle)
                .setTitle(audio.getName())
                .setDescription(audio.getLyrics())
                .build();
    }
}
