package org.laboration3;

import org.laboration3.entities.Categories;
import org.laboration3.entities.Product;
import org.laboration3.service.Warehouse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class Laboration3 {
    public static void main(String[] args) throws InterruptedException {
        Warehouse warehouse = getMockedWarehouse();


        // Example to show get map function
        Map<String, Integer> mapExample = warehouse.getMap();

        for (Map.Entry<String, Integer> p : mapExample.entrySet()) {
            System.out.println("Nyckel: " + p.getKey() + " : " + "Värde " + p.getValue());
        }

        // Added a sleep to check method returns the product that have been modified as excepted
        TimeUnit.SECONDS.sleep(1);
        warehouse.modifyProduct(7, "Basket", Categories.sport, 2);

        List<Product> products = warehouse.getProductThatBeenModified();


        for (Product product : products) {
            System.out.println(product.toString());
        }
    }

    private static Warehouse getMockedWarehouse() {
        Warehouse warehouse = new Warehouse();

        Product product2 = Product.createProduct(
                2,
                "Hårblekning",
                Categories.health,
                2
        );
        Product product3 = Product.createProduct(
                7,
                "Knäskydd",
                Categories.clothes,
                5
        );
        Product product4 = Product.createProduct(
                10,
                "Byxa",
                Categories.clothes,
                10
        );

        warehouse.addProduct(product2);
        warehouse.addProduct(product3);
        warehouse.addProduct(product4);
        return warehouse;
    }
}


