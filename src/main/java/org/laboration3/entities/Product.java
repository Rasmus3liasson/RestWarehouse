package org.laboration3.entities;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record Product(int id, String name, Categories category, int rating, LocalDateTime createdDate, LocalDateTime lastModifiedDate) {

    public static Product createProduct(int id, String name, Categories category, int rating) {
        LocalDateTime date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        return new Product(
                id,
                name,
                category,
                rating,
                date,
                date
        );
    }
}