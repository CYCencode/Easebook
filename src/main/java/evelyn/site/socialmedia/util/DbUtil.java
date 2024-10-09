package evelyn.site.socialmedia.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbUtil {

    private static final Logger logger = LoggerFactory.getLogger(DbUtil.class);

    // 檢查 rowsAffected 並根據結果記錄成功或拋出異常
    public static void checkRowAffected(int rowsAffected, String successMessage, String errorMessage) {
        if (rowsAffected > 0) {
            logger.info(successMessage);
        } else {
            logger.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }
}