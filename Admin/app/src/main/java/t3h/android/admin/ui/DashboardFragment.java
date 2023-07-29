package t3h.android.admin.ui;

import android.annotation.SuppressLint;
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
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import t3h.android.admin.R;
import t3h.android.admin.adapter.DashboardAdapter;
import t3h.android.admin.databinding.FragmentDashboardBinding;
import t3h.android.admin.helper.AppConstant;
import t3h.android.admin.helper.FirebaseAuthHelper;
import t3h.android.admin.listener.OnBackPressedListener;

public class DashboardFragment extends Fragment implements OnBackPressedListener {
    private boolean backPressedOnce = false;
    private Toast toast;
    private FragmentDashboardBinding binding;
    private NavController navController;
    private DashboardAdapter dashboardAdapter;
    private String[] tabLayoutNames;
    private Bundle bundle = new Bundle();

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
        initPagerUI();
    }

    private void initTopAppBar() {
        binding.appBarFragment.topAppBar.setTitle(AppConstant.DASHBOARD);
        binding.appBarFragment.topAppBar.setNavigationIcon(R.drawable.dashboard_ic);
    }

    private void initPagerUI() {
        dashboardAdapter = new DashboardAdapter(this);
        binding.pager.setAdapter(dashboardAdapter);
        tabLayoutNames = getResources().getStringArray(R.array.tabLayoutNames);
        TabLayout tabLayout = binding.tabLayout;
        new TabLayoutMediator(tabLayout, binding.pager,
                (tab, position) -> tab.setText(tabLayoutNames[position])
        ).attach();

        binding.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    initListByPager(AppConstant.ADD_NEW_TOPIC, AppConstant.SEARCH_TOPIC);
                } else {
                    initListByPager(AppConstant.ADD_NEW_AUDIO, AppConstant.SEARCH_AUDIO);
                }
            }
        });
    }

    private void initListByPager(String addNewTxt, String searchTxt) {
        binding.addNewImageView.setContentDescription(addNewTxt);
        binding.searchImageView.setContentDescription(searchTxt);
        binding.searchEdt.setHint(searchTxt);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.appBarFragment.topAppBar.setNavigationOnClickListener(v -> onBackPressed());
        onMenuItemClick();
        binding.addNewImageView.setOnClickListener(v -> onNavigateToCreateFragment());
    }

    private void onBackPressed() {
        if (!binding.appBarFragment.topAppBar.getTitle().equals(AppConstant.DASHBOARD)) {
            requireActivity().onBackPressed();
        }
    }

    @SuppressLint("NonConstantResourceId")
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

    private void onNavigateToCreateFragment() {
        bundle.putBoolean(AppConstant.IS_UPDATE, false);
        String getContentDesc = binding.addNewImageView.getContentDescription().toString();
        if (getContentDesc.equalsIgnoreCase(AppConstant.ADD_NEW_TOPIC)) {
            navController.navigate(R.id.action_dashboardFragment_to_createOrUpdateTopicFragment, bundle);
        } else {
            navController.navigate(R.id.action_dashboardFragment_to_createOrUpdateAudioFragment, bundle);
        }
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