package com.listme.repository;

import com.listme.model.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IOrcamento extends JpaRepository<Orcamento, Long> {
    List<Orcamento> findByClienteId(Long clienteId);
    List<Orcamento> findByFuncionarioIdUsuario(Integer funcionarioId); // Se o ID em Usuario for Integer
}