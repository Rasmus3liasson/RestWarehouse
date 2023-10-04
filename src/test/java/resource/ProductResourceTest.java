package resource;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ProductResourceTest {

    @InjectMocks
    private org.laboration3.resource.ProductResource productResource;

    @Mock
    private Warehouse warehouse;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getProducts() {

        List<Product> mockedProducts = new ArrayList<>();
        mockedProducts.add(new Product(1, "Produkt1", Categories.health, 4, LocalDateTime.now(), LocalDateTime.now()));
        mockedProducts.add(new Product(2, "Produkt2", Categories.health, 7, LocalDateTime.now(), LocalDateTime.now()));

        // Return the mocked version
        when(warehouse.getProductsArr()).thenReturn(mockedProducts);

        Response response = productResource.getProducts();
        assertThat(Response.Status.ACCEPTED.getStatusCode()).isEqualTo(response.getStatus());

        assertThat(response.getEntity()).isInstanceOf(List.class);
        List<Product> products = (List<Product>) response.getEntity();


        assertThat(products).isNotEmpty();
        assertThat(products).size().isEqualTo(2);


    }
}
