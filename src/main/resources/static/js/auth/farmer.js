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

    var statusLabelMap = {
        PAYMENT_COMPLETE: '결제 완료',
        PREPARING:        '상품 준비 중',
        SHIPPING:         '배송 중',
        DELIVERED:        '배송 완료',
        CANCELLED:        '취소',
        REFUNDED:         '환불'
    };
    var statusBgMap = {
        SHIPPING:         'D4EDDA',
        DELIVERED:        'E2E3E5',
        CANCELLED:        'F8D7DA',
        REFUNDED:         'F8D7DA',
        PAYMENT_COMPLETE: 'FFF3CD',
        PREPARING:        'FFF3CD'
    };

    var data = [];
    tableRows.forEach(function (row) {
        var c   = row.children;
        var sel = row.querySelector('select[data-status]');
        var sk  = sel ? sel.dataset.status : '';
        data.push({
            orderNum:    c[0] ? c[0].textContent.trim() : '-',
            productName: c[1] ? c[1].textContent.trim() : '-',
            quantity:    c[2] ? c[2].textContent.trim() : '-',
            amount:      c[3] ? c[3].textContent.trim() : '-',
            status:      statusLabelMap[sk] || sk || '-',
            statusKey:   sk,
            date:        c[5] ? c[5].textContent.trim() : '-'
        });
    });

    var today = new Date().toLocaleDateString('ko-KR');
    var wsData = [
        ['못난이 농가 주문 현황'],
        ['다운로드: ' + today],
        [],
        ['주문번호', '상품명', '수량', '결제액', '상태', '주문일']
    ];
    data.forEach(function (d) {
        wsData.push([d.orderNum, d.productName, d.quantity, d.amount, d.status, d.date]);
    });

    var ws = XLSX.utils.aoa_to_sheet(wsData);

    ws['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: 5 } }];
    ws['!cols']   = [{ wch: 18 }, { wch: 26 }, { wch: 8 }, { wch: 14 }, { wch: 14 }, { wch: 12 }];
    ws['!rows']   = [{ hpt: 34 }, { hpt: 18 }, { hpt: 6 }, { hpt: 26 }];

    var border = {
        top:    { style: 'thin', color: { rgb: 'DDDDDD' } },
        bottom: { style: 'thin', color: { rgb: 'DDDDDD' } },
        left:   { style: 'thin', color: { rgb: 'DDDDDD' } },
        right:  { style: 'thin', color: { rgb: 'DDDDDD' } }
    };

    if (ws['A1']) ws['A1'].s = {
        font:      { bold: true, sz: 15, color: { rgb: '3A2808' } },
        alignment: { horizontal: 'center', vertical: 'center' }
    };
    if (ws['A2']) ws['A2'].s = {
        font:      { sz: 9, color: { rgb: '999999' } },
        alignment: { horizontal: 'left', vertical: 'center' }
    };

    var hdrCols  = ['A', 'B', 'C', 'D', 'E', 'F'];
    var hdrAlign = ['left', 'left', 'center', 'right', 'center', 'center'];
    hdrCols.forEach(function (col, i) {
        var cell = ws[col + '4'];
        if (!cell) return;
        cell.s = {
            font:      { bold: true, sz: 10, color: { rgb: 'FFFFFF' } },
            fill:      { fgColor: { rgb: '3A2808' } },
            alignment: { horizontal: hdrAlign[i], vertical: 'center' },
            border:    border
        };
    });

    data.forEach(function (d, i) {
        var r      = i + 5;
        var rowBg  = i % 2 === 0 ? 'FFFFFF' : 'FBF7F0';
        var aligns = ['left', 'left', 'center', 'right', 'center', 'center'];
        hdrCols.forEach(function (col, ci) {
            var cell = ws[col + r];
            if (!cell) return;
            var bg = (col === 'E' && statusBgMap[d.statusKey]) ? statusBgMap[d.statusKey] : rowBg;
            cell.s = {
                font:      { sz: 10, bold: col === 'E' },
                fill:      { fgColor: { rgb: bg } },
                alignment: { horizontal: aligns[ci], vertical: 'center' },
                border:    border
            };
        });
    });

    var wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, '주문 현황');
    XLSX.writeFile(wb, '주문목록_' + new Date().toISOString().substring(0, 10) + '.xlsx');
}

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('select[data-status]').forEach(function (sel) {
        applyBadgeClass(sel, sel.dataset.status);
    });
});
