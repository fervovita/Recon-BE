package com.project.recon.domain.product.event;

public record ProductEvent(Long productId, EventType type) {

    public enum EventType {
        CREATED,
        UPDATED,
        DELETED
    }
}
