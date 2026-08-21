// Category definitions with icons and colors
const CATEGORIES = [
  { id: 'FOOD', name: 'Food & Dining', color: '#ff6d00', icon: 'fastfood' },
  { id: 'TRAVEL', name: 'Travel & Commute', color: '#0091ea', icon: 'directions_bus' },
  { id: 'BILLS', name: 'Bills & Utilities', color: '#7c4dff', icon: 'receipt_long' },
  { id: 'SHOPPING', name: 'Shopping', color: '#ff4081', icon: 'local_mall' },
  { id: 'GROCERIES', name: 'Groceries', color: '#00c853', icon: 'shopping_cart' },
  { id: 'ENTERTAINMENT', name: 'Entertainment', color: '#aeea00', icon: 'movie' },
  { id: 'OTHERS', name: 'Others', color: '#78909c', icon: 'more_horiz' }
];

const PAYMENT_MODES = [
  { id: 'UPI', name: 'UPI / GPay', tag: 'UPI', icon: 'qr_code_scanner' },
  { id: 'CASH', name: 'Cash', tag: 'CASH', icon: 'payments' },
  { id: 'CREDIT_CARD', name: 'Credit Card', tag: 'CC', icon: 'credit_card' },
  { id: 'DEBIT_CARD', name: 'Debit Card', tag: 'BANK', icon: 'account_balance_wallet' }
];

// Initial Seed Data (Only if no local storage exists)
const DEFAULT_EXPENSES = [
  { id: 1, title: 'Healthy Brunch & Coffee', amount: 380, category: 'FOOD', paymentMode: 'UPI', timestamp: new Date(Date.now() - 2 * 3600000).toISOString(), notes: 'Blue Tokai Coffee' },
  { id: 2, title: 'Cab to Office', amount: 220, category: 'TRAVEL', paymentMode: 'UPI', timestamp: new Date(Date.now() - 5 * 3600000).toISOString(), notes: 'Ola Prime' },
  { id: 3, title: 'Weekly Grocery Staples', amount: 1450, category: 'GROCERIES', paymentMode: 'CREDIT_CARD', timestamp: new Date(Date.now() - 28 * 3600000).toISOString(), notes: "Nature's Basket" },
  { id: 4, title: 'Electricity & Wifi Bill', amount: 1890, category: 'BILLS', paymentMode: 'DEBIT_CARD', timestamp: new Date(Date.now() - 32 * 3600000).toISOString(), notes: 'Airtel Broadband' },
  { id: 5, title: 'Team Dinner', amount: 1200, category: 'FOOD', paymentMode: 'CREDIT_CARD', timestamp: new Date(Date.now() - 52 * 3600000).toISOString(), notes: 'Burmese Kitchen' },
  { id: 6, title: 'Running Shoes', amount: 3499, category: 'SHOPPING', paymentMode: 'CREDIT_CARD', timestamp: new Date(Date.now() - 76 * 3600000).toISOString(), notes: 'Nike Pegasus' },
  { id: 7, title: 'Metro Card Recharge', amount: 500, category: 'TRAVEL', paymentMode: 'UPI', timestamp: new Date(Date.now() - 100 * 3600000).toISOString(), notes: 'Monthly pass' },
  { id: 8, title: 'Movie IMAX Tickets', amount: 860, category: 'ENTERTAINMENT', paymentMode: 'UPI', timestamp: new Date(Date.now() - 124 * 3600000).toISOString(), notes: 'BookMyShow' },
  { id: 9, title: 'Pharmacy & Supplements', amount: 640, category: 'OTHERS', paymentMode: 'CASH', timestamp: new Date(Date.now() - 148 * 3600000).toISOString(), notes: 'Apollo Pharmacy' }
];

// App State
class ExpenseAppState {
  constructor() {
    this.expenses = this.loadExpenses();
    this.monthlyBudget = parseFloat(localStorage.getItem('expense_monthly_budget')) || 35000;
    this.dailyLimit = parseFloat(localStorage.getItem('expense_daily_limit')) || 1200;
    this.selectedCalendarMonth = new Date();
    this.selectedCalendarDate = new Date();
    this.selectedBarIndex = null;
    this.deletedExpense = null;
    this.editingExpense = null;
    this.activeTab = 'tab-home';

    // Quick Log Draft
    this.draftAmount = '';
    this.draftCategory = 'FOOD';
    this.draftPayment = 'UPI';
    this.draftTitle = '';

    // History Filters
    this.historyQuery = '';
    this.historyRange = 'THIS_MONTH';
    this.historyCategory = null;
  }

  loadExpenses() {
    const saved = localStorage.getItem('expenses_data');
    if (saved !== null) {
      try { return JSON.parse(saved); } catch (e) { }
    }
    return DEFAULT_EXPENSES;
  }

  saveExpenses() {
    localStorage.setItem('expenses_data', JSON.stringify(this.expenses));
  }

  saveCaps(budget, limit) {
    this.monthlyBudget = budget;
    this.dailyLimit = limit;
    localStorage.setItem('expense_monthly_budget', budget.toString());
    localStorage.setItem('expense_daily_limit', limit.toString());
  }

