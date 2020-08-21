package com.artarkatesoft.artsfgspring5webfluxrest.controllers;

import com.artarkatesoft.artsfgspring5webfluxrest.domain.Vendor;
import com.artarkatesoft.artsfgspring5webfluxrest.repositories.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.IntStream;

import static com.artarkatesoft.artsfgspring5webfluxrest.controllers.VendorController.BASE_URL;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.MediaType.*;

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

    private Vendor createStubVendor(int stubId) {
        return Vendor.builder()
                .id("id" + stubId)
                .firstName("First" + stubId)
                .lastName("Last" + stubId)
                .build();
    }
}
