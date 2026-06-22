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

function getToken() {
    return localStorage.getItem('token') || sessionStorage.getItem('token');
}

function filterKorean(el) { el.value = el.value.replace(/[^가-힣]/g, ''); }
function filterEmailChars(el) { el.value = el.value.replace(/[^a-zA-Z0-9@._-]/g, ''); }
function filterPasswordChars(el) { el.value = el.value.replace(/[^a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~`]/g, ''); }
function filterDigits(el) { el.value = el.value.replace(/[^0-9]/g, '').slice(0, 11); }
function filterNicknameChars(el) { el.value = el.value.replace(/[^가-힣a-zA-Z]/g, ''); }

/* ══ 실시간 검증 상태 관리 ══ */

function setFgState(fg, state, msg) {
    fg.classList.remove('error', 'valid');
    var err = fg.querySelector('.fg-err');
    var ok = fg.querySelector('.fg-ok');
    if (state === 'error') {
        fg.classList.add('error');
        if (err) err.textContent = msg || '';
        if (ok) ok.textContent = '';
    } else if (state === 'valid') {
        fg.classList.add('valid');
        if (ok) ok.textContent = msg || '';
        if (err) err.textContent = '';
    } else {
        if (err) err.textContent = '';
        if (ok) ok.textContent = '';
    }
}

/* 이메일 중복 실시간 체크 (blur 이벤트) */
var _emailTimer = null;
function checkEmailLive(input) {
    var email = input.value.trim();
    var fg = input.closest('.fg');
    if (!email) { setFgState(fg, ''); return; }
    if (!/^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$/.test(email)) {
        setFgState(fg, 'error', '이메일 형식을 확인해주세요.');
        return;
    }
    clearTimeout(_emailTimer);
    _emailTimer = setTimeout(function () {
        fetch('/api/check/email?email=' + encodeURIComponent(email))
            .then(function (res) { return res.json(); })
            .then(function (result) {
                if (result.exists) {
                    setFgState(fg, 'error', '이미 사용 중인 이메일이에요.');
                } else {
                    setFgState(fg, 'valid', '사용 가능한 이메일이에요. ✓');
                }
            })
            .catch(function () {});
    }, 300);
}

/* 비밀번호 형식 실시간 체크 (input 이벤트) */
var _pwTimer = null;
function validatePasswordLive(input) {
    var pw = input.value;
    var fg = input.closest('.fg');
    if (!pw) { setFgState(fg, ''); return; }
    clearTimeout(_pwTimer);
    _pwTimer = setTimeout(function () {
        fetch('/api/check/password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ password: pw })
        })
            .then(function (res) { return res.json(); })
            .then(function (result) {
                if (result.valid) {
                    setFgState(fg, 'valid', '안전한 비밀번호에요. ✓');
                } else {
                    setFgState(fg, 'error', '영어, 숫자, 특수문자를 포함해 8자 이상 입력해주세요.');
                }
                var confirmInput = document.querySelector('#sf input[name="passwordConfirm"]');
                if (confirmInput && confirmInput.value) validatePasswordConfirmLive(confirmInput);
            })
            .catch(function () {});
    }, 400);
}

/* 비밀번호 확인 실시간 체크 (input 이벤트) */
var _pwConfirmTimer = null;
function validatePasswordConfirmLive(input) {
    var confirm = input.value;
    var fg = input.closest('.fg');
    var pwInput = document.querySelector('#sf input[name="password"]');
    var pw = pwInput ? pwInput.value : '';
    if (!confirm) { setFgState(fg, ''); return; }
    clearTimeout(_pwConfirmTimer);
    _pwConfirmTimer = setTimeout(function () {
        fetch('/api/check/password-confirm', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ password: pw, passwordConfirm: confirm })
        })
            .then(function (res) { return res.json(); })
            .then(function (result) {
                if (result.match) {
                    setFgState(fg, 'valid', '비밀번호가 일치해요. ✓');
                } else {
                    setFgState(fg, 'error', '비밀번호가 일치하지 않아요.');
                }
            })
            .catch(function () {});
    }, 300);
}

/* ══ 역할 전환 (필드 초기화 포함) ══ */
function toggleRoleFields(sel) {
    var isFarmer = sel.value === '판매자(농가)';
    var addrGroup = document.getElementById('addressGroup');
    var nickGroup = document.getElementById('nicknameGroup');
    if (addrGroup) addrGroup.style.display = isFarmer ? 'none' : '';
    if (nickGroup) nickGroup.style.display = isFarmer ? 'none' : '';

    // 역할 전환 시 입력 필드 초기화
    var sf = document.getElementById('sf');
    if (sf) {
        sf.querySelectorAll('input[name]').forEach(function (input) {
            if (input.name !== 'role' && !input.readOnly) input.value = '';
        });
        var postcode = document.getElementById('sf-postcode');
        var address = document.getElementById('sf-address');
        var detail = document.getElementById('sf-address-detail');
        if (postcode) postcode.value = '';
        if (address) address.value = '';
        if (detail) detail.value = '';
        sf.querySelectorAll('.fg').forEach(function (fg) {
            fg.classList.remove('error', 'valid');
            var err = fg.querySelector('.fg-err');
            var ok = fg.querySelector('.fg-ok');
            if (err) err.textContent = '';
            if (ok) ok.textContent = '';
        });
    }
}

function clearFieldErrors() {
    document.querySelectorAll('#sf .fg').forEach(function (fg) {
        fg.classList.remove('error', 'valid');
        var err = fg.querySelector('.fg-err');
        var ok = fg.querySelector('.fg-ok');
        if (err) err.textContent = '';
        if (ok) ok.textContent = '';
    });
}

function showFieldError(name, msg) {
    var input = document.querySelector('#sf [name="' + name + '"]');
    if (!input) return;
    var fg = input.closest('.fg');
    if (!fg) return;
    setFgState(fg, 'error', msg);
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

/* ══ 성공 모달 ══ */
var _smRedirectTimer = null;
function showSuccessModal(title, msg, redirectUrl) {
    var modal = document.getElementById('success-modal');
    if (!modal) { location.href = redirectUrl || '/'; return; }

    document.getElementById('sm-title').textContent = title;
    document.getElementById('sm-msg').textContent = msg;

    var smBtn = document.getElementById('sm-btn');
    if (smBtn && redirectUrl) smBtn.onclick = function () { location.href = redirectUrl; };

    modal.style.display = 'flex';

    // 타이머 바 애니메이션 (3초)
    var bar = document.getElementById('sm-bar');
    if (bar) {
        bar.style.transition = 'none';
        bar.style.width = '100%';
        setTimeout(function () {
            bar.style.transition = 'width 3s linear';
            bar.style.width = '0%';
        }, 60);
    }

    clearTimeout(_smRedirectTimer);
    if (redirectUrl) {
        _smRedirectTimer = setTimeout(function () { location.href = redirectUrl; }, 3000);
    }
}

function handleSmOverlayClick(e) {
    if (e.target === document.getElementById('success-modal')) {
        clearTimeout(_smRedirectTimer);
        document.getElementById('success-modal').style.display = 'none';
    }
}

/* ══ 비밀번호 찾기 ══ */
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
        .then(function (res) { return res.json(); })
        .then(function (result) {
            if (result.token) {
                location.href = '/resetpw?token=' + result.token;
            } else {
                showToast(result.error);
            }
        })
        .catch(function () { showToast('오류가 발생했습니다.'); });
}

/* ══ 로그인 ══ */
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
                var storage = remember ? localStorage : sessionStorage;
                storage.setItem('token', result.token);
                storage.setItem('email', result.email);
                storage.setItem('role', result.role);
                storage.setItem('name', result.name || '');
                document.cookie = 'jwt=' + result.token + '; path=/; SameSite=Lax';
                document.getElementById('lf').style.display = 'none';
                showSuccessModal(
                    '로그인 성공! 🎉',
                    (result.name || '고객') + '님, 환영합니다! 못난이 농작물에 오신 것을 환영해요 🌿',
                    '/'
                );
            } else {
                showToast(result.error);
            }
        })
        .catch(err => showToast('오류가 발생했습니다.'));
}

/* ══ 회원가입 ══ */
function doSignup() {
    const data = {
        name: document.querySelector('#sf input[name="name"]').value,
        role: document.querySelector('#sf select[name="role"]').value,
        email: document.querySelector('#sf input[name="email"]').value,
        password: document.querySelector('#sf input[name="password"]').value,
        passwordConfirm: document.querySelector('#sf input[name="passwordConfirm"]').value,
        phone: document.querySelector('#sf input[name="phone"]').value,
        address: (document.querySelector('#sf input[name="address"]').value + ' ' + document.querySelector('#sf input[name="addressDetail"]').value).trim(),
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
                showSuccessModal(
                    '회원가입 완료! 🎉',
                    '못난이 농작물 가족이 되신 걸 환영해요! 로그인 페이지로 이동합니다 🌿',
                    '/login'
                );
                var smBtn = document.getElementById('sm-btn');
                if (smBtn) smBtn.textContent = '로그인하러 가기 →';
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

/* ══ 주소 검색 ══ */
function openAddressSearch() {
    new daum.Postcode({
        oncomplete: function (data) {
            var addr = data.roadAddress || data.jibunAddress;
            document.getElementById('sf-postcode').value = data.zonecode;
            document.getElementById('sf-address').value = addr;
            document.getElementById('sf-address-detail').focus();
        }
    }).open();
}

/* ══ 소셜 로그인 콜백 처리 ══
   KakaoController가 /login?oauth_token=...&oauth_name=...&oauth_email=...&oauth_role=... 로 리다이렉트하면
   여기서 파라미터를 읽어 localStorage에 저장 후 홈으로 이동 */
(function () {
    var params = new URLSearchParams(window.location.search);
    var token = params.get('oauth_token');
    if (!token) return;

    localStorage.setItem('token', token);
    localStorage.setItem('name',  decodeURIComponent(params.get('oauth_name')  || ''));
    localStorage.setItem('email', decodeURIComponent(params.get('oauth_email') || ''));
    localStorage.setItem('role',  params.get('oauth_role') || 'BUYER');
    document.cookie = 'jwt=' + token + '; path=/; SameSite=Lax';

    var name = localStorage.getItem('name') || '고객';
    showSuccessModal(
        '로그인 성공! 🎉',
        name + '님, 환영합니다! 못난이 농작물에 오신 것을 환영해요 🌿',
        '/'
    );
})();

/* ══ 로그아웃 ══ */
function doLogout() {
    ['token', 'email', 'role', 'name'].forEach(function (k) {
        localStorage.removeItem(k);
        sessionStorage.removeItem(k);
    });
    document.cookie = 'jwt=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; SameSite=Lax';
    location.href = '/login';
}
