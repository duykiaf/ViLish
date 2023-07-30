package t3h.android.admin.helper;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

import t3h.android.admin.model.Audio;
import t3h.android.admin.model.Topic;

public class ItemDiffUtils<T> extends DiffUtil.Callback {
    private List<T> newItemList;
    private List<T> oldItemList;

    public ItemDiffUtils (List<T> newItemList, List<T> oldItemList) {
        this.newItemList = newItemList;
        this.oldItemList = oldItemList;
    }

    @Override
    public int getOldListSize() {
        return oldItemList == null ? 0 : oldItemList.size();
    }

    @Override
    public int getNewListSize() {
        return newItemList == null ? 0 : newItemList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        T oldItem = oldItemList.get(oldItemPosition);
        T newItem = newItemList.get(newItemPosition);
        if (oldItem instanceof Topic && newItem instanceof Topic) {
            return ((Topic) oldItem).getId().equals(((Topic) newItem).getId());
        } else if (oldItem instanceof Audio && newItem instanceof Audio) {
            return ((Audio) oldItem).getId().equals(((Audio) newItem).getId());
        }
        return false;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        T oldItem = oldItemList.get(oldItemPosition);
        T newItem = newItemList.get(newItemPosition);
        if (oldItem instanceof Topic && newItem instanceof Topic) {
            return ((Topic) oldItem).equals(newItem);
        } else if (oldItem instanceof Audio && newItem instanceof Audio) {
            return ((Audio) oldItem).equals(newItem);
        }
        return false;
    }
}