  clearAllData() {
    this.expenses = [];
    this.saveExpenses();
  }

  addExpense(item) {
    this.expenses.unshift(item);
    this.saveExpenses();
  }

  updateExpense(item) {
    const idx = this.expenses.findIndex(e => e.id === item.id);
    if (idx !== -1) {
      this.expenses[idx] = item;
      this.saveExpenses();
    }
  }

  deleteExpense(id) {
    const idx = this.expenses.findIndex(e => e.id === id);
    if (idx !== -1) {
      this.deletedExpense = this.expenses[idx];
      this.expenses.splice(idx, 1);
      this.saveExpenses();
    }
  }

  undoDelete() {
    if (this.deletedExpense) {
      this.expenses.unshift(this.deletedExpense);
      this.deletedExpense = null;
      this.saveExpenses();
    }
  }
}

const state = new ExpenseAppState();

// DOM Elements
const heroTotalMonth = document.getElementById('heroTotalMonth');
const heroTodaySpend = document.getElementById('heroTodaySpend');
const hero7DaySpend = document.getElementById('hero7DaySpend');
const heroDeltaBadge = document.getElementById('heroDeltaBadge');
const heroDeltaIcon = document.getElementById('heroDeltaIcon');
const heroDeltaText = document.getElementById('heroDeltaText');

const budgetPercent = document.getElementById('budgetPercent');
const ringIndicator = document.getElementById('ringIndicator');
const budgetRemaining = document.getElementById('budgetRemaining');
const budgetCapDisplay = document.getElementById('budgetCapDisplay');
const budgetAlertIcon = document.getElementById('budgetAlertIcon');
const budgetWarningText = document.getElementById('budgetWarningText');
const editBudgetQuickBtn = document.getElementById('editBudgetQuickBtn');

const barChartCanvas = document.getElementById('barChartCanvas');
const chartLimitLabel = document.getElementById('chartLimitLabel');
const chartDetailOverlay = document.getElementById('chartDetailOverlay');
const detailDate = document.getElementById('detailDate');
const detailTotal = document.getElementById('detailTotal');
const detailChips = document.getElementById('detailChips');

const recentTransactionsList = document.getElementById('recentTransactionsList');
const calendarDaysGrid = document.getElementById('calendarDaysGrid');
const currentMonthYear = document.getElementById('currentMonthYear');
const calendarSelectedDateLabel = document.getElementById('calendarSelectedDateLabel');
const calendarSelectedDayTotal = document.getElementById('calendarSelectedDayTotal');
const calendarSelectedDayCount = document.getElementById('calendarSelectedDayCount');
const calendarDateTransactionsList = document.getElementById('calendarDateTransactionsList');

const historyFilteredTotal = document.getElementById('historyFilteredTotal');
const historySearchInput = document.getElementById('historySearchInput');
const searchClearBtn = document.getElementById('searchClearBtn');
const categoryFilterRow = document.getElementById('categoryFilterRow');
const historyTransactionsList = document.getElementById('historyTransactionsList');
const exportCsvBtn = document.getElementById('exportCsvBtn');

const quickLogModal = document.getElementById('quickLogModal');
const openQuickLogFab = document.getElementById('openQuickLogFab');
const numpadAmountDisplay = document.getElementById('numpadAmountDisplay');
const quickCategoryChips = document.getElementById('quickCategoryChips');
const quickPaymentChips = document.getElementById('quickPaymentChips');
const quickTitleInput = document.getElementById('quickTitleInput');
const quickSaveBtn = document.getElementById('quickSaveBtn');
const quickSaveBtnText = document.getElementById('quickSaveBtnText');

// Edit Modal Elements
const editTransactionModal = document.getElementById('editTransactionModal');
const editAmountInput = document.getElementById('editAmountInput');
const editTitleInput = document.getElementById('editTitleInput');
const editCategoryChips = document.getElementById('editCategoryChips');
const editPaymentChips = document.getElementById('editPaymentChips');
const editNotesInput = document.getElementById('editNotesInput');
const saveEditBtn = document.getElementById('saveEditBtn');
const deleteInEditModalBtn = document.getElementById('deleteInEditModalBtn');

// Settings Modal Elements
const settingsModal = document.getElementById('settingsModal');
const openSettingsBtn = document.getElementById('openSettingsBtn');
const settingsMonthlyBudgetInput = document.getElementById('settingsMonthlyBudgetInput');
const settingsDailyLimitInput = document.getElementById('settingsDailyLimitInput');
const saveSettingsBtn = document.getElementById('saveSettingsBtn');
const clearAllDataBtn = document.getElementById('clearAllDataBtn');

const snackbar = document.getElementById('snackbar');
const snackbarText = document.getElementById('snackbarText');
const snackbarUndoBtn = document.getElementById('snackbarUndoBtn');
const themeToggleBtn = document.getElementById('themeToggleBtn');
const themeIcon = document.getElementById('themeIcon');

// Format Currency
function formatINR(amount) {
  return '₹' + Math.round(amount).toLocaleString('en-IN');
}

