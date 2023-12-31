package t3h.android.vilishapp.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableEmitter;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import t3h.android.vilishapp.R;
import t3h.android.vilishapp.adapters.FragmentAdapter;
import t3h.android.vilishapp.databinding.FragmentAudioDetailsBinding;
import t3h.android.vilishapp.helpers.AppConstant;
import t3h.android.vilishapp.helpers.AudioHelper;
import t3h.android.vilishapp.helpers.NetworkHelper;
import t3h.android.vilishapp.models.Audio;
import t3h.android.vilishapp.repositories.AudioRepository;
import t3h.android.vilishapp.repositories.DownloadedAudioRepository;
import t3h.android.vilishapp.services.DownloadAudioService;
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
    private HashMap<String, Audio> audioSelected = new HashMap<>();
    private DownloadedAudioRepository downloadedAudioRepository;
    private int deletedRow;

    private BroadcastReceiver downloadCompletedBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isComplete = intent.getBooleanExtra(AppConstant.DOWNLOAD_COMPLETE, false);
            if (isComplete) {
                downloadCompleted();
            }
        }
    };

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
        downloadedAudioRepository = new DownloadedAudioRepository(requireActivity().getApplication());
        initViewPager();
        player = audioViewModel.getExoplayer();
        initAudioSpeedTxt();
        initAudioControlLayout();
        initBookmarkIcon(null);
        initDownloadOrTrashIc(null);
        binding.audioSpeed.setOnClickListener(v -> showSpeedSettingDialog(view));
    }

    private void initAudioSpeedTxt() {
        getCurrentAudioSpeed = player.getPlaybackParameters().speed;
        StringBuffer sb = new StringBuffer();
        binding.audioSpeed.setText(sb.append(getCurrentAudioSpeed).append("x"));
    }

    @Override
    public void onStart() {
        super.onStart();
        requireActivity().registerReceiver(downloadCompletedBroadcast, new IntentFilter(AppConstant.DOWNLOAD_COMPLETED_BROADCAST));
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
//                if (!audioViewModel.getBottomControlClickListener()) {
//                    setUpCurrentMediaItem();
//                } else {
//                    binding.audioTitle.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
//                }
                setUpCurrentMediaItem();
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
//        player.seekTo((Integer) requireArguments().get(AppConstant.CURRENT_MEDIA_ITEM_INDEX), C.TIME_UNSET);
        player.seekTo((Integer) requireArguments().get(AppConstant.CURRENT_MEDIA_ITEM_INDEX), player.getCurrentPosition());
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
        onDownloadIcClickListener();
    }

    private void onDownloadIcClickListener() {
        binding.downloadOrTrashIc.setOnClickListener(v -> {
            String contentDesc = binding.downloadOrTrashIc.getContentDescription().toString();
            Audio audioItem = (Audio) Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.extras.get(AppConstant.AUDIO_ITEM);
            if (contentDesc.equalsIgnoreCase(AppConstant.DOWNLOAD_ICON)) { // tai audio
                if (NetworkHelper.isInternetConnected(requireContext())) {
                    binding.downloadOrTrashIc.setVisibility(View.GONE);
                    binding.audioDetailsProgressBar.setVisibility(View.VISIBLE);

                    audioSelected.put(audioItem.getId(), audioItem);

                    Intent intent = new Intent(requireActivity(), DownloadAudioService.class);
                    intent.putExtra(AppConstant.AUDIO_SELECTED_LIST, audioSelected);
                    audioViewModel.setIsDownloading(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        requireActivity().startForegroundService(intent);
                    } else {
                        requireActivity().startService(intent);
                    }
                } else {
                    Toast.makeText(requireContext(), AppConstant.NETWORK_NOT_AVAILABLE, Toast.LENGTH_SHORT).show();
                }
            } else { // xoa audio da tai xuong
                Completable deleteDownloadedAudioObservable = handleBookmarkOrDownloadTask(audioItem, AppConstant.REMOVE_DOWNLOADED_AUDIO);
                CompletableObserver deleteDownloadedAudioObserver = bookmarkOrDownloadTaskCO(AppConstant.REMOVE_DOWNLOADED_AUDIO);
                if (deleteDownloadedAudioObservable != null) {
                    deleteDownloadedAudioObservable.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(deleteDownloadedAudioObserver);
                } else {
                    Toast.makeText(requireContext(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void downloadCompleted() {
        binding.audioDetailsProgressBar.setVisibility(View.GONE);
        binding.downloadOrTrashIc.setVisibility(View.VISIBLE);
        audioViewModel.setIsDownloading(false);
        initDownloadOrTrashIc(getCurrentAudioId);
    }

    private void initDownloadOrTrashIc(String audioId) {
        handleCurrentAudioId(audioId);
        downloadedAudioRepository.getDownloadedAudioIds().observe(requireActivity(), ids -> {
            for (String id : ids) {
                if (getCurrentAudioId.equals(id)) {
                    setIconType(true);
                    break;
                } else {
                    setIconType(false);
                }
            }
        });
    }

    private void setIconType(boolean isDownloadedAudio) {
        if (isDownloadedAudio) {
            binding.downloadOrTrashIc.setContentDescription(AppConstant.TRASH_ICON);
            binding.downloadOrTrashIc.setImageResource(R.drawable.trash);
        } else {
            binding.downloadOrTrashIc.setContentDescription(AppConstant.DOWNLOAD_ICON);
            binding.downloadOrTrashIc.setImageResource(R.drawable.white_download_ic);
        }
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
                    initDownloadOrTrashIc(getCurrentAudioId);
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
                    initDownloadOrTrashIc(getCurrentAudioId);
                }
            }
        });
    }

    private void initBookmarkIcon(String audioId) {
        handleCurrentAudioId(audioId);
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

    private void handleCurrentAudioId(String audioId) {
        if (audioId == null) {
            getCurrentAudioId = ((Audio) Objects.requireNonNull(player.getCurrentMediaItem())
                    .mediaMetadata.extras.get(AppConstant.AUDIO_ITEM)
            ).getId();
        } else {
            getCurrentAudioId = audioId;
        }
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
            Completable deleteBookmarkObservable = handleBookmarkOrDownloadTask(item, AppConstant.REMOVE_BOOKMARK);
            CompletableObserver deleteBookmarkObserver = bookmarkOrDownloadTaskCO(AppConstant.REMOVE_BOOKMARK);
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
            Completable addBookmarkObservable = handleBookmarkOrDownloadTask(item, AppConstant.ADD_BOOKMARK);
            CompletableObserver completableObserver = bookmarkOrDownloadTaskCO(AppConstant.ADD_BOOKMARK);
            if (addBookmarkObservable != null) {
                addBookmarkObservable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(completableObserver);
            } else {
                Toast.makeText(requireContext(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Completable handleBookmarkOrDownloadTask(Audio item, int flagTask) {
        return Completable.create(emitter -> {
            if (emitter.isDisposed()) {
                emitter.onError(new Exception());
            } else {
                switch (flagTask) {
                    case AppConstant.ADD_BOOKMARK:
                        audioRepository.addBookmark(item);
                        emitter.onComplete();
                        break;
                    case AppConstant.REMOVE_BOOKMARK:
                        deletedRow = audioRepository.deleteBookmark(item);
                        onDeleteComplete(emitter, deletedRow);
                        break;
                    case AppConstant.REMOVE_DOWNLOADED_AUDIO:
                        deletedRow = downloadedAudioRepository.deleteSingleDownloadedAudio(item.getId());
                        onDeleteComplete(emitter, deletedRow);
                        break;
                }
            }
        });
    }

    private void onDeleteComplete(CompletableEmitter emitter, int deletedRow) {
        if (deletedRow > 0) {
            emitter.onComplete();
        } else {
            emitter.onError(new Exception());
        }
    }

    private CompletableObserver bookmarkOrDownloadTaskCO(int flagTask) {
        return new CompletableObserver() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                disposable = d;
            }

            @Override
            public void onComplete() {
                switch (flagTask) {
                    case AppConstant.ADD_BOOKMARK:
                        Toast.makeText(requireContext(), AppConstant.ADD_BOOKMARK_SUCCESS, Toast.LENGTH_SHORT).show();
                        break;
                    case AppConstant.REMOVE_BOOKMARK:
                        Toast.makeText(requireContext(), AppConstant.REMOVE_SUCCESS, Toast.LENGTH_SHORT).show();
                        break;
                    case AppConstant.REMOVE_DOWNLOADED_AUDIO:
                        Toast.makeText(requireContext(), AppConstant.REMOVE_SUCCESS, Toast.LENGTH_SHORT).show();
                        setIconType(false);
                        break;
                }
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                Toast.makeText(requireContext(), AppConstant.FAILED_MESSAGE, Toast.LENGTH_SHORT).show();
                if (flagTask == AppConstant.REMOVE_DOWNLOADED_AUDIO) {
                    setIconType(true);
                }
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
        requireActivity().unregisterReceiver(downloadCompletedBroadcast);
    }
}