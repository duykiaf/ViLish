package t3h.android.admin.helper;

import java.util.UUID;

public class RandomStringGenerator {
    public static String generateRandomString() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
