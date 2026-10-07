// Company Z Employee Management – browser client for the REST API.
// No framework; all values are rendered with textContent (no HTML injection).
'use strict';

const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => [...document.querySelectorAll(sel)];
const state = { token: null, role: null, page: 0, query: '', selected: null, divisions: [], jobTitles: [] };

try {
  state.token = sessionStorage.getItem('ems.token');
  state.role = sessionStorage.getItem('ems.role');
} catch {
  /* storage unavailable: stay logged out */
}

// ------------------------------------------------------------------ helpers
function toast(message, isError = false) {
  const t = $('#toast');
  t.textContent = message;
  t.className = isError ? 'error' : '';
  t.hidden = false;
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => (t.hidden = true), 4000);
}

async function api(method, path, body) {
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (state.token) headers.Authorization = `Bearer ${state.token}`;
  const res = await fetch(path, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  if (res.status === 401 && state.token) {
    logout();
    throw new Error('Session expired, please sign in again');
  }
  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    const details = data?.errors?.map((e) => `${e.field}: ${e.message}`).join('; ');
    throw new Error(details || data?.detail || `${res.status} ${res.statusText}`);
  }
  return data;
}

const money = (v) => (v == null ? '' : Number(v).toLocaleString(undefined, { style: 'currency', currency: 'USD' }));

function table(el, columns, rows, onClick) {
  el.replaceChildren();
  const head = el.createTHead().insertRow();
  for (const c of columns) {
    const th = document.createElement('th');
    th.textContent = c.label;
    if (c.num) th.className = 'num';
    head.appendChild(th);
  }
  const body = el.createTBody();
  if (!rows.length) {
    const td = body.insertRow().insertCell();
    td.colSpan = columns.length;
    td.textContent = 'No results';
    return;
  }
  for (const r of rows) {
    const tr = body.insertRow();
    for (const c of columns) {
      const td = tr.insertCell();
      td.textContent = c.value(r) ?? '';
      if (c.num) td.className = 'num';
    }
    if (onClick) {
      tr.className = 'clickable';
      tr.addEventListener('click', () => onClick(r));
    }
  }
}

function formData(form) {
  return Object.fromEntries(new FormData(form).entries());
}

const blankToNull = (v) => (v === '' || v == null ? null : v);

function fillSelect(select, items, emptyLabel) {
  select.replaceChildren(new Option(emptyLabel, ''));
  for (const it of items) select.add(new Option(`${it.name} (${it.id})`, it.id));
}

const payColumns = [
  { label: 'Pay date', value: (p) => p.payDate },
  { label: 'Earnings', value: (p) => money(p.earnings), num: true },
  { label: 'Fed tax', value: (p) => money(p.fedTax), num: true },
  { label: 'Medicare', value: (p) => money(p.fedMed), num: true },
  { label: 'Soc. sec.', value: (p) => money(p.fedSs), num: true },
  { label: 'State tax', value: (p) => money(p.stateTax), num: true },
  { label: '401(k)', value: (p) => money(p.retire401k), num: true },
  { label: 'Health', value: (p) => money(p.healthCare), num: true },
  { label: 'Net pay', value: (p) => money(p.netPay), num: true },
];

// ------------------------------------------------------------------ session
function showView() {
  const loggedIn = Boolean(state.token);
  $('#login-view').hidden = loggedIn;
  $('#session').hidden = !loggedIn;
  $('#admin-view').hidden = !(loggedIn && state.role === 'HR_ADMIN');
  $('#employee-view').hidden = !(loggedIn && state.role === 'EMPLOYEE');
  if (!loggedIn) return;
  $('#who').textContent = `${sessionStorage.getItem('ems.user') || ''} (${state.role})`;
  if (state.role === 'HR_ADMIN') initAdmin().catch((e) => toast(e.message, true));
  else loadSelf().catch((e) => toast(e.message, true));
}

function logout() {
  state.token = null;
  state.role = null;
  try {
    sessionStorage.clear();
  } catch {
    /* ignore */
  }
  showView();
}

$('#login-form').addEventListener('submit', async (ev) => {
  ev.preventDefault();
  try {
    const t = await api('POST', '/api/v1/auth/login', formData(ev.target));
    state.token = t.accessToken;
    state.role = t.role;
    try {
      sessionStorage.setItem('ems.token', t.accessToken);
      sessionStorage.setItem('ems.role', t.role);
      sessionStorage.setItem('ems.user', t.username);
    } catch {
      /* keep the session in memory only */
    }
    ev.target.reset();
    showView();
  } catch (e) {
    toast(e.message, true);
  }
});
$('#logout').addEventListener('click', logout);

// ------------------------------------------------------------------ employee self-service
async function loadSelf() {
  const me = await api('GET', '/api/v1/me');
  const e = me.employee || {};
  const fields = [
    ['Name', `${e.firstName ?? ''} ${e.lastName ?? ''}`],
    ['E-mail', e.email],
    ['Hire date', e.hireDate],
    ['Salary', money(e.salary)],
    ['SSN', e.ssn],
    ['Division', e.division?.name ?? '—'],
    ['Job title', e.jobTitle?.name ?? '—'],
  ];
  const dl = $('#profile');
  dl.replaceChildren();
  for (const [k, v] of fields) {
    const dt = document.createElement('dt');
    dt.textContent = k;
    const dd = document.createElement('dd');
    dd.textContent = v ?? '';
    dl.append(dt, dd);
  }
  table($('#my-pay'), payColumns, await api('GET', '/api/v1/me/pay-statements'));
}

