package com.empuje.grpc.web;

import com.empuje.grpc.ong.*;
import com.empuje.grpc.ong.Empty;
import org.springframework.stereotype.Service;

@Service
public class DonationGateway {

    private final DonationServiceGrpc.DonationServiceBlockingStub stub;

    public DonationGateway(DonationServiceGrpc.DonationServiceBlockingStub stub) {
        this.stub = stub;
    }

    private AuthContext auth(int actorId, Role role) {
        return AuthContext.newBuilder()
                .setActorId(actorId)
                .setActorRole(role)
                .build();
    }

    // ----- RPCs -----

    public ListDonationsResponse list() {
        return stub.listDonationItems(Empty.getDefaultInstance());
    }

    public ApiResponse create(Category categoria, String descripcion, int cantidad, int actorId, Role role) {
        return stub.createDonationItem(
                CreateDonationRequest.newBuilder()
                        .setAuth(auth(actorId, role))
                        .setCategoria(categoria)
                        .setDescripcion(descripcion == null ? "" : descripcion)
                        .setCantidad(cantidad)
                        .build()
        );
    }

    public ApiResponse update(int id, String descripcion, int cantidad, int actorId, Role role) {
        return stub.updateDonationItem(
                UpdateDonationRequest.newBuilder()
                        .setAuth(auth(actorId, role))
                        .setId(id)
                        .setDescripcion(descripcion == null ? "" : descripcion)
                        .setCantidad(cantidad)
                        .build()
        );
    }

    public ApiResponse softDelete(int id, int actorId, Role role) {
        return stub.softDeleteDonationItem(
                SoftDeleteDonationRequest.newBuilder()
                        .setAuth(auth(actorId, role))
                        .setId(id)
                        .build()
        );
    }
}
