"use strict";

const DEMO_USERS = [
  { id: 1, name: "Administrador TechNova", email: "admin@technova.com", password: "admin123", role: "ADMINISTRADOR" },
  { id: 2, name: "Marina Oliveira", email: "vendas@technova.com", password: "vendas123", role: "FUNCIONARIO" },
  { id: 3, name: "Cliente Balcão", email: "cliente.balcao@technova.com", password: "123456", role: "CLIENTE", clientId: 1 },
  { id: 4, name: "Bruno Lima", email: "bruno@email.com", password: "bruno123", role: "CLIENTE", clientId: 2 }
];

const INITIAL_STATE = {
  user: null,
  page: "login",
  message: null,
  productFormId: null,
  products: [],
  clients: [],
  cart: [],
  orders: [],
  nextIds: {}
};

const NEXT_STATUS = {
  EM_PROCESSAMENTO: "APROVADO",
  APROVADO: "ENVIADO",
  ENVIADO: "ENTREGUE"
};

const STATUS_LABELS = {
  EM_PROCESSAMENTO: "Em processamento",
  APROVADO: "Aprovado",
  ENVIADO: "Enviado",
  ENTREGUE: "Entregue",
  CANCELADO: "Cancelado"
};

const STATUS_CLASSES = {
  EM_PROCESSAMENTO: "status-processing",
  APROVADO: "status-approved",
  ENVIADO: "status-shipped",
  ENTREGUE: "status-delivered",
  CANCELADO: "status-cancelled"
};

const state = structuredClone(INITIAL_STATE);
const application = document.querySelector("#application");
const API = "/api";
const money = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
const dateTime = new Intl.DateTimeFormat("pt-BR", { dateStyle: "medium", timeStyle: "short" });

const escapeHtml = (value) => String(value).replace(/[&<>"']/g, (character) => ({
  "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
})[character]);
const isStaff = () => state.user && state.user.role !== "CLIENTE";
const isAdmin = () => state.user?.role === "ADMINISTRADOR";
const clientById = (id) => state.clients.find((client) => client.id === id);
const productById = (id) => state.products.find((product) => product.id === id);
const cartQuantity = (productId) => state.cart.find((line) => line.productId === productId)?.quantity ?? 0;
const cartTotal = () => state.cart.reduce((sum, line) => sum + line.quantity * productById(line.productId).price, 0);

function notify(text, kind = "info") {
  state.message = { text, kind };
  const feedback = document.querySelector("#feedback");
  if (feedback) feedback.innerHTML = `<div class="alert alert-${kind}" role="${kind === "error" ? "alert" : "status"}">${escapeHtml(text)}</div>`;
}

function feedbackMarkup() {
  return `<div id="feedback" class="feedback" aria-live="polite" aria-atomic="true"></div>`;
}

function brandMarkup() {
  return `<span class="brand-mark" aria-hidden="true">T</span><span class="brand-name">TECHNOVA<span>SOLUÇÕES EM TECNOLOGIA</span></span>`;
}