// Update Status Time
function updateClock() {
  const now = new Date();
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  document.getElementById('statusTime').textContent = `${hours}:${minutes}`;
}
setInterval(updateClock, 1000);
updateClock();

// RENDER DASHBOARD
function renderDashboard() {
  const now = new Date();
  const currentMonth = now.getMonth();
  const currentYear = now.getFullYear();

  // Monthly Total
  const monthExpenses = state.expenses.filter(e => {
    const d = new Date(e.timestamp);
    return d.getMonth() === currentMonth && d.getFullYear() === currentYear;
  });
  const totalMonth = monthExpenses.reduce((sum, e) => sum + e.amount, 0);
  heroTotalMonth.textContent = formatINR(totalMonth);

  // Today & Yesterday Spend
  const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
  const yesterdayStart = todayStart - 86400000;

  const todaySpend = state.expenses
    .filter(e => new Date(e.timestamp).getTime() >= todayStart)
    .reduce((sum, e) => sum + e.amount, 0);

  const yesterdaySpend = state.expenses
    .filter(e => {
      const t = new Date(e.timestamp).getTime();
      return t >= yesterdayStart && t < todayStart;
    })
    .reduce((sum, e) => sum + e.amount, 0);

  heroTodaySpend.textContent = formatINR(todaySpend);

  // 7-day spend
  const sevenDaysAgo = todayStart - 6 * 86400000;
  const running7Day = state.expenses
    .filter(e => new Date(e.timestamp).getTime() >= sevenDaysAgo)
    .reduce((sum, e) => sum + e.amount, 0);
  hero7DaySpend.textContent = formatINR(running7Day);

  // Comparison Delta
  const isSpendingMore = todaySpend > yesterdaySpend;
  if (yesterdaySpend > 0) {
    const delta = Math.round(((todaySpend - yesterdaySpend) / yesterdaySpend) * 100);
    heroDeltaText.textContent = `${delta >= 0 ? '+' : ''}${delta}% vs y'day`;
    heroDeltaBadge.className = `delta-badge ${isSpendingMore ? 'increased' : ''}`;
    heroDeltaIcon.textContent = isSpendingMore ? 'trending_up' : 'trending_down';
  } else {
    heroDeltaText.textContent = `Today: ${formatINR(todaySpend)}`;
    heroDeltaBadge.className = 'delta-badge';
    heroDeltaIcon.textContent = 'calendar_today';
  }

  // Budget Progress
  const pct = Math.min((totalMonth / state.monthlyBudget) * 100, 150);
  budgetPercent.textContent = `${Math.round(pct)}% Used`;
  const remaining = Math.max(state.monthlyBudget - totalMonth, 0);
  budgetRemaining.textContent = formatINR(remaining);
  budgetCapDisplay.textContent = formatINR(state.monthlyBudget);
  chartLimitLabel.textContent = `Limit: ${formatINR(state.dailyLimit)}`;

  // Stroke Dashoffset
  const offset = 138.2 - (138.2 * Math.min(pct, 100)) / 100;
  ringIndicator.style.strokeDashoffset = offset;

  if (pct >= 100) {
    ringIndicator.style.stroke = 'var(--spending-red)';
    budgetPercent.style.color = 'var(--spending-red)';
    budgetAlertIcon.style.display = 'inline-block';
    budgetAlertIcon.style.color = 'var(--spending-red)';
    budgetWarningText.style.display = 'block';
    budgetWarningText.textContent = 'Budget exceeded! Freeze non-essential spend.';
    budgetWarningText.style.color = 'var(--spending-red)';
  } else if (pct >= 80) {
    ringIndicator.style.stroke = 'var(--warning-orange)';
    budgetPercent.style.color = 'var(--warning-orange)';
    budgetAlertIcon.style.display = 'inline-block';
    budgetAlertIcon.style.color = 'var(--warning-orange)';
    budgetWarningText.style.display = 'block';
    budgetWarningText.textContent = 'Approaching budget limit (80% crossed).';
    budgetWarningText.style.color = 'var(--warning-orange)';
  } else {
    ringIndicator.style.stroke = 'var(--safe-green)';
    budgetPercent.style.color = 'var(--safe-green)';
    budgetAlertIcon.style.display = 'none';
    budgetWarningText.style.display = 'none';
  }

  // Recent 5 Transactions
  renderRecentTransactions();

  // Render 7-day Bar Chart
  renderBarChart();
}

