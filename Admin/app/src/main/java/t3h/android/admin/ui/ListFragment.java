package t3h.android.admin.ui;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import t3h.android.admin.R;
import t3h.android.admin.adapter.ItemListAdapter;
import t3h.android.admin.databinding.FragmentListBinding;
import t3h.android.admin.databinding.ItemListLayoutBinding;
import t3h.android.admin.helper.AppConstant;
import t3h.android.admin.model.Audio;
import t3h.android.admin.model.Topic;

public class ListFragment extends Fragment {
    private FragmentListBinding binding;
    private int position;
    private FirebaseDatabase firebaseDatabase;
    private ItemListAdapter<Topic> topicAdapter = new ItemListAdapter<>();
    private List<Topic> topicList = new ArrayList<>();
    private ItemListAdapter<Audio> audioAdapter = new ItemListAdapter<>();
    private List<Audio> audioList = new ArrayList<>();
    private Bundle bundle = new Bundle();
    private NavController navController;

    public ListFragment() {
    }

    public ListFragment(int position) {
        this.position = position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseDatabase = FirebaseDatabase.getInstance();
        navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
        initItemList();
    }

    private void initItemList() {
        binding.listRcv.setLayoutManager(new LinearLayoutManager(requireActivity()));
        binding.progressBar.setVisibility(View.VISIBLE);
        if (position == 1) {
            // init audios list here
            binding.listRcv.setAdapter(audioAdapter);
            fetchAudioList();
            audioAdapter.bindAdapter((audio, itemListLayoutBinding) -> {
                resetViewColor(itemListLayoutBinding);
                if (audio.getStatus() == 0) {
                    bindInActiveItem(itemListLayoutBinding);
                }
                itemListLayoutBinding.itemName.setText(audio.getName());
            });
        } else {
            // init topics list here
            binding.listRcv.setAdapter(topicAdapter);
            fetchTopicList();
            topicAdapter.bindAdapter((topic, itemListLayoutBinding) -> {
                resetViewColor(itemListLayoutBinding);
                if (topic.getStatus() == 0) {
                    bindInActiveItem(itemListLayoutBinding);
                }
                itemListLayoutBinding.itemName.setText(topic.getName());
            });
        }
    }

    private void fetchTopicList() {
        firebaseDatabase.getReference().child(AppConstant.TOPICS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                topicList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Topic model = data.getValue(Topic.class);
                    if (model != null) {
                        topicList.add(model);
                    }
                }
                if (topicList == null) {
                    binding.noDataTxt.setVisibility(View.VISIBLE);
                } else {
                    binding.noDataTxt.setVisibility(View.GONE);
                }
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

    private void fetchAudioList() {
        firebaseDatabase.getReference().child(AppConstant.AUDIO_STORAGE_NAME).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                audioList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Audio audio = data.getValue(Audio.class);
                    if (audio != null) {
                        audioList.add(audio);
                    }
                }
                if (audioList == null) {
                    binding.noDataTxt.setVisibility(View.VISIBLE);
                } else {
                    binding.noDataTxt.setVisibility(View.GONE);
                }
                audioAdapter.updateItemList(audioList);
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireActivity(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetViewColor(ItemListLayoutBinding view) {
        view.itemName.setTextColor(getResources().getColor(R.color.black));
        view.itemListLayout.setBackgroundResource(R.drawable.blue_border_bg);
    }

    private void bindInActiveItem(ItemListLayoutBinding view) {
        view.itemName.setTextColor(getResources().getColor(R.color.dangerColor));
        view.itemListLayout.setBackgroundResource(R.drawable.red_boder_bg);
    }

    @Override
    public void onResume() {
        super.onResume();
        topicAdapter.setOnItemClickListener(topicItem -> {
            bundle.putBoolean(AppConstant.IS_UPDATE, true);
            bundle.putSerializable(AppConstant.TOPIC_INFO, topicItem);
            navController.navigate(R.id.action_dashboardFragment_to_createOrUpdateTopicFragment, bundle);
        });
        audioAdapter.setOnItemClickListener(audioItem -> {
            bundle.putBoolean(AppConstant.IS_UPDATE, true);
            bundle.putSerializable(AppConstant.AUDIO_INFO, audioItem);
            navController.navigate(R.id.action_dashboardFragment_to_createOrUpdateAudioFragment, bundle);
        });
        binding.searchImageView.setOnClickListener(v -> onSearchImageViewClick());
        binding.goToTopImageView.setOnClickListener(v -> binding.listRcv.smoothScrollToPosition(0));
    }

    private void onSearchImageViewClick() {
        if (binding.searchEdtLayout.getVisibility() == View.VISIBLE) {
            binding.searchEdtLayout.setVisibility(View.GONE);
            binding.searchEdt.setText("");
            binding.progressBar.setVisibility(View.VISIBLE);
            if (position == 0) {
                fetchTopicList();
            } else {
                fetchAudioList();
            }
            binding.searchImageView.setBackgroundResource(R.drawable.blue_bg);
        } else {
            binding.searchEdtLayout.setVisibility(View.VISIBLE);
            binding.searchEdt.requestFocus();
            if (position == 0) {
                binding.searchEdt.setHint(AppConstant.SEARCH_TOPIC);
                searchTopicTextChanged();
            } else {
                binding.searchEdt.setHint(AppConstant.SEARCH_AUDIO);
                searchAudioTextChanged();
            }
            binding.searchImageView.setBackgroundResource(R.drawable.pressed_search_btn_bg);
        }
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
                topicSearchList(editable.toString().trim());
            }
        });
    }

    private void searchAudioTextChanged() {
        binding.searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                audioSearchList(editable.toString().trim());
            }
        });
    }

    private void topicSearchList(String keyword) {
        List<Topic> topicSearchList = new ArrayList<>();
        for (Topic data: topicList) {
            if (data.getName().toLowerCase().contains(keyword.toLowerCase())) {
                topicSearchList.add(data);
            }
        }
        if (topicSearchList.isEmpty()) {
            binding.noDataTxt.setVisibility(View.VISIBLE);
        } else {
            binding.noDataTxt.setVisibility(View.GONE);
        }
        topicAdapter.searchList(topicSearchList);
    }

    private void audioSearchList(String keyword) {
        List<Audio> audioSearchList = new ArrayList<>();
        for (Audio data: audioList) {
            if (data.getName().toLowerCase().contains(keyword.toLowerCase())) {
                audioSearchList.add(data);
            }
        }
        if (audioSearchList.isEmpty()) {
            binding.noDataTxt.setVisibility(View.VISIBLE);
        } else {
            binding.noDataTxt.setVisibility(View.GONE);
        }
        audioAdapter.searchList(audioSearchList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}