package com.vms.document.service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class S3Service {

    @Autowired
    private S3Presigner presigner;

    @Value("${aws.bucket-name}")
    private String bucketName;

    // ── Upload pre-signed URL ──────────────────────────────────────────────────
    public Map<String, String> generateUploadUrl(String fileName, String contentType, UUID vendorId) {
        String key = "vendor-" + vendorId + "/" + fileName;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(objectRequest)
                .signatureDuration(Duration.ofMinutes(10))
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

        return Map.of(
                "uploadUrl", presignedRequest.url().toString(),
                "fileKey", key
        );
    }

    // ✅ ADDED: Download pre-signed URL ─────────────────────────────────────────
    public String generateDownloadUrl(String fileKey) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(fileKey)
            .responseContentDisposition("attachment; filename=\"" + fileKey.substring(fileKey.lastIndexOf("/") + 1) + "\"")
            .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .getObjectRequest(getObjectRequest)
            .signatureDuration(Duration.ofMinutes(60))
            .build();

    PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
    return presignedRequest.url().toString();
}
}