// 7-DAY BAR CHART (CANVAS)
function renderBarChart() {
  const ctx = barChartCanvas.getContext('2d');
  const dpr = window.devicePixelRatio || 1;
  const rect = barChartCanvas.getBoundingClientRect();

  barChartCanvas.width = rect.width * dpr;
  barChartCanvas.height = rect.height * dpr;
  ctx.scale(dpr, dpr);

  const w = rect.width;
  const h = rect.height;
  ctx.clearRect(0, 0, w, h);

  // Compute 7 days
  const now = new Date();
  const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
  const dayData = [];

  for (let i = 6; i >= 0; i--) {
    const dayStart = todayStart - i * 86400000;
    const dayEnd = dayStart + 86400000;
    const dateObj = new Date(dayStart);

    const dayExpenses = state.expenses.filter(e => {
      const t = new Date(e.timestamp).getTime();
      return t >= dayStart && t < dayEnd;
    });

    const total = dayExpenses.reduce((sum, e) => sum + e.amount, 0);
    const catMap = {};
    dayExpenses.forEach(e => {
      catMap[e.category] = (catMap[e.category] || 0) + e.amount;
    });

    const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    dayData.push({
      date: dateObj,
      label: i === 0 ? 'Today' : dayNames[dateObj.getDay()],
      total,
      isToday: i === 0,
      categories: catMap
    });
  }

  const maxSpend = Math.max(...dayData.map(d => d.total), state.dailyLimit * 1.3, 2000);
  const chartHeight = h - 28;
  const barSlot = w / 7;
  const barWidth = barSlot * 0.46;

  // Draw Limit Line
  const limitY = chartHeight * (1 - state.dailyLimit / maxSpend);
  ctx.strokeStyle = 'rgba(255, 145, 0, 0.5)';
  ctx.lineWidth = 1.5;
  ctx.setLineDash([6, 5]);
  ctx.beginPath();
  ctx.moveTo(0, limitY);
  ctx.lineTo(w, limitY);
  ctx.stroke();
  ctx.setLineDash([]);

  // Draw Bars
  dayData.forEach((d, idx) => {
    const isSelected = state.selectedBarIndex === idx;
    const isExceeded = d.total > state.dailyLimit;
    const barH = Math.max((d.total / maxSpend) * chartHeight, d.total > 0 ? 8 : 3);
    const x = idx * barSlot + (barSlot - barWidth) / 2;
    const y = chartHeight - barH;

    // Slot Background
    ctx.fillStyle = 'rgba(128, 128, 128, 0.08)';
    roundRect(ctx, x, 0, barWidth, chartHeight, 6);
    ctx.fill();

    // Active Bar
    if (isSelected) {
      ctx.fillStyle = isExceeded ? '#ff5252' : '#66ffa6';
    } else if (isExceeded) {
      ctx.fillStyle = 'rgba(255, 82, 82, 0.85)';
    } else if (d.isToday) {
      ctx.fillStyle = 'var(--primary-accent)';
    } else if (d.total === 0) {
      ctx.fillStyle = 'rgba(128, 128, 128, 0.2)';
    } else {
      ctx.fillStyle = 'rgba(0, 200, 83, 0.65)';
    }

    roundRect(ctx, x, y, barWidth, barH, 6);
    ctx.fill();

    // Day Label
    ctx.fillStyle = d.isToday ? 'var(--primary-accent)' : 'var(--text-muted)';
    ctx.font = `${d.isToday || isSelected ? '700' : '500'} 11px ${getComputedStyle(document.body).getPropertyValue('--font-family')}`;
    ctx.textAlign = 'center';
    ctx.fillText(d.label, x + barWidth / 2, h - 6);
  });

  barChartCanvas._dayData = dayData;
  barChartCanvas._barSlot = barSlot;
}

function roundRect(ctx, x, y, w, h, r) {
  if (w < 2 * r) r = w / 2;
  if (h < 2 * r) r = h / 2;
  ctx.beginPath();
  ctx.moveTo(x + r, y);
  ctx.arcTo(x + w, y, x + w, y + h, r);
  ctx.arcTo(x + w, y + h, x, y + h, r);
  ctx.arcTo(x, y + h, x, y, r);
  ctx.arcTo(x, y, x + w, y, r);
  ctx.closePath();
}

// Bar Chart Click
barChartCanvas.addEventListener('click', (e) => {
  const rect = barChartCanvas.getBoundingClientRect();
  const x = e.clientX - rect.left;
  const idx = Math.floor(x / barChartCanvas._barSlot);

  if (idx >= 0 && idx < 7) {
    state.selectedBarIndex = state.selectedBarIndex === idx ? null : idx;
    renderBarChart();

    if (state.selectedBarIndex !== null) {
      const d = barChartCanvas._dayData[idx];
      chartDetailOverlay.style.display = 'block';
      detailDate.textContent = d.date.toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short' });
      detailTotal.textContent = formatINR(d.total);
      detailTotal.style.color = d.total > state.dailyLimit ? 'var(--spending-red)' : 'var(--primary-accent)';

      detailChips.innerHTML = '';
      const catKeys = Object.keys(d.categories);
      if (catKeys.length > 0) {
        catKeys.forEach(catId => {
          const cat = CATEGORIES.find(c => c.id === catId) || CATEGORIES[6];
          const chip = document.createElement('div');
          chip.className = 'detail-chip';
          chip.style.background = `${cat.color}25`;
          chip.innerHTML = `<span style="width:6px; height:6px; border-radius:50%; background:${cat.color}"></span><span>${cat.name}: ${formatINR(d.categories[catId])}</span>`;
          detailChips.appendChild(chip);
        });
      } else {
        detailChips.innerHTML = '<span style="font-size:11px; color:var(--text-muted)">No expenses recorded</span>';
      }
    } else {
      chartDetailOverlay.style.display = 'none';
    }
  }
});

