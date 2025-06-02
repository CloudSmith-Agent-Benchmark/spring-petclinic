/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;

import jakarta.validation.Valid;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@RestController
@RequestMapping("/owners")
class OwnerController {

	private final OwnerRepository owners;

	public OwnerController(OwnerRepository owners) {
		this.owners = owners;
	}

	@GetMapping(value = "/new", produces = MediaType.APPLICATION_JSON_VALUE)
	public Owner initCreationForm() {
		return new Owner();
	}

	@PostMapping(value = "/new", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Owner> processCreationForm(@Valid @RequestBody Owner owner, BindingResult result) {
		if (result.hasErrors()) {
			return ResponseEntity.badRequest().build();
		}
		Owner savedOwner = this.owners.save(owner);
		return ResponseEntity.ok(savedOwner);
	}

	@GetMapping(value = "/{ownerId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Owner> showOwner(@PathVariable("ownerId") int ownerId) {
		Optional<Owner> owner = this.owners.findById(ownerId);
		if (!owner.isPresent()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(owner.get());
	}

	@PostMapping(value = "/{ownerId}/pets/new", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Pet> processNewPetForm(@PathVariable("ownerId") int ownerId, @Valid @RequestBody Pet pet, BindingResult result) {
		if (result.hasErrors()) {
			return ResponseEntity.badRequest().build();
		}

		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		if (!optionalOwner.isPresent()) {
			return ResponseEntity.notFound().build();
		}

		Owner owner = optionalOwner.get();
		owner.addPet(pet);
		this.owners.save(owner);

		return ResponseEntity.ok(pet);
	}

	@PostMapping(value = "/{ownerId}/pets/{petId}/visits/new", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Visit> processNewVisitForm(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			@Valid @RequestBody Visit visit, BindingResult result) {
		if (result.hasErrors()) {
			return ResponseEntity.badRequest().build();
		}

		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		if (!optionalOwner.isPresent()) {
			return ResponseEntity.notFound().build();
		}

		Owner owner = optionalOwner.get();
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			return ResponseEntity.notFound().build();
		}

		pet.addVisit(visit);
		this.owners.save(owner);

		return ResponseEntity.ok(visit);
	}

}