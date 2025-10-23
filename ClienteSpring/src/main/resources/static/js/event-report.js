document.addEventListener('DOMContentLoaded', () => {
  console.log('[event-report.js] script loaded');
  const btn = document.getElementById('buscar');
  const ex = document.getElementById('excel');
  btn.addEventListener('click', doSearch);
  ex.addEventListener('click', downloadExcel);
});

async function doSearch() {
  const usuarioId = document.getElementById('usuarioId').value;
  const from = document.getElementById('from').value;
  const to = document.getElementById('to').value;
  const reparto = document.getElementById('reparto').value;

  if (!usuarioId) { alert('Usuario ID es obligatorio'); return; }

  const query = `query($usuarioId:Int!,$from:String,$to:String,$reparto:String){\n  eventParticipationReport(usuarioId:$usuarioId,from:$from,to:$to,repartoDonaciones:$reparto){\n    mes\n    year\n    eventos{ dia nombre descripcion donaciones{ descripcion cantidad } }\n  }\n}`;

  const variables = { usuarioId: parseInt(usuarioId), from: from || null, to: to || null, reparto: reparto };

  const res = await fetch('/graphql', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ query, variables })
  });
  console.log('[event-report.js] GraphQL response status', res.status);
  const json = await res.json();
  console.log('[event-report.js] GraphQL response json', json);
  const data = json.data?.eventParticipationReport || [];
  const container = document.getElementById('resultado');
  container.innerHTML = '';

  for (const g of data) {
    const h = document.createElement('h2');
    h.textContent = `${g.mes}/${g.year}`;
    container.appendChild(h);
    const ul = document.createElement('ul');
    for (const ev of g.eventos || []) {
      const li = document.createElement('li');
      li.innerHTML = `<strong>${ev.nombre}</strong> (día ${ev.dia}): ${ev.descripcion}`;
      if (ev.donaciones && ev.donaciones.length) {
        const dl = document.createElement('ul');
        for (const d of ev.donaciones) {
          const dli = document.createElement('li');
          dli.textContent = `${d.descripcion} — ${d.cantidad}`;
          dl.appendChild(dli);
        }
        li.appendChild(dl);
      }
      ul.appendChild(li);
    }
    container.appendChild(ul);
  }
}

async function downloadExcel() {
  const usuarioId = document.getElementById('usuarioId').value;
  const from = document.getElementById('from').value;
  const to = document.getElementById('to').value;
  const reparto = document.getElementById('reparto').value;
  if (!usuarioId) { alert('Usuario ID es obligatorio'); return; }

  const params = new URLSearchParams({ usuarioId, from, to, reparto });
  const url = `/api/informes/eventos/excel?` + params.toString();
  window.location = url;
}