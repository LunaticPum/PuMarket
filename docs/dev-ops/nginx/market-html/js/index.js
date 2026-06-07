// ---------- 全局状态 ----------
let isLoggedIn = false;
let currentUserName = "";
// 存储购买行为: { amount, callback } 保留扩展
let pendingPaymentAmount = 20; // 默认20

// DOM 元素
const userGreetingSpan = document.getElementById("userGreeting");
const manualLoginBtn = document.getElementById("manualLoginBtn");
const loginModal = document.getElementById("loginModal");
const payModal = document.getElementById("payModal");
const singleBuy = document.getElementById("singleBuyBtn");
const groupBuy = document.getElementById("groupBuyBtn");
const doLoginBtn = document.getElementById("doLoginBtn");
const modalPayAmountSpan = document.getElementById("modalPayAmount");
const modalCancelPay = document.getElementById("modalCancelPay");
const modalConfirmPay = document.getElementById("modalConfirmPay");

// 关闭模态框通用
function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.remove("active");
}

function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.add("active");
}

// 绑定关闭按钮事件
document.querySelectorAll("[data-close]").forEach((btn) => {
  btn.addEventListener("click", (e) => {
    const modalId = btn.getAttribute("data-close");
    closeModal(modalId);
  });
});
// 点击遮罩关闭
window.addEventListener("click", (e) => {
  if (e.target.classList && e.target.classList.contains("modal-mask")) {
    e.target.classList.remove("active");
  }
});

// 更新登录UI
function updateLoginUI() {
  if (isLoggedIn && currentUserName) {
    userGreetingSpan.innerText = `🎉 欢迎回来，${currentUserName}！ 可一键拼团`;
    manualLoginBtn.innerText = "切换账号";
    manualLoginBtn.style.background = "#f5f5f5";
    manualLoginBtn.style.color = "#666";
  } else {
    userGreetingSpan.innerText = "👋 未登录，点击购买请先登录";
    manualLoginBtn.innerText = "去登录";
    manualLoginBtn.style.background = "#e4393c";
    manualLoginBtn.style.color = "white";
  }
}

// 登录逻辑 (弹窗登录 + 保留购买意图)
let pendingPurchaseAction = null; // { amount, type }

function performLogin(username, pwd) {
  // 模拟登录校验
  if (!username.trim()) {
    alert("请输入用户名");
    return false;
  }
  // 任何非空密码均视为成功
  currentUserName = username.trim();
  isLoggedIn = true;
  updateLoginUI();
  closeModal("loginModal");
  // 如果有待处理的购买行为，登录成功后自动唤起支付弹窗
  if (pendingPurchaseAction) {
    const action = pendingPurchaseAction;
    pendingPurchaseAction = null;
    // 唤起支付弹窗，传入金额
    showPayModal(action.amount);
  }
  return true;
}

// 显示支付弹窗
function showPayModal(amount) {
  const amountNum = parseFloat(amount);
  const displayAmount = isNaN(amountNum) ? 20.0 : amountNum;
  modalPayAmountSpan.innerText = `支付金额 ¥${displayAmount.toFixed(2)}`;
  openModal("payModal");
}

// 购买前置检查 (登录+弹支付)
function tryPurchase(amount, purchaseTypeDesc) {
  if (!isLoggedIn) {
    // 未登录，存储待支付事件
    pendingPurchaseAction = {
      amount: amount,
      type: purchaseTypeDesc,
    };
    openModal("loginModal");
    return false;
  } else {
    showPayModal(amount);
    return true;
  }
}

// 单独购买
if (singleBuy) {
  singleBuy.addEventListener("click", () => {
    tryPurchase(100, "单独购买");
  });
}
// 开团购买
if (groupBuy) {
  groupBuy.addEventListener("click", () => {
    tryPurchase(20, "开团购买");
  });
}

// 支付弹窗内取消
if (modalCancelPay) {
  modalCancelPay.addEventListener("click", () => {
    closeModal("payModal");
  });
}
// 支付确认
if (modalConfirmPay) {
  modalConfirmPay.addEventListener("click", () => {
    const amountText = modalPayAmountSpan.innerText;
    alert(`✅ 支付成功！${amountText}，订单已生成，请关注物流信息`);
    closeModal("payModal");
  });
}

// 手动登录按钮 (顶部快捷栏)
if (manualLoginBtn) {
  manualLoginBtn.addEventListener("click", () => {
    if (isLoggedIn) {
      // 切换账号 -> 登出
      isLoggedIn = false;
      currentUserName = "";
      updateLoginUI();
      alert("已退出登录");
    } else {
      openModal("loginModal");
    }
  });
}

// 登录模态框内的登录按钮
if (doLoginBtn) {
  doLoginBtn.addEventListener("click", () => {
    const username = document.getElementById("loginUsername").value;
    const password = document.getElementById("loginPassword").value;
    performLogin(username, password);
  });
}

// ---------- 轮播图逻辑 ----------
const slides = document.querySelectorAll(".carousel-slide");
const indicatorsContainer = document.getElementById("indicators");
let currentIndex = 0;
let carouselInterval = null;

