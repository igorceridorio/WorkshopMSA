package br.com.workshopmsa.taxajuros.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaxaJurosResponse {
	
	private TaxaJurosModel taxaJuros;
	private int porta;
}
