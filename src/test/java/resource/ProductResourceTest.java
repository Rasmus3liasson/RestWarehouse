package resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.spi.Dispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.laboration3.entities.Categories;
import org.laboration3.entities.Product;
import org.laboration3.resource.ObjectMapperConvertDate;
import org.laboration3.resource.ProductResource;
import org.laboration3.service.Warehouse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


public class ProductResourceTest {


    private final ObjectMapper objectMapper = ObjectMapperConvertDate.configureObjectMapper();

    private Dispatcher dispatcher;


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
    private ProductResource productResource;

    @Mock
    private Warehouse warehouse;

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        MockitoAnnotations.initMocks(this);
        dispatcher = MockDispatcherFactory.createDispatcher();
        dispatcher.getRegistry().addSingletonResource(productResource);
    }

    @Test
    public void noProductsAvailable() throws URISyntaxException {
        when(warehouse.getProductsArr()).thenReturn(Collections.emptyList());

        MockHttpRequest req = MockHttpRequest.get("/products");
        MockHttpResponse res = new MockHttpResponse();

        dispatcher.invoke(req, res);

        assertThrows(NotFoundException.class, () -> productResource.getProducts());
        assertThat(res.getStatus()).isEqualTo(404);
        assertThat(res.getOutput()).isNotNull();
    }

    @Test
    public void productsAvailable() throws URISyntaxException {
        when(warehouse.getProductsArr()).thenReturn(MockedProducts());

        MockHttpRequest req = MockHttpRequest.get("/products");
        MockHttpResponse res = new MockHttpResponse();

        dispatcher.invoke(req, res);

        assertThat(res.getStatus()).isEqualTo(202);
        assertThat(res.getOutput()).isNotNull();

    }

    @Test
    public void productWithId() throws URISyntaxException, UnsupportedEncodingException, JsonProcessingException {
        when(warehouse.getProductBasedOnId(2)).thenReturn(Collections.singletonList(MockedProducts().get(1)));

        MockHttpRequest req = MockHttpRequest.get("/products/2");
        MockHttpResponse res = new MockHttpResponse();

        dispatcher.invoke(req, res);
        assertThat(res.getStatus()).isEqualTo(200);

        String product = res.getContentAsString();
        assertThat(product).isNotNull();

        Product convertProduct = objectMapper.readValue(product, Product.class);
        assertThat(convertProduct.id()).isEqualTo(2);
        assertThat(convertProduct.name()).isEqualTo("Produkt2");
        assertThat(convertProduct.category()).isEqualTo(Categories.health);
        assertThat(convertProduct.rating()).isEqualTo(7);
    }

    @Test
    public void usingQueryTotalProducts() throws URISyntaxException, UnsupportedEncodingException, JsonProcessingException {
        when(warehouse.getProductsArr()).thenReturn(MockedProducts());

        MockHttpRequest req = MockHttpRequest.get("/products/query?start=2&&end=4");
        MockHttpResponse res = new MockHttpResponse();

        dispatcher.invoke(req, res);
        assertThat(res.getStatus()).isEqualTo(200);

        String products = res.getContentAsString();
        assertThat(products).isNotNull();

        JavaType productType = objectMapper.getTypeFactory().constructCollectionType(List.class, Product.class);
        List<Product> convertProducts = objectMapper.readValue(products, productType);

        assertThat(convertProducts.size()).isEqualTo(3);

        assertThat(convertProducts.get(0).id()).isEqualTo(2);
        assertThat(convertProducts.get(2).id()).isEqualTo(4);
        assertThat(convertProducts).extracting(Product::id).doesNotContain(1);

    }

    @Test
    public void usingPagination() throws URISyntaxException, UnsupportedEncodingException, JsonProcessingException {
        when(warehouse.getProductsArr()).thenReturn(MockedProducts());

        MockHttpRequest req = MockHttpRequest.get("/products/pagination?page=1&size=2");
        MockHttpResponse res = new MockHttpResponse();

        dispatcher.invoke(req, res);

        assertThat(res.getStatus()).isEqualTo(200);

        String response = res.getContentAsString();
        JsonNode jsonRes = objectMapper.readTree(response);

        JsonNode pagination = jsonRes.get("pagination");
        assertThat(pagination).isNotNull();
        assertThat(pagination.get("size").asInt()).isEqualTo(2);
        assertThat(pagination.get("page").asInt()).isEqualTo(1);

        JsonNode products = jsonRes.get("products");
        assertThat(products).isNotNull();
        assertThat(products.size()).isEqualTo(2);
    }

    @Test
    public void usingCategoryFiltering() throws URISyntaxException, UnsupportedEncodingException, JsonProcessingException {

        when(warehouse.getProductsArr()).thenReturn(MockedProducts());
        MockHttpRequest req = MockHttpRequest.get("/products/category/health");
        MockHttpResponse res = new MockHttpResponse();
        dispatcher.invoke(req, res);

        assertThat(res.getStatus()).isEqualTo(200);

        String products = res.getContentAsString();
        assertThat(products).isNotNull();

        JavaType productType = objectMapper.getTypeFactory().constructCollectionType(List.class, Product.class);
        List<Product> convertProducts = objectMapper.readValue(products, productType);

        assertThat(convertProducts).isNotEmpty();
        assertThat(convertProducts.size()).isEqualTo(3);

        assertThat(convertProducts).extracting(Product::category)
                .containsOnly(Categories.health)
                .doesNotContain(Categories.sport);

    }


    // Testing methods
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
