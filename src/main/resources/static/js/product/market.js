// ===== 판매자 버튼 표시 =====
(function() {
    var role = localStorage.getItem('role') || sessionStorage.getItem('role');
    if (role === 'SELLER') {
        var registerBtn = document.getElementById('registerBtn');
        var myProductsBtn = document.getElementById('myProductsBtn');
        if (registerBtn) registerBtn.style.display = 'inline-block';
        if (myProductsBtn) myProductsBtn.style.display = 'inline-block';
    }

    var url = new URL(window.location.href);
    var sellerEmailParam = url.searchParams.get('sellerEmail');
    var btn = document.getElementById('myProductsBtn');
    if (sellerEmailParam && btn) {
        btn.style.background = 'var(--green)';
        btn.style.color = '#fff';
        btn.textContent = '내 상품 ✓';
    }
})();

function toggleMyProducts() {
    var token = localStorage.getItem('token') || sessionStorage.getItem('token');
    var url = new URL(window.location.href);

    if (url.searchParams.get('sellerEmail')) {
        url.searchParams.delete('sellerEmail');
    } else {
        if (!token) { alert('로그인 후 이용해주세요'); return; }
        var payload = JSON.parse(atob(token.split('.')[1]));
        var myEmail = payload.sub;
        url.searchParams.set('sellerEmail', myEmail);
    }
    url.searchParams.set('page', '0');
    window.location.href = url.toString();
}

// ===== 추천 API 공통 =====
var aiRecommendationsLoaded = false;
var popularRecommendationsLoaded = false;

function getRecommendationToken() {
    return localStorage.getItem('token') || sessionStorage.getItem('token') || '';
}

function getLoginUserId() {
    var storedUserId = localStorage.getItem('userId')
        || sessionStorage.getItem('userId')
        || localStorage.getItem('id')
        || sessionStorage.getItem('id');

    if (storedUserId && !isNaN(Number(storedUserId))) {
        return Number(storedUserId);
    }

    var token = getRecommendationToken();
    if (!token || token.split('.').length < 2) {
        return null;
    }

    try {
        var payload = JSON.parse(atob(token.split('.')[1]));
        var tokenUserId = payload.userId || payload.id || payload.memberId || payload.user_id;
        if (tokenUserId && !isNaN(Number(tokenUserId))) {
            return Number(tokenUserId);
        }
    } catch (e) {
        return null;
    }

    return null;
}

function getOrCreateRecommendationSessionId() {
    var sessionId = localStorage.getItem('recommendationSessionId')
        || sessionStorage.getItem('recommendationSessionId');

    if (!sessionId) {
        sessionId = 'mkt-' + Date.now() + '-' + Math.random().toString(36).substring(2, 10);
        localStorage.setItem('recommendationSessionId', sessionId);
    }

    return sessionId;
}

function buildRecommendationTarget() {
    var userId = getLoginUserId();
    if (userId) {
        return {
            query: '?userId=' + encodeURIComponent(userId),
            body: { userId: userId, sessionId: null }
        };
    }

    var sessionId = getOrCreateRecommendationSessionId();
    return {
        query: '?sessionId=' + encodeURIComponent(sessionId),
        body: { userId: null, sessionId: sessionId }
    };
}

function fetchRecommendationJson(url, options) {
    options = options || {};
    options.headers = options.headers || {};
    options.headers['Accept'] = 'application/json';

    var token = getRecommendationToken();
    if (token) {
        options.headers['Authorization'] = 'Bearer ' + token;
    }

    return fetch(url, options).then(function(response) {
        if (!response.ok) {
            throw new Error('추천 API 호출 실패: ' + response.status);
        }
        return response.json();
    });
}

