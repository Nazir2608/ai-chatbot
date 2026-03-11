package com.aichatbot.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
@Slf4j
@Service
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    
    @Value("${storage.s3.bucket}")
    private String bucketName;

    public S3StorageService(@Value("${storage.s3.region:us-east-1}") String region) {
        this.s3Client = S3Client.builder().region(software.amazon.awssdk.regions.Region.of(region)).build();
    }

    private String getKey(String documentId, String filename) {
        return documentId + "/" + filename;
    }

    @Override
    public String store(MultipartFile file, String documentId, String filename) {
        try {
            String key = getKey(documentId, filename);
            PutObjectRequest putRequest = PutObjectRequest.builder().bucket(bucketName).key(key).contentType(file.getContentType()).build();
            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Stored file in S3: s3://{}/{}", bucketName, key);
            return "s3://" + bucketName + "/" + key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file in S3", e);
        }
    }

    @Override
    public void storeText(String content, String documentId, String filename) {
        String key = getKey(documentId, filename);
        PutObjectRequest putRequest = PutObjectRequest.builder().bucket(bucketName).key(key).contentType("application/json").build();
        s3Client.putObject(putRequest, RequestBody.fromString(content));
        log.info("Stored text in S3: s3://{}/{}", bucketName, key);
    }

    @Override
    public InputStream load(String documentId, String filename) {
        String key = getKey(documentId, filename);
        GetObjectRequest getRequest = GetObjectRequest.builder().bucket(bucketName).key(key).build();
        return s3Client.getObject(getRequest);
    }

    @Override
    public Resource loadAsResource(String documentId, String filename) {
        return new InputStreamResource(load(documentId, filename));
    }

    @Override
    public boolean exists(String documentId, String filename) {
        try {
            String key = getKey(documentId, filename);
            HeadObjectRequest headRequest = HeadObjectRequest.builder().bucket(bucketName).key(key).build();
            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    public void deleteAll(String documentId) {
        String prefix = documentId + "/";
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix).build();
        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
        for (S3Object object : listResponse.contents()) {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder().bucket(bucketName).key(object.key()).build();
            s3Client.deleteObject(deleteRequest);
        }
        
        log.info("Deleted all files for document {} from S3", documentId);
    }
}
