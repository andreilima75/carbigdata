package br.com.carbigdata.ocorrencias_reactive.adapter.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitQueueConfig {

    @Bean
    public DirectExchange ocorrenciasExchange() {
        return new DirectExchange("ocorrencias.exchange", true, false);
    }

    @Bean
    public DirectExchange ocorrenciasDLX() {
        return new DirectExchange("ocorrencias.dlx", true, false);
    }

    @Bean
    public Queue ocorrenciasRecebidasQueue() {
        return QueueBuilder.durable("ocorrencias.recebidas")
                .withArgument("x-dead-letter-exchange", "ocorrencias.dlx")
                .withArgument("x-dead-letter-routing-key", "ocorrencias.recebidas.dlq")
                .build();
    }

    @Bean
    public Queue ocorrenciasProcessadasQueue() {
        return QueueBuilder.durable("ocorrencias.processadas")
                .withArgument("x-dead-letter-exchange", "ocorrencias.dlx")
                .withArgument("x-dead-letter-routing-key", "ocorrencias.processadas.dlq")
                .build();
    }

    @Bean
    public Queue ocorrenciasRecebidasDLQ() {
        return new Queue("ocorrencias.recebidas.dlq", true);
    }

    @Bean
    public Queue ocorrenciasProcessadasDLQ() {
        return new Queue("ocorrencias.processadas.dlq", true);
    }

    @Bean
    public Binding bindingRecebidas() {
        return BindingBuilder.bind(ocorrenciasRecebidasQueue())
                .to(ocorrenciasExchange())
                .with("ocorrencias.recebidas.key");
    }

    @Bean
    public Binding bindingProcessadas() {
        return BindingBuilder.bind(ocorrenciasProcessadasQueue())
                .to(ocorrenciasExchange())
                .with("ocorrencias.processadas.key");
    }

    @Bean
    public Binding bindingRecebidasDLQ() {
        return BindingBuilder.bind(ocorrenciasRecebidasDLQ())
                .to(ocorrenciasDLX())
                .with("ocorrencias.recebidas.dlq");
    }

    @Bean
    public Binding bindingProcessadasDLQ() {
        return BindingBuilder.bind(ocorrenciasProcessadasDLQ())
                .to(ocorrenciasDLX())
                .with("ocorrencias.processadas.dlq");
    }
}