package t3h.android.admin.ui;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import t3h.android.admin.R;
import t3h.android.admin.databinding.FragmentCreateOrUpdateTopicBinding;
import t3h.android.admin.helper.AppConstant;
import t3h.android.admin.helper.FirebaseAuthHelper;

public class CreateOrUpdateTopicFragment extends Fragment {
    private FragmentCreateOrUpdateTopicBinding binding;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateOrUpdateTopicBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
        initTopAppBar();
    }

    private void initTopAppBar() {
        binding.appBarFragment.topAppBar.setTitle(AppConstant.CREATE_TOPIC);
        binding.appBarFragment.topAppBar.setNavigationIcon(R.drawable.arrow_back_ic);
        binding.appBarFragment.topAppBar.setNavigationIconTint(Color.WHITE);
    }

    @Override
    public void onResume() {
        super.onResume();
        onBackPressed();
        onMenuItemClick();
    }

    private void onBackPressed() {
        if (!binding.appBarFragment.topAppBar.getTitle().equals(AppConstant.DASHBOARD)) {
            binding.appBarFragment.topAppBar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        }
    }

    private void onMenuItemClick() {
        binding.appBarFragment.topAppBar.setOnMenuItemClickListener(menu -> {
            switch (menu.getItemId()) {
                case R.id.changePasswordItem:
                    navController.navigate(R.id.action_createOrUpdateTopicFragment_to_changePasswordFragment);
                    return true;
                case R.id.logoutItem:
                    FirebaseAuthHelper.signOut();
                    navController.navigate(R.id.signInFragment);
                    return true;
            }
            return false;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}