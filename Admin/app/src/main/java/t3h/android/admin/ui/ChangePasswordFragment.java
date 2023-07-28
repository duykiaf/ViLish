package t3h.android.admin.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
    private Drawable drawableEnd;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onResume() {
        super.onResume();
        binding.backTxt.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.saveBtn.setOnClickListener(v -> changePassword());
        binding.oldPasswordEdt.setOnTouchListener((view, motionEvent) -> {
            setShowOrHidePassword(binding.oldPasswordEdt);
            return false;
        });
        binding.newPasswordEdt.setOnTouchListener((view, motionEvent) -> {
            setShowOrHidePassword(binding.newPasswordEdt);
            return false;
        });
        binding.confirmNewPasswordEdt.setOnTouchListener((view, motionEvent) -> {
            setShowOrHidePassword(binding.confirmNewPasswordEdt);
            return false;
        });
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
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                FirebaseAuthHelper.changePwd(oldPwd, newPwd, confirmNewPwd, task -> {
                    binding.progressBar.setVisibility(View.GONE);
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

    @SuppressLint("ClickableViewAccessibility")
    private void setShowOrHidePassword(EditText edt) {
        edt.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                // Check if the click is inside the drawableEnd bounds
                if (motionEvent.getRawX() >= (edt.getRight() - edt.getCompoundDrawables()[2].getBounds().width()) - edt.getPaddingEnd()) {
                    if (edt.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                        edt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        drawableEnd = ContextCompat.getDrawable(requireActivity(), R.drawable.eye_crossed_ic);
                        edt.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawableEnd, null);
                    } else {
                        edt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        drawableEnd = ContextCompat.getDrawable(requireActivity(), R.drawable.eye_ic);
                        edt.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawableEnd, null);
                    }
                    return true; // Consumed the touch event
                }
            }
            return false; // Allow other touch events to be processed
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}