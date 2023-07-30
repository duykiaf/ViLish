package t3h.android.admin.listener;

import t3h.android.admin.databinding.ItemListLayoutBinding;

public interface OnBindViewListener<T> {
    void onBindView(T model, ItemListLayoutBinding binding);
}
