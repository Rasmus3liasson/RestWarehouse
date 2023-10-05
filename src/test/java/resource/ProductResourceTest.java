package resource;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


public class ProductResourceTest {

    // Method for making a representation of object
    private List<Product> objectRepresentation(Response response) throws JsonProcessingException {

        // To accepts Date
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        List<Product> res = objectMapper.readValue(
                objectMapper.writeValueAsString(response.getEntity()),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Product.class));
        return res;


    }

    @InjectMocks
    private org.laboration3.resource.ProductResource productResource;

    @Mock
    private Warehouse warehouse;
    private List<Product> mockedProducts = new ArrayList<>();


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void getProducts() throws Exception {
        mockedProducts.add(new Product(2, "Produkt2", Categories.health, 7, LocalDateTime.now(), LocalDateTime.now()));
        mockedProducts.add(new Product(1, "Produkt1", Categories.health, 4, LocalDateTime.now(), LocalDateTime.now()));

        // Return the mocked version
        when(warehouse.getProductsArr()).thenReturn(mockedProducts);

        Response response = productResource.getProducts();
        assertThat(Response.Status.ACCEPTED.getStatusCode()).isEqualTo(response.getStatus());

        assertThat(response.getEntity()).isInstanceOf(List.class);

        List<Product> products = objectRepresentation(response);

        assertThat(products).isNotEmpty();
        assertThat(products).isSortedAccordingTo(Comparator.comparing(Product::id));
        assertThat(products).size().isEqualTo(2);
        assertThat(products.get(0).name()).isEqualTo("Produkt1");
    }

    @Test
    public void getProductsWithQuery() throws JsonProcessingException {
        mockedProducts.add(new Product(1, "Produkt1", Categories.health, 4, LocalDateTime.now(), LocalDateTime.now()));
        mockedProducts.add(new Product(2, "Produkt2", Categories.health, 7, LocalDateTime.now(), LocalDateTime.now()));
        mockedProducts.add(new Product(5, "Produkt3", Categories.sport, 2, LocalDateTime.now(), LocalDateTime.now()));
        mockedProducts.add(new Product(7, "Produkt4", Categories.clothes, 2, LocalDateTime.now(), LocalDateTime.now()));

        when(warehouse.getProductsArr()).thenReturn(mockedProducts);

        Response response = productResource.getProductsWithQuery(1,3);

        List<Product> products = objectRepresentation(response);

        assertThat(products).isNotEmpty();
        assertThat(products).size().isEqualTo(3);
        assertThat(products.get(2).id()).isEqualTo(5);
    }

   /*@Test
    public void getProductsWithPagination() {
        // Create some sample products
        List<Product> mockedProducts = new ArrayList<>();
        mockedProducts.add(new Product(1, "Produkt1", Categories.health, 4, LocalDateTime.now(), LocalDateTime.now()));
        mockedProducts.add(new Product(2, "Produkt2", Categories.health, 7, LocalDateTime.now(), LocalDateTime.now()));
        mockedProducts.add(new Product(3, "Produkt3", Categories.health, 5, LocalDateTime.now(), LocalDateTime.now()));

        // Return the mocked version
        when(warehouse.getProductsArr()).thenReturn(mockedProducts);

        // Test with different pagination parameters
        Response response1 = productResource.getProductsWithPagination(2, 1);
        Response response2 = productResource.getProductsWithPagination(2, 2);
        Response response3 = productResource.getProductsWithPagination(1, 2);

        // Assert that the responses are successful
        assertThat(Response.Status.OK.getStatusCode()).isEqualTo(response1.getStatus());
        assertThat(Response.Status.OK.getStatusCode()).isEqualTo(response2.getStatus());
        assertThat(Response.Status.OK.getStatusCode()).isEqualTo(response3.getStatus());

        // Assert that the pagination information is as expected
        assertThat(response1.getEntity().get("page").asInt()).isEqualTo(1);
        assertThat(response1.get("pagination").get("size").asInt()).isEqualTo(2);

        assertThat(jsonResponse2.get("pagination").get("page").asInt()).isEqualTo(2);
        assertThat(jsonResponse2.get("pagination").get("size").asInt()).isEqualTo(1);

        assertThat(jsonResponse3.get("pagination").get("page").asInt()).isEqualTo(2);
        assertThat(jsonResponse3.get("pagination").get("size").asInt()).isEqualTo(2);
    }*/
}
