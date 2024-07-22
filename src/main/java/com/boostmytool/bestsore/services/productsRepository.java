package com.boostmytool.bestsore.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.boostmytool.bestsore.models.Product;

public interface productsRepository extends JpaRepository<Product,Integer> {

}
