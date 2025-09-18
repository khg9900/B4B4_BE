package com.example.emergencyassistb4b4.global.S3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public String uploadFile(MultipartFile file, String dir) throws IOException {

        // 파일 유효성 검사
        if (file == null || file.isEmpty()) {
            throw new IOException("업로드할 파일이 null이거나 비어있습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String filename = UUID.randomUUID() + "_" + originalFilename;
        String fullPath = dir + "/" + filename;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fullPath)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            String uploadedUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + fullPath;

            return uploadedUrl;

        } catch (S3Exception e) {
            log.error("S3Exception 발생: {}", e.awsErrorDetails().errorMessage(), e);
            throw new IOException("S3 업로드 실패 - S3Exception: " + e.awsErrorDetails().errorMessage(), e);

        } catch (IOException e) {
            log.error("IOException 발생: {}", e.getMessage(), e);
            throw e;

        } catch (Exception e) {
            log.error("기타 예외 발생: {}", e.getMessage(), e);
            throw new IOException("S3 업로드 실패 - 기타 예외: " + e.getMessage(), e);
        }
    }
}
