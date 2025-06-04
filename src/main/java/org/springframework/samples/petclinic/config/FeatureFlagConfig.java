package org.springframework.samples.petclinic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "petclinic.features")
public class FeatureFlagConfig {
    private boolean ownerManagement = true;
    private boolean petManagement = true;
    private boolean visitManagement = true;
    private boolean vetManagement = true;

    public boolean isOwnerManagement() {
        return ownerManagement;
    }

    public void setOwnerManagement(boolean ownerManagement) {
        this.ownerManagement = ownerManagement;
    }

    public boolean isPetManagement() {
        return petManagement;
    }

    public void setPetManagement(boolean petManagement) {
        this.petManagement = petManagement;
    }

    public boolean isVisitManagement() {
        return visitManagement;
    }

    public void setVisitManagement(boolean visitManagement) {
        this.visitManagement = visitManagement;
    }

    public boolean isVetManagement() {
        return vetManagement;
    }

    public void setVetManagement(boolean vetManagement) {
        this.vetManagement = vetManagement;
    }
}