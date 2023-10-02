package org.laboration3.entities;

import jakarta.validation.constraints.*;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record Product(@NotNull int id, @NotBlank String name, @NotNull Categories category,
                      @Min(1) @Max(10) @Positive int rating,
                      @PastOrPresent LocalDateTime createdDate, @FutureOrPresent LocalDateTime lastModifiedDate) {

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