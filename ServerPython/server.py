# server.py
# Servidor gRPC mínimo en Python que implementa HelloService.
# Buenas prácticas: clase servicer separada, puerto configurable y logs simples.

import os
from concurrent import futures
import grpc

# Stubs generados desde hello.proto
import hello_pb2
import hello_pb2_grpc


class HelloService(hello_pb2_grpc.HelloServiceServicer):
    """Implementación del servicio definido en hello.proto."""

    def SayHello(self, request, context):
        # request.name viene del mensaje HelloRequest
        message = f"Hola, {request.name}!"
        return hello_pb2.HelloResponse(message=message)


def serve():
    # Permite cambiar el puerto por variable de entorno si lo necesitás.
    port = os.getenv("GRPC_PORT", "50051")

    # Thread pool para manejar múltiples llamadas concurrentes.
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))

    # Registrar nuestro servicer en el server gRPC.
    hello_pb2_grpc.add_HelloServiceServicer_to_server(HelloService(), server)

    # Desarrollo local: sin TLS (plaintext). En producción: usar credenciales.
    server.add_insecure_port(f"[::]:{port}")

    print(f"[gRPC][Python] Escuchando en 0.0.0.0:{port} ...")
    server.start()
    server.wait_for_termination()


if __name__ == "__main__":
    serve()