function loginPage() {
  return `
    <div class="login-page">
      <aside class="login-aside" aria-label="Apresentação TechNova">
        <a class="brand login-brand" href="#" aria-label="TechNova">${brandMarkup()}</a>
        <div class="login-copy">
          <p class="eyebrow">Gestão simples, negócio em movimento</p>
          <h1>Tecnologia para fazer melhor.</h1>
          <p>Consulte produtos, organize pedidos e acompanhe cada etapa da operação TechNova.</p>
        </div>
        <p class="login-footnote">Sistema integrado TechNova</p>
      </aside>
      <main class="login-panel">
        <div class="login-content">
          <p class="eyebrow">Acesso ao sistema</p>
          <h2>Bem-vindo(a)</h2>
          <p>Entre com sua conta para acessar o catálogo e acompanhar os pedidos.</p>
          ${feedbackMarkup()}
          <form id="login-form" class="login-form" novalidate>
            <div class="field">
              <label for="login-email">E-mail</label>
              <input id="login-email" name="email" type="email" autocomplete="username" required maxlength="120" placeholder="voce@empresa.com">
            </div>
            <div class="field">
              <label for="login-password">Senha</label>
              <input id="login-password" name="password" type="password" autocomplete="current-password" required maxlength="100" placeholder="Digite sua senha">
            </div>
            <button class="button button-primary" type="submit">Entrar <span aria-hidden="true">→</span></button>
          </form>
          <section class="demo-accounts" aria-labelledby="demo-title">
            <p id="demo-title">Contas iniciais para acesso</p>
            <div class="demo-account-list">
              ${DEMO_USERS.map((user) => `
                <button class="demo-account" type="button" data-action="fill-login" data-user-id="${user.id}">
                  <span><strong>${escapeHtml(user.role === "ADMINISTRADOR" ? "Administrador" : user.role === "FUNCIONARIO" ? "Funcionário" : "Cliente")}</strong><span>${escapeHtml(user.email)}</span></span>
                  <em>Usar conta</em>
                </button>`).join("")}
            </div>
            <p class="field-hint demo-footnote">Altere as senhas iniciais antes de publicar em produção.</p>
          </section>
        </div>
      </main>
    </div>`;
}

function shell(content) {
  const cartCount = state.cart.reduce((sum, line) => sum + line.quantity, 0);
  const links = [
    ["products", "Produtos"],
    ...(isStaff() ? [["clients", "Clientes"]] : []),
    ["cart", `Carrinho${cartCount ? `<span class="nav-count">(${cartCount})</span>` : ""}`],
    ["orders", "Pedidos"]
  ];
  const initials = state.user.name.split(/\s+/).slice(0, 2).map((part) => part[0]).join("").toLocaleUpperCase("pt-BR");
  const roleName = state.user.role === "ADMINISTRADOR" ? "Administrador" : state.user.role === "FUNCIONARIO" ? "Funcionário" : "Cliente";
  return `
    <header class="site-header">
      <div class="header-inner">
        <a class="brand" href="#" aria-label="TechNova - início">${brandMarkup()}</a>
        <nav class="primary-nav" aria-label="Navegação principal">
          ${links.map(([page, label]) => `<button class="nav-button" type="button" data-action="navigate" data-page="${page}" ${state.page === page ? 'aria-current="page"' : ""}>${label}</button>`).join("")}
        </nav>
        <div class="header-user">
          <span class="user-avatar" aria-hidden="true">${escapeHtml(initials)}</span>
          <span class="user-copy"><span class="user-name">${escapeHtml(state.user.name)}</span><span class="user-role">${roleName}</span></span>
          <button class="logout-button" type="button" data-action="logout">Sair</button>
        </div>
      </div>
    </header>
    <main id="main-content" class="page-wrap">
      ${feedbackMarkup()}
      ${content}
    </main>
    <footer class="site-footer">TechNova · Gestão integrada de produtos, clientes e pedidos.</footer>`;
}

function pageHeading(title, description, eyebrow, action = "") {
  return `<div class="page-heading"><div><p class="eyebrow">${eyebrow}</p><h1 tabindex="-1">${title}</h1><p class="page-description">${description}</p></div>${action}</div>`;
}

