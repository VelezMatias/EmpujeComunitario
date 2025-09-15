Param(
  [int]$OrgId = 42,
  [int]$Partitions = 3,
  [int]$RF = 1
)

$topicsEstaticos = @(
  "solicitud-donaciones",
  "oferta-donaciones",
  "baja-solicitud-donaciones",
  "eventos-solidarios",
  "baja-evento-solidario"
)

$topicsDinamicos = @(
  "transferencia-donaciones.$OrgId",
  "adhesion-evento.$OrgId"
)

$all = $topicsEstaticos + $topicsDinamicos

foreach ($t in $all) {
  docker exec kafka bash -lc "kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --topic $t --partitions $Partitions --replication-factor $RF"
}

Write-Host "`nTopics actuales:"
docker exec kafka bash -lc "kafka-topics --bootstrap-server localhost:9092 --list"
