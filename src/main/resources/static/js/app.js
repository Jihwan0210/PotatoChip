
/* ═══════════════════════════════════════════
   못난이 농작물 — 메인 스크립트
   ═══════════════════════════════════════════ */

/* ── 전역 상태 ── */
var cItems = {
  ci1:{up:11700,qty:1,name:'흠집 완숙 토마토 2kg',emoji:'🍅',farm:'경남 함안 · 홍길동 농장',exp:'유통기한 D-3',ogPrice:18000},
  ci2:{up:8640,qty:2,name:'비균형 브로콜리 1.5kg',emoji:'🥦',farm:'전남 나주 · 이순신 농장',exp:'유통기한 D-5',ogPrice:12000},
  ci3:{up:27000,qty:1,name:'황토 감자 5kg',emoji:'🥔',farm:'강원 해피팜',exp:'유통기한 D-7',ogPrice:36000}
};
var sf=0, pd=0;
var orderHistory=[];
var notifList=[
  {id:'n0',read:false,icon:'🔥',title:'임박 특가 마감 2시간 전!',msg:'굴곡진 제주 당근 2kg — 50% 할인이 곧 종료돼요',time:'10분 전',page:'market'},
  {id:'n1',read:false,icon:'🚚',title:'주문 배송 시작',msg:'홍옥 사과 3kg 주문이 출고됐어요.',time:'1시간 전',page:'mypage'},
  {id:'n2',read:true,icon:'🎉',title:'이번 주 판매 랭킹 업데이트',msg:'홍옥 사과가 이번 주도 1위예요!',time:'어제',page:'ranking'}
];
var thumbColors={'🍅':'ap','🥦':'gn','🥔':'gn','🍎':'gn','🥕':'ap','🍊':'ap','🌾':'mt','🍄':'mt','🥬':'gn','🎁':'mt'};
var bReplies={
  '배송 문의':'보통 주문 후 2~3일 내 배송돼요 🚚',
  '환불 문의':'수령 후 24시간 내 1:1 문의로 신청해주세요 😊',
  '농장 직접구매':'픽업 서비스 탭에서 농장 위치를 확인하세요 📍',
  '포인트 문의':'구매 금액의 1%가 자동 적립돼요 ✨'
};

/* ══ PAGE ROUTING ══ */
function goPage(name){
  var notifP=document.getElementById('notifPopup');
  if(notifP) notifP.style.display='none';
  var recP=document.getElementById('recPopup');
  if(recP) recP.style.display='none';
  var pg=document.getElementById('page-'+name);
  if(pg){
    document.querySelectorAll('.page').forEach(function(p){p.classList.remove('active');});
    pg.classList.add('active');
    window.scrollTo(0,0);
  }
  document.querySelectorAll('.nav-links a').forEach(function(a){
    var oc=a.getAttribute('onclick');
    if(oc && oc.indexOf("'"+name+"'")>=0){a.style.color='var(--dark)';a.style.fontWeight='700';}
    else{a.style.color='';a.style.fontWeight='';}
  });
  // 페이지별 렌더링 훅
  if(name==='cart') renderCart();
  if(name==='mypage'){renderOrderHistory();renderNotifBadge();}
  if(name==='home'||name==='market') renderNotifBadge();
}

/* ══ TOAST ══ */
function showToast(msg, duration){
  var t=document.getElementById('cartToast');
  if(!t){
    t=document.createElement('div');
    t.id='cartToast';
    t.style.cssText='position:fixed;bottom:80px;left:50%;transform:translateX(-50%);background:var(--dark);color:#fff;padding:11px 24px;border-radius:50px;font-size:.82rem;font-weight:500;font-family:Gaegu,cursive;z-index:9999;display:none;white-space:nowrap;box-shadow:0 4px 20px rgba(0,0,0,.22);transition:opacity .3s';
    document.body.appendChild(t);
  }
  t.textContent=msg;
  t.style.display='block';
  t.style.opacity='1';
  clearTimeout(window._toastT);
  window._toastT=setTimeout(function(){
    t.style.opacity='0';
    setTimeout(function(){t.style.display='none';t.style.opacity='1';},300);
  }, duration||2200);
}

/* ══ CART BADGE ══ */
function updateCartBadge(){
  var cnt=Object.keys(cItems).length;
  var badge=document.getElementById('cartBadge');
  if(badge){badge.textContent=cnt;badge.style.display=cnt?'flex':'none';}
}

