package com.ielts.helper.Utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.ielts.helper.configration.OssProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Service
public class OssUtils {
    @Autowired
    private OssProperties ossProperties;

    public String uploadFile(InputStream inputStream, String fileName, String contentType) {
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                    ossProperties.getEndpoint(),
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret()
            );

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);

            ossClient.putObject(ossProperties.getBucketName(), fileName, inputStream, metadata);

            return "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    public String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }
}
