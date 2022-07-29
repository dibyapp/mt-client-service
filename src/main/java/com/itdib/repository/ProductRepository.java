package com.itdib.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.itdib.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
	

}