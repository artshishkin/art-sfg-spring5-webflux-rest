package com.artarkatesoft.artsfgspring5webfluxrest.repositories;

import com.artarkatesoft.artsfgspring5webfluxrest.domain.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoryRepository extends ReactiveMongoRepository<Category, String> {
}
