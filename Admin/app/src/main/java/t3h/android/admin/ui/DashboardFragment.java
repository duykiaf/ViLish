package t3h.android.admin.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import t3h.android.admin.R;
import t3h.android.admin.helper.AppConstant;
import t3h.android.admin.listener.OnBackPressedListener;

public class DashboardFragment extends Fragment implements OnBackPressedListener {
    private boolean backPressedOnce = false;
    private Toast toast;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
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