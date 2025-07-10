package com.example.emergencyassistb4b4.global.S3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

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
            throw new IOException("❗ 업로드할 파일이 null이거나 비어 있습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String filename = UUID.randomUUID() + "_" + originalFilename;
        String fullPath = dir + "/" + filename;

        try {
            System.out.println("✅ S3 업로드 시작: " + originalFilename);
            System.out.println("📦 경로: " + fullPath);
            System.out.println("📏 파일 크기: " + file.getSize());
            System.out.println("📁 Content-Type: " + file.getContentType());

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
            System.out.println("✅ S3 업로드 성공: " + uploadedUrl);
            return uploadedUrl;

        } catch (S3Exception e) {
            System.err.println("❌ S3Exception 발생: " + e.awsErrorDetails().errorMessage());
            throw new IOException("S3 업로드 실패 - S3Exception: " + e.awsErrorDetails().errorMessage(), e);
        } catch (IOException e) {
            System.err.println("❌ IOException 발생: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ 기타 예외 발생: " + e.getMessage());
            throw new IOException("S3 업로드 실패 - 기타 예외: " + e.getMessage(), e);
        }
    }
}
