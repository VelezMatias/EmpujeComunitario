package com.empuje.redongs.dto;

public class PresidentDTO {
    private String id;
    private String name;
    private String address;
    private String phone;
    private String organizationId;

    public PresidentDTO() {}

    public PresidentDTO(String id, String name, String address, String phone, String organizationId) {
        this.id = id; this.name = name; this.address = address; this.phone = phone; this.organizationId = organizationId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
}
