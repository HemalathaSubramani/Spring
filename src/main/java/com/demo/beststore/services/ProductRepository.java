package com.demo.beststore.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.beststore.models.Product;

public interface ProductRepository extends JpaRepository<Product, Integer>{
	

}
