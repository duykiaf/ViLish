package t3h.android.vilishapp.helpers;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import t3h.android.vilishapp.models.Audio;
import t3h.android.vilishapp.models.Topic;

public class ItemDiff<T> extends DiffUtil.ItemCallback<T> {
    @Override
    public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        if (oldItem instanceof Topic && newItem instanceof Topic) {
            return ((Topic) oldItem).getId().equals(((Topic) newItem).getId());
        } else if (oldItem instanceof Audio && newItem instanceof Audio) {
            return ((Audio) oldItem).getAudioStrId().equals(((Audio) newItem).getAudioStrId());
        }
        return false;
    }

    @Override
    public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        if (oldItem instanceof Topic && newItem instanceof Topic) {
            return oldItem.equals(newItem);
        } else if (oldItem instanceof Audio && newItem instanceof Audio) {
            return oldItem.equals(newItem);
        }
        return false;
    }
}
