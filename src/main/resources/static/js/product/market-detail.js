// ── 채팅 상태 변수 ──────────────────────────────────
var stompClient   = null;
var currentRoomId = null;
var isMySellProduct = false;

// DOMContentLoaded: 리뷰 + 관련 상품 로딩
document.addEventListener('DOMContentLoaded', function() {
    initReviewPage();
    loadRelatedProducts(productId);
});

// 기존 switchDetailTab 유지, 리뷰 탭/픽업 탭 추가 동작만 오버라이드
(function() {
    var originalSwitchDetailTab = window.switchDetailTab;

    window.switchDetailTab = function(tab) {
        if (typeof originalSwitchDetailTab === 'function') {
            originalSwitchDetailTab(tab);
        } else {
            ['info', 'review', 'pickup'].forEach(function(name) {
                var tabButton = document.getElementById('dtab-' + name);
                var tabContent = document.getElementById('detail-tab-' + name);
                if (tabButton) {
                    tabButton.style.color = 'var(--muted)';
                    tabButton.style.fontWeight = '400';
                    tabButton.style.borderBottomColor = 'transparent';
                }
                if (tabContent) {
                    tabContent.style.display = 'none';
                }
            });

            var activeButton = document.getElementById('dtab-' + tab);
            var activeContent = document.getElementById('detail-tab-' + tab);
            if (activeButton) {
                activeButton.style.color = 'var(--dark)';
                activeButton.style.fontWeight = '500';
                activeButton.style.borderBottomColor = 'var(--dark)';
            }
            if (activeContent) {
                activeContent.style.display = 'block';
            }
        }

        if (tab === 'review') {
            initReviewPage();
        }

        var mapInitialized = false;
        if (tab === 'pickup') {
            setTimeout(function() {
                if (mapInitialized) return;
                fetch(`/api/products/${productId}`)
                    .then(res => res.json())
                    .then(data => {
                        const lat = data.latitude;
                        const lng = data.longitude;
                        if (!lat || !lng) return;
                        var mapContainer = document.getElementById('map');
                        var mapOption = { center: new kakao.maps.LatLng(lat, lng), level: 3 };
                        var map = new kakao.maps.Map(mapContainer, mapOption);
                        var marker = new kakao.maps.Marker({ position: new kakao.maps.LatLng(lat, lng) });
                        marker.setMap(map);
                        mapInitialized = true;
                    });
            }, 100);
        }
    };
})();



document.addEventListener('DOMContentLoaded', function() {
    var params = new URLSearchParams(location.search);
    var chatRoomId = params.get('chatRoomId');
    if (chatRoomId) {
        setTimeout(function() {
            openChatRoomById(Number(chatRoomId));
        }, 500);
    }
});

document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    if (!token) return;

    const payload = JSON.parse(atob(token.split('.')[1]));
    const myEmail = payload.sub;
    const role = localStorage.getItem('role') || sessionStorage.getItem('role');

    initChatBtn(myEmail, role);

    if (myEmail === sellerEmail) {
        const deleteBtn = document.getElementById('deleteProductBtn');
        const editBtn = document.getElementById('editProductBtn');
        if (deleteBtn) deleteBtn.style.display = 'flex';
        if (editBtn) editBtn.style.display = 'flex';
    }

    if (role === 'SELLER') {
        const cartBtn = document.getElementById('addToCartBtn');
        const buyBtn = document.getElementById('buyNowBtn');
        const wishBtn = document.getElementById('wishBtn2');
        const wishImgBtn = document.getElementById('detailWishBtn');

        if (cartBtn) { cartBtn.disabled = true; cartBtn.style.opacity = '0.4'; cartBtn.style.cursor = 'not-allowed'; }
        if (buyBtn) { buyBtn.disabled = true; buyBtn.style.opacity = '0.4'; buyBtn.style.cursor = 'not-allowed'; }
        if (wishBtn) { wishBtn.disabled = true; wishBtn.style.opacity = '0.4'; wishBtn.style.cursor = 'not-allowed'; wishBtn.textContent = '판매자는 찜 할 수 없습니다'; }
        if (wishImgBtn) { wishImgBtn.disabled = true; wishImgBtn.style.opacity = '0.4'; wishImgBtn.style.cursor = 'not-allowed'; wishImgBtn.title = '판매자는 찜 할 수 없습니다'; }
    }
});

document.getElementById('deleteProductBtn').addEventListener('click', function() {
    const id = this.dataset.productId;
    deleteProduct(id);
});

document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') closeReviewDetail();
});

// ── 상품 삭제 / 장바구니 / 바로구매 ─────────────────────
function deleteProduct(id) {
    if (!confirm('정말 삭제하시겠어요?')) return;
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    fetch('/market/' + id, {
        method: 'DELETE',
        headers: { 'Authorization': 'Bearer ' + token }
    }).then(res => {
        if (res.ok) {
            showToast('상품이 삭제됐어요');
            setTimeout(() => location.href = '/market', 1000);
        } else {
            showToast('삭제 실패했어요');
        }
    }).catch(() => showToast('오류가 발생했어요'));
}

