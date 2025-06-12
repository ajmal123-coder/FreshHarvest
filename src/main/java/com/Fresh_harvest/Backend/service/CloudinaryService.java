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
    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }
}
