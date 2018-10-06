package br.com.workshopmsa.emprestimo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class EmprestimoMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmprestimoMicroserviceApplication.class, args);
	}
}
