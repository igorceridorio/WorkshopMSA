package br.com.workshopmsa.taxajuros.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.workshopmsa.taxajuros.database.domain.TaxaJuros;
import br.com.workshopmsa.taxajuros.database.repository.TaxaJurosRepository;
import br.com.workshopmsa.taxajuros.domain.TaxaJurosModel;
import br.com.workshopmsa.taxajuros.domain.TaxaJurosResponse;
import br.com.workshopmsa.taxajuros.domain.TaxasJurosResponse;

@RestController
@RequestMapping("taxasjuros")
public class TaxaJurosController {
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private TaxaJurosRepository taxaJurosRepository;
	
	@GetMapping("")
	public TaxasJurosResponse getTaxasJuros() throws Exception {
		
		List<TaxaJurosModel> listaTaxasJurosModel = new ArrayList<>();
		List<TaxaJuros> taxasJuros = taxaJurosRepository.findAll();
		
		if (taxasJuros == null) {
			throw new Exception("Os dados dos produtos não foram localizados");
		}
		
		taxasJuros.forEach(tx -> {
			listaTaxasJurosModel.add(TaxaJurosModel
					.builder()
					.idProduto(tx.getIdProduto())
					.jurosAm(tx.getJurosAm())
					.build());
		});
		
		return TaxasJurosResponse
				.builder()
				.listaTaxasJuros(listaTaxasJurosModel)
				.porta(Integer.parseInt(environment.getProperty("local.server.port")))
				.build();
	}
	
	@GetMapping("/{idproduto}")
	public TaxaJurosResponse getTaxaJuros(@PathVariable("idproduto") Long idProduto) throws Exception {

		TaxaJuros taxaJuros = taxaJurosRepository.findByIdProduto(idProduto);
		
		if (taxaJuros == null) {
			throw new Exception("Produto não localizado na base de dados");
		}
		
		return TaxaJurosResponse
				.builder()
				.idProduto(taxaJuros.getIdProduto())
				.jurosAm(taxaJuros.getJurosAm())
				.porta(Integer.parseInt(environment.getProperty("local.server.port")))
				.build();
	}
}