/* ══ CART RENDER ══ */
function renderCart(){
  var list=document.getElementById('cartList');
  var empty=document.getElementById('cartEmpty');
  var summary=document.getElementById('cartSummary');
  if(!list) return;
  var keys=Object.keys(cItems);
  updateCartBadge();
  var cnt=document.getElementById('cartItemCount');
  if(cnt) cnt.textContent=keys.length+'개 상품';
  if(empty) empty.style.display=keys.length?'none':'block';
  if(summary) summary.style.display=keys.length?'':'none';
  list.querySelectorAll('.cart-item').forEach(function(el){el.remove();});
  keys.forEach(function(id){
    var it=cItems[id];
    var cl=thumbColors[it.emoji]||'gn';
    var d=document.createElement('div');
    d.className='cart-item';
    d.id=id;
    var ogHtml=it.ogPrice?'<span class="ct-og">'+it.ogPrice.toLocaleString()+'원</span>':'';
    d.innerHTML='<div class="ct-thumb '+cl+'">'+it.emoji+'</div>'
      +'<div style="flex:1">'
      +'<div class="ct-from">'+it.farm+'</div>'
      +'<div class="ct-nm">'+it.name+'</div>'
      +'<div class="ct-exp">'+it.exp+'</div>'
      +'<div class="qty-ctrl">'
      +'<button class="qty-b" onclick="chgQ(\''+id+'\',-1)">−</button>'
      +'<span class="qty-v" id="q-'+id+'">'+it.qty+'</span>'
      +'<button class="qty-b" onclick="chgQ(\''+id+'\',1)">+</button>'
      +'</div></div>'
      +'<div class="ct-r">'
      +'<span class="ct-del" onclick="rmItem(\''+id+'\')">✕</span>'
      +ogHtml
      +'<span class="ct-price" id="p-'+id+'">'+(it.up*it.qty).toLocaleString()+'원</span>'
      +'</div>';
    list.appendChild(d);
  });
  updSum();
}

/* ══ CART FUNCTIONS ══ */
function addToCart(name,price,emoji,farm){
  var nm=name||'상품';
  var pr=price||9900;
  var em=emoji||'🛒';
  var fm=farm||'못난이 농작물';
  var id='ci'+Date.now();
  cItems[id]={up:pr,qty:1,name:nm,emoji:em,farm:fm,exp:'신선 배송',ogPrice:Math.round(pr*1.35)};
  renderCart();
  showToast('🛒 '+nm+' 장바구니에 담겼어요!');
  updateCartBadge();
}

function chgQ(id,d){
  if(!cItems[id]) return;
  cItems[id].qty=Math.max(1,cItems[id].qty+d);
  var qEl=document.getElementById('q-'+id);
  if(qEl) qEl.textContent=cItems[id].qty;
  var pEl=document.getElementById('p-'+id);
  if(pEl) pEl.textContent=(cItems[id].up*cItems[id].qty).toLocaleString()+'원';
  updSum();
}

function rmItem(id){
  var nm=(cItems[id]&&cItems[id].name)||'상품';
  if(!confirm(nm+'을(를) 장바구니에서 제거할까요?')) return;
  delete cItems[id];
  renderCart();
  showToast('🗑 '+nm+' 제거됐어요');
}

function updSum(){
  var tot=0, ogTot=0;
  Object.values(cItems).forEach(function(it){
    tot+=it.up*it.qty;
    ogTot+=(it.ogPrice||it.up)*it.qty;
  });
  var fin=Math.max(0,tot+sf-pd);
  var disc=ogTot-tot;
  var et=document.getElementById('cs-tot'); if(et) et.textContent=ogTot.toLocaleString()+'원';
  var ed=document.getElementById('cs-disc'); if(ed) ed.textContent='-'+disc.toLocaleString()+'원';
  var ef=document.getElementById('cs-fin'); if(ef) ef.textContent=fin.toLocaleString()+'원';
  var pts=document.getElementById('cartPoints'); if(pts) pts.textContent='🌿 이번 구매로 '+Math.floor(fin/100)+'P 적립 예정!';
}

function selD(el,fee){
  document.querySelectorAll('.del-opt').forEach(function(d){d.classList.remove('on');d.querySelector('input').checked=false;});
  el.classList.add('on'); el.querySelector('input').checked=true;
  sf=fee;
  var es=document.getElementById('cs-ship'); if(es) es.textContent=fee?fee.toLocaleString()+'원':'무료';
  updSum();
}

