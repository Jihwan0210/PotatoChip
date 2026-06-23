var myPageUserId = null;
var selectedReviewTarget = null;
var cachedMyReviews = [];

function getToken() {
    return localStorage.getItem('token') || sessionStorage.getItem('token');
}

function doLogout() {
    localStorage.clear();
    sessionStorage.clear();
    location.href = '/login';
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function () {
    const token = getToken();
    if (!token) {
        location.href = '/login';
        return;
    }

    // 로그아웃 버튼 전환
    const btnNav = document.querySelector('.btn-nav');
    if (btnNav) {
        btnNav.textContent = '로그아웃';
        btnNav.setAttribute('onclick', 'doLogout()');
    }

    loadMyInfo();
    loadMyOrders();
});

// 내 정보 조회
function loadMyInfo() {
    const token = getToken();

    fetch('/mypage/info', {
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + token }
    })
        .then(res => {
            if (res.status === 401) {
                location.href = '/login';
                return null;
            }
            return res.json();
        })
        .then(data => {
            if (!data) return;
            myPageUserId = data.id;
            document.getElementById('mp-name').value     = data.name     || '';
            document.getElementById('mp-nickname').value = data.nickname || '';
            document.getElementById('mp-email').value    = data.email    || '';
            document.getElementById('mp-phone').value    = data.phone    || '';
            document.getElementById('mp-address').value  = data.address  || '';

            var nameEl = document.getElementById('mp-display-name');
            if (nameEl) nameEl.textContent = (data.name || '') + '님';
            var emailEl = document.getElementById('mp-display-email');
            if (emailEl) emailEl.textContent = data.email || '';
        })
        .catch(err => console.error('내 정보 조회 실패:', err));
}

var _savedValues = {};

// 수정 모드 진입
function enterEditMode() {
    var editableIds = ['mp-name', 'mp-nickname', 'mp-phone'];
    editableIds.forEach(function(id) {
        var el = document.getElementById(id);
        _savedValues[id] = el.value;
        el.removeAttribute('readonly');
        el.style.background = '#fff';
        el.style.color = 'var(--text)';
    });
    // 주소: 검색 버튼 표시, 상세주소만 직접 입력 가능
    _savedValues['mp-address'] = document.getElementById('mp-address').value;
    _savedValues['mp-address-detail'] = document.getElementById('mp-address-detail').value;
    var detail = document.getElementById('mp-address-detail');
    detail.removeAttribute('readonly');
    detail.style.background = '#fff';
    detail.style.color = 'var(--text)';
    document.getElementById('mp-address-search-row').style.display = 'flex';

    document.getElementById('btn-edit').style.display = 'none';
    document.getElementById('btn-save').style.display = 'flex';
    document.getElementById('btn-cancel').style.display = 'flex';
}

// 수정 모드 종료 (읽기 전용으로 복귀)
function exitEditMode() {
    var editableIds = ['mp-name', 'mp-nickname', 'mp-phone'];
    editableIds.forEach(function(id) {
        var el = document.getElementById(id);
        el.setAttribute('readonly', true);
        el.style.background = 'var(--beige2)';
        el.style.color = 'var(--muted)';
    });
    var detail = document.getElementById('mp-address-detail');
    detail.setAttribute('readonly', true);
    detail.style.background = 'var(--beige2)';
    detail.style.color = 'var(--muted)';
    document.getElementById('mp-address-search-row').style.display = 'none';

    document.getElementById('btn-edit').style.display = 'flex';
    document.getElementById('btn-save').style.display = 'none';
    document.getElementById('btn-cancel').style.display = 'none';
}

// 취소 - 원래 값으로 되돌리고 읽기 전용 복귀
function cancelEditMode() {
    var editableIds = ['mp-name', 'mp-nickname', 'mp-phone'];
    editableIds.forEach(function(id) {
        document.getElementById(id).value = _savedValues[id] || '';
    });
    document.getElementById('mp-address').value = _savedValues['mp-address'] || '';
    document.getElementById('mp-address-detail').value = _savedValues['mp-address-detail'] || '';
    exitEditMode();
}

