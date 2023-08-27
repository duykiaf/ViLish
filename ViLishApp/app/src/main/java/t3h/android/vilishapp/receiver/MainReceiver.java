package t3h.android.vilishapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import t3h.android.vilishapp.helpers.AppConstant;
import t3h.android.vilishapp.services.PlayerService;

public class MainReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("DNV", "onReceive");
        int getActionFromService = intent.getIntExtra(AppConstant.ACTION_FROM_SERVICE, 0);

        Intent intentFromReceiver = new Intent(context, PlayerService.class);
        Log.e("DNV", String.valueOf(getActionFromService));
        intentFromReceiver.putExtra(AppConstant.ACTION_FROM_RECEIVER, getActionFromService);
        Bundle bundle = new Bundle();
        bundle.putString(AppConstant.TOPIC_NAME, intent.getStringExtra(AppConstant.TOPIC_NAME));
        bundle.putString(AppConstant.AUDIO_NAME, intent.getStringExtra(AppConstant.AUDIO_NAME));
        intentFromReceiver.putExtras(bundle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentFromReceiver);
        }
    }
}
