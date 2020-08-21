package com.artarkatesoft.artsfgspring5webfluxrest.domain;

import lombok.Data;

import java.util.List;

@Data
public class CategoryList {
    private List<Category> categories;
}
