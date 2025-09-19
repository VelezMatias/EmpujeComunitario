package com.empuje.grpc.web;

import ong.*;
import ong.Empty;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;


@Service
public class EventGateway {

    private final EventServiceGrpc.EventServiceBlockingStub stub;

    public EventGateway(EventServiceGrpc.EventServiceBlockingStub stub) {
        this.stub = stub;
    }

    private AuthContext auth(int actorId, Role role) {
        return AuthContext.newBuilder()
                .setActorId(actorId)
                .setActorRole(role)
                .build();
    }

    public ApiResponse assignDonation(int actorId, Role role,
                                      int eventId, int donationId, int cantidad) {
        var req = AssignDonationToEventRequest.newBuilder()
                .setAuth(auth(actorId, role))
                .setEventId(eventId)
                .setDonationId(donationId)
                .setCantidad(cantidad)
                .build();
        return stub.assignDonationToEvent(req);
    }

    public ApiResponse removeDonation(int actorId, Role role,
                                      int eventId, int donationId) {
        var req = RemoveDonationFromEventRequest.newBuilder()
                .setAuth(auth(actorId, role))
                .setEventId(eventId)
                .setDonationId(donationId)
                .build();
        return stub.removeDonationFromEvent(req);
    }

    public List<EventDonationLink> listDonationsByEvent(int eventId) {
        var req = ListDonationsByEventRequest.newBuilder()
                .setEventId(eventId)
                .build();
        return stub.listDonationsByEvent(req).getItemsList();
    }


    // Helper: LocalDateTime (zona local) -> ISO-8601 UTC string con 'Z'
    private static String toIsoUtc(LocalDateTime local, ZoneId zone) {
    return local.atZone(zone).withZoneSameInstant(ZoneOffset.UTC)
                .toInstant()
                .toString(); // ISO-8601 con 'Z'
    }

    // ------ RPCs ------
    public ListEventsResponse listAll() {
        return stub.listEvents(Empty.getDefaultInstance());
    }

    public ApiResponse create(String nombre, String descripcion, String fechaHoraIsoUtc,
                          int actorId, Role role) {
        return stub.createEvent(
                CreateEventRequest.newBuilder()
                        .setAuth(auth(actorId, role))
                        .setNombre(nombre == null ? "" : nombre.trim())
                        .setDescripcion(descripcion == null ? "" : descripcion.trim())
                        .setFechaHora(fechaHoraIsoUtc)
                        .build()
        );
    }

    public ApiResponse createFromLocal(String nombre, String descripcion,
                                       LocalDateTime fechaLocal, ZoneId zonaLocal,
                                       int actorId, Role role) {
        return create(nombre, descripcion, toIsoUtc(fechaLocal, zonaLocal), actorId, role);
    }

    public ApiResponse update(int id, String nombre, String descripcion, String fechaHoraIsoUtc,
                          int actorId, Role role) {
        UpdateEventRequest.Builder b = UpdateEventRequest.newBuilder()
                .setAuth(auth(actorId, role))
                .setId(id);
        if (nombre != null) b.setNombre(nombre.trim());
        if (descripcion != null) b.setDescripcion(descripcion.trim());
        if (fechaHoraIsoUtc != null) b.setFechaHora(fechaHoraIsoUtc);
        return stub.updateEvent(b.build());
    }

    public ApiResponse updateFromLocal(int id, String nombre, String descripcion,
                                       LocalDateTime fechaLocal, ZoneId zonaLocal,
                                       int actorId, Role role) {
        String isoUtc = (fechaLocal != null) ? toIsoUtc(fechaLocal, zonaLocal) : null;
        return update(id, nombre, descripcion, isoUtc, actorId, role);
    }

    public ApiResponse delete(int id, int actorId, Role role) {
        return stub.deleteEvent(
                DeleteEventRequest.newBuilder()
                        .setAuth(auth(actorId, role))
                        .setId(id)
                        .build()
        );
    }

    public ApiResponse assignMember(int eventId, int userId, int actorId, Role role) {
        return stub.assignMember(
                AssignMemberRequest.newBuilder()
                        .setAuth(auth(actorId, role))
                        .setEventId(eventId)
                        .setUserId(userId)
                        .build()
        );
    }

    public ApiResponse removeMember(int eventId, int userId, int actorId, Role role) {
        return stub.removeMember(
                RemoveMemberRequest.newBuilder()
                        .setAuth(auth(actorId, role))
                        .setEventId(eventId)
                        .setUserId(userId)
                        .build()
        );
    }

    public ApiResponse registerDistribution(int eventId,
                                            java.util.List<Distribution> dist,
                                            int actorId, Role role) {
        return stub.registerDistribution(
                RegisterDistributionRequest.newBuilder()
                        .setAuth(auth(actorId, role))
                        .setEventId(eventId)
                        .addAllDist(dist)
                        .build()
        );
    }

    // Builder helper para Distribution
    public static Distribution dist(int donationItemId, int cantidad) {
        return Distribution.newBuilder()
                .setDonationItemId(donationItemId)
                .setCantidad(cantidad)
                .build();
    }







}