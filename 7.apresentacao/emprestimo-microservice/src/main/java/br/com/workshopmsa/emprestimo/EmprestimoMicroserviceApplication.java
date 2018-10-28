package br.com.workshopmsa.emprestimo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import brave.sampler.Sampler;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@EnableHystrix
public class EmprestimoMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmprestimoMicroserviceApplication.class, args);
	}
	
	@Bean  
	public Sampler defaultSampler() {  
		return Sampler.ALWAYS_SAMPLE;  
	}
}
