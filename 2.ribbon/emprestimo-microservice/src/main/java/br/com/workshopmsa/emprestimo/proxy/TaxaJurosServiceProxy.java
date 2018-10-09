package br.com.workshopmsa.emprestimo.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import br.com.workshopmsa.emprestimo.domain.EmprestimoModel;

@FeignClient(name = "taxa-juros-microservice", url = "http://localhost:8000")
public interface TaxaJurosServiceProxy {

	@GetMapping("taxasjuros/{idproduto}")
	public EmprestimoModel getTaxaJuros(@PathVariable("idproduto") Long idProduto) throws Exception;
}
