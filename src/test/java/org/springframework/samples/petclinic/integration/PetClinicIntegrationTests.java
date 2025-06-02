package org.springframework.samples.petclinic.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.PetClinicApplication;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.Vets;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class PetClinicIntegrationTests {

    @LocalServerPort
    int port;

    @Autowired
    private VetRepository vets;

    @Autowired
    private RestTemplateBuilder builder;

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        this.restTemplate = builder
            .rootUri("http://localhost:" + port)
            .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Test
    void testFindAll() {
        vets.findAll();
        vets.findAll(); // served from cache
    }

    @Test
    void testOwnerDetails() {
        ResponseEntity<Owner> result = restTemplate.exchange(
            RequestEntity.get("/owners/1").build(),
            Owner.class
        );
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getFirstName()).isEqualTo("George");
        assertThat(result.getBody().getPets()).isNotEmpty();
    }

    @Test
    void testCreateNewOwner() {
        Map<String, Object> ownerData = new HashMap<>();
        ownerData.put("firstName", "John");
        ownerData.put("lastName", "Doe");
        ownerData.put("address", "123 Main St");
        ownerData.put("city", "Springfield");
        ownerData.put("telephone", "1234567890");

        ResponseEntity<Owner> result = restTemplate.exchange(
            RequestEntity.post("/owners/new")
                .contentType(MediaType.APPLICATION_JSON)
                .body(ownerData),
            Owner.class
        );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isNotNull();
        assertThat(result.getBody().getFirstName()).isEqualTo("John");
    }

    @Test
    void testAddNewPetToOwner() {
        Map<String, Object> petData = new HashMap<>();
        petData.put("name", "Fluffy");
        petData.put("birthDate", LocalDate.now().toString());
        petData.put("type", new PetType() {{ setId(1); setName("cat"); }});

        ResponseEntity<Pet> result = restTemplate.exchange(
            RequestEntity.post("/owners/1/pets/new")
                .contentType(MediaType.APPLICATION_JSON)
                .body(petData),
            Pet.class
        );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getName()).isEqualTo("Fluffy");
    }

    @Test
    void testAddVisitToPet() {
        Map<String, Object> visitData = new HashMap<>();
        visitData.put("date", LocalDate.now().toString());
        visitData.put("description", "Regular checkup");

        ResponseEntity<Visit> result = restTemplate.exchange(
            RequestEntity.post("/owners/1/pets/1/visits/new")
                .contentType(MediaType.APPLICATION_JSON)
                .body(visitData),
            Visit.class
        );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getDescription()).isEqualTo("Regular checkup");
    }

    @Test
    void testListVetsWithSpecialties() {
        ResponseEntity<Vets> result = restTemplate.exchange(
            RequestEntity.get("/vets.json").build(),
            Vets.class
        );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getVetList()).isNotEmpty();
        assertThat(result.getBody().getVetList().get(0).getSpecialties()).isNotNull();
    }

    @Test
    void testNonExistentOwner() {
        assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(
                RequestEntity.get("/owners/999").build(),
                Owner.class
            );
        });
    }

    @Test
    void testCreateOwnerWithInvalidData() {
        Map<String, Object> invalidOwnerData = new HashMap<>();
        invalidOwnerData.put("firstName", "");  // Empty first name
        invalidOwnerData.put("lastName", "Doe");
        invalidOwnerData.put("address", "123 Main St");
        invalidOwnerData.put("city", "Springfield");
        invalidOwnerData.put("telephone", "abc"); // Invalid phone number

        assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(
                RequestEntity.post("/owners/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(invalidOwnerData),
                Owner.class
            );
        });
    }