// 주소 검색 (마이페이지용)
function openMpAddressSearch() {
    new daum.Postcode({
        oncomplete: function(data) {
            var addr = data.roadAddress || data.jibunAddress;
            document.getElementById('mp-postcode').value = data.zonecode;
            document.getElementById('mp-address').value = addr;
            document.getElementById('mp-address-detail').value = '';
            document.getElementById('mp-address-detail').focus();
        }
    }).open();
}

// 내 정보 저장
function saveMyProfile() {
    const token = getToken();
    if (!token) { location.href = '/login'; return; }

    var addrMain   = document.getElementById('mp-address').value.trim();
    var addrDetail = document.getElementById('mp-address-detail').value.trim();
    const body = {
        name:     document.getElementById('mp-name').value,
        nickname: document.getElementById('mp-nickname').value,
        phone:    document.getElementById('mp-phone').value,
        address:  addrDetail ? addrMain + ' ' + addrDetail : addrMain
    };

    fetch('/mypage/info', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify(body)
    })
        .then(res => res.json())
        .then(data => {
            exitEditMode();
            var nameEl = document.getElementById('mp-display-name');
            if (nameEl && body.name) nameEl.textContent = body.name + '님';
            showToast('저장되었어요! ✅');
        })
        .catch(err => console.error('저장 실패:', err));
}

// 마이페이지 탭 전환
function switchMyTab(name) {
    var tabs   = ['orders', 'wishlist', 'coupon', 'myreview', 'profile'];
    var tabIds = ['orders', 'wishlist', 'coupon', 'review',   'profile'];
    tabs.forEach(function (t, i) {
        var c   = document.getElementById('mycontent-' + t);
        var tab = document.getElementById('my-tab-' + tabIds[i]);
        if (c) c.style.display = (t === name ? 'block' : 'none');
        if (tab) {
            tab.classList.toggle('on', t === name);
        }
    });

    if (name === 'profile') loadMyInfo();
    if (name === 'orders') loadMyOrders()
    if (name === 'wishlist') loadWishlist();
    if (name === 'myreview') loadMyReviews();
}

// 찜 목록 조회
function loadWishlist() {
    var token = getToken();
    if (!token) return;
    var container = document.getElementById('wishlist-container');
    if (!container) return;

    fetch('/mypage/wishlist', {
        headers: { 'Authorization': 'Bearer ' + token }
    })
    .then(function(res) {
        if (res.status === 401) { location.href = '/login'; return null; }
        if (!res.ok) throw new Error('status ' + res.status);
        return res.json();
    })
    .then(function(items) {
        if (!items) return;
        if (!Array.isArray(items) || items.length === 0) {
            container.innerHTML = '<div class="mp-empty">찜한 상품이 없어요 🌿</div>';
            return;
        }
        var html = '<div class="wl-grid">';
        items.forEach(function(p) {
            var thumb = p.thumbnailUrl
                ? '<img src="' + p.thumbnailUrl + '" alt="' + p.name + '"/>'
                : '🥬';
            var priceHtml = p.discountPrice
                ? '<span class="wl-price">' + Number(p.discountPrice).toLocaleString() + '원</span>'
                  + '<span class="wl-og">' + Number(p.price).toLocaleString() + '원</span>'
                : '<span class="wl-price">' + Number(p.price).toLocaleString() + '원</span>';
            html += '<div class="wl-card" id="wl-' + p.id + '">'
                + '<div class="wl-thumb">' + thumb + '</div>'
                + '<div class="wl-body">'
                + '<div class="wl-cat">' + (p.category || '') + '</div>'
                + '<div class="wl-name">' + p.name + '</div>'
                + '<div class="wl-price-row">' + priceHtml + '</div>'
                + '<button class="wl-remove" onclick="removeWish(' + p.id + ')">❤️ 찜 해제</button>'
                + '</div></div>';
        });
        html += '</div>';
        container.innerHTML = html;
    })
    .catch(function() {
        container.innerHTML = '<div class="mp-empty">불러오기 실패했어요 ㅠㅠ</div>';
    });
}

