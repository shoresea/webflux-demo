package com.shoresea.webfluxdemo.controller;

import com.shoresea.webfluxdemo.model.Product;
import com.shoresea.webfluxdemo.model.ProductEvent;
import com.shoresea.webfluxdemo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Try using @WebFluxTest(ProductController.class) annotation instead of @SpringBootTest
 * if you don't want to load the entire Spring Application Context while running the test.
 * (Test Slices)
 *
 * @WebFluxTest is similar to @WebMvcTest
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
class ProductControllerTest {

    private WebTestClient productTestClient;
    private List<Product> expectedProductList;

    @Autowired
    private ProductRepository productRepository;

//    @Autowired
//    private ApplicationContext context;

    @BeforeEach
    void beforeEach() {
        this.productTestClient = WebTestClient
                //.bindToApplicationContext(context)    // Another option to bind controller
                .bindToController(new ProductController(productRepository))
                .configureClient()
                .baseUrl("/annotation/products")
                .build();

        this.expectedProductList = productRepository.findAll().collectList().block();
    }

    @Test
    void getAllProducts() {
        productTestClient
                .get()
                .uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class)
                .isEqualTo(expectedProductList);
    }

    @Test
    void testProductWithInvalidIdNotFound() {
        productTestClient.get()
                .uri("/aaa")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testGetProduct() {
        Product expectedProduct = expectedProductList.get(0);
        productTestClient.get()
                .uri("/{id}", expectedProduct.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .isEqualTo(expectedProduct);
    }

    @Test
    void testGetProductEvents() {
        ProductEvent expectedEvent = new ProductEvent(0L, "Product Event");
        var fluxExchangeResult = productTestClient.get()
                .uri("/events")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(ProductEvent.class);

        StepVerifier.create(fluxExchangeResult.getResponseBody())
                .expectNext(expectedEvent)
                .expectNextCount(2)
                .consumeNextWith(productEvent -> assertEquals(Long.valueOf(3), productEvent.getEventId()))
                .thenCancel()
                .verify();
    }
}