package com.shoresea.webfluxdemo;

import com.shoresea.webfluxdemo.handler.ProductHandler;
import com.shoresea.webfluxdemo.model.Product;
import com.shoresea.webfluxdemo.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

// import reactor.core.publisher.Mono;

@SpringBootApplication
public class WebFluxDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebFluxDemoApplication.class, args);
    }

    @Bean
    CommandLineRunner init(ReactiveMongoOperations operations, ProductRepository productRepository) {
        return args -> {
            Flux<Product> initialProductsFlux = Flux.just(
                    new Product(null, "Cafe Latte", 2.99),
                    new Product(null, "Black Tea", 2.49),
                    new Product(null, "Green Tea", 1.99))
                    .flatMap(productRepository::save);

            initialProductsFlux
                    .thenMany(productRepository.findAll())
                    .subscribe(System.out::println);
/*
             // You can use the below commented code if you wish to test on an actual MongoDB instance instead of an embedded one.
             operations.collectionExists(Product.class)
                    .flatMap(exists -> exists ? operations.dropCollection(Product.class) : Mono.just(false))
                    .thenMany(v -> operations.createCollection(Product.class))
                    .thenMany(initialProductsFlux)
                    .thenMany(productRepository.findAll())
                    .subscribe(System.out::println);
 */

        };
    }


    @Bean
    RouterFunction<ServerResponse> routes(ProductHandler productHandler) {
        /**
         * Chained Method of defining Routes
         */
//        return route(GET("/products").and(accept(MediaType.APPLICATION_JSON)), productHandler::getAllProducts)
//                .andRoute(POST("/products").and(contentType(MediaType.APPLICATION_JSON)), productHandler::saveProduct)
//                .andRoute(DELETE("/products").and(accept(MediaType.APPLICATION_JSON)), productHandler::deleteAllProducts)
//                .andRoute(GET("/products/events").and(accept(MediaType.TEXT_EVENT_STREAM)), productHandler::getProductEvents)
//                .andRoute(GET("/products/{id}").and(accept(MediaType.APPLICATION_JSON)), productHandler::getProduct)
//                .andRoute(PUT("/products/{id}").and(contentType(MediaType.TEXT_EVENT_STREAM)), productHandler::updateProduct)
//                .andRoute(DELETE("/products/{id}").and(accept(MediaType.APPLICATION_JSON)), productHandler::deleteProduct);

        return nest(path("products"),
                nest(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON)).or(accept(TEXT_EVENT_STREAM)),
                        route(GET("/"), productHandler::getAllProducts)
                                .andRoute(method(HttpMethod.POST), productHandler::saveProduct)
                                .andRoute(DELETE("/"), productHandler::deleteAllProducts)
                                .andRoute(GET("/events"), productHandler::getProductEvents)
                                .andNest(path("/{id}"),
                                        route(method(HttpMethod.GET), productHandler::getProduct)
                                                .andRoute(PUT("/"), productHandler::updateProduct)
                                                .andRoute(DELETE("/"), productHandler::deleteProduct)
                                )

                )
        );

    }
}
