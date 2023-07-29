package t3h.android.admin.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import t3h.android.admin.ui.ListFragment;

public class DashboardAdapter extends FragmentStateAdapter {
    public DashboardAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new ListFragment(position);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
