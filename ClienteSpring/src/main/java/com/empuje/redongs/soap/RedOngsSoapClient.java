package com.empuje.redongs.soap;

import com.empuje.redongs.dto.OrganizationDTO;
import com.empuje.redongs.dto.PresidentDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

@Component
public class RedOngsSoapClient {

    @Value("${redongs.endpoint}")
    private String endpoint;

    @Value("${redongs.auth.grupo}")
    private String grupo;

    @Value("${redongs.auth.clave}")
    private String clave;

    @Value("${redongs.timeout.ms:10000}")
    private int timeoutMs;

    private RestTemplate rest;

    @PostConstruct
    public void init() {
        // RestTemplate simple (sin dependencias nuevas). Timeout por propiedades del JDK (connect/read).
        this.rest = new RestTemplate();
    }

    public List<OrganizationDTO> listAssociations(List<String> orgIds) {
        String envelope = buildEnvelope("list_associations", orgIds);
        String xml = callSoap(envelope, "list_associations");
        return parseOrganizations(xml);
    }

    public List<PresidentDTO> listPresidents(List<String> orgIds) {
        String envelope = buildEnvelope("list_presidents", orgIds);
        String xml = callSoap(envelope, "list_presidents");
        return parsePresidents(xml);
    }

    // ========= Helpers =========

    private String buildEnvelope(String op, List<String> orgIds) {
        String items = orgIds.stream()
                .map(id -> "<tns:string>" + escapeXml(id.trim()) + "</tns:string>")
                .collect(Collectors.joining("\n        "));

        return """
            <?xml version="1.0" encoding="utf-8"?>
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                              xmlns:auth="auth.headers"
                              xmlns:tns="soap.backend">
              <soapenv:Header>
                <auth:Auth>
                  <auth:Grupo>%s</auth:Grupo>
                  <auth:Clave>%s</auth:Clave>
                </auth:Auth>
              </soapenv:Header>
              <soapenv:Body>
                <tns:%s>
                  <tns:org_ids>
                    %s
                  </tns:org_ids>
                </tns:%s>
              </soapenv:Body>
            </soapenv:Envelope>
            """.formatted(escapeXml(grupo), escapeXml(clave), op, items, op);
    }

    private String callSoap(String body, String soapAction) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        headers.add("SOAPAction", soapAction);

        HttpEntity<String> req = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = rest.exchange(endpoint, HttpMethod.POST, req, String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("SOAP HTTP " + resp.getStatusCodeValue());
        }
        return resp.getBody() == null ? "" : resp.getBody();
    }

    private List<OrganizationDTO> parseOrganizations(String xml) {
        List<OrganizationDTO> out = new ArrayList<>();
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new java.io.ByteArrayInputStream(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            XPath xp = XPathFactory.newInstance().newXPath();
            // local-name() y se evita lidiar con prefijos de namespace
            NodeList nodes = (NodeList) xp.evaluate("//*[local-name()='OrganizationType']", doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                String id = text(xp, nodes.item(i), "*[local-name()='id']/text()");
                String name = text(xp, nodes.item(i), "*[local-name()='name']/text()");
                String address = text(xp, nodes.item(i), "*[local-name()='address']/text()");
                String phone = text(xp, nodes.item(i), "*[local-name()='phone']/text()");
                out.add(new OrganizationDTO(nz(id), nz(name), nz(address), nz(phone)));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parseando Organizations SOAP", e);
        }
        return out;
    }

    private List<PresidentDTO> parsePresidents(String xml) {
        List<PresidentDTO> out = new ArrayList<>();
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new java.io.ByteArrayInputStream(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            XPath xp = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xp.evaluate("//*[local-name()='PresidentType']", doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                String id = text(xp, nodes.item(i), "*[local-name()='id']/text()");
                String name = text(xp, nodes.item(i), "*[local-name()='name']/text()");
                String address = text(xp, nodes.item(i), "*[local-name()='address']/text()");
                String phone = text(xp, nodes.item(i), "*[local-name()='phone']/text()");
                String orgId = text(xp, nodes.item(i), "*[local-name()='organization_id']/text()");
                out.add(new PresidentDTO(nz(id), nz(name), nz(address), nz(phone), nz(orgId)));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parseando Presidents SOAP", e);
        }
        return out;
    }

    // Mini helpers XML/XPath
    private static String text(XPath xp, org.w3c.dom.Node ctx, String expr) throws Exception {
        String v = (String) xp.evaluate(expr, ctx, XPathConstants.STRING);
        return v == null ? "" : v.trim();
    }
    private static String nz(String s) { return s == null ? "" : s; }
    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