function applyPt(){
  pd=Math.min(parseInt(document.getElementById('ptIn').value)||0,2000);
  var ep=document.getElementById('cs-pt'); if(ep) ep.textContent='-'+pd.toLocaleString()+'원';
  updSum();
  if(pd) showToast(pd.toLocaleString()+'P 적용됐어요 ✨');
}

/* ══ ORDER ══ */
function doOrder(){
  var keys=Object.keys(cItems);
  if(!keys.length){showToast('🛒 장바구니가 비어있어요!');return;}
  var finEl=document.getElementById('cs-fin');
  var fin=finEl?parseInt(finEl.textContent.replace(/[^0-9]/g,''))||0:0;
  var delType=document.querySelector('.del-opt.on .del-ti');
  var delLabel=delType?delType.textContent:'일반 배송';
  if(!confirm('결제를 진행할까요? 💳\n\n상품 '+keys.length+'개\n배송: '+delLabel+'\n결제금액: '+(fin.toLocaleString())+'원')) return;
  var now=new Date();
  var mm=String(now.getMonth()+1).padStart(2,'0');
  var dd=String(now.getDate()).padStart(2,'0');
  var rnd=String(Math.floor(Math.random()*90+10));
  var orderId='#'+now.getFullYear()+mm+dd+rnd;
  var orderItems=[];
  keys.forEach(function(id){orderItems.push(JSON.parse(JSON.stringify(cItems[id])));});
  orderHistory.unshift({
    id:orderId,
    date:now.getFullYear()+'.'+mm+'.'+dd,
    items:orderItems,
    total:fin,
    delivery:delLabel,
    status:'결제완료'
  });
  cItems={}; pd=0; sf=0;
  var ptIn=document.getElementById('ptIn'); if(ptIn) ptIn.value='';
  renderCart();
  addNotification('🎉 주문 완료!','주문 '+orderId+' 결제가 완료됐어요.','mypage');
  renderOrderHistory();
  showToast('✅ 주문 완료! 배송을 시작할게요 🚚', 3000);
  setTimeout(function(){
    goPage('mypage');
    setTimeout(function(){switchMyTab('orders');},150);
  },1200);
}

/* ══ ORDER HISTORY RENDER ══ */
function renderOrderHistory(){
  var container=document.getElementById('orderHistoryList');
  if(!container) return;
  if(!orderHistory.length){
    container.innerHTML='<div style="text-align:center;padding:40px 20px;color:var(--muted)">'
      +'<div style="font-size:2.5rem;margin-bottom:10px">📦</div>'
      +'<div style="font-family:Gaegu,cursive;font-size:1rem;color:var(--mid)">아직 주문 내역이 없어요</div>'
      +'<div style="font-size:.78rem;margin-top:6px;margin-bottom:16px">마켓에서 첫 주문을 해보세요!</div>'
      +'<button onclick="goPage(\'market\')" style="background:var(--green);color:#fff;border:none;border-radius:10px;padding:9px 20px;font-size:.85rem;cursor:pointer">마켓 가기</button>'
      +'</div>';
    return;
  }
  var html='';
  orderHistory.forEach(function(order){
    var itemsHtml='';
    order.items.forEach(function(it){
      itemsHtml+='<div style="display:flex;align-items:center;gap:12px;padding:11px 0;border-bottom:1px solid var(--sand)">'
        +'<div style="width:46px;height:46px;border-radius:11px;background:linear-gradient(145deg,var(--gp),var(--gl));display:flex;align-items:center;justify-content:center;font-size:1.5rem;flex-shrink:0">'+it.emoji+'</div>'
        +'<div style="flex:1">'
        +'<div style="font-size:.72rem;color:var(--green);font-weight:500;margin-bottom:1px">'+it.farm+'</div>'
        +'<div style="font-size:.83rem;font-weight:700;color:var(--dark)">'+it.name+'</div>'
        +'<div style="font-size:.78rem;font-weight:700;color:var(--amber)">'+it.up.toLocaleString()+'원 × '+it.qty+'</div>'
        +'</div>'
        +'</div>';
    });
    html+='<div style="background:#fff;border:1.5px solid var(--sand);border-radius:16px;padding:20px 22px;margin-bottom:14px">'
      +'<div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px;padding-bottom:11px;border-bottom:1px solid var(--sand)">'
      +'<div>'
      +'<div style="font-size:.8rem;font-weight:700;color:var(--dark)">주문번호 '+order.id+'</div>'
      +'<div style="font-size:.7rem;color:var(--muted);margin-top:2px">'+order.date+' · '+order.items.length+'개 상품 · '+order.total.toLocaleString()+'원</div>'
      +'</div>'
      +'<div style="font-size:.76rem;font-weight:700;padding:4px 12px;border-radius:20px;background:#f0fff4;color:var(--green);border:1px solid var(--gl)">'+order.status+'</div>'
      +'</div>'
      +itemsHtml
      +'<div style="display:flex;gap:8px;margin-top:14px">'
      +'<button onclick="showToast(\'배송 조회 기능 준비 중이에요 🚚\')" style="flex:1;padding:9px;border:1.5px solid var(--sand);border-radius:9px;background:#fff;font-size:.78rem;cursor:pointer;font-family:inherit;color:var(--mid)">배송 조회</button>'
      +'<button onclick="goPage(\'review\')" style="flex:1;padding:9px;border:1.5px solid var(--gl);border-radius:9px;background:var(--gp);font-size:.78rem;cursor:pointer;font-family:inherit;color:var(--green);font-weight:500">리뷰 작성</button>'
      +'</div>'
      +'</div>';
  });
  container.innerHTML=html;
}