function createIndicators() {
  indicatorsContainer.innerHTML = "";
  slides.forEach((_, idx) => {
    const dot = document.createElement("div");
    dot.classList.add("indicator");
    if (idx === currentIndex) dot.classList.add("active");
    dot.addEventListener("click", () => {
      goToSlide(idx);
      resetCarouselTimer();
    });
    indicatorsContainer.appendChild(dot);
  });
}

function goToSlide(index) {
  if (index < 0) index = 0;
  if (index >= slides.length) index = 0;
  slides.forEach((slide, i) => {
    slide.classList.toggle("active", i === index);
  });
  currentIndex = index;
  updateIndicators();
}

function updateIndicators() {
  const dots = document.querySelectorAll(".indicator");
  dots.forEach((dot, i) => {
    dot.classList.toggle("active", i === currentIndex);
  });
}

function nextSlide() {
  goToSlide(currentIndex + 1);
}

function startCarousel() {
  if (carouselInterval) clearInterval(carouselInterval);
  carouselInterval = setInterval(nextSlide, 3500);
}

function resetCarouselTimer() {
  if (carouselInterval) {
    clearInterval(carouselInterval);
    startCarousel();
  }
}

createIndicators();
startCarousel();

// ---------- 拼团倒计时 + 拼团卡片参与逻辑 (登录检查+支付) ----------
let groupEndTimes = [];

function initGroupData() {
  const now = new Date();
  const endTime1 = new Date(now.getTime() + 11 * 60 * 1000 + 30 * 1000);
  const endTime2 = new Date(
    now.getTime() + 2 * 3600 * 1000 + 15 * 60 * 1000 + 20 * 1000,
  );
  groupEndTimes = [endTime1, endTime2];
}

function getRemainingText(endTime) {
  const now = new Date();
  const diff = endTime - now;
  if (diff <= 0) return "已结束";
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const minutes = Math.floor((diff % 3600000) / (1000 * 60));
  const seconds = Math.floor((diff % 60000) / 1000);
  if (hours > 0)
    return `${hours.toString().padStart(2, "0")}:${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;
  return `${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;
}

const groupData = [
  {
    user: "用户1",
    avatarBg: "#FF8C42",
    initial: "米",
    price: "¥20",
    desc: "还差1人成团",
  },
  {
    user: "用户2",
    avatarBg: "#66BB6A",
    initial: "糖",
    price: "¥20",
    desc: "火热拼单中",
  },
];

function renderGroupCards() {
  const container = document.getElementById("groupList");
  if (!container) return;
  container.innerHTML = "";
  for (let i = 0; i < groupData.length; i++) {
    const item = groupData[i];
    const remainingText = getRemainingText(groupEndTimes[i]);
    const isExpired = groupEndTimes[i] <= new Date();
    const card = document.createElement("div");
    card.className = "group-card";
    card.innerHTML = `
                <div class="group-info">
                    <div class="user-row">
                        <div class="avatar" style="background: ${item.avatarBg};">${item.initial}</div>
                        <span class="user-name">${item.user}</span>
                        <div class="countdown">⏰ 拼单即将结束 <span class="countdown-${i}" style="font-weight:700;">${remainingText}</span></div>
                    </div>
                    <div class="group-price">${item.price} <small>${item.desc}</small></div>
                </div>
                <button class="btn-group-join" data-groupidx="${i}" ${isExpired ? 'disabled style="opacity:0.6;"' : ""}>${isExpired ? "团已结束" : "立即拼团"}</button>
            `;
    container.appendChild(card);
  }
  // 绑定立即拼团事件 (未过期)
  document.querySelectorAll(".btn-group-join").forEach((btn) => {
    if (btn.disabled) return;
    btn.removeEventListener("click", groupJoinHandler);
    btn.addEventListener("click", groupJoinHandler);
  });
}

function groupJoinHandler(e) {
  const btn = e.currentTarget;
  const idx = btn.getAttribute("data-groupidx");
  if (idx !== null && groupEndTimes[idx] > new Date()) {
    // 参与拼团，金额固定20元，调用购买前置检查
    tryPurchase(20, "参与拼团");
  } else if (groupEndTimes[idx] <= new Date()) {
    alert("该团已结束，去开新团吧～");
  }
}

function updateCountdowns() {
  for (let i = 0; i < groupEndTimes.length; i++) {
    const span = document.querySelector(`.countdown-${i}`);
    if (span) {
      const remaining = getRemainingText(groupEndTimes[i]);
      span.innerText = remaining;
      const card = span.closest(".group-card");
      if (groupEndTimes[i] <= new Date() && card) {
        const joinBtn = card.querySelector(".btn-group-join");
        if (joinBtn && joinBtn.innerText !== "团已结束") {
          joinBtn.innerText = "团已结束";
          joinBtn.disabled = true;
          joinBtn.style.opacity = "0.6";
        }
      }
    }
  }
}

let countdownTimer = null;

function startCountdown() {
  if (countdownTimer) clearInterval(countdownTimer);
  countdownTimer = setInterval(() => {
    updateCountdowns();
  }, 1000);
}

function initGroup() {
  initGroupData();
  renderGroupCards();
  startCountdown();
}

initGroup();

// 如果之前未登录，但通过快捷栏登录后，若有pendingPurchaseAction 已经在performLogin里处理，额外处理以防登录直接打开支付
// 确保登出后清空pending
window.addEventListener("load", () => {
  updateLoginUI();
  // 初始未登录，无pending
});