function productsPage() {
  const categories = [...new Set(state.products.map((product) => product.category))].sort((a, b) => a.localeCompare(b, "pt-BR"));
  const editing = state.productFormId ? productById(state.productFormId) : null;
  const form = isStaff() ? `
    <section class="card card-pad form-card" aria-labelledby="product-form-title">
      <div class="card-heading"><div><h2 id="product-form-title">${editing ? "Editar produto" : "Cadastrar produto"}</h2><p>Campos obrigatórios marcados com *.</p></div></div>
      <form id="product-form" data-product-id="${editing ? editing.id : ""}" novalidate>
        <div class="field-grid">
          <div class="field"><label for="product-name">Nome *</label><input id="product-name" name="name" required maxlength="80" value="${escapeHtml(editing?.name ?? "")}"></div>
          <div class="field"><label for="product-category">Categoria *</label><input id="product-category" name="category" required maxlength="50" value="${escapeHtml(editing?.category ?? "")}"></div>
          <div class="field"><label for="product-price">Preço (R$) *</label><input id="product-price" name="price" type="number" min="0" step="0.01" required value="${editing ? editing.price : ""}"></div>
          <div class="field"><label for="product-stock">Estoque *</label><input id="product-stock" name="stock" type="number" min="0" step="1" required value="${editing ? editing.stock : ""}"></div>
          <div class="field field-full"><label for="product-description">Descrição</label><input id="product-description" name="description" maxlength="180" value="${escapeHtml(editing?.description ?? "")}" placeholder="Detalhes opcionais do produto"></div>
        </div>
        <div class="form-actions"><button class="button button-primary" type="submit">${editing ? "Salvar alterações" : "Cadastrar produto"}</button>${editing ? '<button class="button button-outline" type="button" data-action="cancel-edit-product">Cancelar edição</button>' : ""}</div>
      </form>
    </section>` : "";

  const availableProducts = state.products.filter((product) => isStaff() || product.active);
  const productCards = availableProducts.map((product) => `
    <article class="card product-card" data-product-card data-search="${escapeHtml(`${product.name} ${product.category}`.toLocaleLowerCase("pt-BR"))}" data-category="${escapeHtml(product.category)}">
      <div class="product-topline"><span class="product-icon" aria-hidden="true">▦</span><span class="status-pill ${product.active ? "status-active" : "status-inactive"}">${product.active ? "Ativo" : "Inativo"}</span></div>
      <p class="product-category">${escapeHtml(product.category)}</p>
      <h2 class="product-name">${escapeHtml(product.name)}</h2>
      <p class="product-description">${escapeHtml(product.description || "Produto disponível no catálogo TechNova.")}</p>
      <div class="product-meta"><span class="product-price">${money.format(product.price)}</span><span class="stock-label">${product.stock} ${product.stock === 1 ? "unidade" : "unidades"}</span></div>
      <div class="product-actions">
        <button class="button button-primary button-small" type="button" data-action="add-cart" data-product-id="${product.id}" ${!product.active || product.stock <= 0 ? "disabled" : ""}>Adicionar ao carrinho</button>
        ${isStaff() ? `<button class="button button-outline button-small" type="button" data-action="edit-product" data-product-id="${product.id}">Editar</button>` : ""}
        ${isAdmin() ? `<button class="button ${product.active ? "button-danger" : "button-outline"} button-small" type="button" data-action="toggle-product" data-product-id="${product.id}">${product.active ? "Inativar" : "Ativar"}</button>` : ""}
      </div>
    </article>`).join("");

  return `${pageHeading("Produtos", isStaff() ? "Gerencie o catálogo e acompanhe a disponibilidade do estoque." : "Encontre o que você precisa e adicione ao carrinho.", "Catálogo", isStaff() ? '<span class="section-label">Ações administrativas conforme seu perfil</span>' : "")}
    ${form}
    <div class="toolbar" aria-label="Filtros de produtos">
      <label class="visually-hidden" for="product-search">Buscar produto</label>
      <input class="search-input" id="product-search" type="search" placeholder="Buscar por produto ou categoria">
      <div class="field"><label for="product-category-filter">Categoria</label><select id="product-category-filter"><option value="">Todas as categorias</option>${categories.map((category) => `<option value="${escapeHtml(category)}">${escapeHtml(category)}</option>`).join("")}</select></div>
      <span class="toolbar-spacer"></span><span class="section-label">${availableProducts.length} ${availableProducts.length === 1 ? "produto" : "produtos"} no catálogo</span>
    </div>
    <section id="product-grid" class="product-grid" aria-label="Lista de produtos">
      ${productCards || '<div class="empty-state"><p>Nenhum produto cadastrado.</p></div>'}
      <div id="product-empty-filter" class="empty-state" hidden><p>Nenhum produto corresponde aos filtros.</p></div>
    </section>`;
}

