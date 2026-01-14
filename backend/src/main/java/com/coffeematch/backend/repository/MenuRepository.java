package com.coffeematch.backend.repository;

import com.coffeematch.backend.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
}
