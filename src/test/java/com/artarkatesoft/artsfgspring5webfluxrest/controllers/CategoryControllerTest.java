package com.artarkatesoft.artsfgspring5webfluxrest.controllers;

import com.artarkatesoft.artsfgspring5webfluxrest.domain.Category;
import com.artarkatesoft.artsfgspring5webfluxrest.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.IntStream;

import static com.artarkatesoft.artsfgspring5webfluxrest.controllers.CategoryController.BASE_URL;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.http.MediaType.APPLICATION_JSON;

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
                .collect(toList());
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

    @Test
    void createCategory_single() {
        //given
        Category categoryToSave = Category.builder().id("myId").name("myName").build();
        given(categoryRepository.saveAll(any(Publisher.class))).willReturn(Flux.just(categoryToSave));

        //when
        webTestClient
                .post()
                .uri(BASE_URL)
                .contentType(APPLICATION_JSON)
                .bodyValue(categoryToSave)
                .exchange()
                //then
                .expectStatus().isCreated()
                .expectBodyList(Category.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0)).isEqualTo(categoryToSave));
        then(categoryRepository).should().saveAll(any(Publisher.class));
    }

    @Test
    void createCategory_multiple() {
        //given
        List<Category> stubCategories = IntStream.rangeClosed(1, SIZE).mapToObj(this::createStubCategory).collect(toList());
        given(categoryRepository.saveAll(any(Publisher.class))).willReturn(Flux.fromIterable(stubCategories));

        //when
        webTestClient
                .post()
                .uri(BASE_URL)
                .contentType(APPLICATION_JSON)
                .bodyValue(stubCategories)
                .exchange()
                //then
                .expectStatus().isCreated()
                .expectBodyList(Category.class)
                .hasSize(SIZE)
                .isEqualTo(stubCategories);
        then(categoryRepository).should().saveAll(any(Publisher.class));
    }

    @Test
    void updateCategoryUsingPut_whenPresent() {
        //given
        Category repoCategory = Category.builder().id("someId").name("Foo").build();
        Category categoryToUpdate = Category.builder().id("someId").name("Bar").build();
        given(categoryRepository.findById(anyString())).willReturn(Mono.just(repoCategory));
        given(categoryRepository.save(any(Category.class))).willReturn(Mono.just(categoryToUpdate));

        //when
        webTestClient.put().uri(BASE_URL + "/{id}", "someId")
                .contentType(APPLICATION_JSON)
                .bodyValue(categoryToUpdate)
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(Category.class)
                .isEqualTo(categoryToUpdate);
        then(categoryRepository).should().findById(eq("someId"));
        then(categoryRepository).should().save(eq(categoryToUpdate));
    }

    @Test
    void updateCategoryUsingPut_whenAbsent() {
        //given
        Category categoryToUpdate = Category.builder().id("someId").name("Bar").build();
        given(categoryRepository.findById(anyString())).willReturn(Mono.empty());

        //when
        webTestClient.put().uri(BASE_URL + "/{id}", "someId")
                .contentType(APPLICATION_JSON)
                .bodyValue(categoryToUpdate)
                .exchange()
                //then
                .expectStatus().is5xxServerError();
        then(categoryRepository).should().findById(eq("someId"));
        then(categoryRepository).should(never()).save(any());
    }


    @Test
    void updateCategoryNameUsingPatch_whenPresent() {
        //given
        Category repoCategory = Category.builder().id("someId").name("Foo").build();
        Category categoryToUpdate = Category.builder().id("someId").name("Art").build();
        given(categoryRepository.findById(anyString())).willReturn(Mono.just(repoCategory));
        given(categoryRepository.save(any(Category.class))).willReturn(Mono.just(categoryToUpdate));

        //when
        webTestClient.patch().uri(BASE_URL + "/{id}", "someId")
                .contentType(APPLICATION_JSON)
                .bodyValue("{\"name\":\"Art\"}")
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(Category.class)
                .isEqualTo(categoryToUpdate);
        then(categoryRepository).should().findById(eq("someId"));
        then(categoryRepository).should().save(eq(categoryToUpdate));
    }


    @Test
    void updateCategoryUsingPatch_whenAbsent() {
        //given
        Category categoryToUpdate = Category.builder().id("someId").name("Bar").build();
        given(categoryRepository.findById(anyString())).willReturn(Mono.empty());

        //when
        webTestClient.patch().uri(BASE_URL + "/{id}", "someId")
                .contentType(APPLICATION_JSON)
                .bodyValue(categoryToUpdate)
                .exchange()
                //then
                .expectStatus().is5xxServerError();
        then(categoryRepository).should().findById(eq("someId"));
        then(categoryRepository).should(never()).save(any());
    }


    private Category createStubCategory(int stubId) {
        return Category.builder()
                .id("id" + stubId)
                .name("name" + stubId)
                .build();
    }
}
