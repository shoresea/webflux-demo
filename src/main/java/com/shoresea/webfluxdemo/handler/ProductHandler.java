package com.shoresea.webfluxdemo.handler;

import com.shoresea.webfluxdemo.model.Product;
import com.shoresea.webfluxdemo.model.ProductEvent;
import com.shoresea.webfluxdemo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class ProductHandler {
    private ProductRepository productRepository;

    @Autowired
    public ProductHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Mono<ServerResponse> getAllProducts(ServerRequest request) {
        Flux<Product> products = productRepository.findAll();
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(products, Product.class);
    }

    public Mono<ServerResponse> getProduct(ServerRequest request) {
        Mono<Product> product = productRepository.findById(request.pathVariable("id"));
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return product.flatMap(p ->
                ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(p)))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> saveProduct(ServerRequest request) {
        Mono<Product> product = request.bodyToMono(Product.class);

        return product.flatMap(p ->
                ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(productRepository.save(p), Product.class));
    }

    public Mono<ServerResponse> updateProduct(ServerRequest request) {
        Mono<Product> existingProduct = productRepository.findById(request.pathVariable("id"));
        Mono<Product> product = request.bodyToMono(Product.class);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return product.zipWith(existingProduct,
                (p, ep) -> new Product(ep.getId(), p.getName(), p.getPrice()))
                .flatMap(p ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(productRepository.save(p), Product.class))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        Mono<Product> existingProduct = productRepository.findById(request.pathVariable("id"));
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return existingProduct
                .flatMap(product ->
                        ServerResponse.ok().build(productRepository.delete(product))
                )
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> deleteAllProducts(ServerRequest request) {
        return ServerResponse.ok().build(productRepository.deleteAll());
    }

    public Mono<ServerResponse> getProductEvents(ServerRequest request) {
        Flux<ProductEvent> eventFlux = Flux.interval(Duration.ofSeconds(1))
                .map(val -> new ProductEvent(val, "Product Event"));
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(eventFlux, ProductEvent.class);
    }

}