// RECENT TRANSACTIONS
function renderRecentTransactions() {
  const recent = state.expenses.slice(0, 5);
  recentTransactionsList.innerHTML = '';

  if (recent.length === 0) {
    recentTransactionsList.innerHTML = `
      <div style="text-align:center; padding: 24px; color:var(--text-muted);">
        <span class="material-symbols-rounded" style="font-size:42px; opacity:0.4;">receipt</span>
        <p style="font-size:14px; font-weight:700; color:var(--text); margin-top:6px;">No expenses logged yet</p>
        <p style="font-size:12px; margin-top:2px;">Tap the + button to log your first spend</p>
      </div>`;
    return;
  }

  recent.forEach(tx => {
    recentTransactionsList.appendChild(createTransactionCard(tx));
  });
}

function createTransactionCard(tx) {
  const cat = CATEGORIES.find(c => c.id === tx.category) || CATEGORIES[6];
  const mode = PAYMENT_MODES.find(m => m.id === tx.paymentMode) || PAYMENT_MODES[0];
  const dateObj = new Date(tx.timestamp);
  const timeStr = dateObj.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });

  const card = document.createElement('div');
  card.className = 'transaction-card';
  card.style.cursor = 'pointer';
  card.innerHTML = `
    <div class="tx-icon-badge" style="background: ${cat.color}20; color: ${cat.color}">
      <span class="material-symbols-rounded">${cat.icon}</span>
    </div>
    <div class="tx-info">
      <div class="tx-title">${tx.title}</div>
      <div class="tx-meta">
        <span class="tag-pill">${mode.tag}</span>
        <span>${timeStr}</span>
        ${tx.notes ? `<span>• ${tx.notes}</span>` : ''}
      </div>
    </div>
    <div class="tx-amount">-${formatINR(tx.amount)}</div>
    <button class="tx-delete-btn" title="Delete"><span class="material-symbols-rounded">delete</span></button>
  `;

  // Tap to edit
  card.addEventListener('click', (e) => {
    if (e.target.closest('.tx-delete-btn')) return;
    openEditModal(tx);
  });

  card.querySelector('.tx-delete-btn').addEventListener('click', (e) => {
    e.stopPropagation();
    state.deleteExpense(tx.id);
    renderAll();
    showSnackbar(`Deleted ${tx.title}`, true);
  });

  return card;
}

// EDIT TRANSACTION MODAL
let activeEditCategory = 'FOOD';
let activeEditPayment = 'UPI';

function openEditModal(tx) {
  state.editingExpense = tx;
  editAmountInput.value = tx.amount;
  editTitleInput.value = tx.title;
  editNotesInput.value = tx.notes || '';
  activeEditCategory = tx.category;
  activeEditPayment = tx.paymentMode;

  renderEditCategoryChips();
  renderEditPaymentChips();
  editTransactionModal.classList.add('open');
}

function closeEditModal() {
  editTransactionModal.classList.remove('open');
  state.editingExpense = null;
}

function renderEditCategoryChips() {
  editCategoryChips.innerHTML = '';
  CATEGORIES.forEach(cat => {
    const chip = document.createElement('button');
    const isSel = activeEditCategory === cat.id;
    chip.className = `filter-chip ${isSel ? 'active' : ''}`;
    if (isSel) chip.style.background = cat.color;
    chip.innerHTML = `<span class="material-symbols-rounded" style="font-size:16px; margin-right:4px; vertical-align:middle;">${cat.icon}</span>${cat.name}`;
    chip.addEventListener('click', () => {
      activeEditCategory = cat.id;
      renderEditCategoryChips();
    });
    editCategoryChips.appendChild(chip);
  });
}

function renderEditPaymentChips() {
  editPaymentChips.innerHTML = '';
  PAYMENT_MODES.forEach(mode => {
    const chip = document.createElement('button');
    const isSel = activeEditPayment === mode.id;
    chip.className = `filter-chip ${isSel ? 'active' : ''}`;
    chip.innerHTML = `<span class="material-symbols-rounded" style="font-size:16px; margin-right:4px; vertical-align:middle;">${mode.icon}</span>${mode.name}`;
    chip.addEventListener('click', () => {
      activeEditPayment = mode.id;
      renderEditPaymentChips();
    });
    editPaymentChips.appendChild(chip);
  });
}

saveEditBtn.addEventListener('click', () => {
  if (!state.editingExpense) return;
  const amount = parseFloat(editAmountInput.value);
  if (!amount || amount <= 0) return;

  const catObj = CATEGORIES.find(c => c.id === activeEditCategory);
  const updated = {
    ...state.editingExpense,
    title: editTitleInput.value.trim() || `${catObj?.name || 'Expense'} Spend`,
    amount: amount,
    category: activeEditCategory,
    paymentMode: activeEditPayment,
    notes: editNotesInput.value.trim()
  };

  state.updateExpense(updated);
  closeEditModal();
  renderAll();
  showSnackbar(`Updated ${updated.title}`);
});

