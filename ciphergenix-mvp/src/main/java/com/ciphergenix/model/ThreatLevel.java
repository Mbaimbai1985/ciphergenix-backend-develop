package com.ciphergenix.model;

public enum ThreatLevel {
    LOW("Low risk threat detected"),
    MEDIUM("Medium risk threat detected, investigation recommended"),
    HIGH("High risk threat detected, immediate action required"),
    CRITICAL("Critical threat detected, system compromise likely");

    private final String description;

    ThreatLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}