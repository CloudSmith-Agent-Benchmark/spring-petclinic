package org.springframework.samples.petclinic.integration;

import org.junit.runner.RunWith;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "org.springframework.samples.petclinic.integration",
    plugin = {"pretty"}
)
public class CucumberIntegrationTest {
}