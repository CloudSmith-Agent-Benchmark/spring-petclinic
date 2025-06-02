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
        ResponseEntity<Vet[]> result = restTemplate.exchange(
            RequestEntity.get("/vets.json").build(),
            Vet[].class
        );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).hasSizeGreaterThan(0);
        assertThat(result.getBody()[0].getSpecialties()).isNotNull();
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

    public static void main(String[] args) {
        SpringApplication.run(PetClinicApplication.class, args);
    }

}