//    @Test
//    void testUpdateExistingOwner() {
//        Map<String, Object> updatedData = new HashMap<>();
//        updatedData.put("firstName", "George");
//        updatedData.put("lastName", "Franklin-Updated");
//        updatedData.put("address", "110 W. Liberty St");
//        updatedData.put("city", "Madison");
//        updatedData.put("telephone", "6085551023");
//
//        ResponseEntity<Owner> result = restTemplate.exchange(
//            RequestEntity.put("/owners/1")
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(updatedData),
//            Owner.class
//        );
//
//        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(result.getBody()).isNotNull();
//        assertThat(result.getBody().getLastName()).isEqualTo("Franklin-Updated");
//    }
//
    @Test
    void testAddPetWithInvalidDate() {
        Map<String, Object> invalidPetData = new HashMap<>();
        invalidPetData.put("name", "Invalid");
        invalidPetData.put("birthDate", LocalDate.now().plusDays(1).toString()); // Future date
        invalidPetData.put("type", new PetType() {{ setId(1); setName("cat"); }});

        assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(
                RequestEntity.post("/owners/1/pets/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(invalidPetData),
                Pet.class
            );
        });
    }

    @Test
    void testUpdateExistingPet() {
        Map<String, Object> updatedPetData = new HashMap<>();
        updatedPetData.put("name", "Leo Updated");
        updatedPetData.put("birthDate", LocalDate.now().minusYears(2).toString());
        updatedPetData.put("type", new PetType() {{ setId(1); setName("cat"); }});

        ResponseEntity<Pet> result = restTemplate.exchange(
            RequestEntity.put("/owners/1/pets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(updatedPetData),
            Pet.class
        );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getName()).isEqualTo("Leo Updated");
    }

    @Test
    void testVetWithSpecificSpecialty() {
        ResponseEntity<Vets> result = restTemplate.exchange(
            RequestEntity.get("/vets.json").build(),
            Vets.class
        );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();

        // Find a vet with surgery specialty
        boolean hasSurgerySpecialist = result.getBody().getVetList().stream()
            .anyMatch(vet -> vet.getSpecialties().stream()
                .anyMatch(specialty -> "surgery".equalsIgnoreCase(specialty.getName())));

        assertThat(hasSurgerySpecialist).isTrue();
    }

    @Test
    void testVisitWithInvalidDate() {
        Map<String, Object> invalidVisitData = new HashMap<>();
        invalidVisitData.put("date", LocalDate.now().plusYears(1).toString()); // Future date
        invalidVisitData.put("description", "Invalid future visit");

        assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(
                RequestEntity.post("/owners/1/pets/1/visits/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(invalidVisitData),
                Visit.class
            );
        });
    }

    @Test
    void testAddVisitWithConflictingSchedule() {
        // First visit
        Map<String, Object> visit1Data = new HashMap<>();
        visit1Data.put("date", LocalDate.now().toString());
        visit1Data.put("description", "Morning checkup");

        ResponseEntity<Visit> result1 = restTemplate.exchange(
            RequestEntity.post("/owners/1/pets/1/visits/new")
                .contentType(MediaType.APPLICATION_JSON)
                .body(visit1Data),
            Visit.class
        );

        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Second visit same day
        Map<String, Object> visit2Data = new HashMap<>();
        visit2Data.put("date", LocalDate.now().toString());
        visit2Data.put("description", "Afternoon checkup");

        ResponseEntity<Visit> result2 = restTemplate.exchange(
            RequestEntity.post("/owners/1/pets/1/visits/new")
                .contentType(MediaType.APPLICATION_JSON)
                .body(visit2Data),
            Visit.class
        );

        // Should still work as multiple visits per day are allowed
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testVetSpecialtyDistribution() {
        ResponseEntity<Vets> result = restTemplate.exchange(
            RequestEntity.get("/vets.json").build(),
            Vets.class
        );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();

        Map<String, Integer> specialtyCounts = new HashMap<>();
        result.getBody().getVetList().forEach(vet ->
            vet.getSpecialties().forEach(specialty ->
                specialtyCounts.merge(specialty.getName(), 1, Integer::sum)
            )
        );

        // Verify we have at least one vet for each common specialty
        assertThat(specialtyCounts.containsKey("radiology")).isTrue();
        assertThat(specialtyCounts.containsKey("surgery")).isTrue();
        assertThat(specialtyCounts.containsKey("dentistry")).isTrue();
    }

//    @Test
//    void testOwnerWithExtremelyLongValues() {
//        String longString = "a".repeat(100);
//        Map<String, Object> ownerData = new HashMap<>();
//        ownerData.put("firstName", longString);
//        ownerData.put("lastName", longString);
//        ownerData.put("address", longString);
//        ownerData.put("city", longString);
//        ownerData.put("telephone", "1234567890");
//
//        assertThrows(HttpClientErrorException.class, () -> {
//            restTemplate.exchange(
//                RequestEntity.post("/owners/new")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .body(ownerData),
//                Owner.class
//            );
//        });
//    }

    @Test
    void testAddVisitWithEmptyDescription() {
        Map<String, Object> invalidVisitData = new HashMap<>();
        invalidVisitData.put("date", LocalDate.now().toString());
        invalidVisitData.put("description", "");

        assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(
                RequestEntity.post("/owners/1/pets/1/visits/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(invalidVisitData),
                Visit.class
            );
        });
    }

    public static void main(String[] args) {
        SpringApplication.run(PetClinicApplication.class, args);
    }

}
