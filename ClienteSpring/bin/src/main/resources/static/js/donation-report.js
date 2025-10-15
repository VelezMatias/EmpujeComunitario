async function graphql(query, variables) {
  const res = await fetch('/graphql', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({query, variables})
  });
  return res.json();
}

async function buscar() {
  const f = document.getElementById('formDon');
  const vars = {
    tipo: f.tipo.value,
    categoria: f.categoria.value || null,
    dateFrom: f.dateFrom.value || null,
    dateTo: f.dateTo.value || null,
    eliminado: f.eliminado.value || 'AMBOS'
  };

  const q = `
    query($tipo:String!,$categoria:String,$dateFrom:String,$dateTo:String,$eliminado:String){
      donationReportGrouped(tipo:$tipo,categoria:$categoria,dateFrom:$dateFrom,dateTo:$dateTo,eliminado:$eliminado){
        categoria eliminado total
      }
    }`;

  const data = await graphql(q, vars);
  const result = data.data?.donationReportGrouped || [];
  document.getElementById('resultado').textContent = JSON.stringify(result, null, 2);
}
