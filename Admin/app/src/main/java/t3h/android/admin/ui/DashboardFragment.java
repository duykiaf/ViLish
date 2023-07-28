package t3h.android.admin.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import t3h.android.admin.R;
import t3h.android.admin.databinding.FragmentDashboardBinding;
import t3h.android.admin.helper.AppConstant;
import t3h.android.admin.helper.FirebaseAuthHelper;
import t3h.android.admin.listener.OnBackPressedListener;

public class DashboardFragment extends Fragment implements OnBackPressedListener {
    private boolean backPressedOnce = false;
    private Toast toast;
    private FragmentDashboardBinding binding;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
        initTopAppBar();
    }

    private void initTopAppBar() {
        binding.appBarFragment.topAppBar.setTitle(AppConstant.DASHBOARD);
        binding.appBarFragment.topAppBar.setNavigationIcon(R.drawable.dashboard_ic);
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
                    navController.navigate(R.id.action_dashboardFragment_to_changePasswordFragment);
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

    @Override
    public void onFragmentBackPressed() {
        if (backPressedOnce) {
            requireActivity().finishAffinity();
            toast.cancel();
        } else {
            toast = Toast.makeText(requireActivity(), AppConstant.PRESS_AGAIN, Toast.LENGTH_SHORT);
            toast.show();
            backPressedOnce = true;
            new Handler().postDelayed(() -> backPressedOnce = false, 2000);
        }
    }
}