function clientsPage() {
  const rows = state.clients.map((client) => `
    <tr><td><span class="client-initials" aria-hidden="true">${escapeHtml(client.name.split(/\s+/).slice(0, 2).map((part) => part[0]).join("").toLocaleUpperCase("pt-BR"))}</span>${escapeHtml(client.name)}</td>
    <td>${escapeHtml(client.email)}</td><td>${escapeHtml(client.phone || "—")}</td><td>${client.id}</td></tr>`).join("");
  return `${pageHeading("Clientes", "Cadastre clientes e consulte os dados de contato para associar pedidos.", "Relacionamento")}
    <section class="card card-pad form-card" aria-labelledby="client-form-title">
      <div class="card-heading"><div><h2 id="client-form-title">Cadastrar cliente</h2><p>O e-mail deve ser válido e não pode estar cadastrado anteriormente.</p></div></div>
      <form id="client-form" novalidate>
        <div class="field-grid">
          <div class="field"><label for="client-name">Nome completo *</label><input id="client-name" name="name" required maxlength="100" autocomplete="name"></div>
          <div class="field"><label for="client-email">E-mail *</label><input id="client-email" name="email" type="email" required maxlength="120" autocomplete="email"></div>
            <div class="field"><label for="client-password">Senha inicial *</label><input id="client-password" name="password" type="password" required minlength="8" maxlength="100" autocomplete="new-password"></div>
            <div class="field"><label for="client-phone">Telefone *</label><input id="client-phone" name="phone" type="tel" required maxlength="20" autocomplete="tel" placeholder="(00) 00000-0000"></div>
        </div>
        <div class="form-actions"><button class="button button-primary" type="submit">Cadastrar cliente</button></div>
      </form>
    </section>
    <div class="card-heading"><div><h2>Clientes cadastrados</h2><p>${state.clients.length} ${state.clients.length === 1 ? "cliente" : "clientes"} disponíveis para pedidos.</p></div></div>
    <div class="data-table-wrap"><table class="data-table"><caption class="visually-hidden">Clientes cadastrados</caption><thead><tr><th scope="col">Nome</th><th scope="col">E-mail</th><th scope="col">Telefone</th><th scope="col">ID</th></tr></thead><tbody>${rows}</tbody></table></div>`;
}

function cartPage() {
  if (!state.cart.length) {
    return `${pageHeading("Carrinho", "Revise seus itens antes de confirmar o pedido.", "Sua seleção")}
      <div class="empty-state card"><div class="empty-icon" aria-hidden="true">▱</div><h2>Seu carrinho está vazio</h2><p>Explore o catálogo e adicione os produtos que deseja pedir.</p><button class="button button-primary" type="button" data-action="navigate" data-page="products">Ver produtos</button></div>`;
  }
  const items = state.cart.map((line) => {
    const product = productById(line.productId);
    return `<article class="card cart-item">
      <div><p class="cart-product-name">${escapeHtml(product.name)}</p><p class="cart-product-meta">${money.format(product.price)} por unidade · estoque: ${product.stock}</p></div>
      <div class="field cart-quantity"><label for="quantity-${product.id}">Quantidade</label><input class="quantity-input" id="quantity-${product.id}" type="number" min="1" max="${product.stock}" value="${line.quantity}" data-cart-quantity="${product.id}" aria-label="Quantidade de ${escapeHtml(product.name)}"></div>
      <div class="cart-subtotal">${money.format(product.price * line.quantity)}</div>
      <button class="button button-danger button-small cart-remove" type="button" data-action="remove-cart" data-product-id="${product.id}" aria-label="Remover ${escapeHtml(product.name)} do carrinho">Remover</button>
    </article>`;
  }).join("");
  const clientSelector = isStaff() ? `
    <div class="field"><label for="order-client">Cliente do pedido *</label><select id="order-client" required><option value="">Selecione um cliente</option>${state.clients.map((client) => `<option value="${client.id}">${escapeHtml(client.name)}</option>`).join("")}</select></div>` : "";
  return `${pageHeading("Carrinho", "Itens iguais são agrupados; o estoque é conferido novamente ao confirmar.", "Sua seleção")}
    <div class="cart-layout"><section class="cart-list" aria-label="Itens no carrinho">${items}</section>
      <aside class="card summary-card" aria-labelledby="summary-title"><h2 id="summary-title">Resumo do pedido</h2>
        <div class="summary-row"><span>Itens</span><span>${state.cart.reduce((sum, line) => sum + line.quantity, 0)}</span></div>
        <div class="summary-total"><span>Total</span><span>${money.format(cartTotal())}</span></div>
        ${clientSelector}
        <button class="button button-primary" type="button" data-action="confirm-order">Confirmar pedido</button>
        <p class="summary-note">Ao confirmar, o pedido começa em “Em processamento” e o estoque é atualizado no banco.</p>
        <button class="button button-outline full-width spaced-top" type="button" data-action="navigate" data-page="products">Continuar comprando</button>
      </aside>
    </div>`;
}

