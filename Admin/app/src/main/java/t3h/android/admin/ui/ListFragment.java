package t3h.android.admin.ui;

import android.os.Bundle;
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
        if (position == 1) {
            // init audios list here
        } else {
            // init topics list here
            binding.listRcv.setAdapter(topicAdapter);
            binding.listRcv.setLayoutManager(new LinearLayoutManager(requireActivity()));
            fetchTopicList();
            topicAdapter.bindAdapter((model, itemListLayoutBinding) -> {
                if (model.getStatus() == 0) {
                    bindInActiveItem(itemListLayoutBinding);
                }
                itemListLayoutBinding.itemName.setText(model.getName());
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
                topicAdapter.updateItemList(topicList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireActivity(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show();
            }
        });
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}