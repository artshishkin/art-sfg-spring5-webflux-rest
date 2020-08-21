package com.artarkatesoft.artsfgspring5webfluxrest.controllers;

import com.artarkatesoft.artsfgspring5webfluxrest.domain.Category;
import com.artarkatesoft.artsfgspring5webfluxrest.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(CategoryController.BASE_URL)
public class CategoryController {

    public static final String BASE_URL = "/api/v1/categories";

    private final CategoryRepository categoryRepository;

    @GetMapping
    public Flux<Category> list() {
        return categoryRepository.findAll();
    }

    @GetMapping("{id}")
    public Mono<Category> getById(@PathVariable("id") String id) {
        return categoryRepository.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Flux<Category> createCategory(@RequestBody Publisher<Category> categoryPublisher) {
        return categoryRepository.saveAll(categoryPublisher);
    }
}