function ordersPage() {
  const orders = state.orders.filter((order) => isStaff() || order.clientId === state.user.clientId);
  const cards = orders.map((order) => {
    const client = clientById(order.clientId);
    const nextStatus = NEXT_STATUS[order.status];
    return `<article class="card order-card" data-order-status="${order.status}">
      <div class="order-header"><div><h2 class="order-title">Pedido #${order.id}</h2><p class="order-date">${dateTime.format(new Date(order.createdAt))}</p></div><span class="status-pill ${STATUS_CLASSES[order.status]}">${STATUS_LABELS[order.status]}</span></div>
      <div class="order-body"><div class="order-items">${order.items.map((item) => `<span>${item.quantity} × ${escapeHtml(item.name)} <span class="cart-product-meta">(${money.format(item.price)})</span></span>`).join("")}</div><div class="order-total"><span>Total do pedido</span>${money.format(order.total)}</div></div>
      <div class="order-footer"><span class="order-client">Cliente: ${escapeHtml(order.clientName ?? client?.name ?? "Cadastro indisponível")}</span><div class="order-actions">
        ${isAdmin() && nextStatus ? `<button class="button button-outline button-small" type="button" data-action="advance-order" data-order-id="${order.id}">Avançar para ${STATUS_LABELS[nextStatus]}</button>` : ""}
        ${isAdmin() && ["EM_PROCESSAMENTO", "APROVADO"].includes(order.status) ? `<button class="button button-danger button-small" type="button" data-action="cancel-order" data-order-id="${order.id}">Cancelar e devolver estoque</button>` : ""}
      </div></div>
    </article>`;
  }).join("");
  return `${pageHeading("Pedidos", isStaff() ? "Acompanhe os pedidos, atualize as etapas e gerencie cancelamentos." : "Acompanhe o andamento das suas compras.", "Operação")}
    ${isStaff() && orders.length ? `<div class="toolbar"><div class="field"><label for="order-status-filter">Filtrar por status</label><select id="order-status-filter"><option value="">Todos os status</option>${Object.entries(STATUS_LABELS).map(([key, label]) => `<option value="${key}">${label}</option>`).join("")}</select></div><span class="toolbar-spacer"></span><span class="section-label">${orders.length} ${orders.length === 1 ? "pedido" : "pedidos"}</span></div>` : ""}
    <section id="orders-list" class="orders-list" aria-label="Lista de pedidos">${cards || `<div class="empty-state card"><div class="empty-icon" aria-hidden="true">▤</div><h2>Nenhum pedido por aqui</h2><p>${isStaff() ? "Os pedidos confirmados aparecerão nesta lista." : "Quando confirmar um pedido, você poderá acompanhar o andamento nesta página."}</p>${!isStaff() ? '<button class="button button-primary" type="button" data-action="navigate" data-page="products">Explorar produtos</button>' : ""}</div>`}<div id="orders-empty-filter" class="empty-state" hidden><p>Nenhum pedido corresponde a esse status.</p></div></section>`;
}

