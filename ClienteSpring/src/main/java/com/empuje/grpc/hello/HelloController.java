package com.empuje.grpc.hello;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

import com.empuje.grpc.hello.HelloServiceGrpc;
import com.empuje.grpc.hello.HelloRequest;
import com.empuje.grpc.hello.HelloResponse;

@RestController
@RequestMapping("/api")
public class HelloController {

    private final HelloServiceGrpc.HelloServiceBlockingStub stub;

    // Inyectamos el stub configurado en GrpcConfig
    public HelloController(HelloServiceGrpc.HelloServiceBlockingStub stub) {
        this.stub = stub;
    }

    @GetMapping("/hello")
    public Map<String, String> hello(@RequestParam(defaultValue = "mundo") String name) {
        // Construimos la request gRPC
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .build();

        // Llamada gRPC al servidor Python
        HelloResponse response = stub.sayHello(request);

        // Devolvemos un JSON simple
        return Map.of("message", response.getMessage());
    }
}
