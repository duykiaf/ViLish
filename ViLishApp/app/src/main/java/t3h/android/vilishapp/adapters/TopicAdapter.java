package t3h.android.vilishapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import t3h.android.vilishapp.R;
import t3h.android.vilishapp.databinding.TopicItemLayoutBinding;
import t3h.android.vilishapp.helpers.ItemDiff;
import t3h.android.vilishapp.models.Topic;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicItemViewHolder> {
    private List<Topic> topicList, dataSource;
    private Context context;
    private TopicItemClickListener topicItemClickListener;
    private DiffUtil.DiffResult diffResult;

    public TopicAdapter(Context context) {
        topicList = new ArrayList<>();
        dataSource = new ArrayList<>(); // topic source list
        this.context = context;
    }

    public void setOnTopicItemClickListener(TopicItemClickListener listener){
        topicItemClickListener = listener;
    }

    @NonNull
    @Override
    public TopicItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TopicItemViewHolder(TopicItemLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull TopicItemViewHolder holder, int position) {
        Log.e("DNV", String.valueOf(position));
        holder.bindView(topicList.get(position));
    }

    @Override
    public int getItemCount() {
        return topicList == null ? 0 : topicList.size();
    }

    public class TopicItemViewHolder extends RecyclerView.ViewHolder {
        TopicItemLayoutBinding binding;

        public TopicItemViewHolder(TopicItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.topicItem.setOnClickListener(v -> {
                if (topicItemClickListener != null) {
                    topicItemClickListener.onItemClick(topicList.get(getAdapterPosition()));
                }
            });
        }

        public void bindView(Topic topic) {
            binding.topicName.setText(topic.getName());
            Glide.with(context)
                    .load(topic.getImagePath())
                    .placeholder(R.drawable.elife_logo) // Placeholder image while loading
                    .error(R.drawable.elife_logo) // Image to show if loading fails
                    .into(binding.topicImage);
        }
    }

    public void updateItemList(List<Topic> newItemList) {
        diffResult = DiffUtil.calculateDiff(new ItemDiff<>(newItemList, topicList));
        topicList.clear();
        topicList.addAll(newItemList);
        dataSource.clear();
        dataSource.addAll(newItemList);
        diffResult.dispatchUpdatesTo(this);
    }

    public void searchList(List<Topic> searchList) {
        if (dataSource != null) {
            diffResult = DiffUtil.calculateDiff(new ItemDiff<>(searchList, topicList));
            topicList.clear();
            topicList.addAll(searchList);
            diffResult.dispatchUpdatesTo(this);
        }
    }

    public interface TopicItemClickListener{
        void onItemClick(Topic item);
    }
}
