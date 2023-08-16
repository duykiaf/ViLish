package t3h.android.vilishapp.helpers;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import t3h.android.vilishapp.R;
import t3h.android.vilishapp.databinding.FragmentAudioListBinding;
import t3h.android.vilishapp.models.Audio;
import t3h.android.vilishapp.viewmodels.AudioViewModel;

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

    public static void playerListener(ExoPlayer player, FragmentAudioListBinding binding, AudioViewModel audioViewModel) {
        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                // show the playing audio title
                assert mediaItem != null;
                binding.currentAudioTitle.setText(mediaItem.mediaMetadata.title);
                binding.fragmentAudioDetailsLayout.audioTitle.setText(mediaItem.mediaMetadata.title);

                // show audio lyrics and translations
                audioViewModel.setAudioLyrics((String) mediaItem.mediaMetadata.description);
                audioViewModel.setAudioTranslations((String) mediaItem.mediaMetadata.extras.get(AppConstant.AUDIO_TRANSLATIONS));

                // show pause icon
                showPlayOrPauseIcon(true, binding);

                // init seek bar audio duration (AudioDetailsFragment)
                initSeekBarAudioDuration(player, binding);

                if (!player.isPlaying()) {
                    player.play();
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == ExoPlayer.STATE_READY && player.isPlaying()) {
                    // show the playing audio title
                    binding.currentAudioTitle.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    binding.fragmentAudioDetailsLayout.audioTitle.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);

                    // show audio lyrics and translations
                    audioViewModel.setAudioLyrics((String) player.getCurrentMediaItem().mediaMetadata.description);
                    audioViewModel.setAudioTranslations((String) player.getCurrentMediaItem().mediaMetadata.extras.get(AppConstant.AUDIO_TRANSLATIONS));

                    // show pause audio icon
                    showPlayOrPauseIcon(true, binding);

                    // init seek bar audio duration (AudioDetailsFragment)
                    initSeekBarAudioDuration(player, binding);
                } else {
                    // show play audio icon
                    showPlayOrPauseIcon(false, binding);
                }
            }
        });
    }

    private static void showPlayOrPauseIcon(boolean isPlaying, FragmentAudioListBinding binding) {
        int resId;
        if (isPlaying) {
            resId = R.drawable.pause_circle_blue_outline_ic;
        } else {
            resId = R.drawable.play_circle_blue_outline_ic;
        }
        binding.playOrPauseIcon.setImageResource(resId);
        binding.fragmentAudioDetailsLayout.playOrPauseIcon.setImageResource(resId);
    }

    private static void initSeekBarAudioDuration(ExoPlayer player, FragmentAudioListBinding binding) {
        binding.fragmentAudioDetailsLayout.audioDuration.setText(AudioHelper.milliSecondsToTimer((int) player.getDuration()));
        binding.fragmentAudioDetailsLayout.seekBar.setMax((int) player.getDuration());
        binding.fragmentAudioDetailsLayout.seekBar.setProgress((int) player.getCurrentPosition());
        binding.fragmentAudioDetailsLayout.audioCurrentTime.setText(AudioHelper.milliSecondsToTimer((int) player.getCurrentPosition()));
        updatePlayerPositionProgress(player, binding);
    }

    private static void updatePlayerPositionProgress(ExoPlayer player, FragmentAudioListBinding binding) {
        new Handler().postDelayed(() -> {
            if (player.isPlaying() && binding != null) {
                binding.fragmentAudioDetailsLayout.audioCurrentTime.setText(AudioHelper.milliSecondsToTimer((int) player.getCurrentPosition()));
                binding.fragmentAudioDetailsLayout.seekBar.setProgress((int) player.getCurrentPosition());
            }
            updatePlayerPositionProgress(player, binding);
        }, 1000);
    }
}
