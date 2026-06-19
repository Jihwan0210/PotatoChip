/* ══ DETAIL PAGE ══ */
function switchDetailTab(name){
    ['info','review','pickup'].forEach(function(t){
        var el=document.getElementById('detail-tab-'+t);
        var tab=document.getElementById('dtab-'+t);
        if(el) el.style.display=(t===name?'block':'none');
        if(tab){
            tab.style.borderBottom=(t===name?'2.5px solid var(--dark)':'2.5px solid transparent');
            tab.style.color=(t===name?'var(--dark)':'var(--muted)');
            tab.style.fontWeight=(t===name?'500':'400');
        }
    });
}
var detailQtyVal = 1;
function changeDetailQty(d) {
    detailQtyVal = Math.max(1, detailQtyVal + d);
    document.getElementById('detailQty').textContent = detailQtyVal;

    // 총 금액 업데이트
    var priceEl = document.getElementById('detailTotalPrice');
    if (priceEl) {
        var unit = parseFloat(priceEl.getAttribute('data-unit'));
        var total = unit * detailQtyVal;
        priceEl.textContent = total.toLocaleString() + '원';
    }
}

/* ══ WISH ══ */
function toggleDetailWish(btn){
    var w=btn.textContent==='❤️';
    btn.textContent=w?'🤍':'❤️';
    var b2=document.getElementById('wishBtn2');
    if(b2) b2.innerHTML=w?'🤍 찜하기':'❤️ 찜 취소';
}
function toggleDetailWish2(btn){
    var w=btn.innerHTML.indexOf('❤️')>=0;
    btn.innerHTML=w?'🤍 찜하기':'❤️ 찜 취소';
    var b1=document.getElementById('detailWishBtn');
    if(b1) b1.textContent=w?'🤍':'❤️';
}

/* ══ AI 리뷰 총평 ══ */
async function loadAiSummary(){
    var box=document.getElementById('aiReviewSummary');
    var btn=document.getElementById('btnAiSummary');
    if(!box) return;
    if(btn) btn.style.display='none';
    box.innerHTML='<div style="color:var(--muted);font-size:.78rem">⏳ AI가 리뷰 312건을 분석 중이에요...</div>';
    var reviews='생김새는 못났지만 맛은 정말 최고예요! 배송도 빠르고 포장이 꼼꼼해서 하나도 안 상하고 도착했어요. 반신반의했는데 상태가 엄청 좋아요. 당도가 정말 높아요.';
    var body=JSON.stringify({model:'claude-sonnet-4-20250514',max_tokens:300,system:'쇼핑몰 리뷰 분석 AI입니다. 2~3문장으로 간결하게 총평하세요. 이모지를 적절히 사용해 한국어로만 답하세요.',messages:[{role:'user',content:'리뷰 총평: '+reviews}]});
    try{
        var res=await fetch('https://api.anthropic.com/v1/messages',{method:'POST',headers:{'Content-Type':'application/json'},body:body});
        var data=await res.json();
        var summary=(data.content&&data.content[0]&&data.content[0].text)||'리뷰 분석에 실패했어요.';
        box.innerHTML='<div style="font-size:.8rem;color:var(--mid);line-height:1.75">'+summary.replace(/\n/g,'<br>')+'</div>';
    }catch(err){
        box.innerHTML='<div style="font-size:.78rem;color:var(--muted)">AI 분석을 불러오지 못했어요.</div>';
        if(btn) btn.style.display='flex';
    }
}

/* ══ 이미지 미리보기 ══ */
const thumbnailInput = document.querySelector('input[name="thumbnailFile"]');
if (thumbnailInput) {
    thumbnailInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                const preview = document.getElementById('thumbPreview');
                preview.src = e.target.result;
                preview.style.display = 'block';
            };
            reader.readAsDataURL(file);
        }
    });
}

/* ══ REPORT MODAL ══ */
function openReport(){
    var m=document.getElementById('reportModal');
    if(m) m.style.display='flex';
}
function closeReport(){
    var m=document.getElementById('reportModal');
    if(m) m.style.display='none';
}
function submitReport(){
    var type=document.querySelector('input[name="rtype"]:checked');
    if(!type){showToast('신고 유형을 선택해주세요');return;}
    closeReport();
    showToast('🚩 신고가 접수됐어요. 검토 후 조치할게요.');
}
/* ══ MARKET ══ */
function catClick(el){
    document.querySelectorAll('.h-cat').forEach(function(x){x.classList.remove('on');});
    el.classList.add('on');
    var cat=el.textContent.replace(/[🥦🍎🌾🍄🥕🎁⏰🍱]/g,'').trim();
    goPage('market');
    switchMktTab(0);
    setTimeout(function(){
        document.querySelectorAll('.mf').forEach(function(f){
            if(f.textContent.indexOf(cat)>=0) mktF(f);
        });
    },100);
}