function escapeHtml(value) {
    return String(value == null ? '' : value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function getRecommendationEmoji(category) {
    if (category === '과일') return '🍎';
    if (category === '곡류') return '🌾';
    if (category === '버섯') return '🍄';
    if (category === '뿌리채소') return '🥕';
    if (category === '채소') return '🥦';
    return '🌿';
}

function renderAiRecommendations(items) {
    var grid = document.getElementById('aiRecommendationGrid');
    if (!grid) return;

    if (!items || items.length === 0) {
        grid.innerHTML = '<div style="grid-column:1/-1;text-align:center;color:var(--muted);font-size:.82rem;padding:26px 0">아직 추천 상품이 없습니다.</div>';
        return;
    }

    grid.innerHTML = items.map(function(item) {
        var detailUrl = '/market/detail?id=' + encodeURIComponent(item.productId);
        var thumb = item.thumbnailUrl
            ? '<img src="' + escapeHtml(item.thumbnailUrl) + '" alt="상품 이미지" style="width:100%;height:100%;object-fit:cover;position:absolute;top:0;left:0;border-radius:inherit"/>'
            : '<span>' + getRecommendationEmoji(item.productCategory) + '</span>';

        var reason = item.reason || '구매 이력을 바탕으로 추천된 상품이에요.';

        return ''
            + '<div class="pc2" onclick="location.href=\'' + detailUrl + '\'" style="cursor:pointer;display:flex;flex-direction:column;min-height:420px">'
            + '  <div class="pc2-thumb gn" style="position:relative;overflow:hidden;height:180px;flex-shrink:0">'
            + '    <div class="pbdg"><span class="pbdg-t pbdg-new">AI픽</span></div>'
            + '    <div class="plk" onclick="event.stopPropagation();toggleW(this)">🤍</div>'
            +      thumb
            + '  </div>'
            + '  <div class="pc2-body" style="display:flex;flex-direction:column;flex:1">'
            + '    <div class="pc2-farm">' + escapeHtml(item.productCategory || '추천 상품') + '</div>'
            + '    <div class="pc2-nm">' + escapeHtml(item.productName || '상품명 없음') + '</div>'
            +      renderRecommendationPriceHtml(item)
            + '    <button class="btn-add" onclick="event.stopPropagation();location.href=\'' + detailUrl + '\'">🔍 상세보기</button>'
            + '    <div onclick="event.stopPropagation()" style="margin-top:12px;background:#fff;border-top:1px dashed var(--sand);padding-top:10px;font-size:.74rem;color:var(--mid);line-height:1.55">'
            + '      <div style="font-weight:700;color:var(--green);font-family:Gaegu,cursive;font-size:.88rem;margin-bottom:3px">🤖 이 상품을 추천한 이유</div>'
            + '      <div>' + escapeHtml(reason) + '</div>'
            + '    </div>'
            + '  </div>'
            + '</div>';
    }).join('');
}

function renderRecommendationPriceHtml(item) {
    var price = Number(item.productDiscountPrice || item.discountPrice || item.discountedPrice || item.productPrice || item.price || 0);
    var originalPrice = Number(item.productPrice || item.originalPrice || item.price || 0);
    var hasDiscount = originalPrice > 0 && price > 0 && originalPrice > price;

    if (!price) {
        return '<div class="pc2-pr" style="margin-top:7px"><span class="pc2-price">가격 확인</span></div>';
    }

    var discountRate = hasDiscount ? Math.round((1 - price / originalPrice) * 100) : 0;

    return ''
        + '<div class="pc2-pr" style="margin-top:7px">'
        + (hasDiscount ? '<span class="pc2-disc">-' + discountRate + '%</span>' : '')
        + '<span class="pc2-price">' + formatRecommendationPrice(price) + '원</span>'
        + (hasDiscount ? '<span class="pc2-og">' + formatRecommendationPrice(originalPrice) + '원</span>' : '')
        + '</div>';
}

function formatRecommendationPrice(value) {
    return Number(value || 0).toLocaleString('ko-KR');
}

function renderPopularRecommendations(items) {
    var list = document.getElementById('popularRecommendationList');
    if (!list) return;

    if (!items || items.length === 0) {
        list.innerHTML = '<div style="font-size:.78rem;color:var(--muted);padding:14px 0;text-align:center">인기상품이 없습니다.</div>';
        return;
    }

    var visibleItems = items.slice(0, 6);

    list.innerHTML = visibleItems.map(function(item, index) {
        var detailUrl = '/market/detail?id=' + encodeURIComponent(item.productId);
        var image = item.thumbnailUrl
            ? '<img src="' + escapeHtml(item.thumbnailUrl) + '" alt="상품 이미지" style="width:100%;height:100%;object-fit:cover;border-radius:10px"/>'
            : getRecommendationEmoji(item.productCategory);
        var border = index === visibleItems.length - 1 ? '' : 'border-bottom:1px solid var(--sand);';

        return ''
            + '<div onclick="location.href=\'' + detailUrl + '\'" style="display:flex;gap:12px;padding:12px 0;' + border + 'cursor:pointer;align-items:center">'
            + '  <div style="width:54px;height:54px;border-radius:11px;background:linear-gradient(145deg,var(--gp),var(--gl));display:flex;align-items:center;justify-content:center;font-size:1.35rem;flex-shrink:0;overflow:hidden">' + image + '</div>'
            + '  <div style="flex:1;min-width:0">'
            + '    <div style="font-size:.72rem;color:var(--green);font-weight:700;margin-bottom:2px">인기상품 추천</div>'
            + '    <div style="font-size:.86rem;font-weight:700;color:var(--dark);white-space:nowrap;overflow:hidden;text-overflow:ellipsis">' + escapeHtml(item.productName || '상품명 없음') + '</div>'
            +      renderPopularRecommendationPriceHtml(item)
            + '  </div>'
            + '  <div style="font-size:.82rem;color:var(--amber);font-weight:800;flex-shrink:0">#' + escapeHtml(item.rankOrder || index + 1) + '</div>'
            + '</div>';
    }).join('');
}

function renderPopularRecommendationPriceHtml(item) {
    var price = Number(item.productDiscountPrice || item.discountPrice || item.discountedPrice || item.productPrice || item.price || 0);
    var originalPrice = Number(item.productPrice || item.originalPrice || item.price || 0);
    var hasDiscount = originalPrice > 0 && price > 0 && originalPrice > price;

    if (!price) {
        return '<div style="font-size:.78rem;color:var(--amber);font-weight:800;margin-top:4px">가격 확인</div>';
    }

    var discountRate = hasDiscount ? Math.round((1 - price / originalPrice) * 100) : 0;

    return ''
        + '<div style="display:flex;align-items:baseline;gap:6px;margin-top:4px;flex-wrap:wrap">'
        + (hasDiscount ? '<span style="font-size:.72rem;color:var(--amber);font-weight:900">-' + discountRate + '%</span>' : '')
        + '<span style="font-size:.84rem;color:var(--amber);font-weight:900">' + formatRecommendationPrice(price) + '원</span>'
        + (hasDiscount ? '<span style="font-size:.68rem;color:var(--muted);text-decoration:line-through">' + formatRecommendationPrice(originalPrice) + '원</span>' : '')
        + '</div>';
}

function loadAiRecommendations() {
    var grid = document.getElementById('aiRecommendationGrid');
    if (!grid) return;

    if (aiRecommendationsLoaded) return;
    aiRecommendationsLoaded = true;
    grid.innerHTML = '<div style="grid-column:1/-1;text-align:center;color:var(--muted);font-size:.82rem;padding:26px 0">AI 추천 상품을 불러오는 중...</div>';

    var target = buildRecommendationTarget();

    fetchRecommendationJson('/api/ai/recommendations/ai' + target.query)
        .then(function(items) {
            if (items && items.length > 0) return items;

            return fetchRecommendationJson('/api/ai/recommendations/generate/ai', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(target.body)
            });
        })
        .then(renderAiRecommendations)
        .catch(function(error) {
            aiRecommendationsLoaded = false;
            grid.innerHTML = '<div style="grid-column:1/-1;text-align:center;color:var(--muted);font-size:.82rem;padding:26px 0">AI 추천을 불러오지 못했습니다.</div>';
            console.error(error);
        });
}

function loadPopularRecommendations() {
    var list = document.getElementById('popularRecommendationList');
    if (!list) return;

    if (popularRecommendationsLoaded) return;
    popularRecommendationsLoaded = true;
    list.innerHTML = '<div style="font-size:.74rem;color:var(--muted);padding:12px 0;text-align:center">인기상품을 불러오는 중...</div>';

    fetchRecommendationJson('/api/ai/recommendations/popular')
        .then(function(items) {
            if (items && items.length > 0) return items;

            return fetchRecommendationJson('/api/ai/recommendations/generate/popular', {
                method: 'POST'
            });
        })
        .then(renderPopularRecommendations)
        .catch(function(error) {
            popularRecommendationsLoaded = false;
            list.innerHTML = '<div style="font-size:.74rem;color:var(--muted);padding:12px 0;text-align:center">인기상품을 불러오지 못했습니다.</div>';
            console.error(error);
        });
}

// ===== 탭 전환 =====
function switchMktTab(n) {
    for (var i = 0; i < 2; i++) {
        var t = document.getElementById('mkt-tab-' + i);
        var c = document.getElementById('mkt-content-' + i);
        if (t) { t.style.borderBottom = 'none'; t.style.color = 'var(--muted)'; t.style.fontWeight = '400'; }
        if (c) c.style.display = 'none';
    }
    var el = document.getElementById('mkt-tab-' + n);
    var ct = document.getElementById('mkt-content-' + n);
    if (el) { el.style.borderBottom = '2px solid var(--green)'; el.style.color = 'var(--dark)'; el.style.fontWeight = '500'; }
    if (ct) ct.style.display = 'block';

    if (n === 1) {
        loadAiRecommendations();
    }
}

// ===== 정렬 =====
function setSort(type) {
    ['popular','discount','price','newest'].forEach(function(t) {
        var btn = document.getElementById('sort-btn-' + t);
        if (btn) { btn.style.fontWeight = '400'; btn.style.color = 'var(--muted)'; btn.style.background = ''; }
    });

    var active = document.getElementById('sort-btn-' + type);
    if (active) { active.style.fontWeight = '700'; active.style.color = 'var(--green)'; active.style.background = 'var(--gp)'; }

    var url = new URL(window.location.href);
    url.searchParams.set('sort', type);
    url.searchParams.set('page', '0');
    window.location.href = url.toString();
}

// 페이지 로드 시 현재 sort 파라미터로 버튼 활성화
(function() {
    var url = new URL(window.location.href);
    var sort = url.searchParams.get('sort') || 'popular';
    ['popular','discount','price','newest'].forEach(function(t) {
        var b = document.getElementById('sort-btn-' + t);
        if (!b) return;
        if (t === sort) {
            b.style.fontWeight = '700';
            b.style.color = 'var(--green)';
            b.style.background = 'var(--gp)';
        } else {
            b.style.fontWeight = '400';
            b.style.color = 'var(--muted)';
            b.style.background = '';
        }
    });
})();

// ===== 인기상품추천 팝업 =====
function openRecPopup() {
    var popup = document.getElementById('recPopup');
    if (!popup) return;

    popup.style.display = 'block';
    loadPopularRecommendations();
}

function closeRecPopup() {
    var popup = document.getElementById('recPopup');
    if (popup) popup.style.display = 'none';
}

(function initRecPopupDrag() {
    var popup = document.getElementById('recPopup');
    var header = document.getElementById('recPopupHeader');

    if (!popup || !header) {
        return;
    }

    var dragging = false;
    var offsetX = 0;
    var offsetY = 0;

    header.addEventListener('mousedown', function(event) {
        if (event.target && event.target.classList && event.target.classList.contains('rec-popup-close')) {
            return;
        }

        dragging = true;

        var rect = popup.getBoundingClientRect();
        offsetX = event.clientX - rect.left;
        offsetY = event.clientY - rect.top;

        popup.style.left = rect.left + 'px';
        popup.style.top = rect.top + 'px';
        popup.style.right = 'auto';
        popup.style.bottom = 'auto';

        document.body.style.userSelect = 'none';
    });

    document.addEventListener('mousemove', function(event) {
        if (!dragging) {
            return;
        }

        var maxLeft = window.innerWidth - popup.offsetWidth;
        var maxTop = window.innerHeight - popup.offsetHeight;

        var nextLeft = Math.min(Math.max(event.clientX - offsetX, 8), Math.max(maxLeft - 8, 8));
        var nextTop = Math.min(Math.max(event.clientY - offsetY, 8), Math.max(maxTop - 8, 8));

        popup.style.left = nextLeft + 'px';
        popup.style.top = nextTop + 'px';
    });

    document.addEventListener('mouseup', function() {
        dragging = false;
        document.body.style.userSelect = '';
    });
})();
