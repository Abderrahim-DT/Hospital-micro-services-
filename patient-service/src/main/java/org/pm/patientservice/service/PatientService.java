package org.pm.patientservice.service;

import org.pm.patientservice.dto.PatientRequestDTO;
import org.pm.patientservice.dto.PatientResponseDTO;
import org.pm.patientservice.exception.EmailAlreadyExistsException;
import org.pm.patientservice.exception.PatientNotFoundException;
import org.pm.patientservice.grpc.BillingServiceGrpcClient;
import org.pm.patientservice.kafka.kafkaProducer;
import org.pm.patientservice.mapper.PatientMapper;
import org.pm.patientservice.model.Patient;
import org.pm.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    private final BillingServiceGrpcClient billingServiceGrpcClient;

    private final kafkaProducer kafkaProducer;
    public PatientService(PatientRepository patientRepository
    ,BillingServiceGrpcClient billingServiceGrpcClient
    ,kafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getPatients() {

        List<Patient> patients = patientRepository.findAll();

        return patients.stream()
                .map(PatientMapper::toDTO).toList();
    }

public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "A patient with this email"+"already exists "
                            + patientRequestDTO.getEmail());
        }
        Patient newPatient = patientRepository.save(
                PatientMapper.toModel(patientRequestDTO)
        );
        billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(),newPatient.getName(),newPatient.getEmail());


        kafkaProducer.sendEvent(newPatient);

        return PatientMapper.toDTO(newPatient);


}

public PatientResponseDTO updatePatient(UUID id,
                                        PatientRequestDTO patientRequestDTO) {

        Patient patient = patientRepository.findById(id).orElseThrow(
                ()-> new PatientNotFoundException("Pation not found with ID : "+ id));

    if(patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(),id)) {
        throw new EmailAlreadyExistsException(
                "A patient with this email"+"already exists "
                        + patientRequestDTO.getEmail());
    }

    patient.setName(patientRequestDTO.getName());
    patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
    patient.setEmail(patientRequestDTO.getEmail());
    patient.setAddress(patientRequestDTO.getAddress());


    Patient updatedPatient = patientRepository.save(patient);
    return PatientMapper.toDTO(updatedPatient);




}

public void deletePatient(UUID id) {
        patientRepository.deleteById(id);
}




































}
