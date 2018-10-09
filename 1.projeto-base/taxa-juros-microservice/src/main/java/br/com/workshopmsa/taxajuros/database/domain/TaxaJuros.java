package br.com.workshopmsa.taxajuros.database.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class TaxaJuros {

	@Id
	@Column(name = "id_taxa_juros")
	private Long idTaxaJuros;
	
	@Column(name = "id_produto", unique = true)
	private Long idProduto;
	
	@Column(name = "juros_am")
	private BigDecimal jurosAm;
}
