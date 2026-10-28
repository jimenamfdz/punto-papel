const form = document.querySelector('#producto-form');
const productList = document.querySelector('#product-list');
const loading = document.querySelector('#loading');
const count = document.querySelector('#product-count');
const message = document.querySelector('#form-message');
const cancelButton = document.querySelector('#cancelar');
const formTitle = document.querySelector('#form-title');
const formMode = document.querySelector('#form-mode');
const saveButton = document.querySelector('#guardar');
const saleForm = document.querySelector('#venta-form');
const saleProduct = document.querySelector('#venta-producto');
const saleQuantity = document.querySelector('#venta-cantidad');
const saleMessage = document.querySelector('#venta-message');
const filterForm = document.querySelector('#filters-form');
const filterProduct = document.querySelector('#filter-producto');
const filterFrom = document.querySelector('#filter-desde');
const filterTo = document.querySelector('#filter-hasta');
const salesList = document.querySelector('#sales-list');
const loginPanel = document.querySelector('#login-panel');
const appContent = document.querySelector('#app-content');
const loginForm = document.querySelector('#login-form');
const loginUser = document.querySelector('#login-user');
const loginPassword = document.querySelector('#login-password');
const loginMessage = document.querySelector('#login-message');
const currentUserBadge = document.querySelector('#current-user');
const logoutButton = document.querySelector('#logout');
let currentUser = null;

function authHeader() {
  const credentials = sessionStorage.getItem('punto-papel-credentials');
  return credentials ? { Authorization: `Basic ${credentials}` } : {};
}

function toBase64(value) {
  const bytes = new TextEncoder().encode(value);
  let binary = '';
  bytes.forEach(byte => { binary += String.fromCharCode(byte); });
  return btoa(binary);
}

async function apiFetch(url, options = {}) {
  const headers = { ...(options.headers || {}), ...authHeader() };
  const response = await fetch(url, { ...options, headers });
  if (response.status === 401) {
    endSession('La sesión expiró. Vuelve a iniciar sesión.');
  }
  return response;
}

function endSession(messageText = '') {
  sessionStorage.removeItem('punto-papel-credentials');
  sessionStorage.removeItem('punto-papel-user');
  currentUser = null;
  appContent.classList.add('hidden');
  loginPanel.classList.remove('hidden');
  currentUserBadge.classList.add('hidden');
  logoutButton.classList.add('hidden');
  if (messageText) {
    loginMessage.textContent = messageText;
    loginMessage.classList.add('error');
  }
}

function startSession(credentials, user) {
  sessionStorage.setItem('punto-papel-credentials', credentials);
  sessionStorage.setItem('punto-papel-user', JSON.stringify(user));
  currentUser = user;
  loginPanel.classList.add('hidden');
  appContent.classList.remove('hidden');
  currentUserBadge.textContent = `${user.nombreUsuario} · ${user.rol}`;
  currentUserBadge.classList.remove('hidden');
  logoutButton.classList.remove('hidden');
  document.querySelectorAll('.admin-only').forEach(element => {
    element.classList.toggle('hidden', user.rol !== 'ADMIN');
  });
}

loginForm.addEventListener('submit', async event => {
  event.preventDefault();
  loginMessage.textContent = 'Validando…';
  loginMessage.classList.remove('error');
  const username = loginUser.value.trim();
  const password = loginPassword.value;
  try {
    const response = await fetch('/api/usuarios/login', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nombreUsuario: username, password })
    });
    const body = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(body.mensaje || 'No se pudo iniciar sesión.');
    startSession(toBase64(`${username.toLowerCase()}:${password}`), body);
    loginForm.reset();
    await loadProducts();
    await loadSales();
  } catch (error) {
    loginMessage.textContent = error.message;
    loginMessage.classList.add('error');
  }
});

logoutButton.addEventListener('click', () => endSession());

const fields = {
  id: document.querySelector('#producto-id'),
  nombre: document.querySelector('#nombre'),
  precio: document.querySelector('#precio'),
  existencias: document.querySelector('#existencias')
};

function showMessage(text, error = false) {
  message.textContent = text;
  message.classList.toggle('error', error);
}

function formatPrice(value) {
  return new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(value);
}

function renderProducts(products) {
  loading.classList.toggle('hidden', products.length > 0);
  loading.textContent = products.length ? '' : 'Aún no hay productos registrados.';
  count.textContent = `${products.length} ${products.length === 1 ? 'producto' : 'productos'}`;
  const editButton = currentUser?.rol === 'ADMIN'
    ? product => `<button class="button edit" type="button" data-edit="${product.id}">Editar</button>`
    : () => '';
  productList.innerHTML = products.map(product => `
    <article class="product-card">
      <h3>${escapeHtml(product.nombre)}</h3>
      <div class="product-meta"><span class="product-price">${formatPrice(product.precio)}</span><span class="stock">${product.existencias} disponibles</span></div>
      ${editButton(product)}
    </article>`).join('');
  productList.querySelectorAll('[data-edit]').forEach(button => {
    button.addEventListener('click', () => editProduct(products.find(item => item.id === button.dataset.edit)));
  });
  const options = products.length ? products.map(product => `<option value="${product.id}">${escapeHtml(product.nombre)} · ${product.existencias} disponibles</option>`).join('') : '<option value="">No hay productos</option>';
  saleProduct.innerHTML = options;
  filterProduct.innerHTML = '<option value="">Todos los productos</option>' + products.map(product => `<option value="${product.id}">${escapeHtml(product.nombre)}</option>`).join('');
}

