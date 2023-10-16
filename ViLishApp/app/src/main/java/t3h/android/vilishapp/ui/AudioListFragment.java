package t3h.android.vilishapp.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import t3h.android.vilishapp.R;
import t3h.android.vilishapp.adapters.AudioAdapter;
import t3h.android.vilishapp.databinding.FragmentAudioListBinding;
import t3h.android.vilishapp.helpers.AppConstant;
import t3h.android.vilishapp.helpers.ExoplayerHelper;
import t3h.android.vilishapp.models.Audio;
import t3h.android.vilishapp.repositories.AudioRepository;
import t3h.android.vilishapp.repositories.DownloadedAudioRepository;
import t3h.android.vilishapp.services.DownloadAudioService;
import t3h.android.vilishapp.viewmodels.AudioViewModel;

public class AudioListFragment extends Fragment {
    private FragmentAudioListBinding binding;
    private NavController navController;
    private FirebaseDatabase firebaseDatabase;
    private Boolean isAudioListScreen, isBookmarksScreen, isAudioDownloadedScreen, isDownloading;
    private String topicId, oldTopicId;
    private List<Audio> activeAudioList = new ArrayList<>();
    private List<Audio> audioListByTopicId = new ArrayList<>();
    private List<Audio> bookmarksList = new ArrayList<>();
    private List<Audio> downloadedAudioList = new ArrayList<>();
    private int visibility, resId, playOrPauseIconId, currentMediaItemIndex, itemCounter;
    private String contentDesc, getAudioTranslations;
    private AudioAdapter audioAdapter;
    private ExoPlayer player;
    private AudioViewModel audioViewModel;
    private Disposable disposable;
    private AudioRepository audioRepository;
    private HashMap<String, Audio> audioSelected = new HashMap<>();
    private HashMap<String, Integer> audioPositionSelected = new HashMap<>();
    private Bundle playingAudioBundle = new Bundle();
    private boolean isTheFirstTimePlayAudio = true;
    private DownloadedAudioRepository downloadedAudioRepository;
    private StringBuffer itemSelectedCounterTxt;
    private BroadcastReceiver downloadCompletedBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean downloadCompleted = intent.getBooleanExtra(AppConstant.DOWNLOAD_COMPLETE, false);
            if (downloadCompleted) {
                initCheckedOrDownloadOrTrashIcon();
                audioViewModel.setIsDownloading(false);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        firebaseDatabase = FirebaseDatabase.getInstance();
        binding = FragmentAudioListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
        downloadedAudioRepository = new DownloadedAudioRepository(requireActivity().getApplication());
        audioRepository = new AudioRepository(requireActivity().getApplication());
        audioViewModel = new ViewModelProvider(requireActivity()).get(AudioViewModel.class);
        player = audioViewModel.getExoplayer();

        audioAdapter = new AudioAdapter(getContext());
        binding.audiosRcv.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.audiosRcv.setAdapter(audioAdapter);
        binding.progressBar.setVisibility(View.VISIBLE);

        initAudioList();

        // init itemCounter
        audioViewModel.getItemDownloadSelectedCounter().observe(requireActivity(), countValue -> itemCounter = countValue);

        initItemSelectedCounterLayout();

        // get audio url selected list
        audioViewModel.getAudioSelected().observe(requireActivity(), audioSelectedLiveData ->
                audioSelected = audioSelectedLiveData
        );

        // get audio position selected list
        audioViewModel.getAudioCheckedPosList().observe(requireActivity(), posList -> audioPositionSelected = posList);

        initAudioControlBottom();
    }

