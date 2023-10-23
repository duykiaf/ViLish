package t3h.android.vilishapp.ui;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import t3h.android.vilishapp.R;
import t3h.android.vilishapp.adapters.FragmentAdapter;
import t3h.android.vilishapp.databinding.FragmentAudioDetailsBinding;
import t3h.android.vilishapp.helpers.AppConstant;
import t3h.android.vilishapp.helpers.AudioHelper;
import t3h.android.vilishapp.models.Audio;
import t3h.android.vilishapp.repositories.AudioRepository;
import t3h.android.vilishapp.viewmodels.AudioViewModel;

public class AudioDetailsFragment extends Fragment {
    private FragmentAudioDetailsBinding binding;
    private AudioViewModel audioViewModel;
    private AudioRepository audioRepository;
    private ExoPlayer player;
    private boolean isTheFirstOpenTime = true;
    private String bookmarkImgViewContentDesc, audioSpeedStr;
    private Disposable disposable;
    private String getCurrentAudioId;
    private AlertDialog speedSettingDialog;
    private float audioSpeedValue, getCurrentAudioSpeed;
    private PlaybackParameters playbackParameters;
    private RadioButton radioChecked;

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
        getCurrentAudioSpeed = player.getPlaybackParameters().speed;
        StringBuffer sb = new StringBuffer();
        binding.audioSpeed.setText(sb.append(getCurrentAudioSpeed).append("x"));
        initAudioControlLayout();
        initBookmarkIcon(null);
        binding.audioSpeed.setOnClickListener(v -> showSpeedSettingDialog(view));
    }

    private void showSpeedSettingDialog(View v) {
        if (speedSettingDialog == null) {
            View view = LayoutInflater.from(requireActivity()).inflate(
                    R.layout.playback_speed_dialog, v.findViewById(R.id.playbackSpeedDialog)
            );
            initSpeedSettingDialog(view);
            RadioGroup radioGroup = view.findViewById(R.id.playbackSpeedRG);
            setCheckedRadio(view);
            onCheckedRadioBtnChangeListener(view, radioGroup);
            view.findViewById(R.id.closeBtn).setOnClickListener(s -> speedSettingDialog.dismiss());
        }
        speedSettingDialog.show();
    }

    private void initSpeedSettingDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);
        speedSettingDialog = builder.create();
        if (speedSettingDialog.getWindow() != null) {
            speedSettingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
    }

    private void setCheckedRadio(View view) {
        switch (String.valueOf(getCurrentAudioSpeed)) {
            case "0.5":
                radioChecked = view.findViewById(R.id.o5x);
                break;
            case "0.75":
                radioChecked = view.findViewById(R.id.o75x);
                break;
            case "1.25":
                radioChecked = view.findViewById(R.id.o125x);
                break;
            case "1.5":
                radioChecked = view.findViewById(R.id.o15x);
                break;
            default:
                radioChecked = view.findViewById(R.id.radioCheckedDefault);
                break;
        }
        radioChecked.setChecked(true);
    }

    private void onCheckedRadioBtnChangeListener(View view, RadioGroup radioGroup) {
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton radioButton = view.findViewById(checkedId);
            audioSpeedStr = radioButton.getText().toString();
            switch (audioSpeedStr) {
                case "0.5x":
                    audioSpeedValue = 0.5f;
                    break;
                case "0.75x":
                    audioSpeedValue = 0.75f;
                    break;
                case "1x":
                    audioSpeedValue = 1f;
                    break;
                case "1.25x":
                    audioSpeedValue = 1.25f;
                    break;
                case "1.5x":
                    audioSpeedValue = 1.5f;
                    break;
            }
            playbackParameters = new PlaybackParameters(audioSpeedValue);
            binding.audioSpeed.setText(audioSpeedStr);
            player.setPlaybackParameters(playbackParameters);
        });
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
        addOrRemoveBookmark();
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
                    getCurrentAudioId = ((Audio) mediaItem.mediaMetadata.extras.get(AppConstant.AUDIO_ITEM)).getId();
                    initBookmarkIcon(getCurrentAudioId);
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
                    getCurrentAudioId = ((Audio) Objects.requireNonNull(player.getCurrentMediaItem())
                            .mediaMetadata.extras.get(AppConstant.AUDIO_ITEM)
                    ).getId();
                    initBookmarkIcon(getCurrentAudioId);
                }
            }
        });
    }

    private void initBookmarkIcon(String audioId) {
        if (audioId == null) {
            getCurrentAudioId = ((Audio) Objects.requireNonNull(player.getCurrentMediaItem())
                    .mediaMetadata.extras.get(AppConstant.AUDIO_ITEM)
            ).getId();
        } else {
            getCurrentAudioId = audioId;
        }
        audioRepository.getBookmarkAudioIds().observe(requireActivity(), bookmarkAudioIds -> {
            for (String id : bookmarkAudioIds) {
                if (getCurrentAudioId.equals(id)) {
                    setBookmarkIconType(true);
                    break;
                } else {
                    setBookmarkIconType(false);
                }
            }
        });
    }

    private void addOrRemoveBookmark() {
        binding.bookmarkIcon.setOnClickListener(v -> {
            Audio audioItem = (Audio) Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.extras.get(AppConstant.AUDIO_ITEM);
            bookmarkImgViewContentDesc = binding.bookmarkIcon.getContentDescription().toString();
            if (bookmarkImgViewContentDesc.equalsIgnoreCase(AppConstant.BOOKMARK_ICON)) { // audio is bookmarked
                setBookmarkIconType(false);
                handleRemoveBookmark(audioItem);
            } else { // audio isn't bookmarked
                setBookmarkIconType(true);
                handleAddBookmark(audioItem);
            }
        });
    }

    private void setBookmarkIconType(boolean isBookmark) {
        if (isBookmark) {
            binding.bookmarkIcon.setContentDescription(AppConstant.BOOKMARK_ICON);
            binding.bookmarkIcon.setImageResource(R.drawable.blue_bookmark_ic);
        } else {
            binding.bookmarkIcon.setContentDescription(AppConstant.BOOKMARK_BORDER_ICON);
            binding.bookmarkIcon.setImageResource(R.drawable.bookmark_blue_border_ic);
        }
    }

    private void handleRemoveBookmark(Audio item) {
        if (item == null) {
            Toast.makeText(requireContext(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show();
            return;
        } else {
            Completable deleteBookmarkObservable = deleteBookmark(item);
            CompletableObserver deleteBookmarkObserver = deleteBookmarkObserver();
            if (deleteBookmarkObservable != null) {
                deleteBookmarkObservable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(deleteBookmarkObserver);
            } else {
                Toast.makeText(requireContext(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleAddBookmark(Audio item) {
        if (item == null) {
            Toast.makeText(requireContext(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show();
            return;
        } else {
            Completable addBookmarkObservable = addBookmark(item);
            CompletableObserver completableObserver = completableObserver();
            if (addBookmarkObservable != null) {
                addBookmarkObservable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(completableObserver);
            } else {
                Toast.makeText(requireContext(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Completable addBookmark(Audio item) {
        return Completable.create(emitter -> {
            if (emitter.isDisposed()) {
                emitter.onError(new Exception());
            } else {
                // logic add bookmark here
                audioRepository.addBookmark(item);
                emitter.onComplete();
            }
        });
    }

    private CompletableObserver completableObserver() {
        return new CompletableObserver() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                disposable = d;
            }

            @Override
            public void onComplete() {
                Toast.makeText(requireContext(), AppConstant.ADD_BOOKMARK_SUCCESS, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                Toast.makeText(requireContext(), AppConstant.ADD_BOOKMARK_FAILED, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private Completable deleteBookmark(Audio item) {
        return Completable.create(emitter -> {
            if (emitter.isDisposed()) {
                emitter.onError(new Exception());
            } else {
                // logic add bookmark here
                int deleteRow = audioRepository.deleteBookmark(item);
                if (deleteRow > 0) {
                    // complete callback
                    emitter.onComplete();
                } else {
                    emitter.onError(new Exception());
                }
            }
        });
    }

    private CompletableObserver deleteBookmarkObserver() {
        return new CompletableObserver() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                disposable = d;
            }

            @Override
            public void onComplete() {
                Toast.makeText(requireContext(), AppConstant.REMOVE_SUCCESS, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                Toast.makeText(requireContext(), AppConstant.REMOVE_FAILED, Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        audioViewModel.setAudioTitle(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }
}