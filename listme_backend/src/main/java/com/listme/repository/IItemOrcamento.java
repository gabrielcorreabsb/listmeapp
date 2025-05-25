package com.listme.repository;

import com.listme.model.ItemOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IItemOrcamento extends JpaRepository<ItemOrcamento, Long> {
}