package org.laboration3.resource.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptors;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.laboration3.Interceptor.Logging;
import org.laboration3.entities.Product;
import org.laboration3.resource.utils.ObjectMapperConvertDate;
import org.laboration3.service.Warehouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Path("/products")
@Interceptors(Logging.class)
public class
ProductResource {

    @Inject
    private Warehouse warehouse;

    private final ObjectMapper objectMapper;
    private final static Logger logger = LoggerFactory.getLogger(ProductResource.class);

    public ProductResource() {
        objectMapper = ObjectMapperConvertDate.configureObjectMapper();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProducts() throws JsonProcessingException {
        List<Product> productsArr = warehouse.getProductsArr();

        if (productsArr.isEmpty()) {
            throw new NotFoundException("Inga produkter finns tillgängliga");
        }

        Collections.sort(productsArr, Comparator.comparing(Product::id));

        return Response.status(Response.Status.ACCEPTED)
                .type(MediaType.APPLICATION_JSON)
                .entity(objectMapper.writeValueAsString(productsArr))
                .build();

    }

    @GET
    @Path("/filter/size")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductsWithQuery(
            @QueryParam("start") @DefaultValue("1") int start,
            @QueryParam("end") int end
    ) throws JsonProcessingException {

        List<Product> paginatedProducts = warehouse.getProductsArr()
                .stream()
                .filter(p -> p.id() >= start && p.id() <= end)
                .sorted(Comparator.comparing(Product::id))
                .toList();

        if (start <= 0 || end < start) {
            throw new BadRequestException("Ej giltigt start värde");
        } else if (paginatedProducts.isEmpty()) {
            throw new NotFoundException("Finns inga produkter med dessa id:n");
        }

        return Response.ok(objectMapper.writeValueAsString(paginatedProducts), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/pagination")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductsWithPagination(
            @QueryParam("size") @DefaultValue("-1") int size,
            @QueryParam("page") @DefaultValue("-1") int page
    ) throws JsonProcessingException {
        List<Product> allProducts = warehouse.getProductsArr();

        if (allProducts.isEmpty()) {
            throw new BadRequestException("Inga produkter finns");
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
    public Response createProduct(@Valid Product product) throws JsonProcessingException {
       Product newProduct = new org.laboration3.entities.Product(
                product.id(),
                product.name(),
                product.category(),
                product.rating(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        warehouse.addProduct(newProduct);
        logger.info("Produkt " + newProduct + " skapades.");
        return Response.status(Response.Status.CREATED)
                .type(MediaType.APPLICATION_JSON)
                .entity(objectMapper.writeValueAsString(newProduct))
                .build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductById(@PathParam("id") int id) throws JsonProcessingException {
        List<Product> productById = warehouse.getProductBasedOnId(id);

        if (productById.isEmpty()) {
            throw new NotFoundException("Finns ingen product med id: " + id);
        }

        return Response.ok(objectMapper.writeValueAsString(productById.get(0)), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/category/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductsByCategory(@PathParam("category") String category) throws JsonProcessingException {
        List<Product> categoryProduct = warehouse.getProductsArr().stream()
                .filter(p -> p.category().toString().trim().equals(category.trim()))
                .collect(Collectors.toList());

        if (categoryProduct.isEmpty()) {
            throw new NotFoundException("Inga produkter hittades i kategorin: " + category);
        }

        return Response.ok(objectMapper.writeValueAsString(categoryProduct), MediaType.APPLICATION_JSON).build();
    }

}