/* ══ LOGIN / SIGNUP ══ */

function togglePw(btn) {
    var input = btn.closest('.pw-wrap').querySelector('input');
    var icon = btn.querySelector('i');
    if (input.type === 'password') {
        input.type = 'text';
        icon.className = 'ti ti-eye-off';
    } else {
        input.type = 'password';
        icon.className = 'ti ti-eye';
    }
}

// localStorage 또는 sessionStorage에서 토큰 읽기
function getToken() {
    return localStorage.getItem('token') || sessionStorage.getItem('token');
}

function filterKorean(el) { el.value = el.value.replace(/[^가-힣]/g, ''); }
function filterEmailChars(el) { el.value = el.value.replace(/[^a-zA-Z0-9@._-]/g, ''); }
function filterPasswordChars(el) { el.value = el.value.replace(/[^a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~`]/g, ''); }
function filterDigits(el) { el.value = el.value.replace(/[^0-9]/g, '').slice(0, 11); }
function filterNicknameChars(el) { el.value = el.value.replace(/[^가-힣a-zA-Z]/g, ''); }

function toggleRoleFields(sel) {
    var isFarmer = sel.value === '판매자(농가)';
    var addrGroup = document.getElementById('addressGroup');
    var nickGroup = document.getElementById('nicknameGroup');
    if (addrGroup) addrGroup.style.display = isFarmer ? 'none' : '';
    if (nickGroup) nickGroup.style.display = isFarmer ? 'none' : '';
}

function clearFieldErrors() {
    document.querySelectorAll('#sf .fg').forEach(function (fg) {
        fg.classList.remove('error');
        var err = fg.querySelector('.fg-err');
        if (err) err.textContent = '';
    });
}

function showFieldError(name, msg) {
    var input = document.querySelector('#sf [name="' + name + '"]');
    if (!input) return;
    var fg = input.closest('.fg');
    if (!fg) return;
    fg.classList.add('error');
    var err = fg.querySelector('.fg-err');
    if (err) err.textContent = msg;
}

function swTab(t) {
    document.getElementById('lf').style.display = t === 'login' ? 'block' : 'none';
    document.getElementById('sf').style.display = t === 'signup' ? 'block' : 'none';
    document.getElementById('sv').style.display = 'none';
    document.getElementById('ff').style.display = 'none';
    var tl = document.getElementById('tab-l');
    var ts = document.getElementById('tab-s');
    if (tl) { tl.classList.toggle('on', t === 'login'); }
    if (ts) { ts.classList.toggle('on', t === 'signup'); }
}

function showForgotView() {
    document.getElementById('lf').style.display = 'none';
    document.getElementById('sf').style.display = 'none';
    document.getElementById('sv').style.display = 'none';
    document.getElementById('ff').style.display = 'block';
    var tl = document.getElementById('tab-l');
    var ts = document.getElementById('tab-s');
    if (tl) tl.classList.remove('on');
    if (ts) ts.classList.remove('on');
}

function showLoginView() {
    document.getElementById('ff').style.display = 'none';
    document.getElementById('lf').style.display = 'block';
    var tl = document.getElementById('tab-l');
    if (tl) tl.classList.add('on');
}

function doForgotPassword() {
    var email = document.getElementById('fe').value.trim();
    var name = document.getElementById('fn').value.trim();

    if (!email || !name) {
        showToast('이메일과 이름을 모두 입력해주세요!');
        return;
    }

    fetch('/forgot-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email, name: name })
    })
        .then(function(res) { return res.json(); })
        .then(function(result) {
            if (result.token) {
                location.href = '/resetpw?token=' + result.token;
            } else {
                showToast(result.error);
            }
        })
        .catch(function() { showToast('오류가 발생했습니다.'); });
}

function doLogin() {
    const email = document.getElementById('le').value;
    const password = document.getElementById('lp').value;
    const remember = document.querySelector('.f-rem input').checked;

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
                if (remember) {
                    localStorage.setItem('token', result.token);
                    localStorage.setItem('email', result.email);
                    localStorage.setItem('role', result.role);
                } else {
                    sessionStorage.setItem('token', result.token);
                    sessionStorage.setItem('email', result.email);
                    sessionStorage.setItem('role', result.role);
                }
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

    clearFieldErrors();
    let hasError = false;
    const isFarmer = data.role === '판매자(농가)';

    if (!data.name) { showFieldError('name', '이름은 필수 정보입니다.'); hasError = true; }
    else if (!/^[가-힣]+$/.test(data.name)) { showFieldError('name', '이름은 한글만 입력 가능해요.'); hasError = true; }

    if (!data.email) { showFieldError('email', '이메일은 필수 정보입니다.'); hasError = true; }
    else if (!/^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$/.test(data.email)) { showFieldError('email', '이메일 형식을 확인해주세요.'); hasError = true; }

    if (!data.password) { showFieldError('password', '비밀번호는 필수 정보입니다.'); hasError = true; }
    else if (!/^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~`]).{8,}$/.test(data.password)) { showFieldError('password', '영어, 숫자, 특수문자를 포함해 8자 이상 입력해주세요.'); hasError = true; }

    if (!data.passwordConfirm) { showFieldError('passwordConfirm', '비밀번호 확인은 필수 정보입니다.'); hasError = true; }
    else if (data.password !== data.passwordConfirm) { showFieldError('passwordConfirm', '비밀번호가 일치하지 않아요.'); hasError = true; }

    if (!data.phone) { showFieldError('phone', '전화번호는 필수 정보입니다.'); hasError = true; }
    else if (!/^[0-9]+$/.test(data.phone)) { showFieldError('phone', '전화번호는 숫자만 입력 가능해요.'); hasError = true; }

    if (!isFarmer) {
        if (!data.address) { showFieldError('address', '주소는 필수 정보입니다.'); hasError = true; }
    }

    if (!isFarmer && data.nickname && !/^[가-힣a-zA-Z]+$/.test(data.nickname)) { showFieldError('nickname', '닉네임은 한글/영어만 입력 가능해요.'); hasError = true; }

    if (hasError) return;

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
                setTimeout(function () { location.href = '/'; }, 2000);
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

/* ══ 로그아웃 ══ */
function doLogout() {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    localStorage.removeItem('role');
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('email');
    sessionStorage.removeItem('role');
    location.href = '/login';
}