package edu.cit.custodio.mdqueue.feature.clinic;

import edu.cit.custodio.mdqueue.feature.clinic.dto.ClinicRequest;
import edu.cit.custodio.mdqueue.feature.clinic.dto.ClinicResponse;
import edu.cit.custodio.mdqueue.feature.clinic.Clinic;
import edu.cit.custodio.mdqueue.feature.queue.QueueEntity;
import edu.cit.custodio.mdqueue.feature.user.User;
import edu.cit.custodio.mdqueue.feature.clinic.ClinicRepository;
import edu.cit.custodio.mdqueue.feature.queue.QueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClinicService {

    private final ClinicRepository clinicRepository;
    private final QueueRepository queueRepository;

    @Transactional
    public ClinicResponse create(ClinicRequest request, User owner) {
        Clinic clinic = Clinic.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .description(request.getDescription())
                .openingTime(parseTime(request.getOpeningTime()))
                .closingTime(parseTime(request.getClosingTime()))
                .owner(owner)
                .build();
        clinic = clinicRepository.save(clinic);
        return toResponse(clinic);
    }

    public List<ClinicResponse> getAll() {
        return clinicRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ClinicResponse> search(String name) {
        return clinicRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ClinicResponse getById(Long id) {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Clinic not found"));
        return toResponse(clinic);
    }

    public Clinic getEntityById(Long id) {
        return clinicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Clinic not found"));
    }

    @Transactional
    public ClinicResponse update(Long id, ClinicRequest request, User owner) {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Clinic not found"));
        if (!clinic.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("You are not the owner of this clinic");
        }
        clinic.setName(request.getName());
        clinic.setAddress(request.getAddress());
        clinic.setPhoneNumber(request.getPhoneNumber());
        clinic.setDescription(request.getDescription());
        clinic.setOpeningTime(parseTime(request.getOpeningTime()));
        clinic.setClosingTime(parseTime(request.getClosingTime()));
        clinic = clinicRepository.save(clinic);
        return toResponse(clinic);
    }

    @Transactional
    public void delete(Long id, User owner) {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Clinic not found"));
        if (!clinic.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("You are not the owner of this clinic");
        }
        clinicRepository.delete(clinic);
    }

    public List<ClinicResponse> getByOwner(User owner) {
        return clinicRepository.findByOwner(owner).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ClinicResponse toResponse(Clinic clinic) {
        long activeQueues = queueRepository.findByClinicAndStatus(clinic, QueueEntity.Status.OPEN).size();
        return ClinicResponse.builder()
                .id(clinic.getId())
                .name(clinic.getName())
                .address(clinic.getAddress())
                .phoneNumber(clinic.getPhoneNumber())
                .description(clinic.getDescription())
                .openingTime(clinic.getOpeningTime() != null ? clinic.getOpeningTime().toString() : null)
                .closingTime(clinic.getClosingTime() != null ? clinic.getClosingTime().toString() : null)
                .ownerId(clinic.getOwner().getId())
                .ownerName(clinic.getOwner().getFullName())
                .activeQueues((int) activeQueues)
                .build();
    }

    private LocalTime parseTime(String time) {
        if (time == null || time.isBlank()) return null;
        return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
    }
}