deleteInEditModalBtn.addEventListener('click', () => {
  if (!state.editingExpense) return;
  const id = state.editingExpense.id;
  const title = state.editingExpense.title;
  state.deleteExpense(id);
  closeEditModal();
  renderAll();
  showSnackbar(`Deleted ${title}`, true);
});

editTransactionModal.addEventListener('click', (e) => {
  if (e.target === editTransactionModal) closeEditModal();
});

// SETTINGS & BUDGET CAPS MODAL
function openSettingsModal() {
  settingsMonthlyBudgetInput.value = state.monthlyBudget;
  settingsDailyLimitInput.value = state.dailyLimit;
  settingsModal.classList.add('open');
}

function closeSettingsModal() {
  settingsModal.classList.remove('open');
}

openSettingsBtn.addEventListener('click', openSettingsModal);
editBudgetQuickBtn.addEventListener('click', openSettingsModal);

saveSettingsBtn.addEventListener('click', () => {
  const budget = parseFloat(settingsMonthlyBudgetInput.value) || 35000;
  const limit = parseFloat(settingsDailyLimitInput.value) || 1200;
  state.saveCaps(budget, limit);
  closeSettingsModal();
  renderAll();
  showSnackbar('Budget caps updated successfully!');
});

clearAllDataBtn.addEventListener('click', () => {
  if (confirm('Are you sure you want to clear all expenses and start fresh from ₹0?')) {
    state.clearAllData();
    closeSettingsModal();
    renderAll();
    showSnackbar('All data cleared. Fresh start ready!');
  }
});

settingsModal.addEventListener('click', (e) => {
  if (e.target === settingsModal) closeSettingsModal();
});

// RENDER CALENDAR INSPECTOR
function renderCalendar() {
  const current = state.selectedCalendarMonth;
  const year = current.getFullYear();
  const month = current.getMonth();

  currentMonthYear.textContent = current.toLocaleDateString('en-IN', { month: 'long', year: 'numeric' });

  // Compute daily spend map
  const dailySpendMap = {};
  state.expenses.forEach(e => {
    const d = new Date(e.timestamp);
    if (d.getFullYear() === year && d.getMonth() === month) {
      const day = d.getDate();
      dailySpendMap[day] = (dailySpendMap[day] || 0) + e.amount;
    }
  });

  const firstDay = new Date(year, month, 1);
  const startDay = (firstDay.getDay() + 6) % 7; // Monday = 0
  const daysInMonth = new Date(year, month + 1, 0).getDate();

  calendarDaysGrid.innerHTML = '';

  for (let i = 0; i < startDay; i++) {
    const empty = document.createElement('div');
    empty.className = 'cal-day-cell empty';
    calendarDaysGrid.appendChild(empty);
  }

  const today = new Date();
  const isCurrentMonth = today.getFullYear() === year && today.getMonth() === month;

  for (let d = 1; d <= daysInMonth; d++) {
    const cell = document.createElement('div');
    cell.className = 'cal-day-cell';
    const isToday = isCurrentMonth && today.getDate() === d;
    const isSelected = state.selectedCalendarDate.getFullYear() === year &&
                       state.selectedCalendarDate.getMonth() === month &&
                       state.selectedCalendarDate.getDate() === d;

    if (isToday) cell.classList.add('today');
    if (isSelected) cell.classList.add('selected');

    const spend = dailySpendMap[d] || 0;
    cell.innerHTML = `<span>${d}</span>${spend > 0 ? '<div class="cal-dot"></div>' : ''}`;

    cell.addEventListener('click', () => {
      state.selectedCalendarDate = new Date(year, month, d);
      renderCalendar();
    });

    calendarDaysGrid.appendChild(cell);
  }

  const sel = state.selectedCalendarDate;
  calendarSelectedDateLabel.textContent = sel.toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }).toUpperCase();

  const dayExpenses = state.expenses.filter(e => {
    const d = new Date(e.timestamp);
    return d.getFullYear() === sel.getFullYear() &&
           d.getMonth() === sel.getMonth() &&
           d.getDate() === sel.getDate();
  });

  const totalDaySpend = dayExpenses.reduce((sum, e) => sum + e.amount, 0);
  calendarSelectedDayTotal.textContent = formatINR(totalDaySpend);
  calendarSelectedDayCount.textContent = `${dayExpenses.length} item${dayExpenses.length === 1 ? '' : 's'}`;

  calendarDateTransactionsList.innerHTML = '';
  if (dayExpenses.length === 0) {
    calendarDateTransactionsList.innerHTML = `
      <div style="text-align:center; padding: 24px; color:var(--text-muted); background:var(--surface); border:1px solid var(--border); border-radius:16px;">
        <span class="material-symbols-rounded text-primary" style="font-size:36px;">check_circle</span>
        <p style="font-size:14px; font-weight:700; color:var(--text); margin-top:6px;">Zero spend recorded</p>
        <p style="font-size:12px; margin-top:2px;">No expenses on this date</p>
      </div>`;
  } else {
    dayExpenses.forEach(tx => {
      calendarDateTransactionsList.appendChild(createTransactionCard(tx));
    });
  }
}

