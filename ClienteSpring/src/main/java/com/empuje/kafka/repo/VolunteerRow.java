package com.empuje.repo;

public class VolunteerRow {
    private final int usuarioId;
    private final String nombre;
    private final String apellido;
    private final int participaciones;

    public VolunteerRow(int usuarioId, String nombre, String apellido, int participaciones) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.apellido = apellido;
        this.participaciones = participaciones;
    }
    public int getUsuarioId() { return usuarioId; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public int getParticipaciones() { return participaciones; }
}
