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

    grid.innerHTML = items.map(function(item, index) {
        var detailUrl = '/market/detail?id=' + encodeURIComponent(item.productId);
        var thumb = item.thumbnailUrl
            ? '<img src="' + escapeHtml(item.thumbnailUrl) + '" alt="상품 이미지" style="width:100%;height:100%;object-fit:cover;position:absolute;top:0;left:0;border-radius:inherit"/>'
            : '<span>' + getRecommendationEmoji(item.productCategory) + '</span>';

        var reason = item.reason || '구매 이력을 바탕으로 추천된 상품이에요.';

        return ''
            + '<div class="pc2" onclick="location.href=\'' + detailUrl + '\'" style="cursor:pointer;display:flex;flex-direction:column;min-height:420px">'
            + '  <div class="pc2-thumb gn" style="position:relative;overflow:hidden;height:180px;flex-shrink:0">'
            + '    <div class="pbdg"><span class="pbdg-t pbdg-new">AI픽 #' + escapeHtml(index + 1) + '</span></div>'
            + '    <div class="plk" onclick="event.stopPropagation();toggleW(this)">🤍</div>'
            +      thumb
            + '  </div>'
            + '  <div class="pc2-body" style="display:flex;flex-direction:column;flex:1">'
            + '    <div class="pc2-farm">' + escapeHtml(item.productOrigin || item.origin || item.productCategory || '원산지 정보 없음') + '</div>'
            + '    <div style="display:flex;align-items:center;gap:6px;min-width:0">'
            + '      <div class="pc2-nm" style="flex:1;min-width:0;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">' + escapeHtml(item.productName || '상품명 없음') + '</div>'
            +        renderRecommendationStockBadge(item)
            + '    </div>'
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

function getRecommendationStock(item) {
    var stock = item.stockQuantity;
    if (stock == null) stock = item.productStockQuantity;
    if (stock == null) stock = item.productStock;
    if (stock == null) stock = item.stock;

    if (stock == null || stock === '') {
        return null;
    }

    if (!isNaN(Number(stock))) {
        return Number(stock);
    }

    return stock;
}

function renderRecommendationStockBadge(item) {
    var stock = getRecommendationStock(item);
    if (stock == null) {
        return '';
    }

    return '<span style="font-size:.68rem;color:var(--mid);background:#fff;border:1px solid var(--sand);border-radius:999px;padding:2px 7px;white-space:nowrap;flex-shrink:0">재고 ' + escapeHtml(stock) + '</span>';
}

function renderPopularRecommendations(items) {
    var list = document.getElementById('popularRecommendationList');
    if (!list) return;

    if (!items || items.length === 0) {
        list.innerHTML = '<div style="grid-column:1/-1;text-align:center;color:var(--muted);font-size:.82rem;padding:26px 0">인기상품이 없습니다.</div>';
        return;
    }

    var visibleItems = items.slice(0, 12);

    list.innerHTML = visibleItems.map(function(item, index) {
        var detailUrl = '/market/detail?id=' + encodeURIComponent(item.productId);
        var thumb = item.thumbnailUrl
            ? '<img src="' + escapeHtml(item.thumbnailUrl) + '" alt="상품 이미지" style="width:100%;height:100%;object-fit:cover;position:absolute;top:0;left:0;border-radius:inherit"/>'
            : '<span>' + getRecommendationEmoji(item.productCategory) + '</span>';

        return ''
            + '<div class="pc2" onclick="location.href=\'' + detailUrl + '\'" style="cursor:pointer;display:flex;flex-direction:column;min-height:380px">'
            + '  <div class="pc2-thumb gn" style="position:relative;overflow:hidden;height:180px;flex-shrink:0">'
            + '    <div class="pbdg"><span class="pbdg-t pbdg-new">인기 #' + escapeHtml(item.rankOrder || index + 1) + '</span></div>'
            + '    <div class="plk" onclick="event.stopPropagation();toggleW(this)">🤍</div>'
            +      thumb
            + '  </div>'
            + '  <div class="pc2-body" style="display:flex;flex-direction:column;flex:1">'
            + '    <div class="pc2-farm">' + escapeHtml(item.productOrigin || item.origin || item.productCategory || '원산지 정보 없음') + '</div>'
            + '    <div style="display:flex;align-items:center;gap:6px;min-width:0">'
            + '      <div class="pc2-nm" style="flex:1;min-width:0;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">' + escapeHtml(item.productName || '상품명 없음') + '</div>'
            +        renderRecommendationStockBadge(item)
            + '    </div>'
            +      renderRecommendationPriceHtml(item)
            + '    <button class="btn-add" onclick="event.stopPropagation();location.href=\'' + detailUrl + '\'">🔍 상세보기</button>'
            + '  </div>'
            + '</div>';
    }).join('');
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
    list.innerHTML = '<div style="grid-column:1/-1;text-align:center;color:var(--muted);font-size:.82rem;padding:26px 0">인기상품을 불러오는 중...</div>';

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
            list.innerHTML = '<div style="grid-column:1/-1;text-align:center;color:var(--muted);font-size:.82rem;padding:26px 0">인기상품을 불러오지 못했습니다.</div>';
            console.error(error);
        });
}


function openAllProductsTab() {
    var url = new URL(window.location.href);
    var category = url.searchParams.get('category');

    if (category && category !== '' && category !== '전체') {
        url.searchParams.delete('category');
        url.searchParams.set('page', '0');
        window.location.href = url.toString();
        return;
    }

    if (category === '전체') {
        url.searchParams.delete('category');
        url.searchParams.set('page', '0');
        window.location.href = url.toString();
        return;
    }

    switchMktTab(0);
}

// ===== 탭 전환 =====
function switchMktTab(index) {
    var categoryBar = document.getElementById('mktCategoryBar');
    if (categoryBar) {
        categoryBar.style.display = index === 0 ? 'flex' : 'none';
    }

    for (var i = 0; i <= 2; i++) {
        var tab = document.getElementById('mkt-tab-' + i);
        var content = document.getElementById('mkt-content-' + i);

        if (!tab || !content) continue;

        if (i === index) {
            tab.style.borderBottom = '2px solid var(--green)';
            tab.style.fontWeight = '500';
            tab.style.color = 'var(--dark)';
            content.style.display = 'block';
        } else {
            tab.style.borderBottom = 'none';
            tab.style.fontWeight = '400';
            tab.style.color = 'var(--muted)';
            content.style.display = 'none';
        }
    }

    if (index === 1 && typeof loadAiRecommendations === 'function') {
        loadAiRecommendations();
    }

    if (index === 2 && typeof loadPopularRecommendations === 'function') {
        loadPopularRecommendations();
    }
}

(function() {
    var productContent = document.getElementById('mkt-content-0');
    var aiContent = document.getElementById('mkt-content-1');
    var popularContent = document.getElementById('mkt-content-2');
    var categoryBar = document.getElementById('mktCategoryBar');

    if (productContent) productContent.style.display = 'block';
    if (aiContent) aiContent.style.display = 'none';
    if (popularContent) popularContent.style.display = 'none';
    if (categoryBar) categoryBar.style.display = 'flex';
})();

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