function addToCartDetail(btn) {
    const token = sessionStorage.getItem('token') || localStorage.getItem('token');
    if (!token) {
        if (confirm('로그인이 필요해요 🌿\n로그인 페이지로 이동할까요?')) location.href = '/login';
        return;
    }
    var pid = btn.getAttribute('data-id');
    fetch('/cart/items', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
        body: JSON.stringify({ productId: parseInt(pid), quantity: detailQtyVal })
    })
        .then(res => {
            if (res.status === 401) {
                if (confirm('로그인이 필요해요 🌿\n로그인 페이지로 이동할까요?')) location.href = '/login';
            } else if (res.ok) {
                showToast('🛒 장바구니에 담겼어요!');
                updateCartBadge();
            } else {
                showToast('담기 실패했어요 ㅠㅠ');
            }
        })
        .catch(() => showToast('오류가 발생했어요 ㅠㅠ'));
}

function buyNow(pid) {
    const token = sessionStorage.getItem('token') || localStorage.getItem('token');
    if (!token) {
        if (confirm('로그인이 필요해요 🌿\n로그인 페이지로 이동할까요?')) location.href = '/login';
        return;
    }
    fetch('/cart/items', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
        body: JSON.stringify({ productId: parseInt(pid), quantity: detailQtyVal })
    })
        .then(res => {
            if (res.status === 401) {
                if (confirm('로그인이 필요해요 🌿\n로그인 페이지로 이동할까요?')) location.href = '/login';
            } else if (res.ok) {
                location.href = '/cart';
            } else {
                showToast('오류가 발생했어요');
            }
        })
        .catch(() => showToast('오류가 발생했어요'));
}

// ── 관련 상품 추천 ────────────────────────────────────
async function loadRelatedProducts(productId) {
    var list = document.getElementById('relatedProductList');
    if (!list) return;
    list.innerHTML = '<div style="font-size:.78rem;color:var(--muted);padding:12px 0">관련 상품을 불러오는 중입니다.</div>';
    try {
        var response = await fetch('/api/products/' + productId + '/related-recommendations');
        var data = await response.json();
        if (!response.ok || !Array.isArray(data)) {
            list.innerHTML = '<div style="font-size:.78rem;color:var(--muted);padding:12px 0">관련 상품을 불러오지 못했습니다.</div>';
            return;
        }
        if (data.length === 0) {
            list.innerHTML = '<div style="font-size:.78rem;color:var(--muted);padding:12px 0">추천할 관련 상품이 아직 없습니다.</div>';
            return;
        }
        list.innerHTML = data.map(function(item, index) {
            return renderRelatedProductItem(item, index === data.length - 1);
        }).join('');
    } catch (error) {
        list.innerHTML = '<div style="font-size:.78rem;color:var(--muted);padding:12px 0">관련 상품 API에 연결할 수 없습니다.</div>';
    }
}

function renderRelatedProductItem(item, isLast) {
    var relatedProductId = item.productId || item.id;
    var detailUrl = '/market/detail?id=' + relatedProductId;
    var name = item.productName || item.name || '상품명 없음';
    var category = item.productCategory || item.category || '상품';
    var origin = item.productOrigin || item.origin || '';
    var reason = item.reason || '관련 상품이에요.';
    var thumbnailUrl = normalizeRelatedImageUrl(item.thumbnailUrl);
    var price = Number(item.productDiscountPrice || item.discountPrice || item.productPrice || item.price || 0);
    var originalPrice = Number(item.productPrice || item.price || 0);
    var hasDiscount = originalPrice > 0 && price > 0 && originalPrice > price;
    var borderStyle = isLast ? '' : 'border-bottom:1px solid var(--sand);';

    var fallbackThumb =
        "<div style=\"display:" + (thumbnailUrl ? "none" : "flex") + ";width:48px;height:48px;border-radius:9px;background:linear-gradient(145deg,var(--gp),var(--gl));align-items:center;justify-content:center;font-size:1.5rem;flex-shrink:0\">"
        + getRelatedCategoryEmoji(category)
        + "</div>";

    var thumbHtml = thumbnailUrl
        ? "<img src=\"" + escapeHtml(thumbnailUrl) + "\" alt=\"상품 이미지\" onerror=\"this.style.display='none';this.nextElementSibling.style.display='flex';\" style=\"width:48px;height:48px;border-radius:9px;object-fit:cover;flex-shrink:0\">"
        + fallbackThumb
        : fallbackThumb;

    var priceHtml = price > 0
        ? "<div style=\"font-size:.8rem;font-weight:700;color:var(--amber)\">" + formatRelatedPrice(price) + "원</div>"
        : "<div style=\"font-size:.8rem;font-weight:700;color:var(--amber)\">가격 확인</div>";

    var originalPriceHtml = hasDiscount
        ? "<div style=\"font-size:.68rem;color:var(--muted);text-decoration:line-through\">" + formatRelatedPrice(originalPrice) + "원</div>"
        : "";

    return "<div onclick=\"location.href='" + detailUrl + "'\" style=\"display:flex;align-items:center;gap:11px;padding:10px 0;" + borderStyle + "cursor:pointer\">"
        + thumbHtml
        + "<div style=\"flex:1;min-width:0\">"
        + "<div style=\"font-size:.65rem;color:var(--green);font-weight:700;margin-bottom:2px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis\">" + escapeHtml(reason) + "</div>"
        + "<div style=\"font-size:.76rem;font-weight:500;color:var(--dark);white-space:nowrap;overflow:hidden;text-overflow:ellipsis\">" + escapeHtml(name) + "</div>"
        + "<div style=\"font-size:.68rem;color:var(--muted);white-space:nowrap;overflow:hidden;text-overflow:ellipsis\">" + escapeHtml(origin || category) + "</div>"
        + "</div>"
        + "<div style=\"text-align:right;flex-shrink:0\">"
        + priceHtml
        + originalPriceHtml
        + "</div>"
        + "</div>";
}

