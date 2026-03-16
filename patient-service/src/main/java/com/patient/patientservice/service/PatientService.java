package com.patient.patientservice.service;

import com.patient.patientservice.dto.PatientRequestDTO;
import com.patient.patientservice.dto.PatientResponseDTO;
import com.patient.patientservice.exception.EmailAlreadyExistsException;
import com.patient.patientservice.exception.PatientNotFoundException;
import com.patient.patientservice.grpc.BilliServiceGrpcClient;
import com.patient.patientservice.mapper.PatientMapper;
import com.patient.patientservice.model.Patient;
import com.patient.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final BilliServiceGrpcClient billiServiceGrpcClient;

    public PatientService(PatientRepository patientRepository, BilliServiceGrpcClient billiServiceGrpcClient) {
        this.patientRepository = patientRepository;
        this.billiServiceGrpcClient = billiServiceGrpcClient;
    }

    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients = patientRepository.findAll();

        return patients.stream()
                .map(PatientMapper::toDTO).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        validateEmailForCreate(patientRequestDTO.getEmail());

        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));

        billiServiceGrpcClient.createBillingAccount(
                newPatient.getId().toString(),
                newPatient.getName(),
                newPatient.getEmail()
        );

        return PatientMapper.toDTO(newPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) {
        Patient patient = patientRepository.findById(id).orElseThrow(
                () -> new PatientNotFoundException("Patient not found with this id: " + id));
        validateEmailForUpdate(patientRequestDTO.getEmail(), id);

        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatedPatient = patientRepository.save(patient);

        return PatientMapper.toDTO(updatedPatient);
    }

    public void deletePatient(UUID id) {
        patientRepository.deleteById(id);
    }

    private void validateEmailForCreate(String email) {
        if(patientRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("A patient with this email already exists");
        }
    }

    private void validateEmailForUpdate(String email, UUID id) {
        if(patientRepository.existsByEmailAndIdNot(email, id)) {
            throw new EmailAlreadyExistsException("A patient with this email already exists");
        }
    }
}
