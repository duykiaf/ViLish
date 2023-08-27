package t3h.android.vilishapp.ui;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import t3h.android.vilishapp.R;
import t3h.android.vilishapp.adapters.AudioAdapter;
import t3h.android.vilishapp.adapters.FragmentAdapter;
import t3h.android.vilishapp.databinding.FragmentAudioListBinding;
import t3h.android.vilishapp.helpers.AppConstant;
import t3h.android.vilishapp.helpers.AudioHelper;
import t3h.android.vilishapp.helpers.ExoplayerHelper;
import t3h.android.vilishapp.models.Audio;
import t3h.android.vilishapp.repositories.AudioRepository;
import t3h.android.vilishapp.viewmodels.AudioViewModel;

public class AudioListFragment extends Fragment {
    private FragmentAudioListBinding binding;
    private NavController navController;
    private FirebaseDatabase firebaseDatabase;
    private String topicId;
    private List<Audio> activeAudioList = new ArrayList<>();
    private List<Audio> audioListByTopicId = new ArrayList<>();
    private int visibility, resId, playOrPauseIconId, currentMediaItemIndex;
    private String contentDesc;
    private AudioAdapter audioAdapter;
    private ExoPlayer player;
    private AudioViewModel audioViewModel;
    private boolean isTheFirstTimeInitAudioControlsLayout = true;
    private Disposable disposable;
    private AudioRepository audioRepository;

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
        audioRepository = new AudioRepository(requireActivity().getApplication());
        initTopAppBar();
        topicId = requireArguments().getString(AppConstant.TOPIC_ID);
        if (topicId != null) {
            audioAdapter = new AudioAdapter(getContext());
            binding.audiosRcv.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.audiosRcv.setAdapter(audioAdapter);
            binding.progressBar.setVisibility(View.VISIBLE);
            initAudioListByTopicId();
        } else {
            Toast.makeText(requireActivity(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_LONG).show();
        }

        // assign exoplayer
        player = new ExoPlayer.Builder(requireContext()).build();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);

        audioViewModel = new ViewModelProvider(requireActivity()).get(AudioViewModel.class);

