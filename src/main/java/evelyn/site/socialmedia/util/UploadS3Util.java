package evelyn.site.socialmedia.util;

import evelyn.site.socialmedia.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Component
@RequiredArgsConstructor
public class UploadS3Util {
    private final S3Service s3Service;
    public List<String> uploadFiles(List<MultipartFile> files) throws IOException {
        List<String> uploadedFileNames = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String uuidFileName = UUIDGenerator.generateUUID() + "_" + file.getOriginalFilename();
                    s3Service.uploadFile(file, uuidFileName);
                    uploadedFileNames.add(uuidFileName);
                }
            }
        }
        return uploadedFileNames;
    }
    public String uploadFile(MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            // 生成 UUID 檔案名稱
            String uuidFileName = UUIDGenerator.generateUUID() + "_" + file.getOriginalFilename();
            // 上傳到 S3
            s3Service.uploadFile(file, uuidFileName);
            // 返回檔案名稱
            return uuidFileName;
        }
        return null;  // 如果檔案為空或不存在，返回 null
    }
}

