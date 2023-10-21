package t3h.android.vilishapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import t3h.android.vilishapp.R;
import t3h.android.vilishapp.databinding.FragmentSplashScreenBinding;
import t3h.android.vilishapp.helpers.NetworkHelper;
import t3h.android.vilishapp.viewmodels.AudioViewModel;

public class SplashScreenFragment extends Fragment {
    private FragmentSplashScreenBinding binding;
    private NavController navController;
    private AudioViewModel audioViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSplashScreenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
        audioViewModel = new ViewModelProvider(requireActivity()).get(AudioViewModel.class);
        initAnimation();
    }

    private void initAnimation() {
        Animation topAnim = AnimationUtils.loadAnimation(requireActivity(), R.anim.top_animation);
        Animation bottomAnim = AnimationUtils.loadAnimation(requireActivity(), R.anim.bottom_animation);
        binding.logo.setAnimation(topAnim);
        binding.appName.setAnimation(bottomAnim);
        binding.slogan.setAnimation(bottomAnim);
        binding.startBtn.setAnimation(bottomAnim);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.startBtn.setOnClickListener(v -> {
            audioViewModel.setSplashScreenFlag(false);
            boolean networkAvailable = NetworkHelper.isInternetConnected(requireContext());
            if (!networkAvailable) {
                audioViewModel.setIsAudioDownloadedScreen(true);
                audioViewModel.setIsAudioListScreen(false);
                audioViewModel.setIsBookmarksScreen(false);
                navController.navigate(R.id.audioListFragment);
            } else {
                navController.navigate(R.id.action_splashScreenFragment_to_topicsListFragment);
            }
        });
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