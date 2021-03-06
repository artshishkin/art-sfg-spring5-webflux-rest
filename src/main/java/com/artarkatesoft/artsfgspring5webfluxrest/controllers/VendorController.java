package com.artarkatesoft.artsfgspring5webfluxrest.controllers;

import com.artarkatesoft.artsfgspring5webfluxrest.domain.Vendor;
import com.artarkatesoft.artsfgspring5webfluxrest.repositories.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.artarkatesoft.artsfgspring5webfluxrest.controllers.VendorController.BASE_URL;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping(BASE_URL)
@RequiredArgsConstructor
public class VendorController {
    public static final String BASE_URL = "/api/v1/vendors";

    private final VendorRepository vendorRepository;

    @GetMapping
    public Flux<Vendor> list() {
        return vendorRepository.findAll();
    }

    @GetMapping("{id}")
    public Mono<Vendor> findById(@PathVariable String id) {
        return vendorRepository.findById(id);
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public Flux<Vendor> createVendor(@RequestBody Publisher<Vendor> vendorStream) {
        return vendorRepository.saveAll(vendorStream);
    }

    @PutMapping("{id}")
    public Mono<Vendor> updateVendorUsingPut(@PathVariable String id, @RequestBody Mono<Vendor> vendorMono) {
        return vendorRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Vendor with id `" + id + "` NOT FOUND")))
                .then(vendorMono)
                .doOnNext(vendor -> vendor.setId(id))
                .flatMap(vendorRepository::save);
    }

    @PatchMapping("{id}")
    public Mono<Vendor> updateVendorUsingPatch(@PathVariable String id, @RequestBody Mono<Vendor> vendorMono) {
        return vendorRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Vendor with id `" + id + "` NOT FOUND")))
                .zipWith(vendorMono)
                .map(tuple2 -> {
                    Vendor vendorRepo = tuple2.getT1();
                    Vendor vendorWithNewFields = tuple2.getT2();
                    String newFirstName = vendorWithNewFields.getFirstName();
                    String newLastName = vendorWithNewFields.getLastName();
                    if (newFirstName != null) vendorRepo.setFirstName(newFirstName);
                    if (newLastName != null) vendorRepo.setLastName(newLastName);
                    return vendorRepo;
                })
                .flatMap(vendorRepository::save);
    }


}