function normalizeRelatedImageUrl(value) {
    if (value === null || value === undefined) return '';
    var url = String(value).trim();
    if (!url || url === 'null' || url === 'undefined') return '';
    return url;
}

function getRelatedCategoryEmoji(category) {
    if (!category) return '🥬';
    if (category.includes('과일')) return '🍎';
    if (category.includes('곡')) return '🌾';
    if (category.includes('버섯')) return '🍄';
    if (category.includes('뿌리')) return '🥕';
    if (category.includes('채소')) return '🥦';
    return '🥬';
}

function formatRelatedPrice(value) {
    return Number(value || 0).toLocaleString('ko-KR');
}

// ── 리뷰 기능 ─────────────────────────────────────────
function initReviewPage() {
    loadReviewStats(currentProductId);
    loadReviewList(currentProductId);
    loadAiReviewSummary(currentProductId);
    reviewInitialized = true;
}

async function loadReviewStats(productId) {
    try {
        var response = await fetch('/api/reviews/stats?productId=' + productId);
        var data = await response.json();
        var totalReviewCount = Number(data.totalReviewCount || 0);
        var averageRating = Number(data.averageRating || 0);
        var repurchaseRate = Number(data.repurchaseRate || 0);
        var photoReviewCount = Number(data.photoReviewCount || 0);
        document.getElementById('reviewTotalCount').textContent = formatCount(totalReviewCount);
        document.getElementById('reviewAverageRating').textContent = averageRating.toFixed(1);
        document.getElementById('reviewRepurchaseRate').textContent = repurchaseRate.toFixed(1) + '%';
        document.getElementById('reviewPhotoCount').textContent = formatCount(photoReviewCount);
        var tabCount = document.getElementById('detailReviewTabCount');
        if (tabCount) tabCount.textContent = totalReviewCount;
        var ratingText = document.getElementById('detailReviewCountText');
        if (ratingText) ratingText.textContent = averageRating.toFixed(1) + ' (후기 ' + totalReviewCount.toLocaleString() + ')';
        var ratingStars = document.getElementById('detailRatingStars');
        if (ratingStars) ratingStars.textContent = renderReviewStars(Math.round(averageRating));
    } catch (error) {
        document.getElementById('reviewTotalCount').textContent = '-';
        document.getElementById('reviewAverageRating').textContent = '-';
        document.getElementById('reviewRepurchaseRate').textContent = '-';
        document.getElementById('reviewPhotoCount').textContent = '-';
    }
}

async function loadReviewList(productId) {
    var list = document.getElementById('reviewList');
    if (!list) return;
    list.innerHTML = '<div class="review-empty">리뷰를 불러오는 중입니다.</div>';
    try {
        var url = '/api/reviews?productId=' + productId;
        if (currentUserId) url += '&userId=' + currentUserId;
        var response = await fetch(url);
        cachedReviews = await response.json();
        if (!Array.isArray(cachedReviews)) cachedReviews = [];
        renderReviewList();
    } catch (error) {
        list.innerHTML = '<div class="review-empty">리뷰 목록을 불러오지 못했습니다.</div>';
    }
}

async function loadAiReviewSummary(productId) {
    var text = document.getElementById('aiReviewSummaryText');
    var tags = document.getElementById('aiReviewSummaryTags');
    if (!text || !tags) return;
    text.textContent = 'AI 리뷰 총평을 불러오는 중입니다.';
    tags.innerHTML = '<span class="ai-tag">AI 요약</span>';
    try {
        var response = await fetch('/api/products/' + productId + '/ai-review-summary');
        var data = await response.json();
        if (!response.ok) {
            text.textContent = data.message || '아직 등록된 AI 리뷰 총평이 없습니다.';
            tags.innerHTML = '<span class="ai-tag">총평 없음</span>';
            return;
        }
        text.textContent = data.summary;
        tags.innerHTML = '<span class="ai-tag">✅ 리뷰 ' + data.reviewCount + '개 분석</span><span class="ai-tag">✅ AI 총평</span><span class="ai-tag">✅ 상품 ID ' + data.productId + '</span>';
    } catch (error) {
        text.textContent = 'AI 리뷰 총평 API에 연결할 수 없습니다.';
        tags.innerHTML = '<span class="ai-tag">연결 실패</span>';
    }
}

