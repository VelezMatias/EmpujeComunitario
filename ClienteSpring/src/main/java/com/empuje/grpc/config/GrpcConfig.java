package com.empuje.grpc.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.empuje.grpc.users.UsersServiceGrpc;

@Configuration
public class GrpcConfig {

    @Value("${grpc.server.host}")
    private String grpcHost;

    @Value("${grpc.server.port}")
    private int grpcPort;

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel managedChannel() {
        return ManagedChannelBuilder
                .forAddress(grpcHost, grpcPort)
                .usePlaintext()
                .build();
    }

    @Bean
    public UsersServiceGrpc.UsersServiceBlockingStub usersBlockingStub(ManagedChannel channel) {
        return UsersServiceGrpc.newBlockingStub(channel);
    }
}
