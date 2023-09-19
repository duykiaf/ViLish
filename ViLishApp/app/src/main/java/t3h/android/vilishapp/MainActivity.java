package t3h.android.vilishapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.firebase.database.FirebaseDatabase;

import t3h.android.vilishapp.viewmodels.AudioViewModel;

public class MainActivity extends AppCompatActivity {
    private FirebaseDatabase firebaseDatabase;
    private ExoPlayer player;
    private AudioViewModel audioViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
        setContentView(R.layout.activity_main);
        audioViewModel = new ViewModelProvider(this).get(AudioViewModel.class);
        player = audioViewModel.getExoplayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }
}