/* ══ NOTIFICATION ══ */
function addNotification(title,msg,page){
  notifList.unshift({id:'n'+Date.now(),read:false,icon:'🔔',title:title,msg:msg,time:'방금',page:page});
  renderNotifBadge();
  renderNotifPopup();
}

function renderNotifBadge(){
  var unread=notifList.filter(function(n){return!n.read;}).length;
  var bell=document.getElementById('bellIcon');
  if(!bell) return;
  bell.style.position='relative';
  var dot=document.getElementById('notifDot');
  if(!dot){
    dot=document.createElement('span');
    dot.id='notifDot';
    dot.style.cssText='position:absolute;top:-4px;right:-5px;width:8px;height:8px;border-radius:50%;background:var(--amber);border:2px solid #fff';
    bell.appendChild(dot);
  }
  dot.style.display=unread?'block':'none';
}

function renderNotifPopup(){
  var listEl=document.getElementById('notifItemList');
  if(!listEl) return;
  if(!notifList.length){
    listEl.innerHTML='<div style="text-align:center;padding:24px;color:var(--muted);font-size:.8rem">알림이 없어요 🔔</div>';
    return;
  }
  var html='';
  notifList.forEach(function(n){
    html+='<div onclick="readNotif(\''+n.id+'\')" style="display:flex;align-items:flex-start;gap:10px;padding:12px 16px;border-bottom:1px solid var(--sand);cursor:pointer;background:'+(n.read?'#fff':'#f8fff4')+';transition:background .15s">'
      +'<div style="width:7px;height:7px;border-radius:50%;background:'+(n.read?'transparent':'var(--green)')+';flex-shrink:0;margin-top:5px"></div>'
      +'<div style="font-size:1.1rem;flex-shrink:0">'+n.icon+'</div>'
      +'<div style="flex:1">'
      +'<div style="font-size:.8rem;font-weight:500;color:var(--dark);margin-bottom:2px">'+n.title+'</div>'
      +'<div style="font-size:.72rem;color:var(--mid);line-height:1.5;margin-bottom:3px">'+n.msg+'</div>'
      +'<div style="font-size:.68rem;color:var(--muted)">'+n.time+'</div>'
      +'</div>'
      +'</div>';
  });
  listEl.innerHTML=html;
}

function readNotif(id){
  var target=null;
  notifList.forEach(function(n){if(n.id===id){n.read=true;target=n;}});
  renderNotifBadge();
  renderNotifPopup();
  if(target&&target.page){
    setTimeout(function(){
      var popup=document.getElementById('notifPopup');
      if(popup) popup.style.display='none';
      goPage(target.page);
    },200);
  }
}

function markAllRead(){
  notifList.forEach(function(n){n.read=true;});
  renderNotifBadge();
  renderNotifPopup();
  showToast('✅ 모든 알림을 읽었어요');
}

