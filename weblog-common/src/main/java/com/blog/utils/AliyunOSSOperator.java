package com.blog.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.comm.SignVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AliyunOSSOperator {

    private final AliyunOSSProperties aliyunOSSProperties;

    public String upload(byte[] content, String originalFilename) throws Exception {
        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();
        String accessKeyId = aliyunOSSProperties.getAccessKeyId();
        String accessKeySecret = aliyunOSSProperties.getAccessKeySecret();

        // 填写Object完整路径，例如202406/1.png。Object完整路径中不能包含Bucket名称。
        //获取当前系统日期的字符串,格式为 yyyy/MM
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));

        // 安全地处理文件名和扩展名
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        // 生成新的文件名（UUID + 扩展名）
        String newFileName = UUID.randomUUID().toString().replace("-", "") + fileExtension;
        String objectName = dir + "/" + newFileName;

        // 创建OSSClient实例。
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

        OSS ossClient;

        // 优先使用配置文件中的访问凭证，如果未配置则使用环境变量
        if (accessKeyId != null && !accessKeyId.trim().isEmpty() &&
            accessKeySecret != null && !accessKeySecret.trim().isEmpty()) {
            // 使用配置文件中的凭证 - 最简单直接的方式
            log.debug("使用配置文件中的OSS访问凭证");
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        } else {
            // 回退到环境变量
            log.debug("使用环境变量中的OSS访问凭证");
            ossClient = OSSClientBuilder.create()
                    .endpoint(endpoint)
                    .credentialsProvider(CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider())
                    .clientConfiguration(clientBuilderConfiguration)
                    .region(region)
                    .build();
        }

        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));
        } finally {
            ossClient.shutdown();
        }

        return endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + objectName;
    }

}