function render() {
  if (!state.user) {
    application.innerHTML = loginPage();
  } else {
    const page = { products: productsPage, clients: clientsPage, cart: cartPage, orders: ordersPage }[state.page] ?? productsPage;
    application.innerHTML = shell(page());
  }
  if (state.message) notify(state.message.text, state.message.kind);
}

function navigate(page) {
  const permitted = ["products", "cart", "orders"].includes(page) || (page === "clients" && isStaff());
  if (!state.user || !permitted) {
    notify("Você não tem permissão para abrir essa página.", "error");
    return;
  }
  state.page = page;
  state.productFormId = null;
  state.message = null;
  render();
  document.querySelector("#main-content h1")?.focus();
}

async function api(path, options = {}) {
  const headers = { ...(options.body ? { "Content-Type": "application/json" } : {}), ...options.headers };
  const response = await fetch(`${API}${path}`, { ...options, headers, credentials: "same-origin" });
  const data = response.status === 204 ? null : await response.json().catch(() => null);
  if (!response.ok) throw new Error(data?.error || "Não foi possível concluir a operação.");
  return data;
}

async function atualizarDados() {
  const result = await api("/session");
  state.user = result.user ?? null;
  if (!state.user) return;
  const requests = [
    api("/products"),
    api("/orders"),
    isStaff() ? api("/clients") : Promise.resolve([])
  ];
  [state.products, state.orders, state.clients] = await Promise.all(requests);
}

async function executarComFeedback(action) {
  try {
    await action();
  } catch (error) {
    notify(error.message || "Não foi possível concluir a operação.", "error");
  }
}

async function login(form) {
  const email = form.elements.email.value.trim().toLocaleLowerCase("pt-BR");
  const password = form.elements.password.value;
  if (!email || !password) {
    notify("Informe e-mail e senha.", "error");
    return;
  }
  await api("/auth/login", { method: "POST", body: JSON.stringify({ email, password }) });
  await atualizarDados();
  state.page = "products";
  state.message = { text: `Bem-vindo(a), ${state.user.name}.`, kind: "success" };
  render();
}

async function saveProduct(form) {
  const name = form.elements.name.value.trim();
  const category = form.elements.category.value.trim();
  const price = Number(form.elements.price.value);
  const stock = Number(form.elements.stock.value);
  const description = form.elements.description.value.trim();
  if (!name || !category || form.elements.price.value === "" || form.elements.stock.value === "") {
    notify("Preencha nome, categoria, preço e estoque.", "error");
    return;
  }
  if (!Number.isFinite(price) || price < 0 || !Number.isInteger(stock) || stock < 0) {
    notify("O preço deve ser um número não negativo e o estoque deve ser um inteiro não negativo.", "error");
    return;
  }
  const productId = Number(form.dataset.productId);
  const existing = productId ? productById(productId) : null;
  if (existing && stock < cartQuantity(productId)) {
    notify(`O estoque não pode ficar abaixo das ${cartQuantity(productId)} unidade(s) já reservadas no carrinho.`, "error");
    return;
  }
  const body = JSON.stringify({ name, category, price, stock, description });
  const saved = existing
    ? await api(`/products/${existing.id}`, { method: "PUT", body })
    : await api("/products", { method: "POST", body });
  state.products = state.products.filter((product) => product.id !== saved.id);
  state.products.push(saved);
  state.productFormId = null;
  state.message = { text: existing ? "Produto atualizado." : "Produto cadastrado.", kind: "success" };
  render();
}

async function saveClient(form) {
  const name = form.elements.name.value.trim();
  const email = form.elements.email.value.trim().toLocaleLowerCase("pt-BR");
  const password = form.elements.password.value;
  const phone = form.elements.phone.value.trim();
  if (!name || !email || !phone || password.length < 8) {
    notify("Informe nome, e-mail, telefone e uma senha com pelo menos 8 caracteres.", "error");
    return;
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    notify("Informe um e-mail válido.", "error");
    return;
  }
  await api("/clients", { method: "POST", body: JSON.stringify({ name, email, password, phone }) });
  state.clients = await api("/clients");
  state.message = { text: "Cliente cadastrado com sucesso.", kind: "success" };
  render();
}