/* ══ SORT ══ */
function setSort(el){
  el.parentElement.querySelectorAll('div').forEach(function(d){
    d.style.color='var(--muted)';d.style.fontWeight='400';d.style.background='transparent';
  });
  el.style.color='var(--green)';el.style.fontWeight='700';el.style.background='var(--gp)';
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

function mktF(el){
  el.parentElement.querySelectorAll('.mf').forEach(function(x){x.classList.remove('on');});
  el.classList.add('on');
  var cats={'채소':89,'과일':74,'곡류':43,'버섯':28,'뿌리채소':36,'기한임박':32};
  var txt=el.textContent.replace(/[🥦🍎🌾🍄🥕⏰\s]/g,'').trim();
  var cnt=cats[txt]||302;
  var cntEl=document.querySelector('.pgrid-count');
  if(cntEl) cntEl.innerHTML='총 <strong style="color:var(--dark)">'+cnt+'</strong>개 상품';
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

/* ══ WISH / TOGGLE ══ */
function toggleW(el){
  var wished=el.textContent==='❤️';
  el.textContent=wished?'🤍':'❤️';
  showToast(wished?'찜 목록에서 제거됐어요':'❤️ 찜 목록에 추가됐어요!');
}
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

/* ══ CART PAGE QUANTITY ══ */
var detailQtyVal=1;
function changeDetailQty(d){
  detailQtyVal=Math.max(1,detailQtyVal+d);
  var el=document.getElementById('detailQty');
  if(el) el.textContent=detailQtyVal;
}

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

/* ══ LOGIN / SIGNUP ══ */
function swTab(t){
  document.getElementById('lf').style.display=t==='login'?'block':'none';
  document.getElementById('sf').style.display=t==='signup'?'block':'none';
  document.getElementById('sv').style.display='none';
  var tl=document.getElementById('tab-l');
  var ts=document.getElementById('tab-s');
  if(tl){tl.classList.toggle('on',t==='login');}
  if(ts){ts.classList.toggle('on',t==='signup');}
}
function doLogin(){
  var e=document.getElementById('le').value;
  var p=document.getElementById('lp').value;
  if(!e||!p){showToast('이메일과 비밀번호를 입력해주세요!');return;}
  document.getElementById('lf').style.display='none';
  document.getElementById('sv').style.display='block';
  setTimeout(function(){goPage('mypage');},1800);
  var btn=document.querySelector('.btn-nav');
  if(btn){btn.textContent='마이페이지';btn.setAttribute('onclick',"goPage('mypage')");}
}
function doSignup() {
  const data = {
    name: document.querySelector('#sf input[name="name"]').value,
    role: document.querySelector('#sf select[name="role"]').value,
    email: document.querySelector('#sf input[name="email"]').value,
    password: document.querySelector('#sf input[name="password"]').value,
    passwordConfirm: document.querySelector('#sf input[name="passwordConfirm"]').value,
    address: document.querySelector('#sf input[name="address"]').value
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
          setTimeout(function() { goPage('home'); }, 2000);
        } else {
          alert(result.error);
        }
      })
      .catch(err => alert('오류가 발생했습니다.'));
}

/* ══ MYPAGE TABS ══ */
function switchMyTab(name){
  var tabs=['orders','wishlist','coupon','myreview','profile'];
  var tabIds=['orders','wishlist','coupon','review','profile'];
  tabs.forEach(function(t,i){
    var c=document.getElementById('mycontent-'+t);
    var tab=document.getElementById('my-tab-'+tabIds[i]);
    if(c) c.style.display=(t===name?'block':'none');
    if(tab){
      if(t===name){
        tab.style.background='var(--gp)';
        tab.style.borderLeft='3px solid var(--green)';
      }else{
        tab.style.background='transparent';
        tab.style.borderLeft='3px solid transparent';
      }
    }
  });
}

/* ══ FARMER TABS ══ */
function switchFarmerTab(n){
  for(var i=0;i<4;i++){
    var c=document.getElementById('fcontent-'+i);
    var t=document.getElementById('ftab-'+i);
    if(c) c.style.display=(i===n?'block':'none');
    if(t){
      t.style.borderBottom=(i===n?'2px solid var(--dark)':'2px solid transparent');
      t.style.color=(i===n?'var(--dark)':'var(--muted)');
      t.style.fontWeight=(i===n?'500':'400');
    }
  }
}

/* ══ RANK TABS ══ */
function switchRankTab(n){
  for(var i=0;i<4;i++){
    var t=document.getElementById('rtab-'+i);
    if(t){
      t.style.borderBottom=(i===n?'2px solid var(--dark)':'2px solid transparent');
      t.style.color=(i===n?'var(--dark)':'var(--muted)');
      t.style.fontWeight=(i===n?'500':'400');
    }
    var c=document.getElementById('rcontent-'+i);
    if(c) c.style.display='none';
  }
  var rc=document.getElementById('rcontent-'+n);
  if(rc) rc.style.display='block';
}