$('#password-form').addEventListener('submit', async (ev) => {
  ev.preventDefault();
  try {
    await api('PUT', '/api/v1/me/password', formData(ev.target));
    ev.target.reset();
    toast('Password changed');
  } catch (e) {
    toast(e.message, true);
  }
});

// ------------------------------------------------------------------ HR admin
let adminReady = false;
async function initAdmin() {
  if (!adminReady) {
    const [divisions, jobTitles] = await Promise.all([api('GET', '/api/v1/divisions'), api('GET', '/api/v1/job-titles')]);
    state.divisions = divisions;
    state.jobTitles = jobTitles;
    fillSelect($('#employee-form [name=divisionId]'), divisions, '(none)');
    fillSelect($('#employee-form [name=jobTitleId]'), jobTitles, '(none)');
    fillSelect($('#adjust-form [name=divisionId]'), divisions, '(any division)');
    adminReady = true;
  }
  await loadEmployees();
}

$$('.tabs button').forEach((b) =>
  b.addEventListener('click', () => {
    $$('.tabs button').forEach((x) => x.classList.toggle('active', x === b));
    $$('[data-panel]').forEach((p) => (p.hidden = p.dataset.panel !== b.dataset.tab));
    if (b.dataset.tab === 'audit') loadAudit().catch((e) => toast(e.message, true));
  }),
);

const employeeColumns = [
  { label: 'ID', value: (e) => e.id, num: true },
  { label: 'Name', value: (e) => `${e.lastName}, ${e.firstName}` },
  { label: 'E-mail', value: (e) => e.email },
  { label: 'Hired', value: (e) => e.hireDate },
  { label: 'Salary', value: (e) => money(e.salary), num: true },
  { label: 'SSN', value: (e) => e.ssn },
  { label: 'Division', value: (e) => e.division?.name },
  { label: 'Job title', value: (e) => e.jobTitle?.name },
];

async function loadEmployees() {
  const q = encodeURIComponent(state.query);
  const page = await api('GET', `/api/v1/employees?q=${q}&page=${state.page}&size=10`);
  table($('#employee-table'), employeeColumns, page.content, showEmployee);
  $('#page-info').textContent = `page ${page.page + 1} of ${Math.max(1, page.totalPages)} · ${page.totalElements} employees`;
  $('#prev').disabled = page.page === 0;
  $('#next').disabled = page.page + 1 >= page.totalPages;
}

$('#search-form').addEventListener('submit', (ev) => {
  ev.preventDefault();
  state.query = ev.target.q.value.trim();
  state.page = 0;
  loadEmployees().catch((e) => toast(e.message, true));
});
$('#prev').addEventListener('click', () => {
  state.page = Math.max(0, state.page - 1);
  loadEmployees().catch((e) => toast(e.message, true));
});
$('#next').addEventListener('click', () => {
  state.page += 1;
  loadEmployees().catch((e) => toast(e.message, true));
});
$('#ssn-search').addEventListener('click', async () => {
  try {
    const e = await api('POST', '/api/v1/employees/search/ssn', { ssn: $('#search-form').ssn.value });
    table($('#employee-table'), employeeColumns, [e], showEmployee);
    showEmployee(e);
  } catch (e) {
    toast(e.message, true);
  }
});
$('#new-employee').addEventListener('click', () => showEmployee(null));

async function showEmployee(e) {
  state.selected = e;
  const form = $('#employee-form');
  form.reset();
  $('#ssn-out').textContent = '';
  $('#employee-detail').hidden = false;
  $('#detail-title').textContent = e ? `${e.firstName} ${e.lastName} (#${e.id})` : 'New employee';
  $('#reveal-ssn').hidden = !e;
  $('#delete-employee').hidden = !e;
  if (e) {
    for (const k of ['firstName', 'lastName', 'email', 'hireDate', 'salary']) form[k].value = e[k] ?? '';
    form.divisionId.value = e.division?.id ?? '';
    form.jobTitleId.value = e.jobTitle?.id ?? '';
    form.ssn.placeholder = e.ssn ? `${e.ssn} (leave empty to keep)` : 'not on file';
    table($('#detail-pay'), payColumns, await api('GET', `/api/v1/employees/${e.id}/pay-statements`));
  } else {
    form.ssn.placeholder = '123-45-6789';
    table($('#detail-pay'), payColumns, []);
  }
  $('#employee-detail').scrollIntoView({ behavior: 'smooth' });
}

