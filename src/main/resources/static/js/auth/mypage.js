
function getToken() {
    return localStorage.getItem('token') || sessionStorage.getItem('token');
}

function doLogout() {
    localStorage.clear();
    sessionStorage.clear();
    location.href = '/login';
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function () {
    const token = getToken();
    if (!token) {
        location.href = '/login';
        return;
    }

    // 로그아웃 버튼 전환
    const btnNav = document.querySelector('.btn-nav');
    if (btnNav) {
        btnNav.textContent = '로그아웃';
        btnNav.setAttribute('onclick', 'doLogout()');
    }

    loadMyInfo();
    loadMyOrders();
});

// 내 정보 조회
function loadMyInfo() {
    const token = getToken();

    fetch('/mypage/info', {
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + token }
    })
        .then(res => {
            if (res.status === 401) {
                location.href = '/login';
                return null;
            }
            return res.json();
        })
        .then(data => {
            if (!data) return;
            document.getElementById('mp-name').value     = data.name     || '';
            document.getElementById('mp-nickname').value = data.nickname || '';
            document.getElementById('mp-email').value    = data.email    || '';
            document.getElementById('mp-phone').value    = data.phone    || '';
            document.getElementById('mp-address').value  = data.address  || '';

            var nameEl = document.getElementById('mp-display-name');
            if (nameEl) nameEl.textContent = (data.name || '') + '님';
            var emailEl = document.getElementById('mp-display-email');
            if (emailEl) emailEl.textContent = data.email || '';
        })
        .catch(err => console.error('내 정보 조회 실패:', err));
}

var _savedValues = {};

// 수정 모드 진입
function enterEditMode() {
    var editableIds = ['mp-name', 'mp-nickname', 'mp-phone'];
    editableIds.forEach(function(id) {
        var el = document.getElementById(id);
        _savedValues[id] = el.value;
        el.removeAttribute('readonly');
        el.style.background = '#fff';
        el.style.color = 'var(--text)';
    });
    // 주소: 검색 버튼 표시, 상세주소만 직접 입력 가능
    _savedValues['mp-address'] = document.getElementById('mp-address').value;
    _savedValues['mp-address-detail'] = document.getElementById('mp-address-detail').value;
    var detail = document.getElementById('mp-address-detail');
    detail.removeAttribute('readonly');
    detail.style.background = '#fff';
    detail.style.color = 'var(--text)';
    document.getElementById('mp-address-search-row').style.display = 'flex';

    document.getElementById('btn-edit').style.display = 'none';
    document.getElementById('btn-save').style.display = 'flex';
    document.getElementById('btn-cancel').style.display = 'flex';
}

// 수정 모드 종료 (읽기 전용으로 복귀)
function exitEditMode() {
    var editableIds = ['mp-name', 'mp-nickname', 'mp-phone'];
    editableIds.forEach(function(id) {
        var el = document.getElementById(id);
        el.setAttribute('readonly', true);
        el.style.background = 'var(--beige2)';
        el.style.color = 'var(--muted)';
    });
    var detail = document.getElementById('mp-address-detail');
    detail.setAttribute('readonly', true);
    detail.style.background = 'var(--beige2)';
    detail.style.color = 'var(--muted)';
    document.getElementById('mp-address-search-row').style.display = 'none';

    document.getElementById('btn-edit').style.display = 'flex';
    document.getElementById('btn-save').style.display = 'none';
    document.getElementById('btn-cancel').style.display = 'none';
}

// 취소 - 원래 값으로 되돌리고 읽기 전용 복귀
function cancelEditMode() {
    var editableIds = ['mp-name', 'mp-nickname', 'mp-phone'];
    editableIds.forEach(function(id) {
        document.getElementById(id).value = _savedValues[id] || '';
    });
    document.getElementById('mp-address').value = _savedValues['mp-address'] || '';
    document.getElementById('mp-address-detail').value = _savedValues['mp-address-detail'] || '';
    exitEditMode();
}

// 주소 검색 (마이페이지용)
function openMpAddressSearch() {
    new daum.Postcode({
        oncomplete: function(data) {
            var addr = data.roadAddress || data.jibunAddress;
            document.getElementById('mp-postcode').value = data.zonecode;
            document.getElementById('mp-address').value = addr;
            document.getElementById('mp-address-detail').value = '';
            document.getElementById('mp-address-detail').focus();
        }
    }).open();
}

// 내 정보 저장
function saveMyProfile() {
    const token = getToken();
    if (!token) { location.href = '/login'; return; }

    var addrMain   = document.getElementById('mp-address').value.trim();
    var addrDetail = document.getElementById('mp-address-detail').value.trim();
    const body = {
        name:     document.getElementById('mp-name').value,
        nickname: document.getElementById('mp-nickname').value,
        phone:    document.getElementById('mp-phone').value,
        address:  addrDetail ? addrMain + ' ' + addrDetail : addrMain
    };

    fetch('/mypage/info', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify(body)
    })
        .then(res => res.json())
        .then(data => {
            exitEditMode();
            var nameEl = document.getElementById('mp-display-name');
            if (nameEl && body.name) nameEl.textContent = body.name + '님';
            showToast('저장되었어요! ✅');
        })
        .catch(err => console.error('저장 실패:', err));
}

