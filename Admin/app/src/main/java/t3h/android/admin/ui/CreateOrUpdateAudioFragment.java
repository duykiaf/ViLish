package t3h.android.admin.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import t3h.android.admin.R;
import t3h.android.admin.databinding.FragmentCreateOrUpdateAudioBinding;
import t3h.android.admin.helper.AppConstant;
import t3h.android.admin.helper.FirebaseAuthHelper;
import t3h.android.admin.helper.RandomStringGenerator;
import t3h.android.admin.helper.StatusDropdownHelper;
import t3h.android.admin.model.Audio;
import t3h.android.admin.model.DropdownItem;
import t3h.android.admin.model.Topic;

public class CreateOrUpdateAudioFragment extends Fragment {
    private FragmentCreateOrUpdateAudioBinding binding;
    private NavController navController;
    private boolean isUpdateView = false;
    private Uri selectedAudioUri;
    private String title, audioFileName, audioURL, lyrics, translations, topicId, topicName;
    private int audioStatus;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private FirebaseDatabase firebaseDatabase;
    private List<DropdownItem> topicsDropdownList = new ArrayList<>();
    private Audio audio, alreadyAvailableAudio;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateOrUpdateAudioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseDatabase = FirebaseDatabase.getInstance();
        navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
        isUpdateView = requireArguments().getBoolean(AppConstant.IS_UPDATE);
        initTopAppBar();
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                selectedAudioUri = data.getData();
                                // get audio name
                                displayAudioFileName(selectedAudioUri);
                            }
                        } else {
                            Toast.makeText(requireActivity(), AppConstant.NO_AUDIO_SELECTED, Toast.LENGTH_LONG).show();
                        }
                    }
                });
        initTopicSpinner();
    }

    private void displayAudioFileName(Uri selectedAudioUri) {
        Cursor returnCursor = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            returnCursor = requireActivity().getContentResolver().query(selectedAudioUri, null, null, null);
        }
        if (returnCursor != null) {
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            audioFileName = returnCursor.getString(nameIndex);
            binding.fileNamePreview.setText(audioFileName);
        } else {
            Toast.makeText(requireActivity(), AppConstant.AUDIO_SELECTED, Toast.LENGTH_SHORT).show();
        }
    }

    private void initTopAppBar() {
        if (isUpdateView) {
            binding.appBarFragment.topAppBar.setTitle(AppConstant.UPDATE_AUDIO);
            if (requireArguments().get(AppConstant.AUDIO_INFO) != null) {
                alreadyAvailableAudio = (Audio) requireArguments().get(AppConstant.AUDIO_INFO);
                initUpdateUI();
            }
        } else {
            binding.appBarFragment.topAppBar.setTitle(AppConstant.CREATE_AUDIO);
        }
        binding.appBarFragment.topAppBar.setNavigationIcon(R.drawable.arrow_back_ic);
        binding.appBarFragment.topAppBar.setNavigationIconTint(Color.WHITE);
    }

    private void initTopicSpinner() {
        firebaseDatabase.getReference().child(AppConstant.TOPICS).orderByChild("status").equalTo(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        topicsDropdownList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Topic topicInfo = data.getValue(Topic.class);
                            if (topicInfo != null) {
                                topicsDropdownList.add(new DropdownItem(topicInfo.getId(), topicInfo.getName()));
                            }
                        }
                        ArrayAdapter<DropdownItem> adapter = new ArrayAdapter<>(requireActivity(),
                                android.R.layout.simple_spinner_item, topicsDropdownList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.topicsSpinner.setAdapter(adapter);

                        // get selected item
                        displaySelectedTopicItem(topicsDropdownList);

                        onTopicItemSelected();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void onTopicItemSelected() {
        binding.topicsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DropdownItem item = (DropdownItem) adapterView.getSelectedItem();
                topicId = item.getStrHiddenValue();
                topicName = item.getDisplayText();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void displaySelectedTopicItem(List<DropdownItem> topicsDropdownList) {
        if (alreadyAvailableAudio != null) {
            DropdownItem selectedItem = null;
            for (DropdownItem item : topicsDropdownList) {
                if (item.getStrHiddenValue().equals(alreadyAvailableAudio.getTopicId())) {
                    selectedItem = item;
                    break;
                }
            }
            if (selectedItem != null) {
                binding.topicsSpinner.setSelection(topicsDropdownList.indexOf(selectedItem));
            }
        }
    }

    private void initUpdateUI() {
        binding.titleEdt.setText(alreadyAvailableAudio.getName());
        binding.fileNamePreview.setText(alreadyAvailableAudio.getAudioFileName());
        binding.lyricsEdt.setText(alreadyAvailableAudio.getLyrics());
        binding.translationsEdt.setText(alreadyAvailableAudio.getTranslations());
        initStatusDropdown();
    }

    private void initStatusDropdown() {
        binding.selectStatusLabel.setVisibility(View.VISIBLE);
        binding.statusSpinner.setVisibility(View.VISIBLE);
        ArrayAdapter<DropdownItem> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, StatusDropdownHelper.statusDropdown());
        binding.statusSpinner.setAdapter(adapter);
        binding.statusSpinner.setSelection(alreadyAvailableAudio.getStatus());
        binding.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DropdownItem item = (DropdownItem) adapterView.getSelectedItem();
                audioStatus = item.getHiddenValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onResume() {
        super.onResume();
        binding.appBarFragment.topAppBar.setNavigationOnClickListener(v -> onBackPressed());
        onMenuItemClick();
        binding.uploadAudioBtn.setOnClickListener(v -> selectAudio());
        binding.lyricsEdt.setOnTouchListener((view, motionEvent) -> {
            binding.lyricsEdt.requestFocus();
            return scrollableEdt(view, motionEvent, R.id.lyricsEdt);
        });
        binding.translationsEdt.setOnTouchListener((view, motionEvent) -> {
            binding.translationsEdt.requestFocus();
            return scrollableEdt(view, motionEvent, R.id.translationsEdt);
        });
        binding.submitBtnLayout.submitBtn.setOnClickListener(v -> onSubmitBtnClick());
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
                    navController.navigate(R.id.action_createOrUpdateAudioFragment_to_changePasswordFragment);
                    return true;
                case R.id.logoutItem:
                    FirebaseAuthHelper.signOut();
                    navController.navigate(R.id.signInFragment);
                    return true;
            }
            return false;
        });
    }

    private void selectAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        activityResultLauncher.launch(intent);
    }

    private boolean scrollableEdt(View view, MotionEvent motionEvent, int lyricsEdt) {
        if (view.getId() == lyricsEdt) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            if ((motionEvent.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            }
        }
        return false;
    }

    private void onSubmitBtnClick() {
        title = binding.titleEdt.getText().toString().trim();
        lyrics = binding.lyricsEdt.getText().toString().trim();
        translations = binding.translationsEdt.getText().toString().trim();
        if (isUpdateView) {
            if (!alreadyAvailableAudio.getName().equalsIgnoreCase(title)) {
                if (title.isEmpty()) {
                    Toast.makeText(requireActivity(), AppConstant.EMPTY_ERROR, Toast.LENGTH_LONG).show();
                } else {
                    checkAudioTitle();
                }
            } else {
                uploadData();
            }
        } else {
            if (title.isEmpty() || selectedAudioUri == null || lyrics.isEmpty() || translations.isEmpty()) {
                Toast.makeText(requireActivity(), AppConstant.EMPTY_ERROR, Toast.LENGTH_LONG).show();
            } else {
                checkAudioTitle();
            }
        }
    }

    private void checkAudioTitle() {
        firebaseDatabase.getReference().child(AppConstant.AUDIO_STORAGE_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    Audio audio = data.getValue(Audio.class);
                    if (audio != null && title.equalsIgnoreCase(audio.getName())) {
                        Toast.makeText(requireActivity(), AppConstant.NAME_ALREADY_EXISTS, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                uploadData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void uploadData() {
        binding.submitBtnLayout.progressBar.setVisibility(View.VISIBLE);
        binding.submitBtnLayout.progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        if (selectedAudioUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(AppConstant.AUDIO_STORAGE_NAME)
                    .child(String.valueOf(System.currentTimeMillis()));
            storageReference.putFile(selectedAudioUri).addOnSuccessListener(taskSnapshot -> {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete()) ;
                Uri urlImage = uriTask.getResult();
                audioURL = urlImage.toString();
                saveData();
                binding.submitBtnLayout.progressBar.setVisibility(View.GONE);
            }).addOnFailureListener(e -> {
                binding.submitBtnLayout.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireActivity(), AppConstant.CREATE_FAILED, Toast.LENGTH_LONG).show();
            });
        } else {
            audioURL = alreadyAvailableAudio.getAudioFileFromFirebase();
            audioFileName = alreadyAvailableAudio.getAudioFileName();
            saveData();
            binding.submitBtnLayout.progressBar.setVisibility(View.GONE);
        }

    }

    private void saveData() {
        if (isUpdateView) {
            audio = new Audio(alreadyAvailableAudio.getId(), title, audioFileName, audioURL,
                    null, lyrics, translations, audioStatus, topicId, topicName);
        } else {
            audio = new Audio(RandomStringGenerator.generateRandomString(), title, audioFileName, audioURL,
                    null, lyrics, translations, 1, topicId, topicName);
        }
        firebaseDatabase.getReference().child(AppConstant.AUDIO_STORAGE_NAME).child(audio.getId())
                .setValue(audio).addOnCompleteListener(task -> {
                    Toast.makeText(requireActivity(), AppConstant.SAVED, Toast.LENGTH_LONG).show();
                    if (!isUpdateView) {
                        resetForm();
                    } else {
                        if (selectedAudioUri != null) {
                            StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(alreadyAvailableAudio.getAudioFileFromFirebase());
                            reference.delete();
                        }
                        requireActivity().onBackPressed();
                    }
                }).addOnFailureListener(e -> Toast.makeText(requireActivity(), AppConstant.CREATE_FAILED, Toast.LENGTH_LONG).show());
    }

    private void resetForm() {
        binding.titleEdt.setText("");
        binding.lyricsEdt.setText("");
        binding.translationsEdt.setText("");
        binding.fileNamePreview.setText("");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}