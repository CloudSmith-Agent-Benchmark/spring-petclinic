/*
 * Copyright 2012-2024 the original author or authors.
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

package org.springframework.samples.petclinic.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Example integration test class that demonstrates how to use the BaseHttpIntegrationTest
 * to perform HTTP requests to the application endpoints.
 */
class SystemHttpIntegrationTest extends BaseHttpIntegrationTest {

    @Test
    void shouldGetWelcomePage() {
        ResponseEntity<String> response = testRestTemplate.getForEntity("/", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Welcome");
    }

    @Test
    void shouldGet404ForNonExistentEndpoint() {
        ResponseEntity<String> response = testRestTemplate.getForEntity("/non-existent", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldGetOwnersPage() {
        ResponseEntity<String> response = getRestTemplate().getForEntity("/owners/find", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Find Owners");
    }

}