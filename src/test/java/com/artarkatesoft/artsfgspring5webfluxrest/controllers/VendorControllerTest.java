package com.artarkatesoft.artsfgspring5webfluxrest.controllers;

import com.artarkatesoft.artsfgspring5webfluxrest.domain.Vendor;
import com.artarkatesoft.artsfgspring5webfluxrest.repositories.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.IntStream;

import static com.artarkatesoft.artsfgspring5webfluxrest.controllers.VendorController.BASE_URL;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ExtendWith(MockitoExtension.class)
class VendorControllerTest {

    private static final int SIZE = 4;

    WebTestClient webTestClient;

    @Mock
    VendorRepository vendorRepository;

    @InjectMocks
    VendorController vendorController;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(vendorController).build();

    }

    @Test
    void list() {

        //given
        List<Vendor> stubVendorList = IntStream
                .rangeClosed(1, SIZE).mapToObj(this::createStubVendor).collect(toList());
        given(vendorRepository.findAll()).willReturn(Flux.fromIterable(stubVendorList));

        //when
        webTestClient.get().uri(BASE_URL)
                .accept(APPLICATION_JSON)
                .exchange()

                //then
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Vendor.class)
                .hasSize(SIZE)
                .value(vendors -> assertThat(vendors).isEqualTo(stubVendorList));
        then(vendorRepository).should().findAll();
    }

    @Test
    void findById() {
        //given
        Vendor stubVendor = Vendor.builder().id("foo").firstName("bar").lastName("buzz").build();
        given(vendorRepository.findById(anyString())).willReturn(Mono.just(stubVendor));
        //when
        webTestClient.get().uri(BASE_URL + "/{id}", "someId")
                .exchange()
                //then
                .expectStatus().isOk()
                .expectBody(Vendor.class)
                .isEqualTo(stubVendor);

        then(vendorRepository).should().findById(eq("someId"));
    }

    @Test
    void createVendor_single() {
        //given
        Vendor vendorToSave = createStubVendor(1);
        given(vendorRepository.saveAll(any(Publisher.class))).willReturn(Flux.just(vendorToSave));
        //when
        FluxExchangeResult<Vendor> result = webTestClient
                .post()
                .uri(BASE_URL)
                .contentType(APPLICATION_JSON)
                .bodyValue(vendorToSave)
                .exchange()
                //then
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .returnResult(Vendor.class);

        Flux<Vendor> responseBody = result.getResponseBody();
        StepVerifier.create(responseBody)
                .expectNext(vendorToSave)
                .verifyComplete();
        then(vendorRepository).should().saveAll(any(Publisher.class));
    }

    @Test
    void createVendor_multi() {
        //given
        List<Vendor> vendorList = IntStream.rangeClosed(1, 3).mapToObj(this::createStubVendor).collect(toList());
        given(vendorRepository.saveAll(any(Publisher.class))).willReturn(Flux.fromIterable(vendorList));
        //when
        FluxExchangeResult<Vendor> result = webTestClient
                .post()
                .uri(BASE_URL)
                .contentType(APPLICATION_JSON)
                .body(Flux.fromIterable(vendorList), Vendor.class)
                .exchange()
                //then
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .returnResult(Vendor.class);

        Flux<Vendor> responseBody = result.getResponseBody();
        StepVerifier.create(responseBody)
                .expectNextCount(3)
                .verifyComplete();
        then(vendorRepository).should().saveAll(any(Publisher.class));
    }

    @Test
    void updateVendorUsingPut_whenPresent() {
        //given
        Vendor vendorToUpdate = Vendor.builder().firstName("foo").lastName("bar").build();
        Vendor stubVendor = Vendor.builder().id("someId").firstName("foo").lastName("bar").build();
        given(vendorRepository.findById(anyString())).willReturn(Mono.just(stubVendor));
        given(vendorRepository.save(any(Vendor.class))).willReturn(Mono.just(stubVendor));
        //when
        webTestClient.put().uri(BASE_URL + "/{id}", "someId")
                .bodyValue(vendorToUpdate)
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(Vendor.class)
                .isEqualTo(stubVendor);
        then(vendorRepository).should().findById(eq("someId"));
        then(vendorRepository).should().save(eq(stubVendor));
    }

    @Test
    void updateVendorFirstNameUsingPatch_whenPresent() {
        //given
        Vendor stubVendor = Vendor.builder().id("someId").firstName("foo").lastName("bar").build();
        Vendor updatedVendor = Vendor.builder().id("someId").firstName("Art").lastName("bar").build();
        given(vendorRepository.findById(anyString())).willReturn(Mono.just(stubVendor));
        given(vendorRepository.save(any(Vendor.class))).willReturn(Mono.just(updatedVendor));
        //when
        webTestClient.patch().uri(BASE_URL + "/{id}", "someId")
                .contentType(APPLICATION_JSON)
                .bodyValue("{\"firstName\":\"Art\"}")
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(Vendor.class)
                .isEqualTo(stubVendor);
        then(vendorRepository).should().findById(eq("someId"));
        then(vendorRepository).should().save(eq(updatedVendor));
    }

    @Test
    void updateVendorUsingPut_whenAbsent() {
        //given
        Vendor vendorToUpdate = Vendor.builder().firstName("foo").lastName("bar").build();
        Vendor stubVendor = Vendor.builder().id("someId").firstName("foo").lastName("bar").build();
        given(vendorRepository.findById(anyString())).willReturn(Mono.empty());
        //when
        Flux<Object> responseBody = webTestClient.put().uri(BASE_URL + "/{id}", "someId")
                .bodyValue(vendorToUpdate)
                .exchange()
                //then
                .expectStatus().is5xxServerError()
                .returnResult(Object.class)
                .getResponseBody();
        responseBody.subscribe(System.out::println);
//        responseBody.subscribe();
        then(vendorRepository).should().findById(eq("someId"));
        then(vendorRepository).should(never()).save(any());
    }

    @Test
    void updateVendorUsingPatch_whenAbsent() {
        //given
        Vendor vendorToUpdate = Vendor.builder().firstName("foo").lastName("bar").build();
        Vendor stubVendor = Vendor.builder().id("someId").firstName("foo").lastName("bar").build();
        given(vendorRepository.findById(anyString())).willReturn(Mono.empty());
        //when
        Flux<Object> responseBody = webTestClient.patch().uri(BASE_URL + "/{id}", "someId")
                .bodyValue(vendorToUpdate)
                .exchange()
                //then
                .expectStatus().is5xxServerError()
                .returnResult(Object.class)
                .getResponseBody();
        responseBody.subscribe(System.out::println);

        then(vendorRepository).should().findById(eq("someId"));
        then(vendorRepository).should(never()).save(any());
    }

    private Vendor createStubVendor(int stubId) {
        return Vendor.builder()
                .id("id" + stubId)
                .firstName("First" + stubId)
                .lastName("Last" + stubId)
                .build();
    }
}
