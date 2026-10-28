const form = document.querySelector('#producto-form');
const productList = document.querySelector('#product-list');
const loading = document.querySelector('#loading');
const count = document.querySelector('#product-count');
const message = document.querySelector('#form-message');
const cancelButton = document.querySelector('#cancelar');
const formTitle = document.querySelector('#form-title');
const formMode = document.querySelector('#form-mode');
const saveButton = document.querySelector('#guardar');

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
  productList.innerHTML = products.map(product => `
    <article class="product-card">
      <h3>${escapeHtml(product.nombre)}</h3>
      <div class="product-meta"><span class="product-price">${formatPrice(product.precio)}</span><span class="stock">${product.existencias} disponibles</span></div>
      <button class="button edit" type="button" data-edit="${product.id}">Editar</button>
    </article>`).join('');
  productList.querySelectorAll('[data-edit]').forEach(button => {
    button.addEventListener('click', () => editProduct(products.find(item => item.id === button.dataset.edit)));
  });
}

function escapeHtml(value) {
  return String(value).replace(/[&<>'"]/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[character]));
}

async function loadProducts() {
  try {
    const response = await fetch('/api/productos');
    if (!response.ok) throw new Error('No se pudieron cargar los productos.');
    renderProducts(await response.json());
  } catch (error) {
    loading.classList.remove('hidden');
    loading.textContent = error.message;
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
    const response = await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
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
loadProducts();