function addToCart(productId) {
  const product = productById(productId);
  if (!product || !product.active || product.stock <= 0) {
    notify("Este produto não está disponível para compra.", "error");
    return;
  }
  const existing = state.cart.find((line) => line.productId === productId);
  const nextQuantity = (existing?.quantity ?? 0) + 1;
  if (nextQuantity > product.stock) {
    notify(`Estoque insuficiente para ${product.name}. O carrinho não foi alterado.`, "error");
    return;
  }
  if (existing) existing.quantity = nextQuantity;
  else state.cart.push({ productId, quantity: 1 });
  state.message = { text: `${product.name} adicionado ao carrinho.`, kind: "success" };
  render();
}

function changeCartQuantity(input) {
  const productId = Number(input.dataset.cartQuantity);
  const product = productById(productId);
  const quantity = Number(input.value);
  if (!Number.isInteger(quantity) || quantity < 1) {
    notify("A quantidade deve ser um número inteiro maior que zero.", "error");
    return;
  }
  if (!product || quantity > product.stock) {
    notify(`Quantidade indisponível. Estoque atual: ${product?.stock ?? 0}.`, "error");
    return;
  }
  state.cart.find((line) => line.productId === productId).quantity = quantity;
  state.message = null;
  render();
}

async function confirmOrder() {
  if (!state.cart.length) {
    notify("O carrinho está vazio.", "error");
    return;
  }
  const clientId = isStaff() ? Number(document.querySelector("#order-client")?.value) : state.user.clientId;
  if (!clientId || (isStaff() && !clientById(clientId))) {
    notify("Selecione um cliente válido para o pedido.", "error");
    document.querySelector("#order-client")?.focus();
    return;
  }
  for (const line of state.cart) {
    const product = productById(line.productId);
    if (!product || !product.active || line.quantity > product.stock) {
      notify(`Estoque insuficiente ou produto indisponível: ${product?.name ?? "item removido"}.`, "error");
      return;
    }
  }
  const pedido = await api("/orders", {
    method: "POST",
    body: JSON.stringify({
      clientId,
      items: state.cart.map((line) => ({ productId: line.productId, quantity: line.quantity }))
    })
  });
  state.orders.unshift(pedido);
  state.products = await api("/products");
  state.cart = [];
  state.page = "orders";
  state.message = { text: "Pedido confirmado em processamento. O estoque foi atualizado no banco.", kind: "success" };
  render();
}

async function advanceOrder(orderId) {
  if (!isAdmin()) {
    notify("Apenas administradores podem alterar o status dos pedidos.", "error");
    return;
  }
  const order = state.orders.find((candidate) => candidate.id === orderId);
  const nextStatus = order && NEXT_STATUS[order.status];
  if (!nextStatus) {
    notify("Esta transição de status não é permitida.", "error");
    return;
  }
  await api(`/orders/${order.id}/status`, {
    method: "PUT",
    body: JSON.stringify({ status: nextStatus })
  });
  state.orders = await api("/orders");
  state.message = { text: `Pedido #${order.id} atualizado para ${STATUS_LABELS[nextStatus]}.`, kind: "success" };
  render();
}

async function cancelOrder(orderId) {
  if (!isAdmin()) {
    notify("Apenas administradores podem cancelar pedidos.", "error");
    return;
  }
  const order = state.orders.find((candidate) => candidate.id === orderId);
  if (!order || !["EM_PROCESSAMENTO", "APROVADO"].includes(order.status)) {
    notify("Este pedido não pode mais ser cancelado.", "error");
    return;
  }
  if (!window.confirm(`Cancelar o pedido #${order.id} e devolver os itens ao estoque?`)) return;
  await api(`/orders/${order.id}/status`, {
    method: "PUT",
    body: JSON.stringify({ status: "CANCELADO" })
  });
  state.orders = await api("/orders");
  state.products = await api("/products");
  state.message = { text: `Pedido #${order.id} cancelado e estoque devolvido.`, kind: "success" };
  render();
}