$('#employee-form').addEventListener('submit', async (ev) => {
  ev.preventDefault();
  const f = formData(ev.target);
  const body = {
    firstName: f.firstName,
    lastName: f.lastName,
    email: f.email,
    hireDate: f.hireDate,
    salary: f.salary,
    ssn: blankToNull(f.ssn),
    divisionId: blankToNull(f.divisionId),
    jobTitleId: blankToNull(f.jobTitleId),
  };
  try {
    const saved = state.selected
      ? await api('PUT', `/api/v1/employees/${state.selected.id}`, body)
      : await api('POST', '/api/v1/employees', body);
    toast(`Saved ${saved.firstName} ${saved.lastName}`);
    await loadEmployees();
    await showEmployee(saved);
  } catch (e) {
    toast(e.message, true);
  }
});

$('#reveal-ssn').addEventListener('click', async () => {
  try {
    const r = await api('GET', `/api/v1/employees/${state.selected.id}/ssn`);
    $('#ssn-out').textContent = `SSN ${r.ssn} (this view was logged)`;
  } catch (e) {
    toast(e.message, true);
  }
});

$('#delete-employee').addEventListener('click', async () => {
  const e = state.selected;
  if (!e || !window.confirm(`Delete ${e.firstName} ${e.lastName}, their pay history and login?`)) return;
  try {
    await api('DELETE', `/api/v1/employees/${e.id}`);
    $('#employee-detail').hidden = true;
    toast('Employee deleted');
    await loadEmployees();
  } catch (err) {
    toast(err.message, true);
  }
});

$('#adjust-form').addEventListener('submit', async (ev) => {
  ev.preventDefault();
  const f = formData(ev.target);
  const apply = ev.submitter?.value === 'apply';
  const body = {
    percent: f.percent,
    minSalary: blankToNull(f.minSalary),
    maxSalary: blankToNull(f.maxSalary),
    divisionId: blankToNull(f.divisionId),
    dryRun: !apply,
  };
  try {
    const r = await api('POST', '/api/v1/salary-adjustments', body);
    table($('#adjust-result'), [
      { label: 'ID', value: (c) => c.employeeId, num: true },
      { label: 'Name', value: (c) => c.name },
      { label: 'Old salary', value: (c) => money(c.oldSalary), num: true },
      { label: 'New salary', value: (c) => money(c.newSalary), num: true },
    ], r.changes);
    toast(`${r.affected} employee(s) ${r.applied ? 'updated' : 'would be updated (preview)'}`);
    if (r.applied) await loadEmployees();
  } catch (e) {
    toast(e.message, true);
  }
});

$('#payroll-form').addEventListener('submit', async (ev) => {
  ev.preventDefault();
  try {
    const r = await api('POST', '/api/v1/payroll/runs', formData(ev.target));
    $('#payroll-out').textContent = `${r.payDate}: ${r.created} statement(s) created, ${r.skipped} already existed.`;
  } catch (e) {
    toast(e.message, true);
  }
});

$('#report-form').addEventListener('submit', async (ev) => {
  ev.preventDefault();
  const f = formData(ev.target);
  const el = $('#report-table');
  try {
    if (f.kind === 'payroll') {
      const r = await api('GET', `/api/v1/reports/payroll?payDate=${f.payDate}`);
      table(el, [
        { label: 'Pay date', value: (x) => x.payDate },
        { label: 'Statements', value: (x) => x.statements, num: true },
        { label: 'Earnings', value: (x) => money(x.totalEarnings), num: true },
        { label: 'Deductions', value: (x) => money(x.totalDeductions), num: true },
        { label: 'Net', value: (x) => money(x.netTotal), num: true },
      ], [r]);
    } else if (f.kind === 'new-hires') {
      const rows = await api('GET', `/api/v1/reports/new-hires?from=${f.from}&to=${f.to}`);
      table(el, [
        { label: 'ID', value: (x) => x.employeeId, num: true },
        { label: 'Name', value: (x) => `${x.firstName} ${x.lastName}` },
        { label: 'Hire date', value: (x) => x.hireDate },
        { label: 'Job title', value: (x) => x.jobTitle },
      ], rows);
    } else {
      const r = await api('GET', `/api/v1/reports/${f.kind}?month=${f.month}`);
      table(el, [
        { label: r.groupedBy === 'division' ? 'Division' : 'Job title', value: (x) => x.group },
        { label: 'Statements', value: (x) => x.statements, num: true },
        { label: 'Total pay', value: (x) => money(x.totalPay), num: true },
      ], [...r.rows, { group: 'Total', statements: r.rows.reduce((s, x) => s + x.statements, 0), totalPay: r.grandTotal }]);
    }
  } catch (e) {
    toast(e.message, true);
  }
});

async function loadAudit() {
  const page = await api('GET', '/api/v1/audit?size=100');
  table($('#audit-table'), [
    { label: 'When (UTC)', value: (a) => a.occurredAt?.replace('T', ' ').slice(0, 19) },
    { label: 'Who', value: (a) => a.actor },
    { label: 'Action', value: (a) => a.action },
    { label: 'Entity', value: (a) => `${a.entityType}${a.entityId ? ` #${a.entityId}` : ''}` },
    { label: 'Details', value: (a) => a.details },
  ], page.content);
}
$('#audit-refresh').addEventListener('click', () => loadAudit().catch((e) => toast(e.message, true)));

showView();
