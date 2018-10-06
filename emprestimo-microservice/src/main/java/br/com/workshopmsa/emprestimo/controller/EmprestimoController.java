package br.com.workshopmsa.emprestimo.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.workshopmsa.emprestimo.domain.EmprestimoModel;
import br.com.workshopmsa.emprestimo.proxy.TaxaJurosServiceProxy;

@RestController
@RequestMapping("emprestimos")
public class EmprestimoController {

	@Autowired
	private TaxaJurosServiceProxy taxaJurosServiceProxy;
	
	@GetMapping("/calculo/{idproduto}/{valor}/{parcelas}")
	public EmprestimoModel calculoEmprestimo(
			@PathVariable("idproduto") Long idProduto,
			@PathVariable("valor") BigDecimal valorSolicitado,
			@PathVariable("parcelas") int parcelas) throws Exception {
		
		// Realiza a chamada ao endpoint do outro microserviço e obtém a resposta
		EmprestimoModel response = taxaJurosServiceProxy.getTaxaJuros(idProduto);
		
		// TODO
		return EmprestimoModel.
				builder()
				.build();
	}
}
