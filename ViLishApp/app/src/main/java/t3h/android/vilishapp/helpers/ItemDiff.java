package t3h.android.vilishapp.helpers;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

import t3h.android.vilishapp.models.Audio;
import t3h.android.vilishapp.models.Topic;

public class ItemDiff<T> extends DiffUtil.Callback {
    private List<T> oldItemList, newItemList;

    public ItemDiff(List<T> newItemList, List<T> oldItemList) {
        this.oldItemList = oldItemList;
        this.newItemList = newItemList;
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
            return ((Audio) oldItem).getAudioStrId().equals(((Audio) newItem).getAudioStrId());
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
