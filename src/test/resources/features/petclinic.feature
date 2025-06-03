Feature: PetClinic Integration Tests
  As a pet clinic owner
  I want to manage owners, pets, and visits
  So that I can run my clinic efficiently

  Background:
    Given the PetClinic application is running

  Scenario: View owner details
    When I request details for owner with ID 1
    Then I should see owner details with first name "George"
    And the owner should have pets

  Scenario: Create new owner
    When I create a new owner with the following details:
      | firstName | lastName | address     | city        | telephone  |
      | John      | Doe      | 123 Main St | Springfield | 1234567890 |
    Then the owner should be created successfully
    And the owner details should match the input

  Scenario: Add new pet to owner
    When I add a new pet to owner 1 with the following details:
      | name   | birthDate  | type |
      | Fluffy | 2023-01-01 | cat  |
    Then the pet should be added successfully
    And the pet details should match the input

  Scenario: Add visit to pet
    When I add a visit to pet 1 of owner 1 with the following details:
      | date       | description      |
      | 2024-03-20 | Regular checkup |
    Then the visit should be added successfully
    And the visit details should match the input

  Scenario: List vets with specialties
    When I request the list of vets
    Then I should see the list of vets
    And at least one vet should have "surgery" specialty

  Scenario: Invalid owner creation
    When I try to create an owner with invalid data:
      | firstName | lastName | address     | city        | telephone |
      |           | Doe      | 123 Main St | Springfield | abc       |
    Then I should receive a validation error

  Scenario: Add visit with empty description
    When I try to add a visit with empty description:
      | date       | description |
      | 2024-03-20 |            |
    Then I should receive a validation error