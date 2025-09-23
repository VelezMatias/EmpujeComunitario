package com.empuje.ui;

import com.empuje.grpc.ong.ListExternasRequest;
import com.empuje.grpc.ong.ListEventosExternosResponse;
import com.empuje.grpc.ong.ListSolicitudesExternasResponse;
import com.empuje.grpc.ong.OngServiceGrpc;
import io.grpc.ManagedChannel;
import org.springframework.stereotype.Service;

@Service
public class InboxService {

    private final OngServiceGrpc.OngServiceBlockingStub stub;

    public InboxService(ManagedChannel channel) {
        this.stub = OngServiceGrpc.newBlockingStub(channel);
    }

    public ListSolicitudesExternasResponse listarSolicitudes(int page, int size) {
        ListExternasRequest req = ListExternasRequest.newBuilder()
                .setPage(page).setPageSize(size).build();
        return stub.listSolicitudesExternas(req);
    }

    public ListEventosExternosResponse listarEventos(int page, int size) {
        ListExternasRequest req = ListExternasRequest.newBuilder()
                .setPage(page).setPageSize(size).build();
        return stub.listEventosExternos(req);
    }
}
