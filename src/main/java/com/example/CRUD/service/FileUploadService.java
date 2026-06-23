package com.example.CRUD.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.http.HttpHeaders;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class FileUploadService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucketName;




    private final WebClient webClient;


    public String uploadFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        try {

            String fileName =
                    UUID.randomUUID() + "_" + file.getOriginalFilename();

            String uploadUrl =
                    supabaseUrl +
                            "/storage/v1/object/" +
                            bucketName + "/" +
                            fileName;

            webClient.post()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("apikey", supabaseKey)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .bodyValue(file.getBytes())
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return supabaseUrl +
                    "/storage/v1/object/public/" +
                    bucketName + "/" +
                    fileName;

        } catch (Exception e) {
            throw new RuntimeException("Error uploading file", e);
        }
    }
}