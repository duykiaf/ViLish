package t3h.android.admin.ui;

import android.graphics.Color;
import android.graphics.PorterDuff;
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

import com.google.firebase.auth.FirebaseUser;

import t3h.android.admin.R;
import t3h.android.admin.databinding.FragmentSignInBinding;
import t3h.android.admin.helper.AppConstant;
import t3h.android.admin.helper.FirebaseAuthHelper;
import t3h.android.admin.listener.OnBackPressedListener;

public class SignInFragment extends Fragment implements OnBackPressedListener {
    private FragmentSignInBinding binding;
    private NavController navController;
    private FirebaseUser firebaseUser;
    private String email, password;
    private boolean backPressedOnce = false;
    private Toast toast;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignInBinding.inflate(inflater, container, false);
        firebaseUser = FirebaseAuthHelper.getCurrentUser();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.submitBtnLayout.submitText.setText(getResources().getString(R.string.sign_in));
        navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
        if (firebaseUser != null) {
            navController.navigate(R.id.action_signInFragment_to_dashboardFragment);
            onDestroy();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (firebaseUser == null) {
            binding.forgotPwdTxt.setOnClickListener(v -> navController.navigate(R.id.action_signInFragment_to_forgotPasswordFragment));
            binding.submitBtnLayout.submitBtn.setOnClickListener(v -> signIn());
        }
    }

    private void signIn() {
        email = binding.emailEdt.getText().toString().trim();
        password = binding.passwordEdt.getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireActivity(), AppConstant.EMPTY_ERROR, Toast.LENGTH_LONG).show();
        } else {
            binding.submitBtnLayout.progressBar.setVisibility(View.VISIBLE);
            binding.submitBtnLayout.progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            FirebaseAuthHelper.signIn(email, password, task -> {
                binding.submitBtnLayout.progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    navController.navigate(R.id.action_signInFragment_to_dashboardFragment);
                } else {
                    Toast.makeText(requireActivity(), AppConstant.SIGN_FAILED, Toast.LENGTH_LONG).show();
                }
            });
        }
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