package service;

import org.junit.jupiter.api.Test;
import org.laboration3.entities.Categories;
import org.laboration3.entities.Product;
import org.laboration3.service.Warehouse;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class WarehouseTest {


    Warehouse warehouse = new Warehouse();
    // Create test products
    private Product product1 = new Product(1, "Hatt", Categories.clothes, 5, LocalDateTime.now(), LocalDateTime.now());
    private Product product2 = new Product(1, "Tröja", Categories.clothes, 4, LocalDateTime.now(), LocalDateTime.now());
    private Product product3 = new Product(2, "Balsam", Categories.health, 3, LocalDateTime.now(), LocalDateTime.now());
    private Product product4 = new Product(8, "", Categories.health, 3, LocalDateTime.now(), LocalDateTime.now());
    private Product product5 = new Product(4, "Football", Categories.sport, 5, LocalDateTime.now(), LocalDateTime.now());
    private Product product6 = new Product(12, "Basket", Categories.sport, 5, LocalDateTime.now(), LocalDateTime.now());


    @Test
    void testAddProduct() {
        warehouse.addProduct(product1);
        warehouse.addProduct(product3);

        // Check if Products have been added
        assertThat(warehouse.getProductsArr()).contains(product1, product3);

    }

    @Test
    void testAddProductWithSameId() {
        warehouse.addProduct(product1);

        // Shows that function won't add products with same iD
        assertThrows(IllegalArgumentException.class, () -> {
            warehouse.addProduct(product2);
        });
        assertThat(warehouse.getProductsArr()).containsOnly(product1);
        assertThat(warehouse.getProductsArr()).doesNotContain(product2);

    }

    @Test
    void testAddProductWithEmptyProductName() {

        // Shows that product with empty string can't be added
        assertThrows(IllegalArgumentException.class, () -> {
            warehouse.addProduct(product4);
        });

        assertThat(warehouse.getProductsArr()).doesNotContain(product4);
    }


    @Test
    void testModifyProduct() {
        warehouse.addProduct(product1);
        warehouse.addProduct(product3);
        warehouse.addProduct(product5);

        // Product before modification
        Product product3BeforeModification = warehouse.getProductsArr().get(1);

        warehouse.modifyProduct(2, "Mascara", Categories.health, 5);


        assertThat(warehouse).isNotNull();
        assertThat(warehouse.getProductsArr().get(1).name()).isEqualTo("Mascara");
        assertThat(warehouse.getProductsArr().get(1).category()).isEqualTo(Categories.health);

        // Check that the old product don't exist in the warehouse
        assertThat(warehouse.getProductsArr()).doesNotContain(product3BeforeModification);

        warehouse.modifyProduct(2, "Parfym", Categories.health, 10);
        Product productModified = warehouse.getProductsArr().get(1);

        // Check that it contains the new product
        assertThat(warehouse.getProductsArr().contains(productModified));
    }

    @Test
    void testCantModifyProductWithInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> warehouse.modifyProduct(2, " ", Categories.health, 4));

    }

    @Test
    void testCantModifyProductWithWithInvalidRating() {
        assertThrows(IllegalArgumentException.class, () -> warehouse.modifyProduct(2, "Parfym", Categories.health, 11));

    }


    @Test
    void testGetProductArr() {
        warehouse.addProduct(product1);
        warehouse.addProduct(product3);

        List<Product> productsReturned = warehouse.getProductsArr();

        assertNotNull(productsReturned);
        // List contains 2 products
        assertEquals(2, productsReturned.size());

        // Check if specific product is in the list
        assertTrue(productsReturned.contains(product1));
    }

    @Test
    void testGetProductBasedOnId() {

        warehouse.addProduct(product1);
        warehouse.addProduct(product5);

        List<Product> idList = warehouse.getProductBasedOnId(4);
        assertThat(idList.contains(product5));
        assertThat(idList).containsOnly(product5);


        assertThrows(NoSuchElementException.class, () -> {
            warehouse.getProductBasedOnId(9);

        });

    }


    @Test
    void testGetProductsBasedOnCategory() {

        warehouse.addProduct(product1);
        warehouse.addProduct(product3);
        warehouse.addProduct(product5);
        warehouse.addProduct(product6);

        List<Product> productOrderBasedOnCategory = warehouse.getProductBasedOnCategory(Categories.sport);

        assertNotNull(productOrderBasedOnCategory);

        // Gets the names for to check the order
        List<String> order = productOrderBasedOnCategory.stream()
                .map(Product::name)
                .collect(Collectors.toList());

        // Check that the actual order matches the expected order
        assertEquals(List.of("Basket", "Football"), order);

        // Check that List only contains sport categories
        assertThat(productOrderBasedOnCategory).allMatch(product -> product.category() == Categories.sport);
    }

    @Test
    void testGetProductCreatedAfterDate() {
        LocalDateTime date = LocalDateTime.of(2023, 9, 3, 0, 0);
        // Products with different createdValues
        Product product1 = new Product(9, "Fotboll", Categories.sport, 5, date, LocalDateTime.now());
        Product product2 = new Product(2, "Handskar", Categories.sport, 5, date.plusDays(2), LocalDateTime.now());
        Product product3 = new Product(7, "Skor", Categories.sport, 5, date.plusDays(3), LocalDateTime.now());

        warehouse.addProduct(product1);
        warehouse.addProduct(product2);
        warehouse.addProduct(product3);

        List<Product> productsCreatedAfterSpecificDate = warehouse.getProductCreatedAfterDate(date.plusDays(1)
        );

        assertNotNull(productsCreatedAfterSpecificDate);

        // Check that the products that have been created after och equal will be returned.
        assertThat(productsCreatedAfterSpecificDate).containsOnly(product2, product3);


    }

    @Test
    void testGetProductThatBeenModified() {

        LocalDateTime date = LocalDateTime.of(2020, 9, 20, 20, 20);

        Product product1 = new Product(11, "Smink", Categories.health, 1, date, date);
        Product product2 = new Product(33, "Parfym", Categories.health, 5, date, date);
        Product product3 = new Product(19, "Wax", Categories.health, 5, date, date);

        warehouse.addProduct(product1);
        warehouse.addProduct(product2);
        warehouse.addProduct(product3);

        warehouse.modifyProduct(11, "Deo", Categories.health, 3);

        List<Product> modifiedProducts = warehouse.getProductThatBeenModified();

        assertNotNull(modifiedProducts);

        assertThat(modifiedProducts).doesNotContain(product2, product3);

        assertThat(modifiedProducts).containsExactly(new Product(11, "Deo", Categories.health, 3, date, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)));
    }

    @Test
    void testGetCategoriesWithProducts() {

        warehouse.addProduct(product1);
        warehouse.addProduct(product5);
        warehouse.addProduct(product6);

        List<Categories> categoriesThatHaveProducts = warehouse.getCategoriesWithProducts();

        assertNotNull(categoriesThatHaveProducts);

        assertThat(categoriesThatHaveProducts).contains(Categories.sport);

        assertThat(categoriesThatHaveProducts).doesNotContain(Categories.workout);

    }

    @Test
    void testGetHowManyProductsRelatedToCategory() {
        warehouse.addProduct(product1);
        warehouse.addProduct(product3);
        warehouse.addProduct(product5);
        warehouse.addProduct(product6);

        int productsRelatedToCategory = warehouse.getHowManyProductsRelatedToCategory(Categories.sport);
        int withNoProduct = warehouse.getHowManyProductsRelatedToCategory(Categories.workout);

        assertThat(productsRelatedToCategory).isEqualTo(2);
        assertThat(withNoProduct).isEqualTo(0);


    }

    @Test
    void testGetMap() {
        warehouse.addProduct(product1);
        warehouse.addProduct(product3);
        warehouse.addProduct(product5);
        warehouse.addProduct(product6);


        Map<String, Integer> map = warehouse.getMap();

        assertNotEquals(1, map.get("P"));
        assertEquals(2, map.get("B"));

    }

    @Test
    void testRecentMaxRatingDontExist(){

        // Will throw exception when no max rating don't have values or not max rating
        assertThrows(NoSuchElementException.class, () -> warehouse.getRecentMaxRating());

        Product product1 = new Product(7, "Football", Categories.sport, 9, LocalDateTime.now(), LocalDateTime.now());
        warehouse.addProduct(product1);
        assertThrows(NoSuchElementException.class, () -> warehouse.getRecentMaxRating());

    }

    @Test
    void testGetRecentMaxRating() {

        Product product1 = new Product(1, "Smink", Categories.health, 10, LocalDateTime.now(), LocalDateTime.now());
        Product product2 = new Product(2, "Wax", Categories.health, 10, LocalDateTime.now(), LocalDateTime.now());

        warehouse.addProduct(product1);
        warehouse.addProduct(product2);

        List<Product> recentMaxRating = warehouse.getRecentMaxRating();
        assertEquals(2, recentMaxRating.size());
        assertTrue(recentMaxRating.contains(product1) && recentMaxRating.contains(product2));

    }
}


