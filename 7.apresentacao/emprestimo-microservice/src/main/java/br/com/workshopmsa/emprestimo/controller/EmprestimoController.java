package br.com.workshopmsa.emprestimo.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private Logger logger = LoggerFactory.getLogger(this.getClass()); 
	
	@GetMapping("/calculo/{idproduto}/{valor}/{parcelas}")
	public EmprestimoModel calculoEmprestimo(
			@PathVariable("idproduto") Long idProduto,
			@PathVariable("valor") BigDecimal valorSolicitado,
			@PathVariable("parcelas") int parcelas) throws Exception {
		
		// Realiza a chamada ao endpoint do outro microsserviço e obtém a resposta
		EmprestimoModel emprestimo = taxaJurosServiceProxy.getTaxaJuros(idProduto);
		
		calculaValores(valorSolicitado, parcelas, emprestimo);
		
		logger.info("{}", emprestimo);
		return emprestimo;
	}

	private void calculaValores(BigDecimal valorSolicitado, int parcelas, EmprestimoModel emprestimo) {
		
		// Realiza a lógica do empréstimo baseado nos valores recebidos do microsserviço de taxa de juros
		emprestimo.setParcelas(parcelas);
		emprestimo.setValorSolicitado(valorSolicitado);
		emprestimo.setValorParcela(valorSolicitado.divide(BigDecimal.valueOf(parcelas), RoundingMode.HALF_UP));
		
		emprestimo.setValorParcela(emprestimo.getValorParcela()
				.multiply(emprestimo.getJurosAm().divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP))
				.add(emprestimo.getValorParcela()));
		
		emprestimo.setValorFinal(emprestimo.getValorParcela()
				.multiply(BigDecimal.valueOf(emprestimo.getParcelas())));
	}
	
//	public EmprestimoModel calculoEmprestimoFallback(
//		@PathVariable("idproduto") Long idProduto,
//		@PathVariable("valor") BigDecimal valorSolicitado,
//		@PathVariable("parcelas") int parcelas) throws Exception {
//		
//		logger.info("Erro ao tentar obter as informações de taxa de juros, utilizando valores default...");
//		EmprestimoModel emprestimo = new EmprestimoModel();
//		
//		// Atribuindo os valores que seriam retornados pelo microsserviço
//		emprestimo.setIdProduto(99L);
//		emprestimo.setJurosAm(new BigDecimal("0.99"));
//		emprestimo.setPorta(9999);
//		
//		calculaValores(valorSolicitado, parcelas, emprestimo);
//		
//		logger.info("{}", emprestimo);
//		return emprestimo;
//	}
}