function escapeHtml(value) {
  return String(value).replace(/[&<>'"]/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[character]));
}

async function loadProducts() {
  try {
    const response = await apiFetch('/api/productos');
    if (!response.ok) throw new Error('No se pudieron cargar los productos.');
    renderProducts(await response.json());
  } catch (error) {
    loading.classList.remove('hidden');
    loading.textContent = error.message;
  }
}

function queryString() {
  const params = new URLSearchParams();
  if (filterProduct.value) params.set('productoId', filterProduct.value);
  if (filterFrom.value) params.set('desde', filterFrom.value);
  if (filterTo.value) params.set('hasta', filterTo.value);
  return params.toString() ? `?${params}` : '';
}

function renderSales(sales) {
  salesList.innerHTML = sales.length ? sales.map(sale => `
    <div class="sale-row"><div><strong>${escapeHtml(sale.nombreProducto)}</strong><small>${new Date(sale.fecha).toLocaleString('es-MX')} · ${sale.cantidad} unidades</small></div><strong>${formatPrice(sale.total)}</strong></div>`).join('') : '<p class="empty-state">No hay ventas para estos filtros.</p>';
}

function renderSummary(summary) {
  document.querySelector('#summary-sales').textContent = summary.totalVentas;
  document.querySelector('#summary-units').textContent = summary.unidadesVendidas;
  document.querySelector('#summary-total').textContent = formatPrice(summary.importeTotal);
  document.querySelector('#summary-products').innerHTML = summary.porProducto.length ? summary.porProducto.map(item => `
    <div class="summary-product"><span>${escapeHtml(item.nombreProducto)} · ${item.unidadesVendidas} unidades</span><strong>${formatPrice(item.importeTotal)}</strong></div>`).join('') : '<p class="empty-state">Todavía no hay ventas.</p>';
}

async function loadSales() {
  const query = queryString();
  try {
    const [salesResponse, summaryResponse] = await Promise.all([apiFetch(`/api/ventas${query}`), apiFetch(`/api/ventas/resumen${query}`)]);
    const sales = await salesResponse.json();
    const summary = await summaryResponse.json();
    if (!salesResponse.ok || !summaryResponse.ok) throw new Error(summary.mensaje || 'No se pudo cargar el historial.');
    renderSales(sales);
    renderSummary(summary);
  } catch (error) {
    salesList.innerHTML = `<p class="message error">${escapeHtml(error.message)}</p>`;
  }
}

function editProduct(product) {
  fields.id.value = product.id;
  fields.nombre.value = product.nombre;
  fields.precio.value = product.precio;
  fields.existencias.value = product.existencias;
  formTitle.textContent = 'Editar producto';
  formMode.textContent = 'Edición';
  saveButton.textContent = 'Guardar cambios';
  cancelButton.classList.remove('hidden');
  fields.nombre.focus();
  showMessage('Editando producto.');
}

function resetForm() {
  form.reset();
  fields.id.value = '';
  formTitle.textContent = 'Registrar producto';
  formMode.textContent = 'Nuevo';
  saveButton.textContent = 'Guardar producto';
  cancelButton.classList.add('hidden');
  showMessage('');
}

form.addEventListener('submit', async event => {
  event.preventDefault();
  const id = fields.id.value;
  const payload = { nombre: fields.nombre.value.trim(), precio: Number(fields.precio.value), existencias: Number(fields.existencias.value) };
  const url = id ? `/api/productos/${id}` : '/api/productos';
  const method = id ? 'PUT' : 'POST';
  saveButton.disabled = true;
  showMessage('Guardando…');
  try {
    const response = await apiFetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
    const body = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(body.mensaje || 'No se pudo guardar el producto.');
    resetForm();
    showMessage(id ? 'Producto actualizado.' : 'Producto registrado.');
    await loadProducts();
  } catch (error) {
    showMessage(error.message, true);
  } finally {
    saveButton.disabled = false;
  }
});

cancelButton.addEventListener('click', resetForm);
saleForm.addEventListener('submit', async event => {
  event.preventDefault();
  saleMessage.textContent = 'Guardando…';
  saleMessage.classList.remove('error');
  try {
    const response = await apiFetch(`/api/ventas/productos/${saleProduct.value}`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ cantidad: Number(saleQuantity.value) }) });
    const body = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(body.mensaje || 'No se pudo registrar la venta.');
    saleForm.reset();
    saleMessage.textContent = 'Venta registrada correctamente.';
    await loadProducts();
    await loadSales();
  } catch (error) {
    saleMessage.textContent = error.message;
    saleMessage.classList.add('error');
  }
});

filterForm.addEventListener('submit', event => { event.preventDefault(); loadSales(); });
document.querySelector('#clear-filters').addEventListener('click', () => { filterForm.reset(); loadSales(); });

const savedCredentials = sessionStorage.getItem('punto-papel-credentials');
const savedUser = sessionStorage.getItem('punto-papel-user');
if (savedCredentials && savedUser) {
  try {
    startSession(savedCredentials, JSON.parse(savedUser));
    loadProducts().then(loadSales);
  } catch (error) {
    endSession();
  }
}
