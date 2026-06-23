function togglePw(btn) {
    var input = btn.closest('.pw-wrap').querySelector('input');
    var icon  = btn.querySelector('i');
    if (input.type === 'password') {
        input.type        = 'text';
        icon.className    = 'ti ti-eye-off';
    } else {
        input.type        = 'password';
        icon.className    = 'ti ti-eye';
    }
}

function getToken() {
    var params = new URLSearchParams(window.location.search);
    return params.get('token');
}

function showErr(id, msg) {
    var el = document.getElementById(id + '-err');
    var fg = el ? el.closest('.fg') : null;
    if (el) el.textContent = msg;
    if (fg) fg.classList.add('error');
}

function clearErr() {
    document.querySelectorAll('.fg').forEach(function (fg) {
        fg.classList.remove('error');
    });
    document.querySelectorAll('.fg-err').forEach(function (el) {
        el.textContent = '';
    });
}

function doResetPw() {
    clearErr();
    var token    = getToken();
    var pw       = document.getElementById('rp1').value;
    var pwc      = document.getElementById('rp2').value;
    var hasError = false;

    if (!token) {
        showToast('유효하지 않은 링크입니다.');
        return;
    }

    if (!pw) {
        showErr('rp1', '새 비밀번호를 입력해주세요.');
        hasError = true;
    } else if (!/^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~`]).{8,}$/.test(pw)) {
        showErr('rp1', '영어, 숫자, 특수문자를 포함해 8자 이상 입력해주세요.');
        hasError = true;
    }

    if (!pwc) {
        showErr('rp2', '비밀번호 확인을 입력해주세요.');
        hasError = true;
    } else if (pw !== pwc) {
        showErr('rp2', '비밀번호가 일치하지 않아요.');
        hasError = true;
    }

    if (hasError) return;

    fetch('/resetpw', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token: token, password: pw, passwordConfirm: pwc })
    })
        .then(function (res) { return res.json(); })
        .then(function (result) {
            if (result.message) {
                document.getElementById('rpf').style.display = 'none';
                document.getElementById('rpv').style.display = 'block';
            } else {
                showToast(result.error);
            }
        })
        .catch(function () { showToast('오류가 발생했습니다.'); });
}
