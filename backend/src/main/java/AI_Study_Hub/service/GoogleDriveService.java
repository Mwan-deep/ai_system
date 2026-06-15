package AI_Study_Hub.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleDriveService {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.refresh.token}")
    private String refreshToken;

    @Value("${google.drive.folder.id}")
    private String folderId;

    private Drive driveService;

    @PostConstruct
    public void init() throws GeneralSecurityException, IOException {
        // Sử dụng UserCredentials thay cho file JSON
        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        driveService = new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("AI Study Hub")
                .build();
    }

    public String uploadFileToDrive(MultipartFile multipartFile) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(multipartFile.getOriginalFilename());
        fileMetadata.setParents(Collections.singletonList(folderId));

        InputStreamContent mediaContent = new InputStreamContent(
                multipartFile.getContentType(),
                new ByteArrayInputStream(multipartFile.getBytes())
        );

        File file = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        Permission permission = new Permission().setType("anyone").setRole("reader");
        driveService.permissions().create(file.getId(), permission).execute();

        return file.getId();
    }

    public void deleteFileFromDrive(String fileId) {
        try {
            driveService.files().delete(fileId).execute();
        } catch (Exception e) {
            System.err.println("Không thể xóa file trên Drive: " + e.getMessage());
        }
    }

    public Drive getDriveService() {
        return driveService;
    }
}