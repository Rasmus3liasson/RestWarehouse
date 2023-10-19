package resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.spi.Dispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.laboration3.entities.Categories;
import org.laboration3.entities.Product;
import org.laboration3.resource.utils.ObjectMapperConvertDate;
import org.laboration3.resource.api.ProductResource;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;


public class ProductResourceTest {


    private ObjectMapper objectMapper = ObjectMapperConvertDate.configureObjectMapper();

    private Dispatcher dispatcher;


    // Method for making a representation of object with both List and singel products
    private List<Product> objectRepresentation(MockHttpResponse res) throws JsonProcessingException, UnsupportedEncodingException {
        String jsonRes = res.getContentAsString();
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
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        dispatcher = MockDispatcherFactory.createDispatcher();
        dispatcher.getRegistry().addSingletonResource(productResource);
        dispatcher.getProviderFactory().registerProvider(ObjectMapperConvertDate.class);
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
    public void productsAvailable() throws URISyntaxException, JsonProcessingException, UnsupportedEncodingException {
        when(warehouse.getProductsArr()).thenReturn(MockedProducts());

        MockHttpRequest req = MockHttpRequest.get("/products");
        MockHttpResponse res = new MockHttpResponse();

        dispatcher.invoke(req, res);

        assertThat(res.getStatus()).isEqualTo(202);

        List<Product> products = objectRepresentation(res);

        assertThat(products).isNotEmpty();
        assertThat(products.get(0).id()).isEqualTo(1);
        assertThat(products).isNotEmpty();
        assertThat(products).isSortedAccordingTo(Comparator.comparing(Product::id));
        assertThat(products).hasSize(4);
        assertThat(products.get(0).name()).isEqualTo("Produkt1");
    }


    @Test
    public void wrongIdParameter() throws URISyntaxException {
        when(warehouse.getProductBasedOnId(anyInt())).thenReturn(Collections.emptyList());

        MockHttpRequest req = MockHttpRequest.get("/products/9");
        MockHttpResponse res = new MockHttpResponse();


        dispatcher.invoke(req, res);

        assertThat(res.getStatus()).isEqualTo(404);
        assertThrows(NotFoundException.class, () -> productResource.getProductById(2));
    }


    @Test
    public void productWithId() throws URISyntaxException, UnsupportedEncodingException, JsonProcessingException {
        when(warehouse.getProductBasedOnId(2)).thenReturn(Collections.singletonList(MockedProducts().get(1)));

        MockHttpRequest req = MockHttpRequest.get("/products/2");
        MockHttpResponse res = new MockHttpResponse();

        dispatcher.invoke(req, res);
        assertThat(res.getStatus()).isEqualTo(200);


        List<Product> product = objectRepresentation(res);
        assertThat(product).isNotNull();
        assertThat(product.size()).isEqualTo(1);
        assertThat(product.size()).isLessThan(2);
        assertThat(product.get(0).id()).isEqualTo(2);
        assertThat(product.get(0).name()).isEqualTo("Produkt2");
        assertThat(product.get(0).category()).isEqualTo(Categories.health);
        assertThat(product.get(0).rating()).isEqualTo(7);


    }

    @Test
    public void usingInvalidTotalQueries() throws Exception {

        when(warehouse.getProductsArr()).thenReturn(Collections.emptyList());


        MockHttpRequest req = MockHttpRequest.get("/products/filter/size?start=3&end=2");
        MockHttpResponse res = new MockHttpResponse();
        dispatcher.invoke(req, res);


        assertThat(res.getStatus()).isEqualTo(400);
        assertThrows(BadRequestException.class, () -> productResource.getProductsWithQuery(3, 2));
    }


    @Test
    public void usingQueryTotalProducts() throws URISyntaxException, UnsupportedEncodingException, JsonProcessingException {
        when(warehouse.getProductsArr()).thenReturn(MockedProducts());

        MockHttpRequest req = MockHttpRequest.get("/products/filter/size?start=2&&end=4");
        MockHttpResponse res = new MockHttpResponse();

        dispatcher.invoke(req, res);
        assertThat(res.getStatus()).isEqualTo(200);

        List<Product> products = objectRepresentation(res);

        assertThat(products.size()).isEqualTo(3);

        assertThat(products.get(0).id()).isEqualTo(2);
        assertThat(products.get(2).id()).isEqualTo(4);
        assertThat(products).extracting(Product::id).doesNotContain(1);

    }

    @Test
    public void usingWrongPaginationQueries() throws Exception {
        when(warehouse.getProductsArr()).thenReturn(Collections.emptyList());

        MockHttpRequest req = MockHttpRequest.get("/products/pagination?size=10&page=1");
        MockHttpResponse res = new MockHttpResponse();

        dispatcher.invoke(req, res);


        assertThat(res.getStatus()).isEqualTo(400);

        assertThrows(BadRequestException.class, () -> productResource.getProductsWithPagination(2, 3));
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
    public void usingWrongCategoryQuery() throws Exception {
        when(warehouse.getProductBasedOnCategory(Categories.sport)).thenReturn(Collections.emptyList());

        MockHttpRequest req = MockHttpRequest.get("/products/category/majs");
        MockHttpResponse res = new MockHttpResponse();

        dispatcher.invoke(req, res);


        assertThat(res.getStatus()).isEqualTo(404);

        assertThrows(NotFoundException.class, () -> productResource.getProductsByCategory(String.valueOf(Categories.health)));
    }


    @Test
    public void usingCategoryFiltering() throws URISyntaxException, UnsupportedEncodingException, JsonProcessingException {

        when(warehouse.getProductsArr()).thenReturn(MockedProducts());
        MockHttpRequest req = MockHttpRequest.get("/products/category/health");
        MockHttpResponse res = new MockHttpResponse();
        dispatcher.invoke(req, res);

        assertThat(res.getStatus()).isEqualTo(200);
        assertThat(res).isNotNull();

        List<Product> convertProducts = objectRepresentation(res);

        assertThat(convertProducts).isNotEmpty();
        assertThat(convertProducts.size()).isEqualTo(3);

        assertThat(convertProducts).extracting(Product::category)
                .containsOnly(Categories.health)
                .doesNotContain(Categories.sport);

    }

    @Test
    public void postProduct() throws Exception {
        List<Product> mockedProducts = MockedProducts();

        Product newProduct = new Product(8, "NyProdukt", Categories.sport, 5, LocalDateTime.now(), LocalDateTime.now());

        doAnswer(invoke -> {
            Product product = invoke.getArgument(0);
            mockedProducts.add(product);
            return null;
        }).when(warehouse).addProduct(any());

        when(warehouse.getProductsArr()).thenReturn(mockedProducts);

        MockHttpRequest req = MockHttpRequest.post("/products");
        req.contentType("application/json");
        req.content(objectMapper.writeValueAsString(newProduct).getBytes());
        MockHttpResponse res = new MockHttpResponse();

        dispatcher.invoke(req, res);
        assertEquals(201, res.getStatus());

        List<Product> productsArr = warehouse.getProductsArr();
        Product lastProduct = productsArr.get(productsArr.size() - 1);
        assertThat(warehouse.getProductsArr().size()).isEqualTo(5);
        assertThat(lastProduct).isEqualToIgnoringGivenFields(newProduct, "createdDate", "lastModifiedDate");
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
