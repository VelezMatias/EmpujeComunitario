package com.empuje.grpc.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.empuje.grpc.ong.UserServiceGrpc;
import com.empuje.grpc.ong.EventServiceGrpc;
import com.empuje.grpc.ong.DonationServiceGrpc;
import com.empuje.grpc.ong.OngServiceGrpc;
import com.empuje.grpc.ong.UserServiceGrpc; 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.empuje.grpc.ong.DonationServiceGrpc;

@Configuration
public class GrpcConfig {

    @Bean
    public ManagedChannel grpcChannel(
            @Value("${grpc.host:localhost}") String host,
            @Value("${grpc.port:50051}") int port) {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userStub(ManagedChannel ch) {
        return UserServiceGrpc.newBlockingStub(ch);
    }


    @Bean
    public EventServiceGrpc.EventServiceBlockingStub eventStub(ManagedChannel ch) {
        return EventServiceGrpc.newBlockingStub(ch);
    }


    @Bean
    public DonationServiceGrpc.DonationServiceBlockingStub donationStub(ManagedChannel ch) {
        return DonationServiceGrpc.newBlockingStub(ch);
    }

    @Bean
    public OngServiceGrpc.OngServiceBlockingStub ongStub(ManagedChannel ch) {
        return OngServiceGrpc.newBlockingStub(ch);
    }
}
