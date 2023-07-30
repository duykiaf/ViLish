package t3h.android.admin.helper;

import java.util.ArrayList;
import java.util.List;

import t3h.android.admin.model.DropdownItem;

public class StatusDropdownHelper {
    public static List<DropdownItem> statusDropdown() {
        List<DropdownItem> itemList = new ArrayList<>();
        itemList.add(new DropdownItem(0, "Inactive"));
        itemList.add(new DropdownItem(1, "Active"));
        return itemList;
    }
}
