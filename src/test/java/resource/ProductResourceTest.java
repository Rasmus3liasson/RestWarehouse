package resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.laboration3.entities.Categories;
import org.laboration3.entities.Product;
import org.laboration3.service.Warehouse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


public class ProductResourceTest {

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());


    // Method for making a representation of object with both List and singel products
    private List<Product> objectRepresentation(Response response) throws JsonProcessingException {
        String jsonRes = (String) response.getEntity();
        try {
            // List
            JavaType productType = objectMapper.getTypeFactory().constructCollectionType(List.class, Product.class);
            List<Product> products = objectMapper.readValue(jsonRes, productType);
            return products;
        } catch (JsonProcessingException e) {
            // single product
            Product product = objectMapper.readValue(jsonRes, Product.class);
            return Collections.singletonList(product);
        }

    }

    @InjectMocks
    private org.laboration3.resource.ProductResource productResource;

    @Mock
    private Warehouse warehouse;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void getProducts() throws Exception {
        when(warehouse.getProductsArr()).thenReturn(MockedProducts());

        Response response = productResource.getProducts();
        assertThat(Response.Status.ACCEPTED.getStatusCode()).isEqualTo(response.getStatus());

        assertThat(response.getEntity()).isInstanceOf(String.class);

        List<Product> products = objectRepresentation(response);


        assertThat(products).isNotEmpty();
        assertThat(products).isSortedAccordingTo(Comparator.comparing(Product::id));
        assertThat(products).hasSize(4);
        assertThat(products.get(0).name()).isEqualTo("Produkt1");
    }

    @Test
    public void getProductsWithQuery() throws JsonProcessingException {
        when(warehouse.getProductsArr()).thenReturn(MockedProducts());

        Response response = productResource.getProductsWithQuery(1, 3);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        List<Product> products = objectRepresentation(response);

        assertThat(products).isNotEmpty();
        assertThat(products).size().isEqualTo(3);
        assertThat(products.get(2).id()).isEqualTo(3);
    }


    @Test
    public void getProductsWithPagination() throws JsonProcessingException {

        when(warehouse.getProductsArr()).thenReturn(MockedProducts());


        Response response = productResource.getProductsWithPagination(3, 1);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // Read JSON data
        JsonNode jsonRes = objectMapper.readTree((String) response.getEntity());

        int productsTotal = jsonRes.get("products").size();
        int sizePagination = jsonRes.get("pagination").get("size").asInt();

        assertThat(productsTotal).isEqualTo(3);
        assertThat(productsTotal).isEqualTo(sizePagination);

    }


    @Test
    public void getProductById() throws JsonProcessingException {

        when(warehouse.getProductsArr()).thenReturn(MockedProducts());

        Response response = productResource.getProductById(2);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        List<Product> products = objectRepresentation(response);

        assertThat(products.get(0).id()).isEqualTo(2);
        assertThat(products).hasSize(1);
    }

    @Test
    public void getProductsByCategory() throws JsonProcessingException {

        when(warehouse.getProductsArr()).thenReturn(MockedProducts());

        Response response = productResource.getProductsByCategory("health");
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        List<Product> products = objectRepresentation(response);

        assertThat(products).hasSize(3);

        for (Product product : products) {
            assertThat(product.category()).isEqualTo(Categories.health);
        }
    }

    @Test
    public void createProduct() throws JsonProcessingException {
        // Create the new product
        Product newProduct = new Product(6, "Ny Produkt", Categories.sport, 7, LocalDateTime.now(), LocalDateTime.now());

        // Manually adding the product
        List<Product> mockedProducts = MockedProducts();
        mockedProducts.add(newProduct);
        when(warehouse.getProductsArr()).thenReturn(mockedProducts);

        Response response = productResource.createProduct(newProduct);


        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        // Read Json data
        Product createdProduct = objectMapper.readValue((String) response.getEntity(), Product.class);

        assertThat(createdProduct).isEqualToIgnoringGivenFields(newProduct, "createdDate", "lastModifiedDate");

        assertThat(mockedProducts.contains(createdProduct));
    }


    private static List<Product> MockedProducts() {

        List<Product> mockedProducts = new ArrayList<>();
        mockedProducts.add(new Product(1, "Produkt1", Categories.health, 4, LocalDateTime.now(), LocalDateTime.now()));
        mockedProducts.add(new Product(2, "Produkt2", Categories.health, 7, LocalDateTime.now(), LocalDateTime.now()));
        mockedProducts.add(new Product(3, "Produkt3", Categories.health, 5, LocalDateTime.now(), LocalDateTime.now()));
        mockedProducts.add(new Product(4, "Produkt4", Categories.sport, 5, LocalDateTime.now(), LocalDateTime.now()));

        return mockedProducts;
    }


}
