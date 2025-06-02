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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

/**
 * Base class for HTTP integration tests that need to make calls to http://localhost:8080
 * The server will be started on a random port to avoid conflicts.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class BaseHttpIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected RestTemplateBuilder builder;

    @Autowired
    protected TestRestTemplate testRestTemplate;

    /**
     * Creates a pre-configured RestTemplate with the random port.
     * @return RestTemplate configured with the current test server URL
     */
    protected RestTemplate getRestTemplate() {
        return builder.rootUri("http://localhost:" + port).build();
    }

    /**
     * Gets the base URL for the running test server.
     * @return The base URL including protocol, host and port
     */
    protected String getBaseUrl() {
        return String.format("http://localhost:%d", port);
    }

}