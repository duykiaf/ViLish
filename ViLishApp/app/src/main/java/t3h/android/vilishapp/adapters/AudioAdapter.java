package t3h.android.vilishapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import t3h.android.vilishapp.R;
import t3h.android.vilishapp.databinding.AudioItemLayoutBinding;
import t3h.android.vilishapp.helpers.AppConstant;
import t3h.android.vilishapp.helpers.AudioHelper;
import t3h.android.vilishapp.helpers.ItemDiff;
import t3h.android.vilishapp.models.Audio;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {
    private Context context;
    private List<Audio> itemList, dataSource;
    private OnAudioItemClickListener onAudioItemClickListener;
    private DiffUtil.DiffResult diffResult;
    private List<String> bookmarkAudioIds;
    private HashMap<String, Integer> audioCheckedList;

    public AudioAdapter(Context context) {
        this.context = context;
        itemList = new ArrayList<>();
        dataSource = new ArrayList<>();
    }

    public void setOnAudioItemClickListener(OnAudioItemClickListener listener) {
        onAudioItemClickListener = listener;
    }

    public void setBookmarkAudioIds(List<String> bookmarkAudioIds) {
        this.bookmarkAudioIds = bookmarkAudioIds;
    }

    public void setAudioCheckedList(HashMap<String, Integer> audioCheckedList) {
        this.audioCheckedList = audioCheckedList;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AudioViewHolder(AudioItemLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        holder.bindView(itemList.get(position));
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    public class AudioViewHolder extends RecyclerView.ViewHolder {
        private AudioItemLayoutBinding binding;

        public AudioViewHolder(AudioItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.audioItemLayout.setOnClickListener(v -> {
                if (onAudioItemClickListener != null) {
                    onAudioItemClickListener.onItemClick(itemList.get(getAdapterPosition()), getAdapterPosition());
                }
            });

            binding.bookmarkIcon.setOnClickListener(v -> {
                if (onAudioItemClickListener != null) {
                    onAudioItemClickListener.onIconClick(itemList.get(getAdapterPosition()), getAdapterPosition(),
                            binding.bookmarkIcon);
                }
            });

            binding.downloadIcon.setOnClickListener(v -> {
                if (onAudioItemClickListener != null) {
                    onAudioItemClickListener.onIconClick(itemList.get(getAdapterPosition()), getAdapterPosition(),
                            binding.downloadIcon);
                }
            });
        }

        public void bindView(Audio audioInfo) {
            // default icon
            binding.downloadIcon.setContentDescription(AppConstant.DOWNLOAD_ICON);
            binding.downloadIcon.setImageResource(R.drawable.blue_download_ic);

            binding.audioTitle.setText(audioInfo.getName());
            // init audio duration
            initAudioDuration(audioInfo);

            // init bookmark icon
            initBookmarkIcon(audioInfo);

            // init download/trash icon

            // init checked or download icon
            initCheckedOrDownloadIcon(audioInfo);
        }

        private void initAudioDuration(Audio audioInfo) {
            AudioHelper.getAudioDuration(audioInfo.getAudioFileFromFirebase(), new AudioHelper.DurationCallback() {
                @Override
                public void onDurationReceived(String strDuration) {
                    binding.durationTxt.setText(strDuration);
                }

                @Override
                public void onDurationError() {
                    binding.durationTxt.setText(AppConstant.DURATION_DEFAULT);
                }
            });
        }

        private void initBookmarkIcon(Audio audioInfo) {
            if (bookmarkAudioIds != null) {
                for (String id : bookmarkAudioIds) {
                    if (audioInfo.getId().equals(id)) {
                        binding.bookmarkIcon.setContentDescription(context.getString(R.string.bookmark_icon));
                        binding.bookmarkIcon.setImageResource(R.drawable.blue_bookmark_ic);
                        break;
                    } else {
                        binding.bookmarkIcon.setContentDescription(context.getString(R.string.bookmark_border_icon));
                        binding.bookmarkIcon.setImageResource(R.drawable.bookmark_blue_border_ic);
                    }
                }
            }
        }

        private void initCheckedOrDownloadIcon(Audio audioInfo) {
            if (audioCheckedList != null) {
                for (String audioId: audioCheckedList.keySet()) {
                    if (audioInfo.getId().equals(audioId)) {
                        binding.downloadIcon.setContentDescription(AppConstant.CHECK_CIRCLE_ICON);
                        binding.downloadIcon.setImageResource(R.drawable.check_circle_ic);
                        break;
                    } else {
                        binding.downloadIcon.setContentDescription(AppConstant.DOWNLOAD_ICON);
                        binding.downloadIcon.setImageResource(R.drawable.blue_download_ic);
                    }
                }
            }
        }
    }

    public void updateItemList(List<Audio> newItemList) {
        diffResult = DiffUtil.calculateDiff(new ItemDiff<>(newItemList, itemList));
        itemList.clear();
        itemList.addAll(newItemList);
        dataSource.clear();
        dataSource.addAll(newItemList);
        diffResult.dispatchUpdatesTo(this);
    }

    public void searchList(List<Audio> searchList) {
        if (dataSource != null) {
            diffResult = DiffUtil.calculateDiff(new ItemDiff<>(searchList, itemList));
            itemList.clear();
            itemList.addAll(searchList);
            diffResult.dispatchUpdatesTo(this);
        }
    }

    public interface OnAudioItemClickListener {
        void onItemClick(Audio item, int position);

        void onIconClick(Audio item, int position, ImageView icon);
    }
}