    private void initAudioList() {
        String audioScreenTitle = AppConstant.AUDIO_SCREEN_TITLE;
        audioViewModel.audioListScreenFlag().observe(requireActivity(), flag -> isAudioListScreen = flag);
        audioViewModel.bookmarksScreenFlag().observe(requireActivity(), flag -> isBookmarksScreen = flag);
        audioViewModel.audioDownloadedScreenFlag().observe(requireActivity(), flag -> isAudioDownloadedScreen = flag);

        isTheFirstTimePlayAudio = true;

        if (isAudioListScreen) {
            if (requireArguments().get(AppConstant.TOPIC_NAME) != null) {
                audioScreenTitle = (String) requireArguments().get(AppConstant.TOPIC_NAME);
            }
            // get old topic id
            oldTopicId = audioViewModel.getTopicIdLiveData();
            topicId = requireArguments().getString(AppConstant.TOPIC_ID);
            // set current topic id
            audioViewModel.setTopicIdLiveData(topicId);
            if (topicId != null) {
                initAudioListByTopicId();
            } else {
                Toast.makeText(requireActivity(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_LONG).show();
            }
        } else {
            if (isBookmarksScreen) {
                audioScreenTitle = AppConstant.BOOKMARKS;
                audioRepository.getBookmarkList().observe(requireActivity(), bookmarksList -> {
                    this.bookmarksList = bookmarksList;
                    if (bookmarksList == null || bookmarksList.isEmpty()) {
                        visibility = View.VISIBLE;
                        initDeleteAllBtn(false);
                    } else {
                        visibility = View.GONE;
                        initDeleteAllBtn(binding.searchEdtLayout.getVisibility() != View.VISIBLE);
                    }
                    audioAdapter.updateItemList(bookmarksList);
                    binding.messageTxt.setVisibility(visibility);
                });
            } else if (isAudioDownloadedScreen) {
                audioScreenTitle = AppConstant.DOWNLOADED;
                downloadedAudioRepository.getDownloadedAudioList().observe(requireActivity(), downloadedAudios -> {
                    downloadedAudioList = downloadedAudios;
                    if (downloadedAudios == null || downloadedAudios.isEmpty()) {
                        visibility = View.VISIBLE;
                        initDeleteAllBtn(false);
                    } else {
                        visibility = View.GONE;
                        initDeleteAllBtn(binding.searchEdtLayout.getVisibility() != View.VISIBLE);
                    }
                    audioAdapter.updateItemList(downloadedAudios);
                    binding.messageTxt.setVisibility(visibility);
                });
            }
            audioRepository.getBookmarkAudioIds().observe(requireActivity(), bookmarkAudioIds ->
                    audioAdapter.setBookmarkAudioIds(bookmarkAudioIds)
            );
            setAudioCheckedListAndDownloadedAudioIds();
        }
        binding.appBarFragment.topAppBar.setTitle(audioScreenTitle);
        binding.appBarFragment.topAppBar.setNavigationIcon(R.drawable.arrow_back_ic);
    }

    private void setAudioCheckedListAndDownloadedAudioIds() {
        audioAdapter.setAudioCheckedList(audioPositionSelected);
        downloadedAudioRepository.getDownloadedAudioIds().observe(requireActivity(), downloadedAudioIds ->
                audioAdapter.setDownloadedAudioIds(downloadedAudioIds)
        );
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        requireActivity().registerReceiver(downloadCompletedBroadcast, new IntentFilter(AppConstant.DOWNLOAD_COMPLETED_BROADCAST));
    }

    private void initAudioListByTopicId() {
        DatabaseReference databaseReference = firebaseDatabase.getReference().child(AppConstant.AUDIOS);
        databaseReference.keepSynced(true);
        Query baseQuery = databaseReference.orderByChild(AppConstant.STATUS).equalTo(AppConstant.ACTIVE);
        Query finalQuery = databaseReference.orderByChild(AppConstant.TOPIC_ID).equalTo(topicId);
        baseQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                activeAudioList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Audio audio = data.getValue(Audio.class);
                    if (audio != null) {
                        activeAudioList.add(audio);
                    }
                }
                if (activeAudioList != null && !activeAudioList.isEmpty()) {
                    finalQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            audioListByTopicId.clear();
                            for (DataSnapshot data : snapshot.getChildren()) {
                                Audio audio = data.getValue(Audio.class);
                                if (audio != null && activeAudioList.contains(audio)) {
                                    audioListByTopicId.add(audio);
                                }
                            }
                            if (!audioListByTopicId.isEmpty()) {
                                visibility = View.GONE;
                            } else {
                                visibility = View.VISIBLE;
                            }
                            binding.messageTxt.setVisibility(visibility);
                            audioRepository.getBookmarkAudioIds().observe(requireActivity(), bookmarkAudioIds -> {
                                audioAdapter.setBookmarkAudioIds(bookmarkAudioIds);
                                audioAdapter.updateItemList(audioListByTopicId);
                            });
                            setAudioCheckedListAndDownloadedAudioIds();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            systemError();
                        }
                    });
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.messageTxt.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                systemError();
            }
        });
    }

    private void systemError() {
        binding.progressBar.setVisibility(View.GONE);
        Toast.makeText(requireActivity(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show();
    }

    private void initAudioControlBottom() {
        audioViewModel.getExoplayerState().observe(requireActivity(), isStopped -> {
            if (isStopped) {
                binding.audioControlLayout.setVisibility(View.GONE);
            } else {
                binding.audioControlLayout.setVisibility(View.VISIBLE);
                if (isAdded()) {
                    audioViewModel.getAudioTitle().observe(requireActivity(), audioTitle -> binding.currentAudioTitle.setText(audioTitle));
                }
                initPlayOrPauseIcon();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.appBarFragment.topAppBar.setOnMenuItemClickListener(this::onMenuItemClick);
        binding.appBarFragment.topAppBar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        playerControls();
        closeSearchLayout();
        onAudioItemClick();
        onDownloadIcClickListener();
        binding.closeNotification.setOnClickListener(v -> initCheckedOrDownloadOrTrashIcon());
        deleteAllBookmarksOrDownloadedAudios();
    }

    private void playerControls() {
        playerControlsAtAudioListScreen(player);
        playerListener(player);
    }

    private void playerControlsAtAudioListScreen(ExoPlayer player) {
        binding.audioControlLayout.setOnClickListener(v -> {
            audioViewModel.setBottomControlClick(true);
            playingAudioBundle.putBoolean(AppConstant.PLAY_ANOTHER_AUDIO, false);
            playingAudioBundle.putInt(AppConstant.CURRENT_MEDIA_ITEM_INDEX, player.getCurrentMediaItemIndex());
            navController.navigate(R.id.action_audioListFragment_to_audioDetailsFragment, playingAudioBundle);
        });

        binding.previousIcon.setOnClickListener(v -> {
            if (player.hasPreviousMediaItem()) {
                player.seekToPrevious();
            }
        });

        binding.nextIcon.setOnClickListener(v -> {
            if (player.hasNextMediaItem()) {
                player.seekToNext();
            }
        });

        binding.playOrPauseIcon.setOnClickListener(v -> {
            if (player.isPlaying()) {
                player.pause();
            } else {
                if (player.getMediaItemCount() > 0) {
                    player.play();
                }
            }
            initPlayOrPauseIcon();
        });
    }

    private void playerListener(ExoPlayer player) {
        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                // show the playing audio title
                assert mediaItem != null;
                binding.currentAudioTitle.setText(mediaItem.mediaMetadata.title);

                // show audio lyrics and translations
                audioViewModel.setAudioLyrics((String) mediaItem.mediaMetadata.description);
                getAudioTranslations = ((Audio) mediaItem.mediaMetadata.extras.get(AppConstant.AUDIO_ITEM)).getTranslations();
                audioViewModel.setAudioTranslations(getAudioTranslations);

                initPlayOrPauseIcon();

                if (!player.isPlaying()) {
                    player.play();
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == ExoPlayer.STATE_READY && player.isPlaying()) {
                    // show the playing audio title
                    binding.currentAudioTitle.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);

                    // show audio lyrics and translations
                    audioViewModel.setAudioLyrics((String) player.getCurrentMediaItem().mediaMetadata.description);
                    getAudioTranslations = ((Audio) player.getCurrentMediaItem().mediaMetadata.extras.get(AppConstant.AUDIO_ITEM)).getTranslations();
                    audioViewModel.setAudioTranslations(getAudioTranslations);
                }
                initPlayOrPauseIcon();
            }
        });
    }

    private void initPlayOrPauseIcon() {
        if (player.isPlaying()) {
            playOrPauseIconId = R.drawable.pause_circle_blue_outline_ic;
        } else {
            playOrPauseIconId = R.drawable.play_circle_blue_outline_ic;
        }
        binding.playOrPauseIcon.setImageResource(playOrPauseIconId);
    }

    private void onAudioItemClick() {
        audioAdapter.setOnAudioItemClickListener(new AudioAdapter.OnAudioItemClickListener() {
            @Override
            public void onItemClick(Audio item, int position) {
                audioViewModel.setStopState(false);
                currentMediaItemIndex = player.getCurrentMediaItemIndex();

                if (player.isPlaying()) {
                    if (currentMediaItemIndex != position) {
                        playingAudioBundle.putBoolean(AppConstant.PLAY_ANOTHER_AUDIO, true);
                        player.pause();
                        player.seekTo(position, 0);
                    }
                    prepareAndPlayAudio(player);
                } else {
                    if (isAudioListScreen) {
                        // if topic id change, reset media items
                        if (oldTopicId != null && !Objects.equals(oldTopicId, topicId)) {
                            player.setMediaItems(ExoplayerHelper.getMediaItems(audioListByTopicId), 0, 0);
                        }

                        if (currentMediaItemIndex != position || (currentMediaItemIndex == 0 && isTheFirstTimePlayAudio)) {
                            setMediaItemsOnAudiosByIdScreen(position);
                        } else {
                            if (isTheFirstTimePlayAudio) {
                                setMediaItemsOnAudiosByIdScreen(position);
                            } else {
                                prepareAndPlayAudio(player);
                            }
                        }
                    } else if (isBookmarksScreen) {
                        if (position != currentMediaItemIndex || (currentMediaItemIndex == 0 && isTheFirstTimePlayAudio)) {
                            setMediaItemsOnBookmarksScreen(position);
                        } else {
                            if (isTheFirstTimePlayAudio) {
                                setMediaItemsOnBookmarksScreen(position);
                            } else {
                                prepareAndPlayAudio(player);
                            }
                        }
                    } else if (isAudioDownloadedScreen) {
                        if (currentMediaItemIndex != position || (currentMediaItemIndex == 0 && isTheFirstTimePlayAudio)) {
                            setMediaItemsOnDownloadedScreen(position);
                        } else {
                            if (isTheFirstTimePlayAudio) {
                                setMediaItemsOnDownloadedScreen(position);
                            } else {
                                prepareAndPlayAudio(player);
                            }
                        }
                    }
                }
                playingAudioBundle.putInt(AppConstant.CURRENT_MEDIA_ITEM_INDEX, position);
                navController.navigate(R.id.action_audioListFragment_to_audioDetailsFragment, playingAudioBundle);
            }

            @Override
            public void onIconClick(Audio item, int position, ImageView icon) {
                switch (icon.getId()) {
                    case R.id.downloadIcon:
                        isDownloading = audioViewModel.getDownloadingFlag();
                        if (!isDownloading) {
                            binding.selectedNotificationLayout.setVisibility(View.VISIBLE);
                            if (icon.getContentDescription().equals(AppConstant.DOWNLOAD_ICON)) {
                                if (itemCounter < AppConstant.MAX_DOWNLOAD_FILES) {
                                    itemCounter++;

                                    contentDesc = AppConstant.CHECK_CIRCLE_ICON;
                                    resId = R.drawable.check_circle_ic;
                                    initDownloadOrCheckCircleIc(icon);

                                    audioViewModel.setItemDownloadSelectedCounter(itemCounter);

                                    audioSelected.put(item.getId(), item);
                                    audioPositionSelected.put(item.getId(), position);
                                } else {
                                    Toast.makeText(requireContext(), AppConstant.MAX_DOWNLOAD_FILES_MESSAGE, Toast.LENGTH_SHORT).show();
                                }
                            } else if (icon.getContentDescription().equals(AppConstant.CHECK_CIRCLE_ICON)) {
                                audioSelected.remove(item.getId());
                                audioPositionSelected.remove(item.getId());

                                contentDesc = AppConstant.DOWNLOAD_ICON;
                                resId = R.drawable.white_download_ic;
                                initDownloadOrCheckCircleIc(icon);

                                itemCounter--;
                                audioViewModel.setItemDownloadSelectedCounter(itemCounter);
                            }
                            audioViewModel.setAudioSelected(audioSelected);
                            audioViewModel.setAudioCheckedPosListLiveData(audioPositionSelected);
                            initItemSelectedCounterLayout();
                        } else {
                            Toast.makeText(requireContext(), AppConstant.DOWNLOAD_IS_IN_PROGRESS, Toast.LENGTH_SHORT).show();
                        }
//                        if (icon.getContentDescription().equals(AppConstant.TRASH_ICON)) {
//                            Log.e("DNV-path", item.getAudioFileFromDevice() == null ? "null" : item.getAudioFileFromDevice());
//                        }
                        break;
                    case R.id.bookmarkIcon:
                        if (icon.getContentDescription().equals(getString(R.string.bookmark_border_icon))) {
                            contentDesc = getString(R.string.bookmark_icon);
                            resId = R.drawable.blue_bookmark_ic;
                            // add bookmark here
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
                        if (icon.getContentDescription().equals(getString(R.string.bookmark_icon))) {
                            audioViewModel.setBookmarkDeletedPos(position);
                            contentDesc = getString(R.string.bookmark_border_icon);
                            resId = R.drawable.bookmark_blue_border_ic;
                            // remove bookmark here
                            Completable deleteBookmarkObservable = deleteBookmark(item);
                            CompletableObserver deleteBookmarkObserver = deleteBookmarkObserver();
                            if (deleteBookmarkObservable != null) {
                                deleteBookmarkObservable.subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(deleteBookmarkObserver);
                            } else {
                                Toast.makeText(requireContext(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show();
                            }

                            updateAudioStateAfterRemoveBookmark();
                        }
                        icon.setImageResource(resId);
                        icon.setContentDescription(contentDesc);
                        break;
                }
            }
        });
    }

    private void setMediaItemsOnAudiosByIdScreen(int position) {
        player.setMediaItems(ExoplayerHelper.getMediaItems(audioListByTopicId), position, 0);
        isTheFirstTimePlayAudio = false;
    }

    private void setMediaItemsOnBookmarksScreen(int position) {
        // set media items khi chua remove bookmark
        player.setMediaItems(ExoplayerHelper.getMediaItems(bookmarksList), position, 0);
        isTheFirstTimePlayAudio = false;
    }

    private void updateAudioStateAfterRemoveBookmark() {
        // cap nhat trang thai audio khi da remove bookmark
        audioRepository.getBookmarkList().observe(requireActivity(), bookmarksList -> {
            int deletePosition = audioViewModel.getBookmarkDeletedPos();
            int currentMediaItemIndex = player.getCurrentMediaItemIndex();
            if (bookmarksList != null && !bookmarksList.isEmpty()) {
                isTheFirstTimePlayAudio = false;
                if (deletePosition < currentMediaItemIndex) {
                    player.setMediaItems(ExoplayerHelper.getMediaItems(bookmarksList),
                            currentMediaItemIndex - 1, player.getCurrentPosition());
                } else if (deletePosition > currentMediaItemIndex) {
                    player.setMediaItems(ExoplayerHelper.getMediaItems(bookmarksList),
                            currentMediaItemIndex, player.getCurrentPosition());
                } else {
                    player.setMediaItems(ExoplayerHelper.getMediaItems(bookmarksList), 0, 0);
                }
            } else {
                setNoDataState();
            }
        });
    }

    private void setMediaItemsOnDownloadedScreen(int position) {
        // set media items khi chua remove downloaded audio
        player.setMediaItems(ExoplayerHelper.getMediaItems(downloadedAudioList), position, 0);
        isTheFirstTimePlayAudio = false;

        // cap nhat trang thai audio khi da remove downloaded audio
        downloadedAudioRepository.getDownloadedAudioList().observe(requireActivity(), downloadedAudioList -> {
            int deletePosition = audioViewModel.getDownloadAudioDeletePos();
            int currentMediaItemIndex = player.getCurrentMediaItemIndex();
            if (downloadedAudioList != null && !downloadedAudioList.isEmpty()) {
                isTheFirstTimePlayAudio = false;
                if (deletePosition < currentMediaItemIndex) {
                    player.setMediaItems(ExoplayerHelper.getMediaItems(downloadedAudioList),
                            currentMediaItemIndex - 1, player.getCurrentPosition());
                } else if (deletePosition > currentMediaItemIndex) {
                    player.setMediaItems(ExoplayerHelper.getMediaItems(downloadedAudioList),
                            currentMediaItemIndex, player.getCurrentPosition());
                } else {
                    player.setMediaItems(ExoplayerHelper.getMediaItems(downloadedAudioList), 0, 0);
                }
            } else {
                setNoDataState();
            }
        });
    }

    private void setNoDataState() {
        initDeleteAllBtn(false);
        setStopStateAndStopExoplayer();
        binding.audioControlLayout.setVisibility(View.GONE);
    }

    private void initDownloadOrCheckCircleIc(ImageView icon) {
        icon.setImageResource(resId);
        icon.setContentDescription(contentDesc);
    }

    private void initItemSelectedCounterLayout() {
        audioViewModel.getItemDownloadSelectedCounter().observe(requireActivity(), counter -> {
            if (counter == 0) {
                binding.selectedNotificationLayout.setVisibility(View.GONE);
                initDeleteAllBtn(true);
            } else {
                initDeleteAllBtn(false);
                binding.selectedNotificationLayout.setVisibility(View.VISIBLE);
                itemSelectedCounterTxt = new StringBuffer();
                itemSelectedCounterTxt.append(counter).append(AppConstant.ITEM_SELECTED_COUNTER_TXT);
                binding.itemCounterSelected.setText(itemSelectedCounterTxt.toString());
            }
        });
    }

    private void onDownloadIcClickListener() {
        binding.download.setOnClickListener(v -> {
            binding.selectedNotificationLayout.setVisibility(View.GONE);
            Intent intent = new Intent(requireActivity(), DownloadAudioService.class);
            intent.putExtra(AppConstant.AUDIO_SELECTED_LIST, audioSelected);
            audioViewModel.setIsDownloading(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireActivity().startForegroundService(intent);
            } else {
                requireActivity().startService(intent);
            }
        });
    }

    private void initCheckedOrDownloadOrTrashIcon() {
        audioViewModel.setAudioSelected(new HashMap<>());
        audioViewModel.setItemDownloadSelectedCounter(0);
        initItemSelectedCounterLayout();
        audioPositionSelected.clear();
        audioAdapter.notifyDataSetChanged();
    }

    private void closeSearchLayout() {
        binding.closeSearchLayout.setOnClickListener(v -> {
            binding.searchEdtLayout.setVisibility(View.GONE);
            binding.closeSearchLayout.setVisibility(View.GONE);
            initDeleteAllBtn(bookmarksList != null && !bookmarksList.isEmpty());
            reloadAudioListAfterSearch();
        });
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
                Toast.makeText(requireContext(), AppConstant.REMOVE_BOOKMARK_SUCCESS, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                Toast.makeText(requireContext(), AppConstant.REMOVE_BOOKMARK_FAILED, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void prepareAndPlayAudio(ExoPlayer player) {
        player.prepare();
        player.play();
    }

    @SuppressLint("NonConstantResourceId")
    private boolean onMenuItemClick(MenuItem menu) {
        switch (menu.getItemId()) {
            case R.id.searchItem:
                if (binding.searchEdtLayout.getVisibility() == View.VISIBLE) {
                    binding.searchEdtLayout.setVisibility(View.GONE);
                    binding.closeSearchLayout.setVisibility(View.GONE);
                    initDeleteAllBtn(bookmarksList != null && !bookmarksList.isEmpty());
                    reloadAudioListAfterSearch();
                } else {
                    binding.searchEdtLayout.setVisibility(View.VISIBLE);
                    binding.closeSearchLayout.setVisibility(View.VISIBLE);
                    initDeleteAllBtn(false);
                    binding.searchEdt.requestFocus();
                    searchEdtTextChanged();
                }
                return true;
            case R.id.downloadItem:
                audioViewModel.setIsAudioDownloadedScreen(true);
                audioViewModel.setIsAudioListScreen(false);
                audioViewModel.setIsBookmarksScreen(false);
                setStopStateAndStopExoplayer();
                initAudioList();
                return true;
            case R.id.bookmarksItem:
                audioViewModel.setIsBookmarksScreen(true);
                audioViewModel.setIsAudioDownloadedScreen(false);
                audioViewModel.setIsAudioListScreen(false);
                setStopStateAndStopExoplayer();
                initAudioList();
                return true;
        }
        return false;
    }

    private void initDeleteAllBtn(boolean isVisibility) {
        if (!isAudioListScreen) {
            if (isVisibility) {
                binding.deleteAllBtn.setVisibility(View.VISIBLE);
            } else {
                binding.deleteAllBtn.setVisibility(View.GONE);
            }
        }
    }

    private void reloadAudioListAfterSearch() {
        binding.searchEdt.setText("");
        getAudioSearchList("");
    }

    private void searchEdtTextChanged() {
        binding.searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                getAudioSearchList(editable.toString().trim());
            }
        });
    }

    private void getAudioSearchList(String keyword) {
        List<Audio> audioSearchList = new ArrayList<>();
        if (isAudioListScreen) {
            for (Audio audio : audioListByTopicId) {
                if (audio.getName().toLowerCase().contains(keyword.toLowerCase())) {
                    audioSearchList.add(audio);
                }
            }
            searchListResult(audioSearchList);
        } else {
            if (isBookmarksScreen) {
                audioRepository.getBookmarkList().observe(requireActivity(), bookmarksList -> {
                    for (Audio audio : bookmarksList) {
                        if (audio.getName().toLowerCase().contains(keyword.toLowerCase())) {
                            audioSearchList.add(audio);
                        }
                    }
                    searchListResult(audioSearchList);
                });
            } else if (isAudioDownloadedScreen) {
                downloadedAudioRepository.getDownloadedAudioList().observe(requireActivity(), downloadedAudios -> {
                    for (Audio audio : downloadedAudios) {
                        if (audio.getName().toLowerCase().contains(keyword.toLowerCase())) {
                            audioSearchList.add(audio);
                        }
                    }
                    searchListResult(audioSearchList);
                });
            }
        }
    }

    private void searchListResult(List<Audio> audioSearchList) {
        if (audioSearchList.isEmpty()) {
            visibility = View.VISIBLE;
        } else {
            visibility = View.GONE;
        }
        binding.messageTxt.setVisibility(visibility);
        audioAdapter.searchList(audioSearchList);
    }

    private void deleteAllBookmarksOrDownloadedAudios() {
        binding.deleteAllBtn.setOnClickListener(v -> {
            Completable deleteAllObservable = deleteAll();
            CompletableObserver completableObserver = deleteAllCompletableObserver();
            if (deleteAllObservable != null) {
                deleteAllObservable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(completableObserver);
            } else {
                Toast.makeText(requireContext(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show();
            }
        });
        initDeleteAllBtn(false);
    }

    private CompletableObserver deleteAllCompletableObserver() {
        return new CompletableObserver() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                disposable = d;
            }

            @Override
            public void onComplete() {
                Toast.makeText(requireContext(), AppConstant.DELETE_SUCCESS, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                Toast.makeText(requireContext(), AppConstant.DELETE_FAILED, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private Completable deleteAll() {
        return Completable.create(emitter -> {
            if (emitter.isDisposed()) {
                emitter.onError(new Exception());
            } else {
                if (isBookmarksScreen) {
                    audioRepository.deleteAllBookmarks();
                } else if (isAudioDownloadedScreen) {
                    downloadedAudioRepository.deleteAllDownloadedAudios();
                }
                emitter.onComplete();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setStopStateAndStopExoplayer();
        if (disposable != null) {
            disposable.dispose();
        }
        requireActivity().unregisterReceiver(downloadCompletedBroadcast);
    }

    private void setStopStateAndStopExoplayer() {
        audioViewModel.stopExoplayer();
        audioViewModel.setStopState(true);
    }
}