        // player controls method
        playerControls();
    }

    private void initTopAppBar() {
        String audioScreenTitle = AppConstant.AUDIO_SCREEN_TITLE;
        if (requireArguments().get(AppConstant.TOPIC_NAME) != null) {
            audioScreenTitle = (String) requireArguments().get(AppConstant.TOPIC_NAME);
        }
        binding.appBarFragment.topAppBar.setTitle(audioScreenTitle);
        binding.appBarFragment.topAppBar.setNavigationIcon(R.drawable.arrow_back_ic);
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
                            binding.noDataTxt.setVisibility(visibility);
                            audioRepository.getBookmarkAudioIds().observe(requireActivity(), bookmarkAudioIds -> {
                                audioAdapter.setBookmarkAudioIds(bookmarkAudioIds);
                                audioAdapter.updateItemList(audioListByTopicId);
                            });
                            binding.progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            systemError();
                        }
                    });
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.noDataTxt.setVisibility(View.VISIBLE);
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

    private void playerControls() {
        playerControlsAtAudioListScreen();
        playerControlsAtAudioDetailsScreen();
        ExoplayerHelper.playerListener(player, binding, audioViewModel);
    }

    private void playerControlsAtAudioListScreen() {
        binding.audioControlLayout.setOnClickListener(v -> openAudioDetailsScreen());

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
                initPlayOrPauseIcon(false);
            } else {
                if (player.getMediaItemCount() > 0) {
                    player.play();
                    initPlayOrPauseIcon(true);
                }
            }
        });
    }

    private void playerControlsAtAudioDetailsScreen() {
        binding.fragmentAudioDetailsLayout.previousIcon.setOnClickListener(v -> {
            if (player.hasPreviousMediaItem()) {
                player.seekToPrevious();
                updatePlayerPositionProgress(player);
            }
        });

        binding.fragmentAudioDetailsLayout.nextIcon.setOnClickListener(v -> {
            if (player.hasNextMediaItem()) {
                player.seekToNext();
                updatePlayerPositionProgress(player);
            }
        });

        // play/pause
        binding.fragmentAudioDetailsLayout.playOrPauseIcon.setOnClickListener(v -> {
            if (player.isPlaying()) {
                player.pause();
                initPlayOrPauseIcon(false);
            } else {
                player.play();
                initPlayOrPauseIcon(true);
            }
        });

        // set seek bar changed listener
        seekBarChangedListener(player);
    }

    private void initPlayOrPauseIcon(boolean isPlaying) {
        if (isPlaying) {
            playOrPauseIconId = R.drawable.pause_circle_blue_outline_ic;
        } else {
            playOrPauseIconId = R.drawable.play_circle_blue_outline_ic;
        }
        binding.playOrPauseIcon.setImageResource(playOrPauseIconId);
        binding.fragmentAudioDetailsLayout.playOrPauseIcon.setImageResource(playOrPauseIconId);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.appBarFragment.topAppBar.setOnMenuItemClickListener(this::onMenuItemClick);
        binding.appBarFragment.topAppBar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        closeSearchLayout();

        audioAdapter.setOnAudioItemClickListener(new AudioAdapter.OnAudioItemClickListener() {
            @Override
            public void onItemClick(Audio item, int position) {
                currentMediaItemIndex = player.getCurrentMediaItemIndex();

                // open AudioDetailsFragment
                openAudioDetailsScreen();

                // play/pause logic
                if (!player.isPlaying()) { // pause or stop
                    if (currentMediaItemIndex != position || (currentMediaItemIndex == 0 && isTheFirstTimeInitAudioControlsLayout)) {
                        player.setMediaItems(ExoplayerHelper.getMediaItems(audioListByTopicId), position, 0);
                        prepareAndPlayAudio();
                    }
                    isTheFirstTimeInitAudioControlsLayout = false;
                } else { // is playing
                    if (currentMediaItemIndex != position) {
                        player.pause();
                        player.seekTo(position, 0);
                    }
                    prepareAndPlayAudio();
                }

                // show bottom audio controls
                binding.audioControlLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onIconClick(Audio item, int position, ImageView icon) {
                switch (icon.getId()) {
                    case R.id.playOrPauseIcon:
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
                        }
                        icon.setImageResource(resId);
                        icon.setContentDescription(contentDesc);
                        break;
                }
            }
        });

        // exist audio details screen
        binding.fragmentAudioDetailsLayout.topAppBar.setNavigationOnClickListener(v ->
                binding.fragmentAudioDetailsLayout.audioDetailsLayout.setVisibility(View.GONE)
        );
    }

    private void closeSearchLayout() {
        binding.closeSearchLayout.setOnClickListener(v -> {
            binding.searchEdtLayout.setVisibility(View.GONE);
            binding.closeSearchLayout.setVisibility(View.GONE);
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

    // prepare and play
    private void prepareAndPlayAudio() {
        player.prepare();
        player.play();
    }

    private void openAudioDetailsScreen() {
        binding.fragmentAudioDetailsLayout.audioDetailsLayout.setVisibility(View.VISIBLE);
        // checking if the player is playing
        if (player.isPlaying()) {
            binding.fragmentAudioDetailsLayout.audioTitle.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
            binding.playOrPauseIcon.setImageResource(R.drawable.pause_circle_blue_outline_ic);
            initSeekBarAudioDuration(player);
        }
        initViewPager();
    }

    private void initSeekBarAudioDuration(ExoPlayer player) {
        binding.fragmentAudioDetailsLayout.audioDuration.setText(AudioHelper.milliSecondsToTimer((int) player.getDuration()));
        binding.fragmentAudioDetailsLayout.seekBar.setMax((int) player.getDuration());
        binding.fragmentAudioDetailsLayout.seekBar.setProgress((int) player.getCurrentPosition());
        binding.fragmentAudioDetailsLayout.audioCurrentTime.setText(AudioHelper.milliSecondsToTimer((int) player.getCurrentPosition()));
        updatePlayerPositionProgress(player);
    }

    private void updatePlayerPositionProgress(ExoPlayer player) {
        new Handler().postDelayed(() -> {
            if (player.isPlaying() && binding != null) {
                binding.fragmentAudioDetailsLayout.audioCurrentTime.setText(AudioHelper.milliSecondsToTimer((int) player.getCurrentPosition()));
                binding.fragmentAudioDetailsLayout.seekBar.setProgress((int) player.getCurrentPosition());
            }
            updatePlayerPositionProgress(player);
        }, 1000);
    }

    private void seekBarChangedListener(ExoPlayer player) {
        binding.fragmentAudioDetailsLayout.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                binding.fragmentAudioDetailsLayout.audioCurrentTime.setText(AudioHelper.milliSecondsToTimer(progressValue));
                player.seekTo(progressValue);
            }
        });
    }

    private void initViewPager() {
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new LyricsFragment());
        fragmentList.add(new TranslationsFragment());

        FragmentAdapter fragmentAdapter = new FragmentAdapter(requireActivity(), fragmentList);
        binding.fragmentAudioDetailsLayout.pager.setAdapter(fragmentAdapter);

        String[] tabLayoutNames = {AppConstant.LYRICS, AppConstant.TRANSLATIONS};
        new TabLayoutMediator(binding.fragmentAudioDetailsLayout.tabLayout, binding.fragmentAudioDetailsLayout.pager,
                (tab, position) -> tab.setText(tabLayoutNames[position])
        ).attach();
    }

    private boolean onMenuItemClick(MenuItem menu) {
        switch (menu.getItemId()) {
            case R.id.searchItem:
                if (binding.searchEdtLayout.getVisibility() == View.VISIBLE) {
                    visibility = View.GONE;
                    reloadAudioListAfterSearch();
                } else {
                    visibility = View.VISIBLE;
                    binding.searchEdt.requestFocus();
                    searchEdtTextChanged();
                }
                binding.searchEdtLayout.setVisibility(visibility);
                binding.closeSearchLayout.setVisibility(visibility);
                return true;
            case R.id.bookmarksItem:
                audioRepository.getBookmarkList().observe(requireActivity(), audioList -> {
                    for (Audio bookmarkAudio : audioList) {
                        Log.e("DNV", bookmarkAudio.getName());
                    }
                });
                return true;
            case R.id.goToTopItem:
                binding.audiosRcv.smoothScrollToPosition(0);
                return true;
        }
        return false;
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
        for (Audio audio : audioListByTopicId) {
            if (audio.getName().toLowerCase().contains(keyword.toLowerCase())) {
                audioSearchList.add(audio);
            }
        }
        if (audioSearchList.isEmpty()) {
            visibility = View.VISIBLE;
        } else {
            visibility = View.GONE;
        }
        binding.noDataTxt.setVisibility(visibility);
        audioAdapter.searchList(audioSearchList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // release the player
        if (player.isPlaying()) {
            player.stop();
        }
        player.release();

        if (disposable != null) {
            disposable.dispose();
        }

        binding = null;
    }
}