package t3h.android.vilishapp;

import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.firebase.database.FirebaseDatabase;

import t3h.android.vilishapp.receiver.MainReceiver;
import t3h.android.vilishapp.viewmodels.AudioViewModel;

public class MainActivity extends AppCompatActivity implements MainReceiver.ReceiverListener {
    private FirebaseDatabase firebaseDatabase;
    private ExoPlayer player;
    private AudioViewModel audioViewModel;
    private MainReceiver mainReceiver;
    private AlertDialog goToDownloadedScreenDialog;
    private NavController navController;
    private Boolean isSplashScreen, isDownloadedScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainReceiver = new MainReceiver();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
        setContentView(R.layout.activity_main);
        audioViewModel = new ViewModelProvider(this).get(AudioViewModel.class);
        player = audioViewModel.getExoplayer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mainReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        MainReceiver.listener = this;
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mainReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        if (!isConnected) {
            audioViewModel.splashScreenFlag().observe(this, flag -> isSplashScreen = flag);
            audioViewModel.audioDownloadedScreenFlag().observe(this, flag -> isDownloadedScreen = flag);
            // khong hien thi dialog o Splash screen va Downloaded screen
            if (!isSplashScreen && !isDownloadedScreen) {
                navController = Navigation.findNavController(MainActivity.this, R.id.navHostFragment);
                showDialogAfterDisconnected();
            }
        }
    }

    private void showDialogAfterDisconnected() {
        if (goToDownloadedScreenDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.fragment_go_to_downloaded_screen_dialog,
                    findViewById(R.id.offlineDialog)
            );
            builder.setView(view);

            goToDownloadedScreenDialog = builder.create();
            // nếu dialog đã xuất hiện thì set màu nền trong suốt
            if (goToDownloadedScreenDialog.getWindow() != null) {
                goToDownloadedScreenDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            view.findViewById(R.id.okBtn).setOnClickListener(sv -> {
                audioViewModel.setIsAudioDownloadedScreen(true);
                audioViewModel.setIsAudioListScreen(false);
                audioViewModel.setIsBookmarksScreen(false);
                audioViewModel.stopExoplayer();
                audioViewModel.setStopState(true);
                navController.navigate(R.id.audioListFragment);
                goToDownloadedScreenDialog.dismiss();
            });

            view.findViewById(R.id.noBtn).setOnClickListener(sv -> goToDownloadedScreenDialog.dismiss());
        }
        goToDownloadedScreenDialog.show();
    }
}