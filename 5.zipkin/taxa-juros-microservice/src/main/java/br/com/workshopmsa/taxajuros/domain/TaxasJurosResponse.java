package br.com.workshopmsa.taxajuros.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaxasJurosResponse {
	
	private List<TaxaJurosModel> listaTaxasJuros;
	private int porta;
}
