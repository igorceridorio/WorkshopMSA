package br.com.workshopmsa.emprestimo.domain;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmprestimoModel {
	
	private int parcelas;
	private BigDecimal valorSolicitado;
	private BigDecimal valorParcela;
	private BigDecimal valorFinal;
	
	// Valores que serão preenchidos pela chamada ao microsserviço de taxa de juros
	private Long idProduto;
	private BigDecimal jurosAm;
	private int porta;
}
