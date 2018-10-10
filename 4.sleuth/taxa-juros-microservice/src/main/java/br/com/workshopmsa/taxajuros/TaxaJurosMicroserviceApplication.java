package br.com.workshopmsa.taxajuros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TaxaJurosMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaxaJurosMicroserviceApplication.class, args);
	}
}
