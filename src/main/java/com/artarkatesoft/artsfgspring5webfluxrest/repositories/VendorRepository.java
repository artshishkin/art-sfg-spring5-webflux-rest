package com.artarkatesoft.artsfgspring5webfluxrest.repositories;

import com.artarkatesoft.artsfgspring5webfluxrest.domain.Vendor;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface VendorRepository extends ReactiveMongoRepository<Vendor, String> {
}
