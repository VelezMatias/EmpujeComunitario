FROM python:3.10-slim

WORKDIR /app

RUN apt-get update && apt-get install -y protobuf-compiler && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY ../Proto/ong.proto .
RUN python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. ong.proto

COPY . .

EXPOSE 50051

CMD ["python", "-u", "server.py"]
