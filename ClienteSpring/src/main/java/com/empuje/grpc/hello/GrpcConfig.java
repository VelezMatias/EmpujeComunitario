package com.empuje.grpc.hello;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Imports que se generan automáticamente del .proto
// Después de "generateProto", estarán disponibles:
import com.empuje.grpc.hello.HelloServiceGrpc;

@Configuration
public class GrpcConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel managedChannel() {
        // Cliente apunta al server Python en localhost:50051
        // .usePlaintext() es clave si el server no usa TLS.
        return ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();
    }

    @Bean
    public HelloServiceGrpc.HelloServiceBlockingStub helloBlockingStub(ManagedChannel channel) {
        // Stub síncrono para llamadas simples.
        return HelloServiceGrpc.newBlockingStub(channel);
    }
}
