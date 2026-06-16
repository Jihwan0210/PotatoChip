
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
        })
        .catch(err => console.error('내 정보 조회 실패:', err));
}

// 내 정보 수정
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
        .then(() => alert('저장되었어요! ✅'))
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