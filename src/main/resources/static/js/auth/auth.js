/* ══ LOGIN / SIGNUP ══ */
function swTab(t) {
    document.getElementById('lf').style.display = t === 'login' ? 'block' : 'none';
    document.getElementById('sf').style.display = t === 'signup' ? 'block' : 'none';
    document.getElementById('sv').style.display = 'none';
    var tl = document.getElementById('tab-l');
    var ts = document.getElementById('tab-s');
    if (tl) { tl.classList.toggle('on', t === 'login'); }
    if (ts) { ts.classList.toggle('on', t === 'signup'); }
}

function doLogin() {
    const email = document.getElementById('le').value;
    const password = document.getElementById('lp').value;

    if (!email || !password) {
        showToast('이메일과 비밀번호를 입력해주세요!');
        return;
    }

    fetch('/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    })
        .then(res => res.json())
        .then(result => {
            if (result.token) {
                localStorage.setItem('token', result.token);
                localStorage.setItem('email', result.email);
                localStorage.setItem('role', result.role);
                document.getElementById('lf').style.display = 'none';
                document.getElementById('sv').style.display = 'block';
                setTimeout(function () { location.href = '/'; }, 1800);
            } else {
                showToast(result.error);
            }
        })
        .catch(err => showToast('오류가 발생했습니다.'));
}

function doSignup() {
    const data = {
        name: document.querySelector('#sf input[name="name"]').value,
        role: document.querySelector('#sf select[name="role"]').value,
        email: document.querySelector('#sf input[name="email"]').value,
        password: document.querySelector('#sf input[name="password"]').value,
        passwordConfirm: document.querySelector('#sf input[name="passwordConfirm"]').value,
        phone: document.querySelector('#sf input[name="phone"]').value,
        address: document.querySelector('#sf input[name="address"]').value,
        nickname: document.querySelector('#sf input[name="nickname"]').value
    };

    fetch('/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(res => res.json())
        .then(result => {
            if (result.message) {
                document.getElementById('sf').style.display = 'none';
                document.getElementById('sv').style.display = 'block';
                document.getElementById('sv-t').textContent = '회원가입 완료!';
                document.getElementById('sv-m').textContent = '못난이 농작물 가족이 되신 걸 환영해요! 🌿';
                setTimeout(function () { goPage('home'); }, 2000);
            } else {
                alert(result.error);
            }
        })
        .catch(err => alert('오류가 발생했습니다.'));
}

/* ══ MYPAGE TABS ══ */
function switchMyTab(name) {
    var tabs = ['orders', 'wishlist', 'coupon', 'myreview', 'profile'];
    var tabIds = ['orders', 'wishlist', 'coupon', 'review', 'profile'];
    tabs.forEach(function (t, i) {
        var c = document.getElementById('mycontent-' + t);
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
}

/* ══ 로그인 상태 버튼 전환 ══ */
document.addEventListener('DOMContentLoaded', function () {
    var token = localStorage.getItem('token');
    var btnNav = document.querySelector('.btn-nav');
    if (token && btnNav) {
        btnNav.textContent = '로그아웃';
        btnNav.setAttribute('onclick', 'doLogout()');
    }
});

/* ══ 로그아웃 ══ */
function doLogout() {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    localStorage.removeItem('role');
    location.href = '/login';
}