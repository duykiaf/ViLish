package t3h.android.admin.ui;

import android.graphics.Color;
import android.graphics.PorterDuff;
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

import t3h.android.admin.R;
import t3h.android.admin.databinding.FragmentChangePasswordBinding;
import t3h.android.admin.helper.AppConstant;
import t3h.android.admin.helper.FirebaseAuthHelper;

public class ChangePasswordFragment extends Fragment {
    private FragmentChangePasswordBinding binding;
    private NavController navController;
    private String oldPwd, newPwd, confirmNewPwd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.backTxt.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.submitBtnLayout.submitBtn.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        oldPwd = binding.oldPasswordEdt.getText().toString();
        newPwd = binding.newPasswordEdt.getText().toString();
        confirmNewPwd = binding.confirmNewPasswordEdt.getText().toString();
        if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmNewPwd.isEmpty()) {
            Toast.makeText(requireActivity(), AppConstant.EMPTY_ERROR, Toast.LENGTH_LONG).show();
        } else {
            if (!confirmNewPwd.equals(newPwd)) {
                Toast.makeText(requireActivity(), AppConstant.CONFIRM_PWD_NOT_MATCH, Toast.LENGTH_LONG).show();
            } else {
                binding.submitBtnLayout.progressBar.setVisibility(View.VISIBLE);
                binding.submitBtnLayout.progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                FirebaseAuthHelper.changePwd(oldPwd, newPwd, confirmNewPwd, task -> {
                    binding.submitBtnLayout.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(requireActivity(), AppConstant.CHANGE_PWD_SUCCESS, Toast.LENGTH_LONG).show();
                        FirebaseAuthHelper.signOut();
                        navController.navigate(R.id.signInFragment);
                    } else {
                        Toast.makeText(requireActivity(), AppConstant.CHANGE_PWD_FAILED, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}