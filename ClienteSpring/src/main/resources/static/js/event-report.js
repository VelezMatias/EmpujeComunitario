window.__searchLock = false;

(function () {
  const $ = (sel) => document.querySelector(sel);
  const pick = (...sels) => sels.map(s => document.querySelector(s)).find(Boolean) || null;

  function readFromDom() {
    const usuarioIdStr =
      (pick('#usuarioId','[name="usuarioId"]','#usuario','[name="usuario"]')?.value || '').trim();
    const from =
      (pick('#from','[name="from"]','#fechaDesde','[name="fechaDesde"]')?.value || '').trim();
    const to =
      (pick('#to','[name="to"]','#fechaHasta','[name="fechaHasta"]')?.value || '').trim();
    const reparto =
      (pick('#reparto','[name="reparto"]')?.value || 'AMBOS').toString().toUpperCase();

    const usuarioId = usuarioIdStr ? parseInt(usuarioIdStr, 10) : null;
    const payload = { usuarioId, from: from || null, to: to || null, reparto };
    console.log('[event-report.js] readFromDom =>', { usuarioIdStr, from, to, reparto });
    return payload;
  }

  document.addEventListener('DOMContentLoaded', () => {
  console.log('[event-report.js] script loaded');
  const btn = document.getElementById('buscar');
  const ex  = document.getElementById('excel');

  if (btn) btn.addEventListener('click', () => {
    if (window.__searchLock) return;      // ⬅️ evita que “Aplicar” dispare otra búsqueda
    doSearchWith(readFromDom());
  });

  if (ex)  ex.addEventListener('click',  () => downloadExcelWith(readFromDom()));
});

  // Ejecuta la consulta con parámetros (provenientes del DOM o del filtro guardado)
  async function doSearchWith({ usuarioId, from = null, to = null, reparto = 'AMBOS' }) {
    console.log('[event-report.js] doSearchWith args =>', { usuarioId, from, to, reparto });

    if (!usuarioId || !Number.isFinite(usuarioId)) {
      alert('Usuario ID es obligatorio');
      return;
    }

    const query =
      'query($usuarioId:Int!,$from:String,$to:String,$reparto:String){' +
      '  eventParticipationReport(usuarioId:$usuarioId,from:$from,to:$to,repartoDonaciones:$reparto){' +
      '    mes year eventos{ dia nombre descripcion donaciones{ descripcion cantidad } }' +
      '  }' +
      '}';

    const variables = { usuarioId, from, to, reparto };

    let res;
    try {
      res = await fetch('/graphql', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ query, variables })
      });
    } catch (e) {
      console.error('[event-report.js] fetch error', e);
      alert('No se pudo contactar al servidor.');
      return;
    }

    console.log('[event-report.js] GraphQL response status', res.status);
    let json;
    try {
      json = await res.json();
    } catch {
      const txt = await res.text().catch(() => '');
      console.error('[event-report.js] invalid JSON:', txt);
      alert('Respuesta inválida del servidor.');
      return;
    }

    console.log('[event-report.js] GraphQL response json', json);

    if (!res.ok || json.errors) {
      console.error('[event-report.js] GraphQL error', json.errors || json);
      alert('Hubo un error obteniendo el informe.');
      return;
    }

    const data = json?.data?.eventParticipationReport || [];
    const container = document.getElementById('resultado') || (function(){
      const div = document.createElement('div'); div.id = 'resultado'; document.body.appendChild(div); return div;
    })();

    container.innerHTML = '';

    for (const g of data) {
      const h = document.createElement('h2');
      h.textContent = `${g.mes}/${g.year}`;
      container.appendChild(h);

      const ul = document.createElement('ul');
      for (const ev of (g.eventos || [])) {
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

  // Descarga Excel con los mismos parámetros normalizados
  async function downloadExcelWith({ usuarioId, from = '', to = '', reparto = 'AMBOS' }) {
    if (!usuarioId || !Number.isFinite(usuarioId)) {
      alert('Usuario ID es obligatorio');
      return;
    }
    const params = new URLSearchParams({
      usuarioId: String(usuarioId),
      from: from || '',
      to: to || '',
      reparto
    });
    window.location = `/api/informes/eventos/excel?` + params.toString();
  }

  // Compatibilidad: funciones que leen del DOM y delegan en *With*
  function doSearch()      { return doSearchWith(readFromDom()); }
  function downloadExcel() { return downloadExcelWith(readFromDom()); }

  // Exponer para que otros scripts (como el de “Aplicar filtro”) llamen directo
  window.doSearchWith = doSearchWith;
  window.downloadExcelWith = downloadExcelWith;
  window.doSearch = doSearch;
  window.downloadExcel = downloadExcel;
})();
