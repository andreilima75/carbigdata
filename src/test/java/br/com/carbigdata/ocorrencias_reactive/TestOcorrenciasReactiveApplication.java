package br.com.carbigdata.ocorrencias_reactive;

import org.springframework.boot.SpringApplication;

public class TestOcorrenciasReactiveApplication {

	public static void main(String[] args) {
		SpringApplication.from(OcorrenciasReactiveApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
