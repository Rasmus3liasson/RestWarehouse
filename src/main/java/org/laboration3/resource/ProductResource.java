package org.laboration3.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.laboration3.entities.Product;
import org.laboration3.service.Warehouse;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Path("/products")
public class
ProductResource {

    @Inject
    private Warehouse warehouse;

    private final ObjectMapper objectMapper;

    public ProductResource() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProducts() throws JsonProcessingException {

        List<Product> productsArr = warehouse.getProductsArr();

        Collections.sort(productsArr, Comparator.comparing(Product::id));

        return Response.status(Response.Status.ACCEPTED)
                .type(MediaType.APPLICATION_JSON)
                .entity(objectMapper.writeValueAsString(productsArr))
                .build();

    }

    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductsWithQuery(
            @QueryParam("start") @DefaultValue("1") int start,
            @QueryParam("end") int end
    ) throws JsonProcessingException {

        Collections.sort(warehouse.getProductsArr(), Comparator.comparing(org.laboration3.entities.Product::id));

        int total = warehouse.getProductsArr().size();

        start = Math.min(start, total);
        end = Math.min(end, total);
        List<org.laboration3.entities.Product> paginatedProducts = new ArrayList<>(warehouse.getProductsArr().subList(start - 1, end));

        if (start <= 0 || end < start) {
            String errorMessage = "Ej giltigt start värde";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + errorMessage + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.ok(objectMapper.writeValueAsString(paginatedProducts), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductsWithPagination(
            @QueryParam("size") @DefaultValue("-1") int size,
            @QueryParam("page") @DefaultValue("-1") int page
    ) throws JsonProcessingException {
        List<Product> allProducts = warehouse.getProductsArr();

        if (allProducts.isEmpty()) {
            String errorMessage = "Inga produkter finns";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + errorMessage + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        int total = allProducts.size();
        int start;
        int end;

        if (page <= 0) {

            start = 0;
            end = total;
            page = 1;
        } else {
            size = (size == -1) ? 10 : size;
            start = (page - 1) * size;
            end = Math.min(start + size, total);
        }

        List<Product> paginatedProducts = new ArrayList<>(allProducts.subList(start, end));

        Collections.sort(paginatedProducts, Comparator.comparing(Product::id));

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("size", paginatedProducts.size());

        Map<String, Object> products = new HashMap<>();
        products.put("products", paginatedProducts);
        products.put("pagination", pagination);

        return Response.ok(objectMapper.writeValueAsString(products), MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProduct(@Valid org.laboration3.entities.Product product) throws JsonProcessingException {
        org.laboration3.entities.Product newProduct = new org.laboration3.entities.Product(
                product.id(),
                product.name(),
                product.category(),
                product.rating(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        warehouse.addProduct(newProduct);
        return Response.status(Response.Status.CREATED)
                .type(MediaType.APPLICATION_JSON)
                .entity(objectMapper.writeValueAsString(newProduct))
                .build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductById(@PathParam("id") int id) {

        for (org.laboration3.entities.Product product : warehouse.getProductsArr()) {
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
        List<org.laboration3.entities.Product> categoryProduct = warehouse.getProductsArr().stream()
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