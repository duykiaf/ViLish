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

import t3h.android.admin.databinding.FragmentForgotPasswordBinding;
import t3h.android.admin.helper.AppConstant;
import t3h.android.admin.helper.FirebaseAuthHelper;

public class ForgotPasswordFragment extends Fragment {
    private FragmentForgotPasswordBinding binding;
    private String email;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.backTxt.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.resetPwdBtn.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        email = binding.emailEdt.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(requireActivity(), AppConstant.EMAIL_MUST_NOT_EMPTY, Toast.LENGTH_LONG).show();
        } else {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            FirebaseAuthHelper.forgotPwd(email, task -> {
                binding.progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(requireActivity(), AppConstant.CHECK_EMAIL_MESS, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireActivity(), AppConstant.RESET_PWD_FAILED, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}