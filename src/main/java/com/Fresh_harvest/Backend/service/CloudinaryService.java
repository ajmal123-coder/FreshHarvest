package com.Fresh_harvest.Backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile multipartFile) {
        try {
            File file = convertMultiPartToFile(multipartFile);
            Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
            boolean isDeleted = file.delete();
            if (!isDeleted) {
                System.err.println("Failed to delete temp file: " + file.getAbsolutePath());
            }
            return uploadResult.get("url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed: " + e.getMessage(), e);
        }
    }


    public void deleteFileByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        try {
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId == null) {
                System.err.println("Could not extract public ID from URL for deletion: " + imageUrl);
                return;
            }
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            System.err.println("Image deletion from Cloudinary failed for URL: " + imageUrl + ". Error: " + e.getMessage());
        }
    }


    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        File convFile = new File(originalFilename);
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }


    private String extractPublicIdFromUrl(String imageUrl) {
        Pattern pattern = Pattern.compile(".*/upload/(?:v\\d+/)?(.+?)(?:\\.[a-zA-Z0-9]{2,4})?$");
        Matcher matcher = pattern.matcher(imageUrl);

        if (matcher.find()) {
            String fullPath = matcher.group(1);

            return fullPath;
        }
        return null;
    }
}