function renderReviewList() {
    var list = document.getElementById('reviewList');
    if (!list) return;
    var reviews = cachedReviews.slice();
    if (onlyPhotoReview) {
        reviews = reviews.filter(function(review) {
            return normalizeReviewImageUrl(review.imageUrl) !== '';
        });
    }
    if (reviewFilter === 'helpful') {
        reviews.sort(function(a, b) {
            var diff = Number(b.helpfulCount || 0) - Number(a.helpfulCount || 0);
            return diff !== 0 ? diff : new Date(b.createdAt || 0) - new Date(a.createdAt || 0);
        });
    }
    if (reviewFilter === 'high') reviews.sort(function(a, b) { return Number(b.rating || 0) - Number(a.rating || 0); });
    if (reviewFilter === 'low') reviews.sort(function(a, b) { return Number(a.rating || 0) - Number(b.rating || 0); });
    if (reviewFilter === 'recent') reviews.sort(function(a, b) { return new Date(b.createdAt || 0) - new Date(a.createdAt || 0); });

    if (reviews.length === 0) {
        list.innerHTML = '<div class="review-empty">아직 등록된 리뷰가 없습니다.</div>';
        return;
    }

    list.innerHTML = reviews.map(function(review) {
        var isMine = currentUserId !== null && review.userId === currentUserId;
        var avatarClass = review.rating >= 5 ? 'ap' : review.rating >= 4 ? 'gn' : 'mt';
        var helpfulCount = Number(review.helpfulCount || 0);
        var isHelpful = Boolean(review.helpfulByCurrentUser);
        var imageUrl = normalizeReviewImageUrl(review.imageUrl);
        var imageHtml = imageUrl
            ? '<div style="margin-top:10px"><img src="' + escapeHtml(imageUrl) + '" alt="리뷰 이미지" onerror="this.parentElement.style.display=\'none\'" style="max-width:180px;border-radius:10px;border:1px solid var(--beige)"></div>'
            : '';
        var editDeleteHtml = isMine
            ? '<div style="display:flex;gap:6px"><button class="hlp-btn" onclick="event.stopPropagation(); openReviewEditForm(' + review.reviewId + ')">수정</button><button class="hlp-btn" onclick="event.stopPropagation(); deleteReview(' + review.reviewId + ')">삭제</button></div>'
            : '';
        return '<div class="rv2" onclick="openReviewDetail(' + review.reviewId + ')">'
            + '<div class="rv2-top"><div class="rv2-auth">'
            + '<div class="rv2-av ' + avatarClass + '">' + getReviewEmoji(review.rating) + '</div>'
            + '<div><div class="rv2-nm">' + escapeHtml(review.displayName || '익명 구매자') + '</div><div class="rv2-dt">' + formatReviewDate(review.createdAt) + '</div></div>'
            + '</div><div class="rv2-stars">' + renderReviewStars(review.rating) + '</div></div>'
            + '<div class="rv2-txt">' + escapeHtml(review.content) + '</div>'
            + imageHtml
            + '<div class="rv2-foot"><div style="display:flex;gap:6px;align-items:center;flex-wrap:wrap">'
            + '<button class="hlp-btn ' + (isHelpful ? 'helpful-active' : '') + '" onclick="event.stopPropagation(); toggleReviewHelpful(' + review.reviewId + ')">👍 도움돼요 <span id="helpfulCount-' + review.reviewId + '">' + helpfulCount + '</span></button>'
            + editDeleteHtml
            + '</div><span class="rv2-cert">' + (review.repurchaseIntent ? '재구매 의향 있음' : '재구매 의향 없음') + '</span></div>'
            + '</div>';
    }).join('');
}

