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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.samples.petclinic.config.FeatureFlagConfig;
import org.springframework.samples.petclinic.config.FeatureDisabledException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link OwnerController}
 *
 * @author Colin But
 * @author Wick Dynex
 */
@WebMvcTest(OwnerController.class)
@DisabledInNativeImage
@DisabledInAotMode
class OwnerControllerTests {

        private static final int TEST_OWNER_ID = 1;

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private OwnerRepository owners;

        @MockitoBean
        private FeatureFlagConfig featureFlagConfig;

        private Owner george() {
                Owner george = new Owner();
                george.setId(TEST_OWNER_ID);
                george.setFirstName("George");
                george.setLastName("Franklin");
                george.setAddress("110 W. Liberty St.");
                george.setCity("Madison");
                george.setTelephone("6085551023");
                Pet max = new Pet();
                PetType dog = new PetType();
                dog.setName("dog");
                max.setType(dog);
                max.setName("Max");
                max.setBirthDate(LocalDate.now());
                george.addPet(max);
                max.setId(1);
                return george;
        }

        @BeforeEach
        void setup() {

                Owner george = george();
                given(this.owners.findByLastNameStartingWith(eq("Franklin"), any(Pageable.class)))
                        .willReturn(new PageImpl<>(List.of(george)));

                given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(george));
                Visit visit = new Visit();
                visit.setDate(LocalDate.now());
                george.getPet("Max").getVisits().add(visit);

                // Enable all features by default
                given(this.featureFlagConfig.isOwnerManagement()).willReturn(true);
                given(this.featureFlagConfig.isPetManagement()).willReturn(true);
                given(this.featureFlagConfig.isVisitManagement()).willReturn(true);
                given(this.featureFlagConfig.isVetManagement()).willReturn(true);
        }

        @Test
        void testInitCreationFormWithFeatureEnabled() throws Exception {
                given(this.featureFlagConfig.isOwnerManagement()).willReturn(true);
                mockMvc.perform(get("/owners/new"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").exists());
        }

        @Test
        void testInitCreationFormWithFeatureDisabled() throws Exception {
                given(this.featureFlagConfig.isOwnerManagement()).willReturn(false);
                mockMvc.perform(get("/owners/new"))
                        .andExpect(status().isServiceUnavailable());
        }

        @Test
        void testProcessCreationFormSuccessWithFeatureEnabled() throws Exception {
                given(this.featureFlagConfig.isOwnerManagement()).willReturn(true);
                Owner owner = george();
                given(this.owners.save(any(Owner.class))).willReturn(owner);

                mockMvc.perform(post("/owners/new")
                                .contentType("application/json")
                                .content("{\"firstName\":\"Joe\",\"lastName\":\"Bloggs\",\"address\":\"123 Caramel Street\",\"city\":\"London\",\"telephone\":\"1316761638\"}"))
                        .andExpect(status().isOk());
        }

        @Test
        void testProcessCreationFormWithFeatureDisabled() throws Exception {
                given(this.featureFlagConfig.isOwnerManagement()).willReturn(false);
                mockMvc.perform(post("/owners/new")
                                .contentType("application/json")
                                .content("{\"firstName\":\"Joe\",\"lastName\":\"Bloggs\",\"address\":\"123 Caramel Street\",\"city\":\"London\",\"telephone\":\"1316761638\"}"))
                        .andExpect(status().isServiceUnavailable());
        }

        @Test
        void testShowOwnerWithFeatureEnabled() throws Exception {
                given(this.featureFlagConfig.isOwnerManagement()).willReturn(true);
                mockMvc.perform(get("/owners/{ownerId}", TEST_OWNER_ID))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.lastName").value("Franklin"))
                        .andExpect(jsonPath("$.firstName").value("George"))
                        .andExpect(jsonPath("$.address").value("110 W. Liberty St."))
                        .andExpect(jsonPath("$.city").value("Madison"))
                        .andExpect(jsonPath("$.telephone").value("6085551023"));
        }

        @Test
        void testShowOwnerWithFeatureDisabled() throws Exception {
                given(this.featureFlagConfig.isOwnerManagement()).willReturn(false);
                mockMvc.perform(get("/owners/{ownerId}", TEST_OWNER_ID))
                        .andExpect(status().isServiceUnavailable());
        }

        @Test
        void testAddNewPetWithFeatureEnabled() throws Exception {
                given(this.featureFlagConfig.isPetManagement()).willReturn(true);
                mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
                                .contentType("application/json")
                                .content("{\"name\":\"Fluffy\",\"birthDate\":\"2023-01-01\",\"type\":{\"id\":1,\"name\":\"cat\"}}"))
                        .andExpect(status().isOk());
        }

        @Test
        void testAddNewPetWithFeatureDisabled() throws Exception {
                given(this.featureFlagConfig.isPetManagement()).willReturn(false);
                mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
                                .contentType("application/json")
                                .content("{\"name\":\"Fluffy\",\"birthDate\":\"2023-01-01\",\"type\":{\"id\":1,\"name\":\"cat\"}}"))
                        .andExpect(status().isServiceUnavailable());
        }

        @Test
        void testAddNewVisitWithFeatureEnabled() throws Exception {
                given(this.featureFlagConfig.isVisitManagement()).willReturn(true);
                mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, 1)
                                .contentType("application/json")
                                .content("{\"date\":\"2024-03-20\",\"description\":\"Regular checkup\"}"))
                        .andExpect(status().isOk());
        }

        @Test
        void testAddNewVisitWithFeatureDisabled() throws Exception {
                given(this.featureFlagConfig.isVisitManagement()).willReturn(false);
                mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, 1)
                                .contentType("application/json")
                                .content("{\"date\":\"2024-03-20\",\"description\":\"Regular checkup\"}"))
                        .andExpect(status().isServiceUnavailable());
        }

}
