package t3h.android.vilishapp.helpers;

import android.media.MediaPlayer;

import java.io.IOException;

public class AudioHelper {
    public interface DurationCallback {
        void onDurationReceived(String strDuration);
        void onDurationError();
    }

    public static void getAudioDuration(String audioUrl, DurationCallback callback) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.setOnPreparedListener(mp -> {
                int duration = mp.getDuration();
                callback.onDurationReceived(milliSecondsToTimer(duration));
                mp.release();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                callback.onDurationError();
                mp.release();
                return true;
            });
            mediaPlayer.prepareAsync(); // prepare the MediaPlayer asynchronously
        } catch (IOException e) {
            e.printStackTrace();
            callback.onDurationError();
        }
    }

    public static String milliSecondsToTimer(int milliSeconds) {
        StringBuilder timerStr = new StringBuilder("");
        StringBuilder secondsStr = new StringBuilder("");
        int hours = milliSeconds / (1000 * 60 * 60);
        int minutes = (milliSeconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (milliSeconds % (1000 * 60 * 60)) % (1000 * 60) / 1000;
        if (hours > 0) {
            timerStr.append(hours).append(":");
        }
        if (seconds < 10) {
            secondsStr.append("0").append(seconds);
        } else {
            secondsStr.append(seconds);
        }
        timerStr.append(minutes).append(":").append(secondsStr);
        return timerStr.toString();
    }
}
