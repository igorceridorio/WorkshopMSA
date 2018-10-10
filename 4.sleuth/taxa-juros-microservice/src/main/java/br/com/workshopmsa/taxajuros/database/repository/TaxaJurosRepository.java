package br.com.workshopmsa.taxajuros.database.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import br.com.workshopmsa.taxajuros.database.domain.TaxaJuros;

public interface TaxaJurosRepository extends Repository<TaxaJuros, Long> {
	
	TaxaJuros findByIdProduto(Long idProduto);
	List<TaxaJuros> findAll();
}