/* ══ BOARD TABS ══ */
function switchBoardTab(i){
  for(var j=0;j<4;j++){
    var t=document.getElementById('btab-'+j);
    if(t){t.style.borderBottom='2px solid transparent';t.style.color='var(--muted)';t.style.fontWeight='400';}
    var c=document.getElementById('bcontent-'+j);
    if(c) c.style.display='none';
  }
  var el=document.getElementById('btab-'+i);
  if(el){el.style.borderBottom='2px solid var(--dark)';el.style.color='var(--dark)';el.style.fontWeight='500';}
  var ct=document.getElementById('bcontent-'+i);
  if(ct) ct.style.display='block';
}


/* ══ FAQ FILTER ══ */
function faqFilter(cat){
  // 태그 버튼 스타일
  var tags=document.querySelectorAll('.faq-tag');
  tags.forEach(function(t){
    var active=t.getAttribute('onclick').includes("'"+cat+"'");
    t.style.background=active?'var(--green)':'';
    t.style.color=active?'#fff':'var(--mid)';
    t.style.border=active?'1px solid var(--green)':'1.5px solid var(--sand)';
  });
  // 상단 탭 스타일
  var tops=document.querySelectorAll('.faq-top-tab');
  tops.forEach(function(t){
    var active=t.getAttribute('onclick').includes("'"+cat+"'");
    t.style.color=active?'var(--dark)':'var(--muted)';
    t.style.fontWeight=active?'500':'400';
    t.style.borderBottom=active?'2px solid var(--dark)':'2px solid transparent';
  });
  // FAQ 아이템 표시/숨김
  var items=document.querySelectorAll('.faq-item');
  items.forEach(function(item){
    var ic=item.getAttribute('data-cat');
    item.style.display=(cat==='all'||ic===cat)?'block':'none';
  });
}

/* ══ FAQ ══ */
function toggleFaq(el){
  var ans=el.parentElement.querySelector('.faq-a');
  var arr=el.querySelector('span');
  var isOpen=ans.style.display==='block';
  ans.style.display=isOpen?'none':'block';
  if(arr) arr.style.transform=isOpen?'rotate(0deg)':'rotate(180deg)';
}

/* ══ NOTIFICATIONS ══ */
function toggleNotif(){
  var p=document.getElementById('notifPopup');
  if(!p) return;
  var isOpen=p.style.display==='block';
  p.style.display=isOpen?'none':'block';
  if(!isOpen) renderNotifPopup();
}

