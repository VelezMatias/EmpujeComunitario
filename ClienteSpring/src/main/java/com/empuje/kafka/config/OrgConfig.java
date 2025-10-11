package com.empuje.kafka.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrgConfig {
  private final int orgId;
  public OrgConfig(@Value("${ORG_ID:42}") int orgId) { this.orgId = orgId; }
  public int getOrgId() { return orgId; }
}