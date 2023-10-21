package t3h.android.vilishapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import t3h.android.vilishapp.helpers.NetworkHelper;

public class MainReceiver extends BroadcastReceiver {
    public static ReceiverListener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            if (listener != null) {
                listener.onNetworkChange(NetworkHelper.isInternetConnected(context));
            }
        }
    }

    public interface ReceiverListener {
        void onNetworkChange(boolean isConnected);
    }
}
