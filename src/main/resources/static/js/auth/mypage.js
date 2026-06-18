
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