// 찜 해제
function removeWish(productId) {
    var token = getToken();
    if (!token) return;
    fetch('/wishlist/' + productId, {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token }
    })
    .then(function(res) { return res.json(); })
    .then(function(data) {
        if (!data.added) {
            var card = document.getElementById('wl-' + productId);
            if (card) card.remove();
            var container = document.getElementById('wishlist-container');
            if (container && !container.querySelector('.wl-card')) {
                container.innerHTML = '<div class="mp-empty">찜한 상품이 없어요 🌿</div>';
            }
            showToast('찜 목록에서 제거됐어요');
        }
    })
    .catch(function() { showToast('오류가 발생했어요 ㅠㅠ'); });
}

// 비밀번호 변경
function changePassword() {
    var token = getToken();
    if (!token) { location.href = '/login'; return; }

    var currentPw = document.getElementById('cp-current').value;
    var newPw     = document.getElementById('cp-new').value;
    var confirmPw = document.getElementById('cp-confirm').value;

    if (!currentPw || !newPw || !confirmPw) {
        showToast('모든 항목을 입력해주세요.');
        return;
    }
    if (!/^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~`]).{8,}$/.test(newPw)) {
        showToast('새 비밀번호: 영어, 숫자, 특수문자 포함 8자 이상이어야 해요.');
        return;
    }
    if (newPw !== confirmPw) {
        showToast('새 비밀번호가 일치하지 않아요.');
        return;
    }

    fetch('/mypage/changepw', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
        body: JSON.stringify({ currentPassword: currentPw, newPassword: newPw, confirmPassword: confirmPw })
    })
        .then(function(res) { return res.json(); })
        .then(function(data) {
            if (data.message) {
                document.getElementById('cp-current').value = '';
                document.getElementById('cp-new').value = '';
                document.getElementById('cp-confirm').value = '';
                showToast('비밀번호가 변경되었어요! ✅');
            } else {
                showToast(data.error);
            }
        })
        .catch(function() { showToast('오류가 발생했습니다.'); });
}

// 회원 탈퇴
function withdrawAccount() {
    var token = getToken();
    if (!token) { location.href = '/login'; return; }

    var pw     = document.getElementById('wd-pw').value;
    var reason = document.getElementById('wd-reason').value;

    if (!pw) {
        showToast('비밀번호를 입력해주세요.');
        return;
    }

    if (!confirm('정말 탈퇴하시겠어요? 탈퇴 후에는 복구가 어렵습니다.')) return;

    fetch('/mypage/withdraw', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
        body: JSON.stringify({ password: pw, reason: reason })
    })
        .then(function(res) { return res.json(); })
        .then(function(data) {
            if (data.message) {
                localStorage.clear();
                sessionStorage.clear();
                showToast('탈퇴가 완료되었어요. 이용해주셔서 감사합니다 🌿');
                setTimeout(function() { location.href = '/'; }, 2000);
            } else {
                showToast(data.error);
            }
        })
        .catch(function() { showToast('오류가 발생했습니다.'); });
}


function loadMyOrders() {
    var token = getToken();
    var container = document.getElementById('orders-container');

    if (!token || !container) return;

    container.innerHTML = '<div class="mp-empty">주문 내역을 불러오는 중...</div>';

    fetch('/mypage/orders', {
        headers: { 'Authorization': 'Bearer ' + token }
    })
        .then(function(res) {
            if (res.status === 401) { location.href = '/login'; return null; }
            if (!res.ok) throw new Error('status ' + res.status);
            return res.json();
        })
        .then(function(orders) {
            if (!orders) return;

            if (!Array.isArray(orders) || orders.length === 0) {
                container.innerHTML = '<div class="mp-empty">주문 내역이 없어요 🌿</div>';
                return;
            }

            var html = '';

            orders.forEach(function(order, index) {
                var items = order.orderItems || order.items || [];
                var firstItem = items[0] || {};
                var thumbnail = firstItem.thumbnailUrl || 'https://placehold.co/56x56?text=🥔';
                var firstName = firstItem.productName || '상품 정보 없음';
                var displayName = items.length > 1 ? firstName + ' 외 ' + (items.length - 1) + '개' : firstName;
                var finalAmount = (order.totalAmount || 0) + (order.totalShippingFee || 0);
                var dateStr = order.createdAt ? order.createdAt.replace('T', ' ').slice(0, 16) : '';
                var STATUS_LABELS = {
                    PAYMENT_COMPLETE: '결제 완료', PREPARING: '배송 준비',
                    SHIPPING: '배송 중', DELIVERED: '배송 완료',
                    CANCELLED: '취소', REFUNDED: '환불'
                };
                var DELIVERY_LABELS = { delivery: '일반 택배', express: '당일 배송', pickup: '농장 픽업' };

                html += '<div style="border:1.5px solid #e8dcc8;border-radius:14px;padding:16px;margin-bottom:14px;background:#fff">';

                // 주문 헤더
                html += '<div style="display:flex;gap:14px;padding-bottom:12px;align-items:flex-start;border-bottom:1.5px solid #f6f2ec">'
                    + '<img src="' + thumbnail + '" style="width:56px;height:56px;object-fit:cover;border-radius:8px;flex-shrink:0" onerror="this.src=\'https://placehold.co/56x56?text=🥔\'"/>'
                    + '<div style="flex:1;min-width:0">'
                    + '<div style="display:flex;justify-content:space-between;align-items:center;gap:8px;margin-bottom:2px">'
                    + '<span style="font-weight:700;font-size:.92rem;color:var(--dark);white-space:nowrap;overflow:hidden;text-overflow:ellipsis">' + escapeMpHtml(displayName) + '</span>'
                    + '<span style="font-size:.74rem;font-weight:700;color:var(--green);background:#eef7e6;border-radius:20px;padding:2px 10px;flex-shrink:0">' + (STATUS_LABELS[order.status] || order.status) + '</span>'
                    + '</div>'
                    + '<div style="font-size:.74rem;color:var(--muted);margin-bottom:4px">' + escapeMpHtml(order.orderNumber || '') + '</div>'
                    + '<div style="font-size:.78rem;color:var(--muted);margin-bottom:8px">' + dateStr + ' · ' + (DELIVERY_LABELS[order.deliveryType] || order.deliveryType || '') + '</div>'
                    + '<div style="display:flex;justify-content:space-between;align-items:center">'
                    + '<span style="font-weight:700;color:#F39C12">' + Number(finalAmount).toLocaleString() + '원</span>'
                    + '<a href="/orders/complete/' + order.id + '?token=' + token + '" style="font-size:.78rem;color:var(--green);text-decoration:underline">상세보기</a>'
                    + '</div>'
                    + '</div>'
                    + '</div>';

                // 상품별 리뷰 버튼
                items.forEach(function(item) {
                    var thumb = item.thumbnailUrl
                        ? '<img src="' + escapeMpHtml(item.thumbnailUrl) + '" alt="상품 이미지" style="width:48px;height:48px;object-fit:cover;border-radius:10px">'
                        : '<div style="width:48px;height:48px;border-radius:10px;background:var(--beige2);display:flex;align-items:center;justify-content:center">🥬</div>';

                    var buttonHtml = '';
                    if (item.reviewed) {
                        buttonHtml = '<button disabled style="width:86px;height:28px;border:none;border-radius:8px;background:#ddd;color:#777;font-size:.66rem;font-weight:700;margin-left:auto;flex-shrink:0;font-family:inherit">리뷰 완료</button>';
                    } else if (item.status === 'DELIVERED') {
                        buttonHtml = '<button style="width:74px;height:28px;border:none;border-radius:8px;background:var(--green);color:#fff;font-size:.66rem;font-weight:700;cursor:pointer;margin-left:auto;flex-shrink:0;font-family:inherit" onclick="openReviewModal(' + item.productId + ',' + item.orderItemId + ',\'' + escapeMpAttr(item.productName || '') + '\')">리뷰 작성</button>';
                    } else {
                        buttonHtml = '<button disabled style="width:96px;height:28px;border:none;border-radius:8px;background:#ddd;color:#777;font-size:.66rem;font-weight:700;margin-left:auto;flex-shrink:0;font-family:inherit">작성 불가</button>';
                    }

                    html += '<div style="display:flex;gap:12px;align-items:center;padding-top:10px;margin-top:4px">'
                        + thumb
                        + '<div style="flex:1;min-width:0">'
                        + '<div style="font-weight:700;color:var(--dark);font-size:.84rem">' + escapeMpHtml(item.productName || '') + '</div>'
                        + '<div style="font-size:.72rem;color:var(--muted);margin-top:3px">' + Number(item.price || 0).toLocaleString() + '원 · ' + item.quantity + '개</div>'
                        + '</div>'
                        + buttonHtml
                        + '</div>';
                });

                html += '</div>';
            });

            container.innerHTML = html;
        })
        .catch(function() {
            container.innerHTML = '<div class="mp-empty">주문 내역을 불러오지 못했어요 ㅠㅠ</div>';
        });
}


function loadMyReviews() {
    var token = getToken();
    var container = document.getElementById('myreview-container');

    if (!token || !container) return;

    container.innerHTML = '<div class="mp-empty">작성한 리뷰를 불러오는 중...</div>';

    fetch('/mypage/reviews', {
        headers: { 'Authorization': 'Bearer ' + token }
    })
        .then(function(res) {
            if (res.status === 401) {
                location.href = '/login';
                return null;
            }
            if (!res.ok) throw new Error('status ' + res.status);
            return res.json();
        })
        .then(function(reviews) {
            if (!reviews) return;

            cachedMyReviews = reviews;

            if (!Array.isArray(reviews) || reviews.length === 0) {
                container.innerHTML = '<div class="mp-empty">작성한 리뷰가 없어요 🌿</div>';
                return;
            }

            var html = '';

            reviews.forEach(function(review) {
                var thumb = review.thumbnailUrl
                    ? '<img src="' + escapeMpHtml(review.thumbnailUrl) + '" alt="상품 이미지" style="width:58px;height:58px;object-fit:cover;border-radius:12px">'
                    : '<div style="width:58px;height:58px;border-radius:12px;background:var(--beige2);display:flex;align-items:center;justify-content:center">🥬</div>';

                html += '<div style="border:1px solid var(--sand);border-radius:14px;padding:16px;margin-bottom:14px;background:#fff">'
                    + '<div style="display:flex;gap:12px;align-items:flex-start">'
                    + thumb
                    + '<div style="flex:1;min-width:0">'
                    + '<div onclick="location.href=\'/market/detail?id=' + review.productId + '\'" style="font-weight:800;color:var(--dark);font-size:.9rem;cursor:pointer;text-decoration:underline;text-underline-offset:3px">' + escapeMpHtml(review.productName || '') + '</div>'
                    + '<div style="font-size:.78rem;color:#f5a400;margin:5px 0">' + renderMpStars(review.rating) + '</div>'
                    + '<div style="font-size:.8rem;color:var(--mid);line-height:1.6;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden">' + escapeMpHtml(review.content || '') + '</div>'
                    + '<div style="font-size:.68rem;color:var(--muted);margin-top:8px">' + formatMpDate(review.createdAt) + '</div>'
                    + '</div>'
                    + '<button type="button" class="mp-btn outline" style="height:30px;font-size:.68rem;padding:0 10px;white-space:nowrap" onclick="openMyReviewDetail(' + review.reviewId + ')">상세보기</button>'
                    + '</div>'
                    + '</div>';
            });

            container.innerHTML = html;
        })
        .catch(function() {
            container.innerHTML = '<div class="mp-empty">리뷰를 불러오지 못했어요 ㅠㅠ</div>';
        });
}

function openReviewModal(productId, orderItemId, productName) {
    selectedReviewTarget = {
        productId: productId,
        orderItemId: orderItemId,
        productName: productName
    };

    document.getElementById('review-product-name').textContent = productName;
    document.getElementById('review-rating').value = '5';
    document.getElementById('review-content').value = '';
    document.getElementById('review-repurchase').checked = false;
    document.getElementById('review-anonymous').checked = false;

    setMyReviewRating(5);

    var fileInput = document.getElementById('review-image-file');
    if (fileInput) fileInput.value = '';

    var preview = document.getElementById('review-image-preview');
    if (preview) {
        preview.style.display = 'none';
        preview.innerHTML = '';
    }

    var modal = document.getElementById('review-modal');
    if (modal) {
        modal.style.display = 'flex';
    }
}

function closeReviewModal() {
    selectedReviewTarget = null;

    var modal = document.getElementById('review-modal');
    if (modal) {
        modal.style.display = 'none';
    }
}

async function submitMyReview() {
    var token = getToken();

    if (!token) {
        location.href = '/login';
        return;
    }

    if (!selectedReviewTarget) {
        showMpMessage('리뷰 작성 대상이 없습니다.');
        return;
    }

    if (!myPageUserId) {
        showMpMessage('사용자 정보를 불러오지 못했어요.');
        return;
    }

    var content = document.getElementById('review-content').value.trim();

    if (!content) {
        showMpMessage('리뷰 내용을 입력해주세요.');
        return;
    }

    var imageUrl = null;

    try {
        imageUrl = await uploadReviewImageIfExists(token);
    } catch (e) {
        showMpMessage(e.message || '이미지 업로드에 실패했어요.');
        return;
    }

    var body = {
        productId: selectedReviewTarget.productId,
        userId: myPageUserId,
        orderItemId: selectedReviewTarget.orderItemId,
        rating: Number(document.getElementById('review-rating').value),
        content: content,
        imageUrl: imageUrl,
        repurchaseIntent: document.getElementById('review-repurchase').checked,
        isAnonymous: document.getElementById('review-anonymous').checked
    };

    fetch('/api/reviews', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify(body)
    })
        .then(function(res) {
            return res.json().then(function(data) {
                return { ok: res.ok, data: data };
            });
        })
        .then(function(result) {
            if (!result.ok) {
                showMpMessage(result.data.message || '리뷰 등록에 실패했어요.');
                return;
            }

            closeReviewModal();
            showMpMessage('리뷰가 등록되었어요! ✅');
            loadMyOrders();
            loadMyReviews();
        })
        .catch(function() {
            showMpMessage('리뷰 등록 중 오류가 발생했어요.');
        });
}

function getOrderStatusText(status) {
    switch (status) {
        case 'PAYMENT_COMPLETE': return '결제 완료';
        case 'PREPARING': return '배송 준비';
        case 'SHIPPING': return '배송 중';
        case 'DELIVERED': return '배송 완료';
        case 'CANCELLED': return '취소';
        case 'REFUNDED': return '환불';
        default: return status || '';
    }
}

function renderMpStars(rating) {
    var score = Number(rating || 0);
    var result = '';

    for (var i = 1; i <= 5; i++) {
        result += i <= score ? '★' : '☆';
    }

    return result;
}

function formatMpDate(value) {
    if (!value) return '';
    return String(value).replace('T', ' ').substring(0, 16);
}

function escapeMpHtml(value) {
    return String(value == null ? '' : value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

function escapeMpAttr(value) {
    return String(value == null ? '' : value)
        .replaceAll('\\', '\\\\')
        .replaceAll("'", "\\'");
}

function showMpMessage(message) {
    if (typeof showToast === 'function') {
        showToast(message);
        return;
    }

    alert(message);
}

function setMyReviewRating(score) {
    document.getElementById('review-rating').value = score;

    var stars = document.querySelectorAll('#review-star-box span');
    stars.forEach(function(star, index) {
        star.textContent = index < score ? '★' : '☆';
    });
}

function openMyReviewDetail(reviewId) {
    var review = cachedMyReviews.find(function(item) {
        return Number(item.reviewId) === Number(reviewId);
    });

    if (!review) {
        showMpMessage('리뷰 정보를 찾을 수 없어요.');
        return;
    }

    var modal = document.getElementById('my-review-detail-modal');
    var content = document.getElementById('my-review-detail-content');

    if (!modal || !content) return;

    var imageHtml = review.imageUrl
        ? '<div style="margin-bottom:14px;background:#fff;border:1.5px solid var(--beige);border-radius:14px;padding:12px;text-align:center"><img src="' + escapeMpHtml(review.imageUrl) + '" alt="리뷰 이미지" style="max-width:100%;max-height:420px;object-fit:contain;border-radius:10px"></div>'
        : '<div style="margin-bottom:14px;background:#fff;border:1.5px solid var(--beige);border-radius:14px;padding:14px;text-align:center;color:var(--muted);font-size:.78rem">등록된 리뷰 사진이 없습니다.</div>';

    content.innerHTML =
        '<div style="display:flex;align-items:center;justify-content:space-between;gap:12px;flex-wrap:wrap;margin-bottom:14px">'
        + '<div>'
        + '<div onclick="location.href=\'/market/detail?id=' + review.productId + '\'" style="font-size:.95rem;font-weight:800;color:var(--dark);cursor:pointer;text-decoration:underline;text-underline-offset:3px">' + escapeMpHtml(review.productName || '') + '</div>'
        + '<div style="font-size:.7rem;color:var(--muted);margin-top:4px">' + formatMpDate(review.createdAt) + '</div>'
        + '</div>'
        + '<div style="color:var(--amber);font-size:1rem;font-weight:700">' + renderMpStars(review.rating) + '</div>'
        + '</div>'
        + imageHtml
        + '<div style="background:var(--beige3);border:1.5px solid var(--beige);border-radius:14px;padding:16px;font-size:.84rem;color:var(--mid);line-height:1.85;white-space:pre-wrap;word-break:keep-all;overflow-wrap:anywhere;margin-bottom:14px">' + escapeMpHtml(review.content || '-') + '</div>'
        + '<div style="display:grid;grid-template-columns:repeat(3,1fr);gap:10px;margin-bottom:14px">'
        + '<div style="background:#fff;border:1.5px solid var(--beige);border-radius:12px;padding:12px;text-align:center"><div style="font-size:.67rem;color:var(--muted);margin-bottom:5px">평점</div><div style="font-size:.82rem;color:var(--dark);font-weight:700">' + escapeMpHtml(review.rating || '-') + '점</div></div>'
        + '<div style="background:#fff;border:1.5px solid var(--beige);border-radius:12px;padding:12px;text-align:center"><div style="font-size:.67rem;color:var(--muted);margin-bottom:5px">재구매 의향</div><div style="font-size:.82rem;color:var(--dark);font-weight:700">' + (review.repurchaseIntent ? '있음' : '없음') + '</div></div>'
        + '<div style="background:#fff;border:1.5px solid var(--beige);border-radius:12px;padding:12px;text-align:center"><div style="font-size:.67rem;color:var(--muted);margin-bottom:5px">익명 여부</div><div style="font-size:.82rem;color:var(--dark);font-weight:700">' + (review.isAnonymous ? '익명' : '공개') + '</div></div>'
        + '</div>'
        + '<div style="display:flex;justify-content:flex-end;gap:8px">'
        + '<button type="button" class="mp-btn outline" onclick="location.href=\'/market/detail?id=' + review.productId + '\'">상품으로 이동</button>'
        + '<button type="button" class="mp-btn gray" onclick="closeMyReviewDetail()">닫기</button>'
        + '</div>';

    modal.style.display = 'flex';
}

function closeMyReviewDetail() {
    var modal = document.getElementById('my-review-detail-modal');
    if (modal) {
        modal.style.display = 'none';
    }
}

function previewReviewImage() {
    var fileInput = document.getElementById('review-image-file');
    var preview = document.getElementById('review-image-preview');

    if (!fileInput || !preview) return;

    var file = fileInput.files && fileInput.files[0];

    if (!file) {
        preview.style.display = 'none';
        preview.innerHTML = '';
        return;
    }

    var url = URL.createObjectURL(file);
    preview.style.display = 'block';
    preview.innerHTML = '<img src="' + url + '" alt="리뷰 이미지 미리보기" style="max-width:100%;max-height:160px;object-fit:contain;border:1px solid var(--sand);border-radius:10px">';
}

async function uploadReviewImageIfExists(token) {
    var fileInput = document.getElementById('review-image-file');

    if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
        return null;
    }

    var formData = new FormData();
    formData.append('image', fileInput.files[0]);

    var response = await fetch('/api/reviews/upload-image', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        body: formData
    });

    var data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || '이미지 업로드 실패');
    }

    return data.imageUrl;
}

document.addEventListener('change', function(event) {
    if (event.target && event.target.id === 'review-image-file') {
        previewReviewImage();
    }
});

