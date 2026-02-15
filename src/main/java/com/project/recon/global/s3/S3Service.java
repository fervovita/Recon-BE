package com.project.recon.global.s3;

import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file, String dirName) {
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            var s3Resource = s3Template.upload(bucket, fileName, file.getInputStream(),
                    ObjectMetadata.builder().contentType(file.getContentType()).build());
            return s3Resource.getURL().toString();
        } catch (IOException e) {
            log.error("S3 파일 업로드 실패: {}", e.getMessage());
            throw new GeneralException(GeneralErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public void delete(String fileUrl) {
        try {
            // 전체 URL에서 경로 부분만 추출
            String path = new java.net.URL(fileUrl).getPath();

            // 맨 앞의 '/' 제거
            String key = path.substring(1);

            s3Template.deleteObject(bucket, key);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", e.getMessage());
        }
    }


    public List<String> uploadFiles(List<MultipartFile> files, String dirName) {
        if (files == null || files.isEmpty())
            return Collections.emptyList();

        List<String> uploadedUrls = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                uploadedUrls.add(upload(file, dirName));
            }

            return uploadedUrls;
        } catch (Exception e) {
            // 이미 업로드된 파일 모두 삭제
            uploadedUrls.forEach(this::delete);

            throw new GeneralException(GeneralErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}
