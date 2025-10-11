 🟢 Topic: # 📄 Kafka Payloads – EmpujeComunitario

Este documento define los **esquemas JSON** para cada topic del trabajo práctico.  
Regla general: todos los mensajes llevan un `idempotency_key = "<org_id>:<id>"` para evitar reprocesos.

---

##.`solicitud-donaciones`

**Clave de partición**: `solicitud_id`  


{
  "org_id": 42,
  "solicitud_id": "SOL-2025-0001",
  "items": [
    {
      "categoria": "ALIMENTOS",
      "descripcion": "Puré de tomates",
      "cantidad": 20,
      "unidad": "cajas"
    }
  ],
  "fecha_hora": "2025-09-15T03:10:00-03:00",
  "idempotency_key": "42:SOL-2025-0001"
}
```

---

##`transferencia-donaciones.<org_id_destino>`

**Clave de partición**: `solicitud_id`  


{
  "org_id_origen": 42,
  "org_id_destino": 77,
  "solicitud_id": "SOL-2025-0001",
  "items": [
    {
      "categoria": "ALIMENTOS",
      "descripcion": "Puré de tomates",
      "cantidad": 10,
      "unidad": "cajas"
    }
  ],
  "fecha_hora": "2025-09-15T03:20:00-03:00",
  "idempotency_key": "42:SOL-2025-0001:TRF-0001"
}
```

---

##`oferta-donaciones`

**Clave de partición**: `oferta_id`  


{
  "org_id": 42,
  "oferta_id": "OFE-2025-0001",
  "items": [
    {
      "categoria": "ROPA",
      "descripcion": "Camperas de abrigo",
      "cantidad": 50,
      "unidad": "unidades"
    }
  ],
  "fecha_hora": "2025-09-15T03:30:00-03:00",
  "idempotency_key": "42:OFE-2025-0001"
}
```

---

##`baja-solicitud-donaciones`

**Clave de partición**: `solicitud_id`  


{
  "org_id": 42,
  "solicitud_id": "SOL-2025-0001",
  "motivo": "Ya no se requiere la donación",
  "fecha_hora": "2025-09-15T04:00:00-03:00",
  "idempotency_key": "42:SOL-2025-0001:BAJA"
}
```

---

##`eventos-solidarios`

**Clave de partición**: `evento_id`  


{
  "org_id": 42,
  "evento_id": "EVT-2025-0001",
  "titulo": "Colecta de alimentos de invierno",
  "descripcion": "Jornada solidaria para reunir alimentos no perecederos.",
  "lugar": "Plaza Central",
  "fecha_inicio": "2025-09-20T10:00:00-03:00",
  "fecha_fin": "2025-09-20T18:00:00-03:00",
  "idempotency_key": "42:EVT-2025-0001"
}
```

---

##`baja-evento-solidario`

**Clave de partición**: `evento_id`  


{
  "org_id": 42,
  "evento_id": "EVT-2025-0001",
  "motivo": "Cancelación por mal clima",
  "fecha_hora": "2025-09-18T12:00:00-03:00",
  "idempotency_key": "42:EVT-2025-0001:BAJA"
}
```

---

##`adhesion-evento.<org_id_organizador>`

**Clave de partición**: `evento_id`  


{
  "org_id_organizador": 42,
  "evento_id": "EVT-2025-0001",
  "org_id_adherente": 77,
  "fecha_hora": "2025-09-19T15:00:00-03:00",
  "idempotency_key": "77:EVT-2025-0001:ADH"
}
```



