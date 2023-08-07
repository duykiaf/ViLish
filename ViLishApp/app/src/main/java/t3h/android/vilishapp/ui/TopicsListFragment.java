package t3h.android.vilishapp.ui;

import android.os.Bundle;
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

import autodispose2.AutoDispose;
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import t3h.android.vilishapp.R;
import t3h.android.vilishapp.adapters.TopicAdapter;
import t3h.android.vilishapp.databinding.FragmentTopicsListBinding;
import t3h.android.vilishapp.helpers.ItemDiff;
import t3h.android.vilishapp.helpers.NetworkHelper;
import t3h.android.vilishapp.viewmodels.TopicViewModel;

public class TopicsListFragment extends Fragment {
    private FragmentTopicsListBinding binding;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTopicsListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
        if (NetworkHelper.isInternetConnected(requireContext())) {
            Toast.makeText(requireActivity(), "Internet connected", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireActivity(), "Internet disconnected", Toast.LENGTH_SHORT).show();
        }
        initTopicsList();
    }

    private void initTopicsList() {
        TopicViewModel topicViewModel = new ViewModelProvider(requireActivity()).get(TopicViewModel.class);
        TopicAdapter topicAdapter = new TopicAdapter(new ItemDiff<>());
        topicViewModel.flowable.to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(requireActivity())))
                .subscribe(pagingData -> topicAdapter.submitData(getLifecycle(), pagingData));

    }

    @Override
    public void onResume() {
        super.onResume();
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