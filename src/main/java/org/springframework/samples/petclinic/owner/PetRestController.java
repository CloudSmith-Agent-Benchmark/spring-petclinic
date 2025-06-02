package org.springframework.samples.petclinic.owner;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/owners/{ownerId}/pets")
class PetRestController {

    private final OwnerRepository owners;

    public PetRestController(OwnerRepository owners) {
        this.owners = owners;
    }

    @PutMapping("/{petId}")
    public ResponseEntity<Pet> updatePet(@PathVariable("ownerId") int ownerId,
                                       @PathVariable("petId") int petId,
                                       @Valid @RequestBody Pet pet,
                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        Owner owner = owners.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        Pet existingPet = owner.getPet(petId);
        if (existingPet == null) {
            throw new ResourceNotFoundException("Pet not found");
        }

        // Update existing pet's properties
        existingPet.setName(pet.getName());
        existingPet.setBirthDate(pet.getBirthDate());
        existingPet.setType(pet.getType());

        owners.save(owner);
        return ResponseEntity.ok(existingPet);
    }

    @PostMapping("/new")
    public ResponseEntity<Pet> addPet(@PathVariable("ownerId") int ownerId,
                                    @Valid @RequestBody Pet pet,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        Owner owner = owners.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        if (pet.isNew() && owner.getPet(pet.getName(), true) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        owner.addPet(pet);
        owners.save(owner);
        return ResponseEntity.ok(pet);
    }
}