document.getElementById('prevMonthBtn').addEventListener('click', () => {
  state.selectedCalendarMonth = new Date(state.selectedCalendarMonth.getFullYear(), state.selectedCalendarMonth.getMonth() - 1, 1);
  renderCalendar();
});
document.getElementById('nextMonthBtn').addEventListener('click', () => {
  state.selectedCalendarMonth = new Date(state.selectedCalendarMonth.getFullYear(), state.selectedCalendarMonth.getMonth() + 1, 1);
  renderCalendar();
});

// RENDER HISTORY SCREEN
function renderHistory() {
  const query = state.historyQuery.toLowerCase();
  const now = new Date();
  const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();

  let filtered = state.expenses.filter(e => {
    const t = new Date(e.timestamp).getTime();
    if (state.historyRange === 'THIS_WEEK') {
      const weekStart = todayStart - ((now.getDay() + 6) % 7) * 86400000;
      return t >= weekStart;
    } else if (state.historyRange === 'THIS_MONTH') {
      const monthStart = new Date(now.getFullYear(), now.getMonth(), 1).getTime();
      return t >= monthStart;
    }
    return true;
  });

  if (state.historyCategory) {
    filtered = filtered.filter(e => e.category === state.historyCategory);
  }

  if (query.trim()) {
    filtered = filtered.filter(e => e.title.toLowerCase().includes(query) || (e.notes && e.notes.toLowerCase().includes(query)));
  }

  const total = filtered.reduce((sum, e) => sum + e.amount, 0);
  historyFilteredTotal.textContent = formatINR(total);

  historyTransactionsList.innerHTML = '';
  if (filtered.length === 0) {
    historyTransactionsList.innerHTML = `
      <div style="text-align:center; padding: 40px 20px; color:var(--text-muted);">
        <span class="material-symbols-rounded" style="font-size:48px; opacity:0.4;">search_off</span>
        <p style="font-size:15px; font-weight:700; color:var(--text); margin-top:8px;">No matching expenses found</p>
        <p style="font-size:12px; margin-top:2px;">Try adjusting keywords or active filters</p>
      </div>`;
  } else {
    filtered.forEach(tx => {
      historyTransactionsList.appendChild(createTransactionCard(tx));
    });
  }
}

// Category filter chips in history
function renderHistoryCategoryChips() {
  categoryFilterRow.innerHTML = '';
  CATEGORIES.forEach(cat => {
    const btn = document.createElement('button');
    const isSelected = state.historyCategory === cat.id;
    btn.className = `filter-chip ${isSelected ? 'active' : ''}`;
    btn.style.borderColor = cat.color;
    btn.innerHTML = `<span style="display:inline-block; width:6px; height:6px; border-radius:50%; background:${cat.color}; margin-right:4px;"></span>${cat.name}`;
    btn.addEventListener('click', () => {
      state.historyCategory = state.historyCategory === cat.id ? null : cat.id;
      renderHistoryCategoryChips();
      renderHistory();
    });
    categoryFilterRow.appendChild(btn);
  });
}

historySearchInput.addEventListener('input', (e) => {
  state.historyQuery = e.target.value;
  searchClearBtn.style.display = state.historyQuery ? 'flex' : 'none';
  renderHistory();
});
searchClearBtn.addEventListener('click', () => {
  state.historyQuery = '';
  historySearchInput.value = '';
  searchClearBtn.style.display = 'none';
  renderHistory();
});

document.querySelectorAll('[data-range]').forEach(chip => {
  chip.addEventListener('click', () => {
    document.querySelectorAll('[data-range]').forEach(c => c.classList.remove('active'));
    chip.classList.add('active');
    state.historyRange = chip.dataset.range;
    renderHistory();
  });
});

// CSV Export
exportCsvBtn.addEventListener('click', () => {
  let csv = 'ID,Date,Title,Category,PaymentMode,Amount,Notes\n';
  state.expenses.forEach(e => {
    const cat = CATEGORIES.find(c => c.id === e.category)?.name || e.category;
    const mode = PAYMENT_MODES.find(m => m.id === e.paymentMode)?.name || e.paymentMode;
    const dateStr = new Date(e.timestamp).toISOString();
    csv += `${e.id},"${dateStr}","${e.title.replace(/"/g, '""')}","${cat}","${mode}",${e.amount},"${(e.notes || '').replace(/"/g, '""')}"\n`;
  });

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `ExpenseTracker_Export_${Date.now()}.csv`;
  a.click();
  URL.revokeObjectURL(url);
  showSnackbar('CSV backup downloaded successfully!');
});

// QUICK LOG MODAL & NUMPAD
function openQuickModal() {
  state.draftAmount = '';
  state.draftCategory = 'FOOD';
  state.draftPayment = 'UPI';
  state.draftTitle = '';
  quickTitleInput.value = '';
  updateNumpadDisplay();
  renderQuickChips();
  quickLogModal.classList.add('open');
}

function closeQuickModal() {
  quickLogModal.classList.remove('open');
}

