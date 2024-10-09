package evelyn.site.socialmedia.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ChatRoomIdUtil {

    // 使用SHA-256來生成聊天室ID
    public static String generateChatRoomId(String user1Id, String user2Id) throws NoSuchAlgorithmException {
        // 排序兩個UUID
        String sortedIds = Arrays.asList(user1Id, user2Id).stream()
                .sorted()
                .collect(Collectors.joining("-"));

        // 使用SHA-256進行編碼
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(sortedIds.getBytes(StandardCharsets.UTF_8));

        // 將結果轉換為十六進位制的字串表示
        return bytesToHex(hash);
    }

    // 將byte數組轉換成十六進制的字符串
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

