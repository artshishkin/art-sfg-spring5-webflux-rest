package com.artarkatesoft.artsfgspring5webfluxrest.bootstrap;

import com.artarkatesoft.artsfgspring5webfluxrest.domain.CategoryList;
import com.artarkatesoft.artsfgspring5webfluxrest.domain.Vendor;
import com.artarkatesoft.artsfgspring5webfluxrest.repositories.CategoryRepository;
import com.artarkatesoft.artsfgspring5webfluxrest.repositories.VendorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class BootstrapData implements CommandLineRunner {

    private final VendorRepository vendorRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    @Value("classpath:examples/categories.json")
    Resource categoryResource;

    @Override
    public void run(String... args) throws Exception {
        CategoryList categoryList = objectMapper.readValue(categoryResource.getFile(), CategoryList.class);
        vendorRepository.count()
                .log("Vendors count")
                .filter(count -> count > 0)
                .switchIfEmpty(
                        vendorRepository.deleteAll()
                                .then(categoryRepository.deleteAll())
                                .thenMany(Flux.range(1, 5)
                                        .map(this::createStubVendor)
                                        .flatMap(vendorRepository::save))
                                .log("Vendor saved")
                                .thenMany(categoryRepository.saveAll(categoryList.getCategories()))
                                .log("Category saved")
                                .then(Mono.just(1L))
                )
                .subscribe();
    }

    private Vendor createStubVendor(int stubId) {
        return Vendor.builder()
                .firstName("First" + stubId)
                .lastName("Last" + stubId)
                .build();
    }
}
