package org.springframework.samples.petclinic.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    void testTpsLimitExceeded() {
        // Create test data for a visit
        Map<String, Object> visitData = new HashMap<>();
        visitData.put("date", LocalDate.now().toString());
        visitData.put("description", "Quick checkup");

        // Create 100 parallel requests
        List<CompletableFuture<ResponseEntity<Visit>>> futures = IntStream.range(0, 100)
            .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                return restTemplate.exchange(
                    RequestEntity.post("/owners/1/pets/1/visits/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(visitData),
                    Visit.class
                );
            }))
            .collect(Collectors.toList());

        // Wait for all requests to complete and verify their status
        futures.stream()
            .map(CompletableFuture::join)
            .forEach(response -> {
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            });
    }

    public static void main(String[] args) {
        SpringApplication.run(PetClinicApplication.class, args);
    }
}