function openReviewDetail(reviewId) {
    var review = cachedReviews.find(function(item) { return Number(item.reviewId) === Number(reviewId); });
    if (!review) { showReviewToast('리뷰 정보를 찾을 수 없어요.'); return; }
    var detailModal = document.getElementById('reviewDetailModal');
    var detailContent = document.getElementById('reviewDetailContent');
    if (!detailModal || !detailContent) return;
    var imageUrl = normalizeReviewImageUrl(review.imageUrl);
    var helpfulCount = Number(review.helpfulCount || 0);
    var isMine = currentUserId !== null && review.userId === currentUserId;
    var imageHtml = imageUrl
        ? '<div class="review-detail-image"><img src="' + escapeHtml(imageUrl) + '" alt="리뷰 이미지" onerror="this.parentElement.style.display=\'none\'"></div>'
        : '<div class="review-detail-image" style="color:var(--muted);font-size:.78rem">등록된 리뷰 사진이 없습니다.</div>';
    var actionHtml = isMine
        ? '<button class="hlp-btn" type="button" onclick="closeReviewDetail(); openReviewEditForm(' + review.reviewId + ')">수정</button>'
        + '<button class="hlp-btn" type="button" onclick="closeReviewDetail(); deleteReview(' + review.reviewId + ')">삭제</button>'
        : '';
    detailContent.innerHTML =
        '<div class="review-detail-meta"><div class="review-detail-user">'
        + '<div class="review-detail-avatar">' + getReviewEmoji(review.rating) + '</div>'
        + '<div><div class="review-detail-name">' + escapeHtml(review.displayName || '익명 구매자') + '</div>'
        + '<div class="review-detail-date">' + formatReviewDate(review.createdAt) + '</div></div>'
        + '</div><div class="review-detail-stars">' + renderReviewStars(review.rating) + '</div></div>'
        + imageHtml
        + '<div class="review-detail-content">' + escapeHtml(review.content || '-') + '</div>'
        + '<div class="review-detail-info">'
        + '<div class="review-detail-info-card"><div class="review-detail-info-label">평점</div><div class="review-detail-info-value">' + escapeHtml(review.rating || '-') + '점</div></div>'
        + '<div class="review-detail-info-card"><div class="review-detail-info-label">재구매 의향</div><div class="review-detail-info-value">' + (review.repurchaseIntent ? '있음' : '없음') + '</div></div>'
        + '<div class="review-detail-info-card"><div class="review-detail-info-label">도움돼요</div><div class="review-detail-info-value">' + helpfulCount + '</div></div>'
        + '</div>'
        + '<div class="review-detail-actions">'
        + '<button class="hlp-btn ' + (review.helpfulByCurrentUser ? 'helpful-active' : '') + '" type="button" onclick="toggleReviewHelpfulFromDetail(' + review.reviewId + ')">👍 도움돼요</button>'
        + actionHtml
        + '<button class="hlp-btn" type="button" onclick="closeReviewDetail()">닫기</button>'
        + '</div>';
    detailModal.style.display = 'flex';
}

function closeReviewDetail() {
    var detailModal = document.getElementById('reviewDetailModal');
    if (detailModal) detailModal.style.display = 'none';
}

async function toggleReviewHelpfulFromDetail(reviewId) {
    await toggleReviewHelpful(reviewId);
    openReviewDetail(reviewId);
}

function setReviewFilter(filter, element) {
    reviewFilter = filter;
    document.querySelectorAll('.review-filter-group .mf').forEach(function(item) {
        if (item.id !== 'photoOnlyFilter') item.classList.remove('on');
    });
    if (element) element.classList.add('on');
    renderReviewList();
}

function togglePhotoOnly(element) {
    onlyPhotoReview = !onlyPhotoReview;
    if (element) {
        if (onlyPhotoReview) element.classList.add('on');
        else element.classList.remove('on');
    }
    renderReviewList();
}

function openReviewForm() {
    if (!currentUserId) {
        showReviewToast('로그인 후 리뷰를 작성할 수 있어요.');
        setTimeout(function() { location.href = '/login'; }, 900);
        return;
    }
    document.getElementById('reviewFormTitle').textContent = '리뷰 작성하기';
    document.getElementById('reviewEditId').value = '';
    document.getElementById('reviewRating').value = '5';
    document.getElementById('reviewContent').value = '';
    document.getElementById('reviewImageUrl').value = '';
    document.getElementById('reviewRepurchaseIntent').checked = true;
    document.getElementById('reviewAnonymous').checked = false;
    document.getElementById('reviewFormModal').style.display = 'flex';
}

function openReviewEditForm(reviewId) {
    var review = cachedReviews.find(function(item) { return item.reviewId === reviewId; });
    if (!review) { showReviewToast('수정할 리뷰를 찾을 수 없어요.'); return; }
    if (review.userId !== currentUserId) { showReviewToast('본인이 작성한 리뷰만 수정할 수 있어요.'); return; }
    document.getElementById('reviewFormTitle').textContent = '리뷰 수정하기';
    document.getElementById('reviewEditId').value = review.reviewId;
    document.getElementById('reviewRating').value = review.rating;
    document.getElementById('reviewContent').value = review.content;
    document.getElementById('reviewImageUrl').value = review.imageUrl || '';
    document.getElementById('reviewRepurchaseIntent').checked = review.repurchaseIntent;
    document.getElementById('reviewAnonymous').checked = Boolean(review.isAnonymous);
    document.getElementById('reviewFormModal').style.display = 'flex';
}

function closeReviewForm() {
    document.getElementById('reviewFormModal').style.display = 'none';
}

