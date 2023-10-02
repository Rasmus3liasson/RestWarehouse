package org.laboration3.resource;

import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.laboration3.entities.Categories;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Path("/products")
public class
Product {

    private List<org.laboration3.entities.Product> productsArr = new ArrayList<>();

    public Product() {
        productsArr.add(new org.laboration3.entities.Product(1, "Produkt1", Categories.health, 4, LocalDateTime.now(), LocalDateTime.now()));
        productsArr.add(new org.laboration3.entities.Product(2, "Produkt2", Categories.health, 7, LocalDateTime.now(), LocalDateTime.now()));
        productsArr.add(new org.laboration3.entities.Product(3, "Produkt3", Categories.sport, 7, LocalDateTime.now(), LocalDateTime.now()));


    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProducts() {

        Collections.sort(productsArr, Comparator.comparing(org.laboration3.entities.Product::id));

        return Response.status(Response.Status.ACCEPTED).type(MediaType.APPLICATION_JSON)
                .entity(productsArr)
                .build();

    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductsWithQuery(
            @QueryParam("start") @DefaultValue("1") int start,
            @QueryParam("end") @DefaultValue("10") int end
    ) {
        if (start <= 0 || end < start) {
            String errorMessage = "Ej giltigt start värde";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + errorMessage + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        Collections.sort(productsArr, Comparator.comparing(org.laboration3.entities.Product::id));

        int total = productsArr.size();

        start = Math.min(start, total);
        end = Math.min(end, total);

        List<org.laboration3.entities.Product> paginatedProducts = new ArrayList<>(productsArr.subList(start - 1, end));

        return Response.ok(paginatedProducts, MediaType.APPLICATION_JSON).build();
    }




    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProduct(@Valid org.laboration3.entities.Product product) {

        org.laboration3.entities.Product newProduct = new org.laboration3.entities.Product(product.id(),
                product.name(),
                product.category(),
                product.rating(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        productsArr.add(newProduct);
        System.out.println(productsArr);
        return Response.status(Response.Status.CREATED).type(MediaType.APPLICATION_JSON)
                .entity(newProduct)
                .build();
    }
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductById(@PathParam("id") int id) {

        for (org.laboration3.entities.Product product : productsArr) {
            if (product.id() == id) {
                return Response.ok(product, MediaType.APPLICATION_JSON).build();
            }
        }

        String errorMessage = "Finns ingen product med id: " + id;
        return Response.status(Response.Status.NOT_FOUND).entity("{\"error\": \"" + errorMessage + "\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Path("/category/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductsByCategory(@PathParam("category") String category) {
        List<org.laboration3.entities.Product> categoryProduct = productsArr.stream()
                .filter(p -> p.category().toString().trim().equals(category.trim()))
                .collect(Collectors.toList());

        if (categoryProduct.isEmpty()) {
            String errorMessage = "Inga produkter hittades i kategorin: " + category;
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"" + errorMessage + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        return Response.ok(categoryProduct, MediaType.APPLICATION_JSON).build();
    }

}