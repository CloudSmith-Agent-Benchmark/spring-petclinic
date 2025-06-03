package org.springframework.samples.petclinic.integration;

import cucumber.api.CucumberOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@CucumberOptions(features = "src/test/resources/features")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {
}