async function submitReviewForm() {
    if (!currentUserId) { showReviewToast('로그인 후 리뷰를 작성할 수 있어요.'); return; }
    var reviewId = document.getElementById('reviewEditId').value;
    var rating = Number(document.getElementById('reviewRating').value);
    var content = document.getElementById('reviewContent').value.trim();
    var imageUrl = document.getElementById('reviewImageUrl').value.trim();
    var repurchaseIntent = document.getElementById('reviewRepurchaseIntent').checked;
    var isAnonymous = document.getElementById('reviewAnonymous').checked;
    if (!content) { showReviewToast('리뷰 내용을 입력해주세요.'); return; }
    var isEdit = reviewId !== '';
    var url = isEdit ? '/api/reviews/' + reviewId : '/api/reviews';
    var method = isEdit ? 'PUT' : 'POST';
    var body = isEdit
        ? { userId: currentUserId, rating: rating, content: content, imageUrl: imageUrl || null, repurchaseIntent: repurchaseIntent, isAnonymous: isAnonymous }
        : { productId: currentProductId, userId: currentUserId, orderItemId: null, rating: rating, content: content, imageUrl: imageUrl || null, repurchaseIntent: repurchaseIntent, isAnonymous: isAnonymous };
    try {
        var response = await fetch(url, { method: method, headers: {'Content-Type': 'application/json'}, body: JSON.stringify(body) });
        var data = await response.json();
        if (!response.ok) { showReviewToast(data.message || '리뷰 저장에 실패했습니다.'); return; }
        closeReviewForm();
        showReviewToast(isEdit ? '리뷰가 수정되었습니다.' : '리뷰가 등록되었습니다.');
        await loadReviewStats(currentProductId);
        await loadReviewList(currentProductId);
        await loadAiReviewSummary(currentProductId);
    } catch (error) {
        showReviewToast('서버 연결에 실패했습니다.');
    }
}

async function deleteReview(reviewId) {
    if (!currentUserId) { showReviewToast('로그인 후 이용할 수 있어요.'); return; }
    if (!confirm('리뷰를 삭제할까요?')) return;
    try {
        var response = await fetch('/api/reviews/' + reviewId + '?userId=' + currentUserId, { method: 'DELETE' });
        var data = await response.json();
        if (!response.ok) { showReviewToast(data.message || '리뷰 삭제에 실패했습니다.'); return; }
        showReviewToast('리뷰가 삭제되었습니다.');
        await loadReviewStats(currentProductId);
        await loadReviewList(currentProductId);
        await loadAiReviewSummary(currentProductId);
    } catch (error) {
        showReviewToast('서버 연결에 실패했습니다.');
    }
}

async function toggleReviewHelpful(reviewId) {
    if (!currentUserId) {
        showReviewToast('로그인 후 도움돼요를 누를 수 있어요.');
        setTimeout(function() { location.href = '/login'; }, 900);
        return;
    }
    try {
        var response = await fetch('/api/reviews/' + reviewId + '/helpful?userId=' + currentUserId, { method: 'POST' });
        var data = await response.json();
        if (!response.ok) { showReviewToast(data.message || '도움돼요 처리에 실패했습니다.'); return; }
        cachedReviews = cachedReviews.map(function(review) {
            if (review.reviewId === reviewId) {
                review.helpfulCount = data.helpfulCount;
                review.helpfulByCurrentUser = data.helpfulByCurrentUser;
            }
            return review;
        });
        renderReviewList();
        showReviewToast('도움돼요가 반영됐어요 🌿');
    } catch (error) {
        showReviewToast('서버 연결에 실패했습니다.');
    }
}

function renderReviewStars(rating) {
    var safeRating = Number(rating || 0);
    var stars = '';
    for (var i = 1; i <= 5; i++) stars += i <= safeRating ? '★' : '☆';
    return stars;
}

function getReviewEmoji(rating) {
    if (rating >= 5) return '😊';
    if (rating >= 4) return '🌿';
    if (rating >= 3) return '🙂';
    return '🥬';
}

function formatReviewDate(value) {
    if (!value) return '-';
    return String(value).replace('T', ' ').substring(0, 10);
}

function formatCount(value) {
    var number = Number(value || 0);
    if (number >= 1000) return number.toLocaleString() + '+';
    return String(number);
}

function normalizeReviewImageUrl(value) {
    if (value === null || value === undefined) return '';
    var url = String(value).trim();
    if (!url || url === 'null' || url === 'undefined') return '';
    return url;
}

function escapeHtml(value) {
    if (value === null || value === undefined) return '';
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

function showReviewToast(message) {
    if (typeof showToast === 'function') { showToast(message); return; }
    alert(message);
}

// ── 채팅 ──────────────────────────────────────────────
function initChatBtn(myEmail, role) {
    var chatBtn = document.getElementById('chatBtn');
    if (!chatBtn) return;
    if (myEmail === sellerEmail) {
        isMySellProduct = true;
        chatBtn.innerHTML = '💬 내 채팅 보기';
    } else if (role === 'SELLER') {
        chatBtn.style.display = 'none';
    }
}

function handleChatBtnClick() {
    if (isMySellProduct) openChatListModal();
    else openChatModal();
}

function openChatModal() {
    var token = localStorage.getItem('token') || sessionStorage.getItem('token');
    if (!token) {
        if (confirm('로그인이 필요해요 🌿\n로그인 페이지로 이동할까요?')) location.href = '/login';
        return;
    }
    fetch('/chat/rooms', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
        body: JSON.stringify({ productId: productId })
    })
        .then(function(res) { return res.json(); })
        .then(function(room) {
            if (room.message) { showToast(room.message); return; }
            currentRoomId = room.id;
            document.getElementById('chatRoomLabel').textContent = '🌾 ' + room.sellerName + ' 농부님';
            document.getElementById('chatMsgList').innerHTML = '';
            document.getElementById('chatModal').style.display = 'flex';
            loadChatHistory(currentRoomId, token);
            connectStomp(currentRoomId, token);
        })
        .catch(function() { showToast('채팅을 시작할 수 없어요.'); });
}

