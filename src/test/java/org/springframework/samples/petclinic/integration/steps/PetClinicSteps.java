package org.springframework.samples.petclinic.integration.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.vet.Vets;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class PetClinicSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplateBuilder builder;

    private RestTemplate restTemplate;
    private ResponseEntity<?> lastResponse;
    private Exception lastException;

    @Before
    public void setUp() {
        this.restTemplate = builder
            .rootUri("http://localhost:" + port)
            .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Given("the PetClinic application is running")
    public void thePetClinicApplicationIsRunning() {
        // The application is already running due to @SpringBootTest
    }

    @When("I request details for owner with ID {int}")
    public void iRequestDetailsForOwnerWithId(Integer id) {
        try {
            lastResponse = restTemplate.exchange(
                RequestEntity.get("/owners/" + id).build(),
                Owner.class
            );
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("I should see owner details with first name {string}")
    public void iShouldSeeOwnerDetailsWithFirstName(String firstName) {
        assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Owner owner = (Owner) lastResponse.getBody();
        assertThat(owner).isNotNull();
        assertThat(owner.getFirstName()).isEqualTo(firstName);
    }

    @Then("the owner should have pets")
    public void theOwnerShouldHavePets() {
        Owner owner = (Owner) lastResponse.getBody();
        assertThat(owner.getPets()).isNotEmpty();
    }

    @When("I create a new owner with the following details:")
    public void iCreateANewOwnerWithTheFollowingDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> ownerData = rows.get(0);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", ownerData.get("firstName"));
        requestBody.put("lastName", ownerData.get("lastName"));
        requestBody.put("address", ownerData.get("address"));
        requestBody.put("city", ownerData.get("city"));
        requestBody.put("telephone", ownerData.get("telephone"));

        try {
            lastResponse = restTemplate.exchange(
                RequestEntity.post("/owners/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody),
                Owner.class
            );
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the owner should be created successfully")
    public void theOwnerShouldBeCreatedSuccessfully() {
        assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(lastResponse.getBody()).isNotNull();
    }

    @Then("the owner details should match the input")
    public void theOwnerDetailsShouldMatchTheInput() {
        Owner owner = (Owner) lastResponse.getBody();
        assertThat(owner.getId()).isNotNull();
        assertThat(owner.getFirstName()).isEqualTo("John");
        assertThat(owner.getLastName()).isEqualTo("Doe");
        assertThat(owner.getAddress()).isEqualTo("123 Main St");
        assertThat(owner.getCity()).isEqualTo("Springfield");
        assertThat(owner.getTelephone()).isEqualTo("1234567890");
    }

    @When("I add a new pet to owner {int} with the following details:")
    public void iAddANewPetToOwnerWithTheFollowingDetails(Integer ownerId, DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> petData = rows.get(0);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", petData.get("name"));
        requestBody.put("birthDate", petData.get("birthDate"));
        requestBody.put("type", new PetType() {{ setId(1); setName(petData.get("type")); }});

        try {
            lastResponse = restTemplate.exchange(
                RequestEntity.post("/owners/" + ownerId + "/pets/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody),
                Pet.class
            );
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the pet should be added successfully")
    public void thePetShouldBeAddedSuccessfully() {
        assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(lastResponse.getBody()).isNotNull();
    }

    @Then("the pet details should match the input")
    public void thePetDetailsShouldMatchTheInput() {
        Pet pet = (Pet) lastResponse.getBody();
        assertThat(pet.getName()).isEqualTo("Fluffy");
        assertThat(pet.getType().getName()).isEqualTo("cat");
    }

    @When("I add a visit to pet {int} of owner {int} with the following details:")
    public void iAddAVisitToPetOfOwnerWithTheFollowingDetails(Integer petId, Integer ownerId, DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> visitData = rows.get(0);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("date", visitData.get("date"));
        requestBody.put("description", visitData.get("description"));

        try {
            lastResponse = restTemplate.exchange(
                RequestEntity.post("/owners/" + ownerId + "/pets/" + petId + "/visits/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody),
                Visit.class
            );
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the visit should be added successfully")
    public void theVisitShouldBeAddedSuccessfully() {
        assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(lastResponse.getBody()).isNotNull();
    }

    @Then("the visit details should match the input")
    public void theVisitDetailsShouldMatchTheInput() {
        Visit visit = (Visit) lastResponse.getBody();
        assertThat(visit.getDescription()).isEqualTo("Regular checkup");
    }

    @When("I request the list of vets")
    public void iRequestTheListOfVets() {
        try {
            lastResponse = restTemplate.exchange(
                RequestEntity.get("/vets.json").build(),
                Vets.class
            );
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("I should see the list of vets")
    public void iShouldSeeTheListOfVets() {
        assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Vets vets = (Vets) lastResponse.getBody();
        assertThat(vets).isNotNull();
        assertThat(vets.getVetList()).isNotEmpty();
    }

    @Then("at least one vet should have {string} specialty")
    public void atLeastOneVetShouldHaveSpecialty(String specialty) {
        Vets vets = (Vets) lastResponse.getBody();
        boolean hasSurgerySpecialist = vets.getVetList().stream()
            .anyMatch(vet -> vet.getSpecialties().stream()
                .anyMatch(s -> specialty.equalsIgnoreCase(s.getName())));
        assertThat(hasSurgerySpecialist).isTrue();
    }

    @When("I try to create an owner with invalid data:")
    public void iTryToCreateAnOwnerWithInvalidData(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> ownerData = rows.get(0);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", ownerData.get("firstName"));
        requestBody.put("lastName", ownerData.get("lastName"));
        requestBody.put("address", ownerData.get("address"));
        requestBody.put("city", ownerData.get("city"));
        requestBody.put("telephone", ownerData.get("telephone"));

        try {
            lastResponse = restTemplate.exchange(
                RequestEntity.post("/owners/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody),
                Owner.class
            );
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I try to add a visit with empty description:")
    public void iTryToAddAVisitWithEmptyDescription(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> visitData = rows.get(0);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("date", visitData.get("date"));
        requestBody.put("description", visitData.get("description"));

        try {
            lastResponse = restTemplate.exchange(
                RequestEntity.post("/owners/1/pets/1/visits/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody),
                Visit.class
            );
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("I should receive a validation error")
    public void iShouldReceiveAValidationError() {
        assertThat(lastException).isInstanceOf(HttpClientErrorException.class);
    }
}