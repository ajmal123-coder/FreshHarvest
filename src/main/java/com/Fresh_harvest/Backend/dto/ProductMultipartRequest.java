package com.Fresh_harvest.Backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

public class ProductMultipartRequest {

    @Schema(description = "Product JSON object as string", example = "{\"name\":\"Sample Product\",\"description\":\"description\", \"price\":100}")
    private String product;

    @Schema(description = "Product image file", type = "string", format = "binary")
    private MultipartFile file;

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