function updateNumpadDisplay() {
  numpadAmountDisplay.textContent = state.draftAmount || '0';
  const val = parseFloat(state.draftAmount) || 0;
  quickSaveBtn.disabled = val <= 0;
  quickSaveBtnText.textContent = val > 0 ? `Log ₹${Math.round(val)} Expense` : 'Enter Amount';
}

function renderQuickChips() {
  quickCategoryChips.innerHTML = '';
  CATEGORIES.forEach(cat => {
    const chip = document.createElement('button');
    const isSel = state.draftCategory === cat.id;
    chip.className = `filter-chip ${isSel ? 'active' : ''}`;
    if (isSel) chip.style.background = cat.color;
    chip.innerHTML = `<span class="material-symbols-rounded" style="font-size:16px; margin-right:4px; vertical-align:middle;">${cat.icon}</span>${cat.name}`;
    chip.addEventListener('click', () => {
      state.draftCategory = cat.id;
      renderQuickChips();
    });
    quickCategoryChips.appendChild(chip);
  });

  quickPaymentChips.innerHTML = '';
  PAYMENT_MODES.forEach(mode => {
    const chip = document.createElement('button');
    const isSel = state.draftPayment === mode.id;
    chip.className = `filter-chip ${isSel ? 'active' : ''}`;
    chip.innerHTML = `<span class="material-symbols-rounded" style="font-size:16px; margin-right:4px; vertical-align:middle;">${mode.icon}</span>${mode.name}`;
    chip.addEventListener('click', () => {
      state.draftPayment = mode.id;
      renderQuickChips();
    });
    quickPaymentChips.appendChild(chip);
  });
}

// Numpad key clicks
document.querySelectorAll('.num-key').forEach(btn => {
  btn.addEventListener('click', () => {
    const key = btn.dataset.key;
    if (key === 'DEL') {
      state.draftAmount = state.draftAmount.slice(0, -1);
    } else if (key === '.') {
      if (!state.draftAmount.includes('.')) {
        state.draftAmount = (state.draftAmount || '0') + '.';
      }
    } else {
      if (state.draftAmount.length < 8) {
        state.draftAmount = state.draftAmount === '0' ? key : state.draftAmount + key;
      }
    }
    updateNumpadDisplay();
  });
});

quickSaveBtn.addEventListener('click', () => {
  const amount = parseFloat(state.draftAmount);
  if (!amount || amount <= 0) return;

  const catObj = CATEGORIES.find(c => c.id === state.draftCategory);
  const title = quickTitleInput.value.trim() || `${catObj?.name || 'Expense'} Spend`;

  const newExpense = {
    id: Date.now(),
    title: title,
    amount: amount,
    category: state.draftCategory,
    paymentMode: state.draftPayment,
    timestamp: new Date().toISOString(),
    notes: ''
  };

  state.addExpense(newExpense);
  closeQuickModal();
  renderAll();
  showSnackbar(`Logged ₹${Math.round(amount)} for ${title}`);
});

openQuickLogFab.addEventListener('click', openQuickModal);
quickLogModal.addEventListener('click', (e) => {
  if (e.target === quickLogModal) closeQuickModal();
});

// NAVIGATION
document.querySelectorAll('.nav-item').forEach(item => {
  item.addEventListener('click', () => {
    const tabId = item.dataset.tab;
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    document.querySelectorAll('.screen-tab').forEach(t => t.classList.remove('active'));

    item.classList.add('active');
    document.getElementById(tabId).classList.add('active');
    state.activeTab = tabId;

    if (tabId === 'tab-calendar') renderCalendar();
    if (tabId === 'tab-history') {
      renderHistoryCategoryChips();
      renderHistory();
    }
  });
});

document.getElementById('viewAllBtn').addEventListener('click', () => {
  document.querySelector('[data-tab="tab-history"]').click();
});

// SNACKBAR
let snackbarTimer = null;
function showSnackbar(msg, showUndo = false) {
  snackbarText.textContent = msg;
  snackbarUndoBtn.style.display = showUndo ? 'inline-block' : 'none';
  snackbar.classList.add('show');
  clearTimeout(snackbarTimer);
  snackbarTimer = setTimeout(() => {
    snackbar.classList.remove('show');
  }, 4000);
}
snackbarUndoBtn.addEventListener('click', () => {
  state.undoDelete();
  renderAll();
  snackbar.classList.remove('show');
});

// THEME TOGGLE
themeToggleBtn.addEventListener('click', () => {
  if (document.body.classList.contains('dark-mode')) {
    document.body.classList.remove('dark-mode');
    document.body.classList.add('light-mode');
    themeIcon.textContent = 'dark_mode';
  } else {
    document.body.classList.remove('light-mode');
    document.body.classList.add('dark-mode');
    themeIcon.textContent = 'light_mode';
  }
  renderBarChart();
});

// MASTER RENDER
function renderAll() {
  renderDashboard();
  if (state.activeTab === 'tab-calendar') renderCalendar();
  if (state.activeTab === 'tab-history') renderHistory();
}

// Initial Run
renderAll();
