package br.com.carbigdata.ocorrencias_reactive.adapter.web.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioAsyncClient;
import io.minio.PutObjectArgs;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.XmlParserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class MinioService {

    private final MinioAsyncClient minioAsyncClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("bmp", "image/bmp");
        MIME_TYPES.put("webp", "image/webp");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("csv", "text/csv");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("xml", "application/xml");
        MIME_TYPES.put("zip", "application/zip");
        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MIME_TYPES.put("xls", "application/vnd.ms-excel");
        MIME_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MIME_TYPES.put("ppt", "application/vnd.ms-powerpoint");
        MIME_TYPES.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
    }

    public MinioService(MinioAsyncClient minioAsyncClient) {
        this.minioAsyncClient = minioAsyncClient;
    }

    public Mono<String> uploadFileReactive(FilePart filePart) {
        return Mono.fromCallable(() -> generateFileName(filePart.filename()))
                .flatMap(fileName ->
                        createBucketIfNotExistsReactive()
                                .then(uploadStreamReactive(filePart, fileName))
                )
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<String> uploadStreamReactive(FilePart filePart, String fileName) {
        return filePart.content()
                .collect(ByteArrayOutputStream::new,
                        (baos, dataBuffer) -> {
                            try {
                                baos.write(dataBuffer.asInputStream().readAllBytes());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .map(ByteArrayOutputStream::toByteArray)
                .flatMap(bytes ->
                        Mono.fromFuture(() ->
                                {
                                    try {
                                        return minioAsyncClient.putObject(
                                                PutObjectArgs.builder()
                                                        .bucket(bucketName)
                                                        .object(fileName)
                                                        .stream(new ByteArrayInputStream(bytes),
                                                                bytes.length, -1)
                                                        .contentType(detectContentTypeFromBytes(filePart, bytes))
                                                        .build()
                                        );
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        )
                )
                .thenReturn(fileName)
                .doOnNext(name ->
                        log.info("Upload via stream concluído: {}", name));
    }

    private Mono<Void> createBucketIfNotExistsReactive() {
        return Mono.fromFuture(() ->
                        {
                            try {
                                return minioAsyncClient.bucketExists(
                                        BucketExistsArgs.builder()
                                                .bucket(bucketName)
                                                .build()
                                );
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
                .flatMap(exists -> exists ? Mono.empty() :
                        Mono.fromFuture(() ->
                                        {
                                            try {
                                                return minioAsyncClient.makeBucket(
                                                        MakeBucketArgs.builder()
                                                                .bucket(bucketName)
                                                                .build()
                                                );
                                            } catch (InsufficientDataException | InternalException |
                                                     InvalidKeyException | IOException | NoSuchAlgorithmException |
                                                     XmlParserException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                )
                                .then()
                );
    }

    private String generateFileName(String originalFileName) {
        String extension = getExtension(originalFileName);
        return System.currentTimeMillis() + "_" + java.util.UUID.randomUUID() + extension;
    }

    private String detectContentTypeFromBytes(FilePart filePart, byte[] bytes) {
        if (filePart.headers().getContentType() != null) {
            String headerType = filePart.headers().getContentType().toString();
            if (!headerType.equals("application/octet-stream")) {
                return headerType;
            }
        }

        String filename = filePart.filename().toLowerCase();
        String extension = getExtension(filename).toLowerCase().replace(".", "");
        String mimeType = MIME_TYPES.get(extension);

        if (mimeType != null) {
            return mimeType;
        }

        if (bytes.length >= 4) {
            if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) {
                return "image/jpeg";
            }
            if (bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50 &&
                    bytes[2] == (byte) 0x4E && bytes[3] == (byte) 0x47) {
                return "image/png";
            }
        }

        return "application/octet-stream";
    }

    private String getExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}