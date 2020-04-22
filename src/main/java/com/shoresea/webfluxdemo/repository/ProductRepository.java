package com.shoresea.webfluxdemo.repository;

import com.shoresea.webfluxdemo.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {

    Flux<Product> findByName(String name);

    // Flux<Product> findByNameOrderByPrice(Publisher<String> name);
}
