package t3h.android.vilishapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
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
    private int resId;
    private String contentDesc;

    public AudioAdapter(Context context) {
        this.context = context;
        itemList = new ArrayList<>();
        dataSource = new ArrayList<>();
    }

    public void setOnAudioItemClickListener(OnAudioItemClickListener listener) {
        onAudioItemClickListener = listener;
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

//            binding.playOrPauseIcon.setOnClickListener(v -> {
//                if (binding.playOrPauseIcon.getContentDescription().equals(AppConstant.PLAY_ICON)) {
//                    contentDesc = AppConstant.PAUSE_ICON;
//                    resId = R.drawable.pause_circle_blue_outline_ic;
//                } else {
//                    contentDesc = AppConstant.PLAY_ICON;
//                    resId = R.drawable.play_circle_blue_outline_ic;
//                }
//                binding.playOrPauseIcon.setContentDescription(contentDesc);
//                binding.playOrPauseIcon.setImageResource(resId);

//                if (onAudioItemClickListener != null) {
//                    onAudioItemClickListener.onPlayOrPauseIconClick(itemList.get(getAdapterPosition()), getAdapterPosition(),
//                            binding.playOrPauseIcon);
//                }
//            });

//            binding.bookmarkIcon.setOnClickListener(v -> {
//                if (binding.bookmarkIcon.getContentDescription().equals(AppConstant.BOOKMARK_BORDER_ICON)) {
//                    contentDesc = AppConstant.BOOKMARK_ICON;
//                    resId = R.drawable.blue_bookmark_ic;
//                } else {
//                    contentDesc = AppConstant.BOOKMARK_BORDER_ICON;
//                    resId = R.drawable.bookmark_blue_border_ic;
//                }
//                binding.bookmarkIcon.setContentDescription(contentDesc);
//                binding.bookmarkIcon.setImageResource(resId);

//                if (onAudioItemClickListener != null) {
//                    onAudioItemClickListener.onPlayOrPauseIconClick(itemList.get(getAdapterPosition()), getAdapterPosition(),
//                            binding.bookmarkIcon);
//                }
//            });
        }

        public void bindView(Audio audioInfo) {
            binding.audioTitle.setText(audioInfo.getName());
            // init audio duration
            initAudioDuration(audioInfo);
            // init play/pause icon

            // init bookmark icon

            // init download/trash icon

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

        void onPlayOrPauseIconClick(Audio item, int position, ImageView icon);
    }
}
