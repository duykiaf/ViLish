package t3h.android.vilishapp.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import t3h.android.vilishapp.R;
import t3h.android.vilishapp.adapters.FragmentAdapter;
import t3h.android.vilishapp.databinding.FragmentAudioDetailsBinding;
import t3h.android.vilishapp.helpers.AppConstant;
import t3h.android.vilishapp.helpers.AudioHelper;
import t3h.android.vilishapp.repositories.AudioRepository;
import t3h.android.vilishapp.viewmodels.AudioViewModel;

public class AudioDetailsFragment extends Fragment {
    private FragmentAudioDetailsBinding binding;
    private AudioViewModel audioViewModel;
    private AudioRepository audioRepository;
    private ExoPlayer player;
    private boolean isTheFirstOpenTime = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAudioDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        audioViewModel = new ViewModelProvider(requireActivity()).get(AudioViewModel.class);
        audioRepository = new AudioRepository(requireActivity().getApplication());
        initViewPager();
        player = audioViewModel.getExoplayer();
        initAudioControlLayout();
    }

    private void initViewPager() {
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new LyricsFragment());
        fragmentList.add(new TranslationsFragment());

        FragmentAdapter fragmentAdapter = new FragmentAdapter(requireActivity(), fragmentList);
        binding.pager.setAdapter(fragmentAdapter);

        String[] tabLayoutNames = {AppConstant.LYRICS, AppConstant.TRANSLATIONS};
        new TabLayoutMediator(binding.tabLayout, binding.pager,
                (tab, position) -> tab.setText(tabLayoutNames[position])
        ).attach();
    }

    private void initAudioControlLayout() {
        if (!player.isPlaying()) {
            if (isTheFirstOpenTime) {
                isTheFirstOpenTime = false;
                if (!audioViewModel.getBottomControlClickListener()) {
                    setUpCurrentMediaItem();
                } else {
                    binding.audioTitle.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                }
                audioViewModel.setBottomControlClick(false);
            }
            player.play();
        } else {
            if (isTheFirstOpenTime) {
                isTheFirstOpenTime = false;
                if (requireArguments().get(AppConstant.PLAY_ANOTHER_AUDIO) != null &&
                        requireArguments().getBoolean(AppConstant.PLAY_ANOTHER_AUDIO)) {
                    setUpCurrentMediaItem();
                    player.play();
                }
                binding.audioTitle.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
            }
        }
        binding.playOrPauseIcon.setImageResource(R.drawable.pause_circle_blue_outline_ic);
        initSeekBarAudioDuration();
        seekBarChangedListener();
        playerListener();
    }

    private void setUpCurrentMediaItem() {
        player.seekTo((Integer) requireArguments().get(AppConstant.CURRENT_MEDIA_ITEM_INDEX), C.TIME_UNSET);
        player.prepare();
    }

    private void initSeekBarAudioDuration() {
        binding.audioDuration.setText(AudioHelper.milliSecondsToTimer((int) player.getDuration()));
        binding.seekBar.setMax((int) player.getDuration());
        binding.seekBar.setProgress((int) player.getCurrentPosition());
        binding.audioCurrentTime.setText(AudioHelper.milliSecondsToTimer((int) player.getCurrentPosition()));
        updatePlayerPositionProgress();
    }

    private void updatePlayerPositionProgress() {
        new Handler().postDelayed(() -> {
            if (player.isPlaying() && binding != null) {
                binding.audioCurrentTime.setText(AudioHelper.milliSecondsToTimer((int) player.getCurrentPosition()));
                binding.seekBar.setProgress((int) player.getCurrentPosition());
            }
            updatePlayerPositionProgress();
        }, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.topAppBar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        playerControls();
    }

    private void playerControls() {
        binding.previousIcon.setOnClickListener(v -> {
            if (player.hasPreviousMediaItem()) {
                player.seekToPrevious();
                updatePlayerPositionProgress();
            }
        });

        binding.nextIcon.setOnClickListener(v -> {
            if (player.hasNextMediaItem()) {
                player.seekToNext();
                updatePlayerPositionProgress();
            }
        });

        binding.playOrPauseIcon.setOnClickListener(v -> {
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.play();
            }
            initPlayOrPauseIcon();
        });

        seekBarChangedListener();

        playerListener();
    }

    private void initPlayOrPauseIcon() {
        int playOrPauseIconId;
        if (player.isPlaying()) {
            playOrPauseIconId = R.drawable.pause_circle_blue_outline_ic;
        } else {
            playOrPauseIconId = R.drawable.play_circle_blue_outline_ic;
        }
        binding.playOrPauseIcon.setImageResource(playOrPauseIconId);
    }

    private void seekBarChangedListener() {
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressValue = seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(progressValue);
                binding.audioCurrentTime.setText(AudioHelper.milliSecondsToTimer(progressValue));
                player.seekTo(progressValue);
            }
        });
    }

    private void playerListener() {
        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                assert mediaItem != null;
                binding.audioTitle.setText(mediaItem.mediaMetadata.title);
                initPlayOrPauseIcon();
                initSeekBarAudioDuration();
                seekBarChangedListener();
                assert mediaItem.mediaMetadata.extras != null;
                if (isAdded()) {
                    initBookmarkIcon(mediaItem.mediaMetadata.extras.getString(AppConstant.AUDIO_ID));
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == ExoPlayer.STATE_READY && player.isPlaying()) {
                    binding.audioTitle.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    initSeekBarAudioDuration();
                    seekBarChangedListener();
                }
                initPlayOrPauseIcon();
                if (isAdded()) {
                    initBookmarkIcon(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.extras.getString(AppConstant.AUDIO_ID));
                }
            }
        });
    }

    private void initBookmarkIcon(String audioId) {
        String getCurrentAudioId;
        if (audioId == null) {
            getCurrentAudioId =
                    Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.extras.getString(AppConstant.AUDIO_ID);
        } else {
            getCurrentAudioId = audioId;
        }
        audioRepository.getBookmarkAudioIds().observe(requireActivity(), bookmarkAudioIds -> {
            for (String id : bookmarkAudioIds) {
                if (getCurrentAudioId.equals(id)) {
                    binding.bookmarkIcon.setContentDescription(getString(R.string.bookmark_icon));
                    binding.bookmarkIcon.setImageResource(R.drawable.blue_bookmark_ic);
                    break;
                } else {
                    binding.bookmarkIcon.setContentDescription(getString(R.string.bookmark_border_icon));
                    binding.bookmarkIcon.setImageResource(R.drawable.bookmark_blue_border_ic);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        audioViewModel.setAudioTitle(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}