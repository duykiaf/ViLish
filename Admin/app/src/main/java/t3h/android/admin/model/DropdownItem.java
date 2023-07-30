package t3h.android.admin.model;

import androidx.annotation.NonNull;

public class DropdownItem {
    private int hiddenValue;
    private String displayText;

    public DropdownItem(int hiddenValue, String displayText) {
        this.hiddenValue = hiddenValue;
        this.displayText = displayText;
    }

    public DropdownItem() {
    }

    public int getHiddenValue() {
        return hiddenValue;
    }

    public void setHiddenValue(int hiddenValue) {
        this.hiddenValue = hiddenValue;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    @NonNull
    @Override
    public String toString() {
        return displayText;
    }
}
