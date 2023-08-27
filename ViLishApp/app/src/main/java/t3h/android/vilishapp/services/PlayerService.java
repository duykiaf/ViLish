package t3h.android.vilishapp.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;

import java.util.List;
import java.util.Objects;

import t3h.android.vilishapp.R;
import t3h.android.vilishapp.databinding.FragmentAudioListBinding;
import t3h.android.vilishapp.helpers.AppConstant;
import t3h.android.vilishapp.helpers.ExoplayerHelper;
import t3h.android.vilishapp.models.Audio;
import t3h.android.vilishapp.receiver.MainReceiver;
import t3h.android.vilishapp.viewmodels.AudioViewModel;

public class PlayerService extends Service {
    private ExoPlayer player;
    private FragmentAudioListBinding binding;
    private String topicName, audioName;
    private int audioItemPosition, currentMediaItemIndex;
    private AudioViewModel audioViewModel;
    private boolean isTheFirstTimeStartService = true;

    public class PlayerServiceBinder extends Binder {
        public PlayerService getPlayerService() {
            return PlayerService.this;
        }
    }

    public void setBinding(FragmentAudioListBinding binding) {
        this.binding = binding;
    }

    public void setPlayer(ExoPlayer player) {
        this.player = player;
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    public void setAudioViewModel(AudioViewModel audioViewModel) {
        this.audioViewModel = audioViewModel;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        player = new ExoPlayer.Builder(getApplicationContext()).build();
//        player.setRepeatMode(Player.REPEAT_MODE_ALL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            topicName = bundle.getString(AppConstant.TOPIC_NAME);
            audioName = bundle.getString(AppConstant.AUDIO_NAME);
//            audioItemPosition = bundle.getInt(AppConstant.ITEM_POSITION);
//            currentMediaItemIndex = bundle.getInt(AppConstant.CURRENT_MEDIA_ITEM_INDEX);

            sendNotification();
        }

        int getActionFromReceiver = intent.getIntExtra(AppConstant.ACTION_FROM_RECEIVER, 0);
        Log.e("DNV-onStartCommand", String.valueOf(getActionFromReceiver));
        if (getActionFromReceiver != 0) {
            playerControls(getActionFromReceiver);
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayerServiceBinder();
    }

    private void sendNotification() {
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(this, "tag");
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, AppConstant.CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.elife_logo)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2, 3, 4)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setContentTitle(player == null ? audioName : Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title)
                .setContentText(topicName)
                .addAction(R.drawable.bookmark_blue_border_ic, AppConstant.BOOKMARK, null) // #0
                .addAction(R.drawable.skip_previous_ic, AppConstant.PREVIOUS, getPendingIntent(this, AppConstant.ACTION_PREV)); // #1

        if (isTheFirstTimeStartService) {
            Log.e("DNV", "isTheFirstTimeStartService");
            notificationBuilder
                    .addAction(R.drawable.pause_circle_blue_outline_ic, AppConstant.PAUSE, getPendingIntent(this, AppConstant.ACTION_PAUSE));  // #2
            isTheFirstTimeStartService = false;
        } else {
            if (player.isPlaying()) {
                Log.e("DNV", "isPlaying");
                notificationBuilder
                        .addAction(R.drawable.pause_circle_blue_outline_ic, AppConstant.PAUSE, getPendingIntent(this, AppConstant.ACTION_PAUSE));  // #2
            } else {
                Log.e("DNV", "isPause");
                notificationBuilder
                        .addAction(R.drawable.play_circle_blue_outline_ic, AppConstant.PLAY, getPendingIntent(this, AppConstant.ACTION_PLAY));  // #2
            }
        }

        notificationBuilder
                .addAction(R.drawable.skip_next_ic, AppConstant.NEXT, getPendingIntent(this, AppConstant.ACTION_NEXT))     // #3
                .addAction(R.drawable.close_ic, AppConstant.CLOSE, getPendingIntent(this, AppConstant.ACTION_CLEAR_NOTIFICATION)); // #4

        startForeground(AppConstant.NOTIFICATION_ID, notificationBuilder.build());
    }

    private PendingIntent getPendingIntent(Context context, int action) {
        Log.e("DNV", "getPendingIntent");
        Log.e("DNV", String.valueOf(action));
        Intent intent = new Intent(this, MainReceiver.class);
        intent.putExtra(AppConstant.ACTION_FROM_SERVICE, action);
        intent.putExtra(AppConstant.TOPIC_NAME, topicName);
        intent.putExtra(AppConstant.AUDIO_NAME, audioName);
        return PendingIntent.getBroadcast(context.getApplicationContext(), AppConstant.REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    public void playerControls(int action) {
        Log.e("DNV", "playerControls");
        Log.e("DNV", String.valueOf(action));
        switch (action) {
            case AppConstant.ACTION_PREV:
                if (player.hasPreviousMediaItem()) {
                    player.seekToPrevious();
                    sendNotification(); // update notification
                    ExoplayerHelper.playerListener(player, binding, audioViewModel);
                }
                break;
            case AppConstant.ACTION_PAUSE:
                if (player.isPlaying()) {
                    player.pause();
                    sendNotification();
                    ExoplayerHelper.playerListener(player, binding, audioViewModel);
                }
                break;
            case AppConstant.ACTION_PLAY:
                if (!player.isPlaying()) {
                    player.prepare();
                    player.play();
                    sendNotification();
                    ExoplayerHelper.playerListener(player, binding, audioViewModel);
                }
                break;
            case AppConstant.ACTION_NEXT:
                if (player.hasNextMediaItem()) {
                    player.seekToNext();
                    sendNotification();
                    ExoplayerHelper.playerListener(player, binding, audioViewModel);
                }
                break;
            case AppConstant.ACTION_CLEAR_NOTIFICATION:
                stopSelf();
                break;
        }
        ExoplayerHelper.playerListener(player, binding, audioViewModel);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public int getAudioItemPosition() {
        return audioItemPosition;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}