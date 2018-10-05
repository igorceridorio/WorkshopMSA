package br.com.workshopmsa.taxajuros.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.workshopmsa.taxajuros.database.repository.TaxaJurosRepository;
import br.com.workshopmsa.taxajuros.domain.TaxaJurosModel;
import br.com.workshopmsa.taxajuros.domain.TaxasJurosResponse;

@RestController
@RequestMapping("taxajuros")
public class TaxaJurosController {
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private TaxaJurosRepository taxaJurosRepository;
	
	@GetMapping("/")
	public TaxasJurosResponse getTaxasJuros() {
		
		List<TaxaJurosModel> listaTaxasJuros = new ArrayList<>();
		
		taxaJurosRepository.findAll().forEach(tx -> {
			listaTaxasJuros.add(TaxaJurosModel
					.builder()
					.idProduto(tx.getIdProduto())
					.jurosAm(tx.getJurosAm())
					.build());
		});
		
		return TaxasJurosResponse
				.builder()
				.listaTaxasJuros(listaTaxasJuros)
				.porta(Integer.parseInt(environment.getProperty("local.server.port")))
				.build();
	}
}
