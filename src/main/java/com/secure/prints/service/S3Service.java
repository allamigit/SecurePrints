package com.secure.prints.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class S3Service {

    private final S3Client s3Client;
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3Service() {
        ProfileCredentialsProvider provider = ProfileCredentialsProvider.create("default");
        this.s3Client = S3Client.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(DefaultCredentialsProvider.create())
                //.credentialsProvider(InstanceProfileCredentialsProvider.create())
                //.credentialsProvider(ProfileCredentialsProvider.create("default"))
                .build();
    }

    // ---------- Upload ----------
    public String uploadFile(String folderName, MultipartFile file) throws IOException {
        String key = folderName + "/" + file.getOriginalFilename();
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

        return "https://" + bucketName + ".s3." + s3Client.serviceClientConfiguration().region().id() + ".amazonaws.com/" + key;
    }

    // ---------- Download ----------
    public byte[] downloadFile(String folderName, String fileName) throws IOException {
        String key = folderName + "/" + fileName;
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {
            return s3Object.readAllBytes();
        }
    }

    // ---------- Save to Local File ----------
    public Path downloadFileToLocal(String folderName, String fileName, Path destination) throws IOException {
        String key = folderName + "/" + fileName;
        byte[] content = this.downloadFile(bucketName, key);
        return Files.write(destination, content);
    }

}

