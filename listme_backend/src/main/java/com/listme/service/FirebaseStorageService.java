package com.listme.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Slf4j
public class FirebaseStorageService {

    @Value("${firebase.storage.bucket}")
    private String bucketName;

    public String uploadImage(MultipartFile file) {
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            Storage storage = StorageClient.getInstance().bucket().getStorage();

            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            storage.create(blobInfo, file.getBytes());

            // URL do Firebase Storage
            return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    bucketName,
                    fileName.replace("/", "%2F"));

        } catch (Exception e) {
            log.error("Erro ao fazer upload da imagem: ", e);
            throw new RuntimeException("Erro ao fazer upload da imagem: " + e.getMessage());
        }
    }

    private String generateFileName(String originalFileName) {
        return "noticias/" + UUID.randomUUID().toString() + "_" +
                originalFileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    public void deleteFile(String fileUrl) {
        try {
            // Extrair o nome do arquivo da URL
            String fileName = extractFileNameFromUrl(fileUrl);
            if (fileName == null) {
                log.warn("Nome do arquivo não pôde ser extraído da URL: {}", fileUrl);
                return;
            }

            Storage storage = StorageClient.getInstance().bucket().getStorage();
            BlobId blobId = BlobId.of(bucketName, fileName);

            // Verificar se o arquivo existe
            Blob blob = storage.get(blobId);
            if (blob != null) {
                boolean deleted = storage.delete(blobId);
                if (deleted) {
                    log.info("Arquivo deletado com sucesso: {}", fileName);
                } else {
                    log.warn("Arquivo não pôde ser deletado: {}", fileName);
                }
            } else {
                log.warn("Arquivo não encontrado: {}", fileName);
            }

        } catch (Exception e) {
            log.error("Erro ao deletar arquivo: ", e);
            throw new RuntimeException("Erro ao deletar arquivo: " + e.getMessage());
        }
    }

    private String extractFileNameFromUrl(String fileUrl) {
        try {
            // Remove os parâmetros da URL
            String urlWithoutParams = fileUrl.split("\\?")[0];

            // Extrai o nome do arquivo da URL
            String[] parts = urlWithoutParams.split("/o/");
            if (parts.length > 1) {
                // Decodifica o nome do arquivo
                return parts[1].replace("%2F", "/");
            }
            return null;
        } catch (Exception e) {
            log.error("Erro ao extrair nome do arquivo da URL: ", e);
            return null;
        }
    }
}
