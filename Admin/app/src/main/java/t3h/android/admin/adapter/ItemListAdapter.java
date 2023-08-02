package t3h.android.admin.adapter;

import android.util.Log;
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
    private List<T> itemList, dataSource;
    private OnBindViewListener<T> onBindViewListener;
    private OnItemClickListener<T> onItemClickListener;
    private DiffUtil.DiffResult diffResult;

    public ItemListAdapter() {
        itemList = new ArrayList<>();
        dataSource = new ArrayList<>();
    }

    public void bindAdapter(OnBindViewListener<T> listener) {
        onBindViewListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        onItemClickListener = listener;
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
        Log.e("DNV", String.valueOf(position));
        holder.bindView(itemList.get(position), onBindViewListener);
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    public class ItemViewHolder<T> extends RecyclerView.ViewHolder {
        private ItemListLayoutBinding binding;

        public ItemViewHolder(ItemListLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.itemListLayout.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClicked(itemList.get(getAdapterPosition()));
                }
            });
        }

        public void bindView(T item, OnBindViewListener<T> listener) {
            listener.onBindView(item, binding);
        }
    }

    public void updateItemList(List<T> newItemList) {
        diffResult = DiffUtil.calculateDiff(new ItemDiffUtils<>(itemList, newItemList));
        itemList.clear();
        itemList.addAll(newItemList);
        dataSource.clear();
        dataSource.addAll(newItemList);
        diffResult.dispatchUpdatesTo(this);
    }

    public void searchList(List<T> searchList) {
        diffResult = DiffUtil.calculateDiff(new ItemDiffUtils<>(dataSource, searchList));
        itemList.clear();
        itemList.addAll(searchList);
        diffResult.dispatchUpdatesTo(this);
    }
}