// 마이페이지 탭 전환
function switchMyTab(name) {
    var tabs   = ['orders', 'wishlist', 'coupon', 'myreview', 'profile'];
    var tabIds = ['orders', 'wishlist', 'coupon', 'review',   'profile'];
    tabs.forEach(function (t, i) {
        var c   = document.getElementById('mycontent-' + t);
        var tab = document.getElementById('my-tab-' + tabIds[i]);
        if (c) c.style.display = (t === name ? 'block' : 'none');
        if (tab) {
            tab.classList.toggle('on', t === name);
        }
    });

    if (name === 'profile') loadMyInfo();
    if (name === 'orders') loadMyOrders()
    if (name === 'wishlist') loadWishlist();
}

// 찜 목록 조회
function loadWishlist() {
    var token = getToken();
    if (!token) return;
    var container = document.getElementById('wishlist-container');
    if (!container) return;

    fetch('/mypage/wishlist', {
        headers: { 'Authorization': 'Bearer ' + token }
    })
    .then(function(res) {
        if (res.status === 401) { location.href = '/login'; return null; }
        if (!res.ok) throw new Error('status ' + res.status);
        return res.json();
    })
    .then(function(items) {
        if (!items) return;
        if (!Array.isArray(items) || items.length === 0) {
            container.innerHTML = '<div class="mp-empty">찜한 상품이 없어요 🌿</div>';
            return;
        }
        var html = '<div class="wl-grid">';
        items.forEach(function(p) {
            var thumb = p.thumbnailUrl
                ? '<img src="' + p.thumbnailUrl + '" alt="' + p.name + '"/>'
                : '🥬';
            var priceHtml = p.discountPrice
                ? '<span class="wl-price">' + Number(p.discountPrice).toLocaleString() + '원</span>'
                  + '<span class="wl-og">' + Number(p.price).toLocaleString() + '원</span>'
                : '<span class="wl-price">' + Number(p.price).toLocaleString() + '원</span>';
            html += '<div class="wl-card" id="wl-' + p.id + '">'
                + '<div class="wl-thumb">' + thumb + '</div>'
                + '<div class="wl-body">'
                + '<div class="wl-cat">' + (p.category || '') + '</div>'
                + '<div class="wl-name">' + p.name + '</div>'
                + '<div class="wl-price-row">' + priceHtml + '</div>'
                + '<button class="wl-remove" onclick="removeWish(' + p.id + ')">❤️ 찜 해제</button>'
                + '</div></div>';
        });
        html += '</div>';
        container.innerHTML = html;
    })
    .catch(function() {
        container.innerHTML = '<div class="mp-empty">불러오기 실패했어요 ㅠㅠ</div>';
    });
}

// 찜 해제
function removeWish(productId) {
    var token = getToken();
    if (!token) return;
    fetch('/wishlist/' + productId, {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token }
    })
    .then(function(res) { return res.json(); })
    .then(function(data) {
        if (!data.added) {
            var card = document.getElementById('wl-' + productId);
            if (card) card.remove();
            var container = document.getElementById('wishlist-container');
            if (container && !container.querySelector('.wl-card')) {
                container.innerHTML = '<div class="mp-empty">찜한 상품이 없어요 🌿</div>';
            }
            showToast('찜 목록에서 제거됐어요');
        }
    })
    .catch(function() { showToast('오류가 발생했어요 ㅠㅠ'); });
}

