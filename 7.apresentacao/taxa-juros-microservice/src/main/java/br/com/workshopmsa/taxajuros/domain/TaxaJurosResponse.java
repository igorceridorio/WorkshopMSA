package br.com.workshopmsa.taxajuros.domain;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaxaJurosResponse {
	
	private Long idProduto;
	private BigDecimal jurosAm;
	private int porta;
}
