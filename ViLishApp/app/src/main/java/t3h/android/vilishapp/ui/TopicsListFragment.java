package t3h.android.vilishapp.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import t3h.android.vilishapp.R;
import t3h.android.vilishapp.adapters.TopicAdapter;
import t3h.android.vilishapp.databinding.FragmentTopicsListBinding;
import t3h.android.vilishapp.helpers.AppConstant;
import t3h.android.vilishapp.models.Topic;
import t3h.android.vilishapp.viewmodels.AudioViewModel;

public class TopicsListFragment extends Fragment {
    private FragmentTopicsListBinding binding;
    private NavController navController;
    private FirebaseDatabase firebaseDatabase;
    private List<Topic> topicList = new ArrayList<>();
    private TopicAdapter topicAdapter;
    private int visibility;
    private AudioViewModel audioViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        firebaseDatabase = FirebaseDatabase.getInstance();
        binding = FragmentTopicsListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
        audioViewModel = new ViewModelProvider(requireActivity()).get(AudioViewModel.class);
        initTopicsList();
    }

    private void initTopicsList() {
        topicAdapter = new TopicAdapter(requireContext());
        binding.topicsRcv.setLayoutManager(new GridLayoutManager(requireContext(), AppConstant.SPAN_COUNT));
        binding.topicsRcv.setAdapter(topicAdapter);
        binding.progressBar.setVisibility(View.VISIBLE);
        fetchTopicList();
    }

    private void fetchTopicList() {
        DatabaseReference databaseReference = firebaseDatabase.getReference().child(AppConstant.TOPICS);
        databaseReference.keepSynced(true);
        Query query = databaseReference.orderByChild(AppConstant.STATUS).equalTo(AppConstant.ACTIVE);
        query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        topicList.clear();
                        for (DataSnapshot data: snapshot.getChildren()) {
                            Topic topicItem = data.getValue(Topic.class);
                            if (topicItem != null) {
                                topicList.add(topicItem);
                            }
                        }
                        if (topicList == null) {
                            visibility = View.VISIBLE;
                        } else {
                            visibility = View.GONE;
                        }
                        binding.noDataTxt.setVisibility(visibility);
                        topicAdapter.updateItemList(topicList);
                        binding.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireActivity(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        onMenuItemClick();
        binding.closeSearchLayout.setOnClickListener(v -> {
            binding.searchEdtLayout.setVisibility(View.GONE);
            binding.closeSearchLayout.setVisibility(View.GONE);
            reloadListAfterSearch();
        });
        topicAdapter.setOnTopicItemClickListener(item -> {
            audioViewModel.setIsAudioListScreen(true);

            Bundle bundle = new Bundle();
            bundle.putString(AppConstant.TOPIC_ID, item.getId());
            bundle.putString(AppConstant.TOPIC_NAME, item.getName());
            navController.navigate(R.id.action_topicsListFragment_to_audioListFragment, bundle);
        });
    }

    @SuppressLint("NonConstantResourceId")
    private void onMenuItemClick() {
        binding.appBarFragment.topAppBar.setOnMenuItemClickListener(menu -> {
            switch (menu.getItemId()) {
                case R.id.searchItem:
                    if (binding.searchEdtLayout.getVisibility() == View.VISIBLE) {
                        visibility = View.GONE;
                        reloadListAfterSearch();
                    } else {
                        visibility = View.VISIBLE;
                        binding.searchEdt.requestFocus();
                        searchTopicTextChanged();
                    }
                    binding.searchEdtLayout.setVisibility(visibility);
                    binding.closeSearchLayout.setVisibility(visibility);
                    return true;
                case R.id.downloadItem:
                    audioViewModel.setIsAudioDownloadedScreen(true);
                    audioViewModel.setIsAudioListScreen(false);
                    audioViewModel.setIsBookmarksScreen(false);
                    navController.navigate(R.id.action_topicsListFragment_to_audioListFragment);
                    return true;
                case R.id.bookmarksItem:
                    audioViewModel.setIsBookmarksScreen(true);
                    audioViewModel.setIsAudioDownloadedScreen(false);
                    audioViewModel.setIsAudioListScreen(false);
                    navController.navigate(R.id.action_topicsListFragment_to_audioListFragment);
                    return true;
            }
            return false;
        });
    }

    private void reloadListAfterSearch() {
        binding.searchEdt.setText("");
        fetchTopicList();
    }

    private void searchTopicTextChanged() {
        binding.searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                getTopicSearchList(editable.toString().trim());
            }
        });
    }

    private void getTopicSearchList(String keyword) {
        List<Topic> topicSearchList = new ArrayList<>();
        for (Topic topic: topicList) {
            if (topic.getName().toLowerCase().contains(keyword.toLowerCase())) {
                topicSearchList.add(topic);
            }
        }
        if (topicSearchList.isEmpty()) {
            visibility = View.VISIBLE;
        } else {
            visibility = View.GONE;
        }
        binding.noDataTxt.setVisibility(visibility);
        topicAdapter.searchList(topicSearchList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}