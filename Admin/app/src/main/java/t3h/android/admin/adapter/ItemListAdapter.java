package t3h.android.admin.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import t3h.android.admin.databinding.ItemListLayoutBinding;
import t3h.android.admin.helper.ItemDiffUtils;
import t3h.android.admin.listener.OnBindViewListener;
import t3h.android.admin.listener.OnItemClickListener;

public class ItemListAdapter<T> extends RecyclerView.Adapter<ItemListAdapter.ItemViewHolder> {
    private List<T> oldItemList, newItemList;
    private OnBindViewListener<T> onBindViewListener;
    private OnItemClickListener<T> onItemClickListener;

    public ItemListAdapter() {
        newItemList = new ArrayList<>();
        oldItemList = new ArrayList<>();
    }

    public void setData(List<T> data) {
        newItemList = data;
    }

    public void bindAdapter(OnBindViewListener<T> listener) {
        onBindViewListener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder<>(ItemListLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent, false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bindView(newItemList.get(position), onBindViewListener);
    }

    @Override
    public int getItemCount() {
        return newItemList == null ? 0 : newItemList.size();
    }

    public class ItemViewHolder<T> extends RecyclerView.ViewHolder {
        private ItemListLayoutBinding binding;

        public ItemViewHolder(ItemListLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.itemListLayout.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClicked(newItemList.get(getAdapterPosition()));
                }
            });
        }

        public void bindView(T item, OnBindViewListener<T> listener) {
            listener.onBindView(item, binding);
        }
    }

    public void updateItemList(List<T> newItemList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ItemDiffUtils<T>(oldItemList, newItemList));
        oldItemList.clear();
        oldItemList.addAll(newItemList);
        diffResult.dispatchUpdatesTo(this);
    }
}
