package com.empuje.kafka;

public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String SOLICITUD_DONACIONES = "solicitud-donaciones";
    public static final String BAJA_SOLICITUD_DONACIONES = "baja-solicitud-donaciones";
    public static final String OFERTA_DONACIONES = "oferta-donaciones";
    public static final String EVENTOS_SOLIDARIOS = "eventos-solidarios";
    public static final String BAJA_EVENTO_SOLIDARIO = "baja-evento-solidario";

    public static String adhesionEvento(int orgIdOrganizador) {
        return "adhesion-evento." + orgIdOrganizador;
    }
    public static String transferenciaDonaciones(int orgId) {
        return "transferencia-donaciones." + orgId;
    }
}
