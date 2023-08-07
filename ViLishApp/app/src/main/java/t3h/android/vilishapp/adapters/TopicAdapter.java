package t3h.android.vilishapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import t3h.android.vilishapp.databinding.TopicItemLayoutBinding;
import t3h.android.vilishapp.models.Topic;

public class TopicAdapter extends PagingDataAdapter<Topic, TopicAdapter.TopicItemViewHolder> {
    public TopicAdapter(@NonNull DiffUtil.ItemCallback<Topic> diffCallback) {
        super(diffCallback);
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
        holder.bindView(getItem(position));
    }

    public class TopicItemViewHolder extends RecyclerView.ViewHolder {
        TopicItemLayoutBinding binding;

        public TopicItemViewHolder(TopicItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindView(Topic topic) {
            binding.topicName.setText(topic.getName());
        }
    }
}
