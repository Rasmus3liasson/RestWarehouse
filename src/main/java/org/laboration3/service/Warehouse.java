package org.laboration3.service;

import org.laboration3.entities.Categories;
import org.laboration3.entities.Product;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class Warehouse {

    private final List<Product> productsArr = new ArrayList<>();


    public void addProduct(Product p) {
        // Check if name is not an empty string and if id already exist
        if (!p.name().isEmpty() &&
                productsArr.stream()
                        .noneMatch(productId -> productId.id() == p.id())) {
            // Checks if rating is correct
            if (p.rating() >= 1 && p.rating() <= 10) {
                productsArr.add(p);
            } else {
                throw new IllegalArgumentException("Rating kan bara vara 1-10");
            }
        } else {
            throw new IllegalArgumentException("Kan inte lägga till product " + p.id());
        }
    }

    public void modifyProduct(int productId, String newName, Categories newCategory, int newRating) {
        if (newRating < 1 || newRating > 10) {
            throw new IllegalArgumentException("Rating måste vara mellan 1-10");
        }
        if (newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Måste fylla i ett på produkten");
        }
        Optional<Product> product = productsArr.stream()
                .filter(p -> p.id() == productId)
                .findFirst();

        product.ifPresentOrElse(
                p -> {
                    Product changedProduct = new Product(productId, newName, newCategory, newRating, p.createdDate(),
                            LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
                    productsArr.set(productsArr.indexOf(p), changedProduct);
                },
                () -> {
                    System.out.println("Produkt med id " + productId + " kunde inte lokaliseras");
                }
        );
    }

    public List<Product> getProductsArr() {
        return new ArrayList<>(productsArr);
    }

    public List<Product> getProductBasedOnId(int id) {
        List<Product> productById = productsArr.stream()
                .filter(p -> p.id() == id).toList();

        if (productById.isEmpty()) {
            throw new NoSuchElementException("Finns ingen produkt med detta id");
        }

        return productById;
    }

    public List<Product> getProductBasedOnCategory(Categories category) {
        List<Product> sortedByCategory = productsArr.stream()
                .filter(p -> p.category().equals(category))
                .sorted(Comparator.comparing(p -> p.name().toLowerCase()))
                .toList();
        return sortedByCategory;
    }

    public List<Product> getProductCreatedAfterDate(LocalDateTime date) {
        List<Product> createdAfterDate = productsArr.stream()
                .filter(p -> p.createdDate().isAfter(date))
                .toList();
        return createdAfterDate;
    }

    public List<Product> getProductThatBeenModified() {
        List<Product> modifiedProducts = productsArr.stream()
                .filter(p -> !p.createdDate().isEqual(p.lastModifiedDate()))
                .toList();
        return modifiedProducts;
    }


    // VG assignments
    public List<Categories> getCategoriesWithProducts() {
        List<Categories> categoriesContainsProduct = new ArrayList<>();

        for (Product p : productsArr) {
            Categories category = p.category();
            // check if category exist before adding
            if (!categoriesContainsProduct.contains(category)) {
                categoriesContainsProduct.add(category);
            }
        }

        return categoriesContainsProduct;
    }

    public int getHowManyProductsRelatedToCategory(Categories category) {
        List<Product> productsInCategory = new ArrayList<>();

        for (Product p : productsArr) {
            if (p.category() == category) {
                productsInCategory.add(p);
            }
        }

        return productsInCategory.size();
    }

    public Map<String, Integer> getMap() {
        return productsArr.stream()
                .collect(Collectors.groupingBy(
                        p -> p.name().substring(0, 1),
                        Collectors.summingInt(p -> 1)
                ));
    }

    public List<Product> getRecentMaxRating() {
        List<Product> maxRatingProduct = productsArr.stream()
                .filter(p -> p.rating() == 10 &&
                        p.createdDate().getMonth() == LocalDateTime.now().getMonth())
                .sorted(Comparator.comparing(Product::createdDate).reversed())
                .toList();

        if (maxRatingProduct.isEmpty()) {
            throw new NoSuchElementException("Det fanns ingen produkt med högsta rating");

        }

        return maxRatingProduct;
    }
}
