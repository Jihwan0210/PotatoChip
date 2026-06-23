function getToken() {
    return localStorage.getItem('token') || sessionStorage.getItem('token') || '';
}

function doLogout() {
    ['token', 'email', 'role', 'name'].forEach(function (k) {
        localStorage.removeItem(k);
        sessionStorage.removeItem(k);
    });
    document.cookie = 'jwt=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; SameSite=Lax';
    location.href = '/login';
}

function farmerToast(msg) {
    var t = document.createElement('div');
    t.textContent = msg;
    t.style.cssText = 'position:fixed;bottom:28px;left:50%;transform:translateX(-50%);background:#3A2808;color:#fff;padding:10px 22px;border-radius:100px;font-size:.82rem;z-index:9999;white-space:nowrap';
    document.body.appendChild(t);
    setTimeout(function () { t.remove(); }, 2600);
}

function applyBadgeClass(sel, status) {
    var map = {
        SHIPPING:         'ft-badge ft-badge-success',
        DELIVERED:        'ft-badge ft-badge-default',
        CANCELLED:        'ft-badge ft-badge-danger',
        REFUNDED:         'ft-badge ft-badge-danger',
        PAYMENT_COMPLETE: 'ft-badge ft-badge-warning',
        PREPARING:        'ft-badge ft-badge-warning'
    };
    sel.className = map[status] || 'ft-badge ft-badge-warning';
}

function switchFarmerTab(idx) {
    for (var i = 0; i < 4; i++) {
        var tab     = document.getElementById('ftab-'     + i);
        var content = document.getElementById('fcontent-' + i);
        if (tab)     tab.classList.toggle('on', i === idx);
        if (content) content.style.display = i === idx ? 'block' : 'none';
    }
}

function updateOrderStatus(selectEl) {
    var orderId   = selectEl.dataset.orderId;
    var newStatus = selectEl.value;
    fetch('/api/seller/orders/' + orderId + '/status', {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
        body: JSON.stringify({ status: newStatus })
    })
        .then(function (r) { return r.ok ? r.json() : Promise.reject(); })
        .then(function ()  { applyBadgeClass(selectEl, newStatus); farmerToast('주문 상태가 변경되었습니다.'); })
        .catch(function () { farmerToast('상태 변경에 실패했습니다. 다시 시도해주세요.'); });
}

function editStockFromBtn(btn) {
    var productId   = btn.dataset.productId;
    var productName = btn.dataset.productName;
    var stock       = parseInt(btn.dataset.stock, 10);
    var input = prompt(productName + '\n현재 재고: ' + stock + '개\n새 재고 수량을 입력하세요:', stock);
    if (input === null) return;
    var n = parseInt(input, 10);
    if (isNaN(n) || n < 0) { alert('올바른 재고 수를 입력해주세요.'); return; }
    fetch('/api/seller/products/' + productId + '/stock', {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
        body: JSON.stringify({ stock: n })
    })
        .then(function (r) { return r.ok ? r.json() : Promise.reject(); })
        .then(function () {
            btn.dataset.stock = n;
            var row       = btn.parentElement.parentElement;
            var stockDiv  = row.children[1];
            var badgeSpan = row.children[3].querySelector('span');
            stockDiv.textContent = n + '개';
            stockDiv.className   = n === 0 ? 'ft-stock-out' : (n < 10 ? 'ft-stock-low' : 'ft-stock-ok');
            badgeSpan.textContent = n === 0 ? '품절' : (n < 10 ? '재고 부족' : '판매 중');
            badgeSpan.className   = 'ft-badge ' + (n === 0 ? 'ft-badge-danger' : (n < 10 ? 'ft-badge-warning' : 'ft-badge-success'));
            farmerToast(productName + ' 재고가 ' + n + '개로 수정되었습니다.');
        })
        .catch(function () { farmerToast('재고 수정에 실패했습니다. 다시 시도해주세요.'); });
}

function exportOrders() {
    var tableRows = document.querySelectorAll('#fcontent-0 .ft-table .ft-order-grid.ft-table-row');
    if (!tableRows.length) { farmerToast('다운로드할 주문이 없습니다.'); return; }
    var rows = [['주문번호', '상품명', '수량', '결제액', '상태', '주문일']];
    tableRows.forEach(function (row) {
        var c   = row.children;
        var sel = row.querySelector('select[data-status]');
        rows.push([
            c[0] ? c[0].textContent.trim() : '-',
            c[1] ? c[1].textContent.trim() : '-',
            c[2] ? c[2].textContent.trim() : '-',
            c[3] ? c[3].textContent.trim() : '-',
            sel  ? sel.dataset.status      : '-',
            c[5] ? c[5].textContent.trim() : '-'
        ]);
    });
    var csv  = '﻿' + rows.map(function (r) {
        return r.map(function (v) { return '"' + String(v).replace(/"/g, '""') + '"'; }).join(',');
    }).join('\r\n');
    var blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    var link = document.createElement('a');
    link.href     = URL.createObjectURL(blob);
    link.download = '주문목록_' + new Date().toISOString().substring(0, 10) + '.csv';
    link.click();
}

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('select[data-status]').forEach(function (sel) {
        applyBadgeClass(sel, sel.dataset.status);
    });
});
