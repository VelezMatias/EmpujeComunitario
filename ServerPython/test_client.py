import grpc
import users_pb2, users_pb2_grpc

def run():
    # Conectarse al server Python en el puerto 50051
    channel = grpc.insecure_channel("localhost:50051")
    stub = users_pb2_grpc.UsersServiceStub(channel)

    # Probar login con un usuario de tu BD dump (ej: coord1 / coord123)
    response = stub.Login(users_pb2.LoginRequest(
        username="coord1",
        password="coord123"
    ))
    print("Login response:", response)

if __name__ == "__main__":
    run()
