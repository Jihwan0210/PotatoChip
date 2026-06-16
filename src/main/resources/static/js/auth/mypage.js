
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
    var editableIds = ['mp-name', 'mp-nickname', 'mp-phone', 'mp-address'];
    editableIds.forEach(function(id) {
        var el = document.getElementById(id);
        _savedValues[id] = el.value;
        el.removeAttribute('readonly');
        el.style.background = '#fff';
        el.style.color = 'var(--text)';
    });
    document.getElementById('btn-edit').style.display = 'none';
    document.getElementById('btn-save').style.display = 'flex';
    document.getElementById('btn-cancel').style.display = 'flex';
}

// 수정 모드 종료 (읽기 전용으로 복귀)
function exitEditMode() {
    var editableIds = ['mp-name', 'mp-nickname', 'mp-phone', 'mp-address'];
    editableIds.forEach(function(id) {
        var el = document.getElementById(id);
        el.setAttribute('readonly', true);
        el.style.background = 'var(--beige2)';
        el.style.color = 'var(--muted)';
    });
    document.getElementById('btn-edit').style.display = 'flex';
    document.getElementById('btn-save').style.display = 'none';
    document.getElementById('btn-cancel').style.display = 'none';
}

// 취소 - 원래 값으로 되돌리고 읽기 전용 복귀
function cancelEditMode() {
    var editableIds = ['mp-name', 'mp-nickname', 'mp-phone', 'mp-address'];
    editableIds.forEach(function(id) {
        document.getElementById(id).value = _savedValues[id] || '';
    });
    exitEditMode();
}

// 내 정보 저장
function saveMyProfile() {
    const token = getToken();
    if (!token) { location.href = '/login'; return; }

    const body = {
        name:     document.getElementById('mp-name').value,
        nickname: document.getElementById('mp-nickname').value,
        phone:    document.getElementById('mp-phone').value,
        address:  document.getElementById('mp-address').value
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
            if (t === name) {
                tab.style.background = 'var(--gp)';
                tab.style.borderLeft = '3px solid var(--green)';
            } else {
                tab.style.background = 'transparent';
                tab.style.borderLeft = '3px solid transparent';
            }
        }
    });

    if (name === 'profile') loadMyInfo();
}