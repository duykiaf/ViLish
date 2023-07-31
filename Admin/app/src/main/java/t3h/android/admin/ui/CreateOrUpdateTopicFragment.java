package t3h.android.admin.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import t3h.android.admin.R;
import t3h.android.admin.databinding.FragmentCreateOrUpdateTopicBinding;
import t3h.android.admin.helper.AppConstant;
import t3h.android.admin.helper.FirebaseAuthHelper;
import t3h.android.admin.helper.RandomStringGenerator;
import t3h.android.admin.helper.StatusDropdownHelper;
import t3h.android.admin.model.DropdownItem;
import t3h.android.admin.model.Topic;

public class CreateOrUpdateTopicFragment extends Fragment {
    private FragmentCreateOrUpdateTopicBinding binding;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private NavController navController;
    private boolean isUpdateView = false;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private Uri selectedImgUri;
    private String topicName, imageURL;
    private int topicStatus;
    private Topic topic, alreadyAvailableTopic;
    private List<String> topicNames = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateOrUpdateTopicBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
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
                                selectedImgUri = data.getData();
                                Glide.with(requireActivity())
                                        .load(selectedImgUri)
                                        .centerCrop()
                                        .placeholder(R.drawable.image_ic)
                                        .error(R.drawable.broken_image_ic)
                                        .into(binding.imagePreview);
                            }
                        } else {
                            Toast.makeText(requireActivity(), AppConstant.NO_IMAGE_SELECTED, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void initTopAppBar() {
        if (isUpdateView) {
            binding.appBarFragment.topAppBar.setTitle(AppConstant.UPDATE_TOPIC);
            if (requireArguments().getSerializable(AppConstant.TOPIC_INFO) != null) {
                alreadyAvailableTopic = (Topic) requireArguments().getSerializable(AppConstant.TOPIC_INFO);
                initUpdateUi();
            }
        } else {
            binding.appBarFragment.topAppBar.setTitle(AppConstant.CREATE_TOPIC);
        }
        binding.appBarFragment.topAppBar.setNavigationIcon(R.drawable.arrow_back_ic);
        binding.appBarFragment.topAppBar.setNavigationIconTint(Color.WHITE);
    }

    private void initUpdateUi() {
        binding.nameEdt.setText(alreadyAvailableTopic.getName());
        Glide.with(requireActivity())
                .load(alreadyAvailableTopic.getImagePath())
                .centerCrop()
                .placeholder(R.drawable.image_ic)
                .error(R.drawable.broken_image_ic)
                .into(binding.imagePreview);
        initStatusDropdown();
    }

    private void initStatusDropdown() {
        binding.selectStatusLabel.setVisibility(View.VISIBLE);
        binding.statusSpinner.setVisibility(View.VISIBLE);
        ArrayAdapter<DropdownItem> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, StatusDropdownHelper.statusDropdown());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.statusSpinner.setAdapter(adapter);
        binding.statusSpinner.setSelection(alreadyAvailableTopic.getStatus());
        binding.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DropdownItem item = (DropdownItem) adapterView.getSelectedItem();
                topicStatus = item.getHiddenValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.appBarFragment.topAppBar.setNavigationOnClickListener(v -> onBackPressed());
        onMenuItemClick();
        binding.addImage.setOnClickListener(v -> handleSelectedImage());
        binding.saveBtn.setOnClickListener(v -> onSaveBtnClick());
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
                    navController.navigate(R.id.action_createOrUpdateTopicFragment_to_changePasswordFragment);
                    return true;
                case R.id.logoutItem:
                    FirebaseAuthHelper.signOut();
                    navController.navigate(R.id.signInFragment);
                    return true;
            }
            return false;
        });
    }

    private void handleSelectedImage() {
        Intent imagePicker = new Intent(Intent.ACTION_PICK);
        imagePicker.setType("image/*");
        activityResultLauncher.launch(imagePicker);
    }

    private void onSaveBtnClick() {
        topicName = binding.nameEdt.getText().toString().trim();
        if (isUpdateView) {

        } else {
            if (topicName.isEmpty() || selectedImgUri == null) {
                Toast.makeText(requireActivity(), AppConstant.EMPTY_ERROR, Toast.LENGTH_LONG).show();
            } else {
                checkTopicName();
            }
        }
    }

    private void checkTopicName() {
        databaseReference.child(AppConstant.TOPICS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    topicNames.clear();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Topic model = data.getValue(Topic.class);
                        if (model != null) {
                            topicNames.add(model.getName().toLowerCase());
                        }
                    }
                    if (!topicNames.isEmpty() && topicNames.contains(topicName.toLowerCase())) {
                        Toast.makeText(requireActivity(), AppConstant.NAME_ALREADY_EXISTS, Toast.LENGTH_LONG).show();
                    } else {
                        uploadData();
                    }
                } else {
                    uploadData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void uploadData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(AppConstant.IMG_STORAGE_NAME)
                .child(String.valueOf(System.currentTimeMillis()));
        storageReference.putFile(selectedImgUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isComplete()) ;
            Uri urlImage = uriTask.getResult();
            imageURL = urlImage.toString();
            saveData();
            binding.progressBar.setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(requireActivity(), AppConstant.CREATE_TOPIC_FAILED, Toast.LENGTH_LONG).show();
        });
    }

    private void saveData() {
        if (isUpdateView) {
            Log.e("DNV", "update");
        } else {
            topic = new Topic(RandomStringGenerator.generateRandomString(), topicName, imageURL, 1);
        }
        databaseReference.child(AppConstant.TOPICS).child(topic.getId()).setValue(topic).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(requireActivity(), AppConstant.SAVED, Toast.LENGTH_LONG).show();
                if (!isUpdateView) {
                    resetForm();
                } else {
                    requireActivity().onBackPressed();
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(requireActivity(), AppConstant.CREATE_TOPIC_FAILED, Toast.LENGTH_LONG).show());
    }

    private void resetForm() {
        binding.nameEdt.setText("");
        binding.imagePreview.setImageResource(R.drawable.image_ic);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}