function openChatListModal() {
    var token = localStorage.getItem('token') || sessionStorage.getItem('token');
    if (!token) return;
    fetch('/chat/rooms/seller/product/' + productId, {
        headers: { 'Authorization': 'Bearer ' + token }
    })
        .then(function(res) { return res.json(); })
        .then(function(rooms) {
            var content = document.getElementById('chatListContent');
            if (!rooms.length) {
                content.innerHTML = '<div style="padding:24px;text-align:center;color:var(--muted);font-size:.84rem">아직 채팅이 없어요.</div>';
            } else {
                content.innerHTML = rooms.map(function(room) {
                    var unread = room.unreadCount > 0
                        ? '<span style="background:#E84040;color:#fff;font-size:.6rem;font-weight:700;border-radius:50%;padding:2px 5px;margin-left:6px">' + room.unreadCount + '</span>'
                        : '';
                    var lastTime = room.lastMessageAt ? room.lastMessageAt.substring(11, 16) : '';
                    return '<div onclick="openChatFromList(' + room.id + ')" style="display:flex;align-items:center;gap:12px;padding:14px 16px;border-bottom:1px solid var(--sand);cursor:pointer;transition:background .15s" onmouseover="this.style.background=\'var(--beige2)\'" onmouseout="this.style.background=\'#fff\'">'
                        + '<div style="width:38px;height:38px;border-radius:50%;background:var(--gp);display:flex;align-items:center;justify-content:center;font-size:1rem;flex-shrink:0">👤</div>'
                        + '<div style="flex:1;min-width:0">'
                        + '<div style="font-size:.84rem;font-weight:600;color:var(--dark)">' + escapeHtml(room.buyerName) + ' 님' + unread + '</div>'
                        + '<div style="font-size:.72rem;color:var(--muted);margin-top:2px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">' + escapeHtml(room.lastMessage || '메시지 없음') + '</div>'
                        + '</div>'
                        + '<div style="font-size:.65rem;color:var(--muted);flex-shrink:0">' + lastTime + '</div>'
                        + '</div>';
                }).join('');
            }
            document.getElementById('chatListModal').style.display = 'flex';
        })
        .catch(function() { showToast('채팅 목록을 불러올 수 없어요.'); });
}


function openChatRoomById(roomId) {
    var token = localStorage.getItem('token') || sessionStorage.getItem('token');
    if (!token || !roomId) return;
    currentRoomId = roomId;
    var listModal = document.getElementById('chatListModal');
    if (listModal) listModal.style.display = 'none';
    document.getElementById('chatMsgList').innerHTML = '';
    document.getElementById('chatRoomLabel').textContent = '채팅방';
    document.getElementById('chatModal').style.display = 'flex';
    loadChatHistory(roomId, token);
    connectStomp(roomId, token);
}

function openChatFromList(roomId) {
    var token = localStorage.getItem('token') || sessionStorage.getItem('token');
    currentRoomId = roomId;
    document.getElementById('chatListModal').style.display = 'none';
    document.getElementById('chatMsgList').innerHTML = '';
    document.getElementById('chatRoomLabel').textContent = '구매자와의 채팅';
    document.getElementById('chatModal').style.display = 'flex';
    loadChatHistory(roomId, token);
    connectStomp(roomId, token);
}

function loadChatHistory(roomId, token) {
    fetch('/chat/rooms/' + roomId + '/messages', {
        headers: { 'Authorization': 'Bearer ' + token }
    })
        .then(function(res) { return res.json(); })
        .then(function(messages) {
            var list = document.getElementById('chatMsgList');
            list.innerHTML = '';
            messages.forEach(function(msg) { appendChatMessage(msg); });
            list.scrollTop = list.scrollHeight;
            fetch('/chat/rooms/' + roomId + '/read', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + token }
            }).then(function(){
                if (typeof loadNotificationCount === 'function') loadNotificationCount();
            });
        });
}

function connectStomp(roomId, token) {
    if (stompClient) stompClient.deactivate();
    stompClient = new StompJs.Client({
        webSocketFactory: function() { return new SockJS('/ws-chat'); },
        connectHeaders: { Authorization: 'Bearer ' + token },
        onConnect: function() {
            stompClient.subscribe('/topic/room/' + roomId, function(frame) {
                var msg = JSON.parse(frame.body);
                appendChatMessage(msg);
                var list = document.getElementById('chatMsgList');
                list.scrollTop = list.scrollHeight;
            });
        },
        onStompError: function() { showToast('채팅 연결에 실패했어요.'); }
    });
    stompClient.activate();
}