function mktF(el) {
    el.parentElement.querySelectorAll('.mf').forEach(function(x) {
        x.classList.remove('on');
    });
    el.classList.add('on');

    var txt = el.textContent.replace(/[🥦🍎🌾🍄🥕⏰\s]/g, '').trim();
    location.href = '/market?category=' + encodeURIComponent(txt);
}
function switchMktTab(i){
    for(var j=0;j<4;j++){
        var t=document.getElementById('mkt-tab-'+j);
        if(t){t.style.borderBottom='2px solid transparent';t.style.color='var(--muted)';t.style.fontWeight='400';}
        var c=document.getElementById('mkt-content-'+j);
        if(c) c.style.display='none';
    }
    var el=document.getElementById('mkt-tab-'+i);
    if(el){el.style.borderBottom='2px solid var(--green)';el.style.color='var(--dark)';el.style.fontWeight='500';}
    var ct=document.getElementById('mkt-content-'+i);
    if(ct) ct.style.display='block';
}

function doMktSearch() {
    var keyword = document.getElementById('mktSearch').value.trim();
    var searchType = document.getElementById('mktSearchType').value;
    var url = new URL(window.location.href);
    url.searchParams.set('keyword', keyword);
    url.searchParams.set('searchType', searchType);
    url.searchParams.set('page', '0');
    window.location.href = url.toString();
}

function clearMktSearch() {
    location.href = '/market';
}
/* ══ WISHLIST (localStorage) ══ */

function getWishKey() {
    const email = localStorage.getItem('email') || sessionStorage.getItem('email');
    return email ? 'wishlist_' + email : 'wishlist_guest';
}

function isLoggedIn() {
    return !!(localStorage.getItem('email') || sessionStorage.getItem('email'));
}

// 찜 목록 가져오기
function getWishlist() {
    return JSON.parse(localStorage.getItem(getWishKey()) || '[]');
}

// 찜 저장
function saveWishlist(list) {
    localStorage.setItem(getWishKey(), JSON.stringify(list));
}

// 찜 토글 (상세페이지 메인 이미지 버튼)
function toggleDetailWish(btn) {
    if (!isLoggedIn()) {
        showToast('로그인 후 이용해주세요');
        return;
    }
    const list = getWishlist();
    const id = String(productId);
    const idx = list.indexOf(id);
    if (idx === -1) {
        list.push(id);
        btn.textContent = '❤️';
        showToast('❤️ 찜 목록에 추가됐어요!');
    } else {
        list.splice(idx, 1);
        btn.textContent = '🤍';
        showToast('찜 목록에서 제거됐어요');
    }
    saveWishlist(list);
    syncWishBtns();
}

// 찜 토글 (상세페이지 하단 찜하기 버튼)
function toggleDetailWish2(btn) {
    toggleDetailWish(document.getElementById('detailWishBtn'));
}

// 두 버튼 동기화
function syncWishBtns() {
    const list = getWishlist();
    const id = String(productId);
    const isWished = list.includes(id);
    const btn1 = document.getElementById('detailWishBtn');
    const btn2 = document.getElementById('wishBtn2');
    if (btn1) btn1.textContent = isWished ? '❤️' : '🤍';
    if (btn2) btn2.textContent = isWished ? '❤️ 찜됨' : '🤍 찜하기';
}

// 마켓 찜 토글 (API 기반)
async function toggleW(btn) {
    if (!isLoggedIn()) {
        showToast('로그인 후 이용해주세요');
        return;
    } const role = localStorage.getItem('role') || sessionStorage.getItem('role');
    if (role === 'SELLER') {
        showToast('판매자 계정은 찜 기능을 이용할 수 없어요');
        return;
    }
    const card = btn.closest('[data-id]') || btn.closest('.pc2');
    const id = card ? String(card.dataset.id || '') : '';
    if (!id) return;

    const token = localStorage.getItem('token') || sessionStorage.getItem('token');

    try {
        const res = await fetch('/wishlist/' + id, {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const data = await res.json();
        if (data.added) {
            btn.textContent = '❤️';
            showToast('❤️ 찜 목록에 추가됐어요!');
        } else {
            btn.textContent = '🤍';
            showToast('찜 목록에서 제거됐어요');
        }
    } catch (e) {
        showToast('오류가 발생했어요');
    }
}

// 페이지 로드 시 찜 상태 복원
document.addEventListener('DOMContentLoaded', function() {
    // 상세페이지면 버튼 동기화
    if (typeof productId !== 'undefined') {
        syncWishBtns();
    }

    // 마켓 카드 찜 버튼 상태 복원 (API 기반)
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    if (token) {
        fetch('/wishlist', {
            headers: { 'Authorization': 'Bearer ' + token }
        })
            .then(res => res.json())
            .then(list => {
                const ids = list.map(p => String(p.id));
                document.querySelectorAll('.plk').forEach(function(btn) {
                    const card = btn.closest('[data-id]') || btn.closest('.pc2');
                    if (card && ids.includes(String(card.dataset.id || ''))) {
                        btn.textContent = '❤️';
                    }
                });
            })
            .catch(e => console.error('찜 목록 로드 실패', e));
    }


});