// 비밀번호 변경
function changePassword() {
    var token = getToken();
    if (!token) { location.href = '/login'; return; }

    var currentPw = document.getElementById('cp-current').value;
    var newPw     = document.getElementById('cp-new').value;
    var confirmPw = document.getElementById('cp-confirm').value;

    if (!currentPw || !newPw || !confirmPw) {
        showToast('모든 항목을 입력해주세요.');
        return;
    }
    if (!/^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~`]).{8,}$/.test(newPw)) {
        showToast('새 비밀번호: 영어, 숫자, 특수문자 포함 8자 이상이어야 해요.');
        return;
    }
    if (newPw !== confirmPw) {
        showToast('새 비밀번호가 일치하지 않아요.');
        return;
    }

    fetch('/mypage/changepw', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
        body: JSON.stringify({ currentPassword: currentPw, newPassword: newPw, confirmPassword: confirmPw })
    })
        .then(function(res) { return res.json(); })
        .then(function(data) {
            if (data.message) {
                document.getElementById('cp-current').value = '';
                document.getElementById('cp-new').value = '';
                document.getElementById('cp-confirm').value = '';
                showToast('비밀번호가 변경되었어요! ✅');
            } else {
                showToast(data.error);
            }
        })
        .catch(function() { showToast('오류가 발생했습니다.'); });
}

// 회원 탈퇴
function withdrawAccount() {
    var token = getToken();
    if (!token) { location.href = '/login'; return; }

    var pw     = document.getElementById('wd-pw').value;
    var reason = document.getElementById('wd-reason').value;

    if (!pw) {
        showToast('비밀번호를 입력해주세요.');
        return;
    }

    if (!confirm('정말 탈퇴하시겠어요? 탈퇴 후에는 복구가 어렵습니다.')) return;

    fetch('/mypage/withdraw', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
        body: JSON.stringify({ password: pw, reason: reason })
    })
        .then(function(res) { return res.json(); })
        .then(function(data) {
            if (data.message) {
                localStorage.clear();
                sessionStorage.clear();
                showToast('탈퇴가 완료되었어요. 이용해주셔서 감사합니다 🌿');
                setTimeout(function() { location.href = '/'; }, 2000);
            } else {
                showToast(data.error);
            }
        })
        .catch(function() { showToast('오류가 발생했습니다.'); });
}
var STATUS_LABELS = {
    PAYMENT_COMPLETE: '결제완료',
    PREPARING: '상품준비중',
    SHIPPING: '배송중',
    DELIVERED: '배송완료',
    CANCELLED: '주문취소',
    REFUNDED: '환불완료'
};

var DELIVERY_LABELS = {
    delivery: '일반 택배',
    express: '당일 배송',
    pickup: '농장 픽업'
};

function loadMyOrders() {
    const token = getToken();
    fetch('/orders/my', {
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + token }
    })
        .then(function (res) {
            if (res.status === 401) { location.href = '/login'; return null; }
            return res.json();
        })
        .then(function (orders) {
            if (!orders) return;
            renderMyOrders(orders);
        })
        .catch(function (err) { console.error('주문 내역 조회 실패:', err); });
}

function renderMyOrders(orders) {
    const token = getToken();
    const box = document.getElementById('mycontent-orders');
    if (!box) return;

    if (!orders.length) {
        box.innerHTML =
            '<div class="mp-card">' +
            '<div class="mp-section-title">📦 주문 내역</div>' +
            '<div style="text-align:center;color:var(--muted);font-size:.84rem;padding:32px 0">주문 내역이 없어요 🌿</div>' +
            '</div>';
        return;
    }

    var html = '<div class="mp-card"><div class="mp-section-title">📦 주문 내역</div>';

    orders.forEach(function (order, index) {
        var items = order.orderItems || [];
        var firstItem = items[0] || {};
        var thumbnail = firstItem.thumbnailUrl
            ? firstItem.thumbnailUrl
            : 'https://placehold.co/56x56?text=🥔';
        var firstName = firstItem.productName || '상품 정보 없음';
        var displayName = items.length > 1
            ? firstName + ' 외 ' + (items.length - 1) + '개'
            : firstName;

        var finalAmount = (order.totalAmount || 0) + (order.totalShippingFee || 0);
        var dateStr = order.createdAt ? order.createdAt.replace('T', ' ').slice(0, 16) : '';
        var isLast = index === orders.length - 1;

        html +=
            '<div style="display:flex;gap:14px;padding:16px 0;align-items:flex-start;' +
            (!isLast ? 'border-bottom:1.5px solid #e8dcc8;margin-bottom:2px;' : '') + '">' +
            '<img src="' + thumbnail + '" ' +
            'style="width:56px;height:56px;object-fit:cover;border-radius:8px;flex-shrink:0;background:var(--bg)" ' +
            'onerror="this.src=\'https://placehold.co/56x56?text=🥔\'"/>' +
            '<div style="flex:1;min-width:0">' +
            '<div style="display:flex;justify-content:space-between;align-items:center;gap:8px;margin-bottom:2px">' +
            '<span style="font-weight:700;font-size:.92rem;color:var(--dark);white-space:nowrap;overflow:hidden;text-overflow:ellipsis">' +
            displayName +
            '</span>' +
            '<span style="font-size:.74rem;font-weight:700;color:var(--green);background:#eef7e6;border-radius:20px;padding:2px 10px;flex-shrink:0">' +
            (STATUS_LABELS[order.status] || order.status) +
            '</span>' +
            '</div>' +
            '<div style="font-size:.74rem;color:var(--muted);margin-bottom:6px">' + order.orderNumber + '</div>' +
            '<div style="font-size:.78rem;color:var(--muted);margin-bottom:10px">' +
            dateStr + ' · ' + (DELIVERY_LABELS[order.deliveryType] || order.deliveryType) +
            '</div>' +
            '<div style="display:flex;justify-content:space-between;align-items:center">' +
            '<span style="font-weight:700;color:#F39C12">' + finalAmount.toLocaleString() + '원</span>' +
            '<a href="/orders/complete/' + order.id + '?token=' + token + '" ' +
            'style="font-size:.78rem;color:var(--green);text-decoration:underline">상세보기</a>' +
            '</div>' +
            '</div>' +
            '</div>';
    });
    html += '</div>';
    box.innerHTML = html;
}
