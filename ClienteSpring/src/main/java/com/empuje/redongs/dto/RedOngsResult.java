package com.empuje.redongs.dto;

import java.util.List;

public class RedOngsResult {
    private List<OrganizationDTO> organizations;
    private List<PresidentDTO> presidents;

    public RedOngsResult() {}

    public RedOngsResult(List<OrganizationDTO> organizations, List<PresidentDTO> presidents) {
        this.organizations = organizations;
        this.presidents = presidents;
    }

    public List<OrganizationDTO> getOrganizations() { return organizations; }
    public void setOrganizations(List<OrganizationDTO> organizations) { this.organizations = organizations; }

    public List<PresidentDTO> getPresidents() { return presidents; }
    public void setPresidents(List<PresidentDTO> presidents) { this.presidents = presidents; }
}
