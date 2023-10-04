package resource;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.laboration3.entities.Categories;
import org.laboration3.service.Warehouse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

        List<org.laboration3.entities.Product> mockedProducts = new CopyOnWriteArrayList<>();
        mockedProducts.add(new org.laboration3.entities.Product(1, "Produkt1", Categories.health, 4, LocalDateTime.now(), LocalDateTime.now()));
        mockedProducts.add(new org.laboration3.entities.Product(2, "Produkt2", Categories.health, 7, LocalDateTime.now(), LocalDateTime.now()));

        // Mock warehouse
        when(warehouse.getProductsArr()).thenReturn(mockedProducts);


        Response response = productResource.getProducts();
        assertThat(Response.Status.ACCEPTED.getStatusCode()).isEqualTo(response.getStatus());

    }
}