function sendChatMessage() {
    var input = document.getElementById('chatInput');
    var content = input.value.trim();
    if (!content || !stompClient || !stompClient.connected) return;
    stompClient.publish({
        destination: '/app/chat/' + currentRoomId,
        body: JSON.stringify({ content: content })
    });
    input.value = '';
}

function appendChatMessage(msg) {
    var token = sessionStorage.getItem('token') || localStorage.getItem('token');
    var myEmail = JSON.parse(atob(token.split('.')[1])).sub;
    var isMe = msg.senderEmail === myEmail;
    var list = document.getElementById('chatMsgList');

    var wrap = document.createElement('div');
    wrap.dataset.msgId = msg.id;
    wrap.style.cssText = 'display:flex;flex-direction:column;align-items:' + (isMe ? 'flex-end' : 'flex-start') + ';margin-bottom:10px';

    if (!isMe) {
        var nameEl = document.createElement('div');
        nameEl.style.cssText = 'font-size:.65rem;color:var(--muted);margin-bottom:3px;padding-left:2px';
        nameEl.textContent = msg.senderName;
        wrap.appendChild(nameEl);
    }

    var row = document.createElement('div');
    row.style.cssText = 'display:flex;align-items:flex-end;gap:5px;' + (isMe ? 'flex-direction:row-reverse' : '');

    var bubble = document.createElement('div');
    bubble.style.cssText = 'max-width:72%;padding:9px 13px;border-radius:' + (isMe ? '14px 14px 4px 14px' : '14px 14px 14px 4px')
        + ';background:' + (isMe ? '#FEE500' : '#fff')
        + ';color:' + (isMe ? '#3C1E1E' : 'var(--dark)')
        + ';font-size:.83rem;line-height:1.55;word-break:keep-all;box-shadow:0 1px 2px rgba(0,0,0,.08);border:1.5px solid ' + (isMe ? 'transparent' : 'var(--sand)');
    bubble.textContent = msg.content;

    var time = document.createElement('div');
    time.style.cssText = 'font-size:.6rem;color:var(--muted);flex-shrink:0;padding-bottom:2px';
    time.textContent = msg.sentAt ? msg.sentAt.substring(11, 16) : '';

    row.appendChild(bubble);
    row.appendChild(time);

    if (isMe && msg.id) {
        var delBtn = document.createElement('button');
        delBtn.textContent = '삭제';
        delBtn.style.cssText = 'border:none;background:transparent;font-size:.58rem;color:#bbb;cursor:pointer;padding:0 2px 2px;flex-shrink:0;font-family:inherit;opacity:0;transition:opacity .15s';
        delBtn.onclick = function() { deleteChatMessage(msg.id, wrap); };
        row.appendChild(delBtn);
        row.addEventListener('mouseenter', function() { delBtn.style.opacity = '1'; });
        row.addEventListener('mouseleave', function() { delBtn.style.opacity = '0'; });
    }

    wrap.appendChild(row);
    list.appendChild(wrap);
}

function deleteChatMessage(messageId, wrapEl) {
    if (!confirm('이 메시지를 삭제할까요?')) return;
    var token = sessionStorage.getItem('token') || localStorage.getItem('token');
    fetch('/chat/messages/' + messageId, {
        method: 'DELETE',
        headers: { 'Authorization': 'Bearer ' + token }
    }).then(function(res) {
        if (res.ok) {
            wrapEl.remove();
        } else {
            res.json().then(function(data) { showToast(data.message || '삭제에 실패했어요.'); });
        }
    }).catch(function() { showToast('오류가 발생했어요.'); });
}

function closeChatModal() {
    document.getElementById('chatModal').style.display = 'none';
    if (stompClient) { stompClient.deactivate(); stompClient = null; }
    currentRoomId = null;
}

// ── 픽업 탭 ──────────────────────────────────────────
function handlePickupTab(element) {
    const pickupAvailable = element.dataset.pickup === 'true';
    if (!pickupAvailable) {
        showToast('🚫 픽업 서비스가 불가능한 제품입니다.');
        return;
    }
    switchDetailTab('pickup');
}

function openKakaoRoute() {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    if (!token) {
        if (confirm('로그인이 필요해요 🌿\n로그인 페이지로 이동할까요?')) location.href = '/login';
        return;
    }
    fetch('/api/route/kakao?productId=' + productId, {
        headers: { 'Authorization': 'Bearer ' + token }
    })
        .then(function(res) { return res.json().then(function(data) { return { ok: res.ok, data: data }; }); })
        .then(function(result) {
            if (!result.ok) { showToast(result.data.message || '길찾기 정보를 불러오지 못했어요 ㅠㅠ'); return; }
            window.open(result.data.url, '_blank');
        })
        .catch(function() { showToast('서버 연결에 실패했어요 ㅠㅠ'); });
}

function openNaverSearch() {
    fetch('/api/products/' + productId)
        .then(function(res) { return res.json(); })
        .then(function(data) {
            var address = data.address || '판매처';
            window.open('https://map.naver.com/p/search/' + encodeURIComponent(address), '_blank');
        })
        .catch(function() { showToast('판매처 정보를 불러오지 못했어요 ㅠㅠ'); });
}