function filterProducts() {
  const query = document.querySelector("#product-search")?.value.trim().toLocaleLowerCase("pt-BR") ?? "";
  const category = document.querySelector("#product-category-filter")?.value ?? "";
  let visible = 0;
  document.querySelectorAll("[data-product-card]").forEach((card) => {
    const match = card.dataset.search.includes(query) && (!category || card.dataset.category === category);
    card.hidden = !match;
    if (match) visible++;
  });
  const empty = document.querySelector("#product-empty-filter");
  if (empty) empty.hidden = visible > 0 || document.querySelectorAll("[data-product-card]").length === 0;
}

function filterOrders() {
  const status = document.querySelector("#order-status-filter")?.value ?? "";
  let visible = 0;
  document.querySelectorAll(".order-card").forEach((card) => {
    const match = !status || card.dataset.orderStatus === status;
    card.hidden = !match;
    if (match) visible++;
  });
  const empty = document.querySelector("#orders-empty-filter");
  if (empty) empty.hidden = visible > 0;
}

async function onClick(event) {
  const button = event.target.closest("[data-action]");
  if (!button) return;
  const action = button.dataset.action;
  const productId = Number(button.dataset.productId);
  switch (action) {
    case "fill-login": {
      const user = DEMO_USERS.find((candidate) => candidate.id === Number(button.dataset.userId));
      if (!user) return;
      document.querySelector("#login-email").value = user.email;
      document.querySelector("#login-password").value = user.password;
      document.querySelector("#login-password").focus();
      break;
    }
    case "navigate": navigate(button.dataset.page); break;
    case "logout":
      await api("/auth/logout", { method: "POST" });
      state.user = null;
      state.page = "login";
      state.cart = [];
      state.products = [];
      state.clients = [];
      state.orders = [];
      state.productFormId = null;
      state.message = null;
      render();
      break;
    case "add-cart": addToCart(productId); break;
    case "remove-cart":
      state.cart = state.cart.filter((line) => line.productId !== productId);
      state.message = { text: "Item removido do carrinho.", kind: "success" };
      render();
      break;
    case "edit-product":
      if (!isStaff()) return notify("Seu perfil não pode editar produtos.", "error");
      state.productFormId = productId;
      state.message = null;
      render();
      document.querySelector("#product-name")?.focus();
      break;
    case "cancel-edit-product":
      state.productFormId = null;
      state.message = null;
      render();
      break;
    case "toggle-product": {
      if (!isAdmin()) return notify("Apenas administradores podem ativar ou inativar produtos.", "error");
      const product = productById(productId);
      if (!product) return notify("Produto não encontrado.", "error");
      const updated = await api(`/products/${productId}/active`, { method: "PATCH" });
      state.products = state.products.map((item) => item.id === updated.id ? updated : item);
      state.message = { text: `Produto ${updated.active ? "ativado" : "inativado"}.`, kind: "success" };
      render();
      break;
    }
    case "confirm-order": await confirmOrder(); break;
    case "advance-order": await advanceOrder(Number(button.dataset.orderId)); break;
    case "cancel-order": await cancelOrder(Number(button.dataset.orderId)); break;
    default: break;
  }
}

application.addEventListener("click", (event) => {
  void executarComFeedback(() => onClick(event));
});
application.addEventListener("submit", (event) => {
  event.preventDefault();
  void executarComFeedback(async () => {
    if (event.target.id === "login-form") await login(event.target);
    if (event.target.id === "product-form" && isStaff()) await saveProduct(event.target);
    if (event.target.id === "client-form" && isStaff()) await saveClient(event.target);
  });
});
application.addEventListener("input", (event) => {
  if (event.target.id === "product-search") filterProducts();
});
application.addEventListener("change", (event) => {
  if (event.target.id === "product-category-filter") filterProducts();
  if (event.target.id === "order-status-filter") filterOrders();
  if (event.target.matches("[data-cart-quantity]")) changeCartQuantity(event.target);
});

void executarComFeedback(async () => {
  await atualizarDados();
  render();
});
