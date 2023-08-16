package t3h.android.vilishapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import t3h.android.vilishapp.databinding.FragmentTranslationsBinding;
import t3h.android.vilishapp.viewmodels.AudioViewModel;

public class TranslationsFragment extends Fragment {
    private FragmentTranslationsBinding binding;
    private AudioViewModel audioViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTranslationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        audioViewModel = new ViewModelProvider(requireActivity()).get(AudioViewModel.class);
        audioViewModel.getAudioTranslations().observe(requireActivity(), translations -> binding.lyricsTranslation.setText(translations));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}