/* ══ REC POPUP ══ */
function openRecPopup(){
  for(var j=0;j<4;j++){
    var t=document.getElementById('mkt-tab-'+j);
    if(t){t.style.borderBottom='2px solid transparent';t.style.color='var(--muted)';t.style.fontWeight='400';}
  }
  var t3=document.getElementById('mkt-tab-3');
  if(t3){t3.style.borderBottom='2px solid var(--green)';t3.style.color='var(--dark)';t3.style.fontWeight='500';}
  var p=document.getElementById('recPopup');
  if(p) p.style.display='flex';
}
function closeRecPopup(){
  var p=document.getElementById('recPopup');
  if(p) p.style.display='none';
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

/* ══ FARMER CHAT ══ */
function openFarmerChat(){
  var m=document.getElementById('farmerChatModal');
  if(m){
    m.style.display='flex';
    setTimeout(function(){var i=document.getElementById('farmerChatInput');if(i)i.focus();},100);
  }
}
function closeFarmerChat(){
  var m=document.getElementById('farmerChatModal');
  if(m) m.style.display='none';
}
function sendFarmerMsg(){
  var input=document.getElementById('farmerChatInput');
  var text=input?input.value.trim():'';
  if(!text) return;
  if(input) input.value='';
  var msgs=document.getElementById('farmerMsgs');
  if(!msgs) return;
  var now=new Date();
  var time=String(now.getHours()).padStart(2,'0')+':'+String(now.getMinutes()).padStart(2,'0');
  var d=document.createElement('div');
  d.style.cssText='display:flex;justify-content:flex-end;margin-bottom:10px';
  d.innerHTML='<div style="max-width:220px;padding:8px 12px;border-radius:12px 12px 3px 12px;font-size:.74rem;line-height:1.6;background:var(--green);color:#fff">'+text+'<div style="font-size:.6rem;opacity:.7;text-align:right;margin-top:2px">'+time+'</div></div>';
  msgs.appendChild(d);
  msgs.scrollTop=msgs.scrollHeight;
  setTimeout(function(){
    var replies=['네, 알겠습니다! 😊','확인해드릴게요~','감사합니다! 좋은 하루 되세요 🌿','픽업은 평일 오전 9시~오후 5시 가능해요!'];
    var r=replies[Math.floor(Math.random()*replies.length)];
    var d2=document.createElement('div');
    d2.style.cssText='display:flex;gap:6px;align-items:flex-end;margin-bottom:10px';
    d2.innerHTML='<div style="width:24px;height:24px;border-radius:50%;background:var(--gp);display:flex;align-items:center;justify-content:center;font-size:.76rem;flex-shrink:0">🧑‍🌾</div><div style="max-width:220px;padding:8px 12px;border-radius:12px 12px 12px 3px;font-size:.74rem;line-height:1.6;background:var(--beige2)">'+r+'</div>';
    msgs.appendChild(d2);
    msgs.scrollTop=msgs.scrollHeight;
  },800);
}

/* ══ CUSTOMER CHAT ══ */
function toggleChat(){
  var w=document.getElementById('chatWin');
  if(!w) return;
  w.style.display=w.style.display==='flex'?'none':'flex';
  if(w.style.display==='flex'){
    var d=document.getElementById('fab-dot');
    if(d) d.style.display='none';
  }
}
function quickM(btn){
  var t=btn.textContent;
  appendMsg(t,'user');
  setTimeout(function(){appendMsg(bReplies[t]||'고객센터로 문의해주세요 😊','bot');},600);
}
function sendChat(){
  var i=document.getElementById('ci-in');
  var t=i?i.value.trim():'';
  if(!t) return;
  appendMsg(t,'user');
  if(i) i.value='';
  setTimeout(function(){appendMsg(bReplies[t]||'궁금하신 점을 고객센터로 연락해주세요! 😊','bot');},600);
}
function appendMsg(t,r){
  var m=document.getElementById('chatMsgs');
  if(!m) return;
  var d=document.createElement('div');
  d.style.cssText='display:flex;gap:6px;align-items:flex-end;'+(r==='user'?'flex-direction:row-reverse':'');
  var bubble='';
  if(r==='bot') bubble='<div style="width:24px;height:24px;border-radius:50%;background:var(--gp);display:flex;align-items:center;justify-content:center;font-size:.76rem;flex-shrink:0">🤖</div>';
  bubble+='<div style="max-width:220px;padding:8px 12px;border-radius:'+(r==='bot'?'12px 12px 12px 3px':'12px 12px 3px 12px')+';font-size:.74rem;line-height:1.6;background:'+(r==='bot'?'var(--warm)':'var(--green)')+';color:'+(r==='bot'?'inherit':'#fff')+'">'+t+'</div>';
  d.innerHTML=bubble;
  m.appendChild(d);
  m.scrollTop=m.scrollHeight;
}

/* ══ SCROLL ══ */
window.addEventListener('scroll',function(){
  var n=document.getElementById('nav');
  if(n) n.classList.toggle('scrolled', scrollY>10);

  var nav=document.querySelector('nav');
  if(nav) nav.style.boxShadow=window.scrollY>10?'0 2px 20px rgba(42,30,6,.1)':'none';
});

/* ══ 초기화 ══ */
window.addEventListener('DOMContentLoaded',function(){
  /* REVEAL 스크롤 애니메이션 */
  var obs=new IntersectionObserver(function(es){
    es.forEach(function(e){
      if(e.isIntersecting){e.target.classList.add('on');obs.unobserve(e.target);}
    });
  },{threshold:.08});
  document.querySelectorAll('.rv').forEach(function(el){obs.observe(el);});

  renderCart();
  renderNotifBadge();
  renderNotifPopup();
  renderOrderHistory();
  document.addEventListener('click',function(e){
    var popup=document.getElementById('notifPopup');
    var bell=document.getElementById('bellIcon');
    if(popup&&popup.style.display==='block'&&bell&&!popup.contains(e.target)&&!bell.contains(e.target)){
      popup.style.display='none';
    }
  });
});
