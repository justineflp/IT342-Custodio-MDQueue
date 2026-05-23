package edu.cit.custodio.mdqueue.feature.file;

import edu.cit.custodio.mdqueue.feature.appointment.Appointment;
import edu.cit.custodio.mdqueue.feature.appointment.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final UploadedFileRepository fileRepository;
    private final AppointmentRepository appointmentRepository;
    
    private final String uploadDir = "uploads/";

    public UploadedFile storeFile(Long appointmentId, MultipartFile file) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
            String fileName = UUID.randomUUID().toString() + "_" + originalName;
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            UploadedFile uploadedFile = UploadedFile.builder()
                    .appointment(appointment)
                    .fileName(originalName)
                    .filePathOrUrl(filePath.toString())
                    .fileType(file.getContentType())
                    .sizeBytes(file.getSize())
                    .build();

            return fileRepository.save(uploadedFile);
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + file.getOriginalFilename() + ". Please try again!", ex);
        }
    }

    public List<UploadedFile> getFilesForAppointment(Long appointmentId) {
        return fileRepository.findByAppointmentId(appointmentId);
    }
}
