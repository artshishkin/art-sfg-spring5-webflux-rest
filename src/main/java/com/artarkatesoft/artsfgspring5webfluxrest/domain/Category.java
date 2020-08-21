package com.artarkatesoft.artsfgspring5webfluxrest.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {
    @Id
    private String id;
    private String name;
}
