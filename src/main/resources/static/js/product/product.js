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

    // 수량 href 업데이트
    const btn = document.getElementById('buyNowBtn');
    btn.href = `/cart/items/buy-now?productId=${productId}&quantity=${detailQtyVal}`;
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
    var categoryMap = {
        '전체': null,
        '채소': '채소',
        '과일': '과일',
        '곡류': '곡류',
        '버섯': '버섯',
        '뿌리채소': '뿌리채소',
        '기한임박': '기한임박'
    };
    var selectedCat = categoryMap[txt];

    // 탭 0으로 강제 전환 추가
    switchMktTab(0);

    document.querySelectorAll('.pgrid .pc2').forEach(function(card) {
        if (!selectedCat || card.dataset.category === selectedCat) {
            card.style.display = '';
        } else {
            card.style.display = 'none';
        }
    });

    var visible = document.querySelectorAll('.pgrid .pc2:not([style*="display: none"])').length;
    var cntEl = document.querySelector('.pgrid-count');
    if (cntEl) cntEl.innerHTML = '총 <strong style="color:var(--dark)">' + visible + '</strong>개 상품';

    // 상품 목록으로 스크롤
    var pgrid = document.querySelector('.pgrid');
    if (pgrid) pgrid.scrollIntoView({behavior: 'smooth', block: 'start'});
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

function doMktSearch(){
    var q=document.getElementById('mktSearch');
    var keyword=(q?q.value.trim():'').toLowerCase();
    var c=document.getElementById('mktSearchClear');
    if(c) c.style.display=keyword?'inline':'none';
    if(!keyword) return;
    switchMktTab(0);
    var cards=document.querySelectorAll('#mkt-content-0 .pc2');
    var shown=0;
    cards.forEach(function(card){
        var nm=card.textContent.toLowerCase();
        var match=nm.indexOf(keyword)>=0;
        card.style.display=match?'':'none';
        if(match) shown++;
    });
    var cnt=document.querySelector('.pgrid-count');
    if(cnt) cnt.innerHTML='검색 결과: <strong style="color:var(--dark)">'+shown+'</strong>개';
}

function clearMktSearch(){
    var q=document.getElementById('mktSearch');
    var c=document.getElementById('mktSearchClear');
    if(q) q.value='';
    if(c) c.style.display='none';
    document.querySelectorAll('#mkt-content-0 .pc2').forEach(function(card){card.style.display='';});
    var cnt=document.querySelector('.pgrid-count');
    if(cnt) cnt.innerHTML='총 <strong style="color:var(--dark)">302</strong>개 상품';
}

