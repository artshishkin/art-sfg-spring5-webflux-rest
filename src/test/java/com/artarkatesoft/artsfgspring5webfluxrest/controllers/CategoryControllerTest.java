package com.artarkatesoft.artsfgspring5webfluxrest.controllers;

import com.artarkatesoft.artsfgspring5webfluxrest.domain.Category;
import com.artarkatesoft.artsfgspring5webfluxrest.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.artarkatesoft.artsfgspring5webfluxrest.controllers.CategoryController.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.MediaType.*;

@WebFluxTest(CategoryController.class)
class CategoryControllerTest {

    private static final int SIZE = 6;

    @MockBean
    CategoryRepository categoryRepository;

    @Autowired
    WebTestClient webTestClient;

    private List<Category> stubCategoryList;

    @BeforeEach
    void setUp() {
        stubCategoryList = IntStream.rangeClosed(1, SIZE)
                .mapToObj(this::createStubCategory)
                .collect(Collectors.toList());
    }

    @Test
    void list() {
        //given
        given(categoryRepository.findAll()).willReturn(Flux.fromIterable(stubCategoryList));

        //when
        webTestClient.get()
                .uri(BASE_URL)
                .accept(APPLICATION_JSON)
                .exchange()

                //then
                .expectStatus().isOk()
                .expectBodyList(Category.class)
                .hasSize(SIZE)
                .value(categories -> assertThat(categories).isEqualTo(stubCategoryList));
        then(categoryRepository).should().findAll();

    }

    @Test
    void getById() {
        //given
        Category stubCategory = Category.builder()
                .id("foo")
                .name("bar")
                .build();
        given(categoryRepository.findById(anyString())).willReturn(Mono.just(stubCategory));
        //when
        webTestClient.get().uri(BASE_URL + "/{id}", "someStubId")
                .accept(APPLICATION_JSON)
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(Category.class)
                .isEqualTo(stubCategory);
    }

    private Category createStubCategory(int stubId) {
        return Category.builder()
                .id("id" + stubId)
                .name("name" + stubId)
                .build();
    }
}
