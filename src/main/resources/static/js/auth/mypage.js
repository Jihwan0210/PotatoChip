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

            var pointEl = document.getElementById('mp-display-points');
            if (pointEl) pointEl.textContent = Number(data.points || data.point || 0).toLocaleString();
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

// 수정 모드 종료
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

// 취소
function cancelEditMode() {
    var editableIds = ['mp-name', 'mp-nickname', 'mp-phone'];

    editableIds.forEach(function(id) {
        document.getElementById(id).value = _savedValues[id] || '';
    });

    document.getElementById('mp-address').value = _savedValues['mp-address'] || '';
    document.getElementById('mp-address-detail').value = _savedValues['mp-address-detail'] || '';

    exitEditMode();
}

// 주소 검색
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

    if (!token) {
        location.href = '/login';
        return;
    }

    var addrMain = document.getElementById('mp-address').value.trim();
    var addrDetail = document.getElementById('mp-address-detail').value.trim();

    const body = {
        name: document.getElementById('mp-name').value,
        nickname: document.getElementById('mp-nickname').value,
        phone: document.getElementById('mp-phone').value,
        address: addrDetail ? addrMain + ' ' + addrDetail : addrMain
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
    var tabs = ['orders', 'wishlist', 'coupon', 'myreview', 'profile'];
    var tabIds = ['orders', 'wishlist', 'coupon', 'review', 'profile'];

    tabs.forEach(function (t, i) {
        var c = document.getElementById('mycontent-' + t);
        var tab = document.getElementById('my-tab-' + tabIds[i]);

        if (c) c.style.display = (t === name ? 'block' : 'none');

        if (tab) {
            tab.classList.toggle('on', t === name);
        }
    });

    if (name === 'orders') loadMyOrders();
    if (name === 'profile') loadMyInfo();
    if (name === 'wishlist') loadWishlist();
    if (name === 'myreview') loadMyReviews();
}

// 상품 상세 이동
function goProductDetail(productId) {
    if (!productId) return;
    location.href = '/market/detail?id=' + encodeURIComponent(productId);
}

// 주문 내역 조회
function loadMyOrders() {
    var token = getToken();
    var container = document.getElementById('orders-container');

    if (!token || !container) return;

    container.innerHTML = '<div class="mp-empty">주문 내역을 불러오는 중...</div>';

    fetch('/mypage/orders', {
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
        .then(function(orders) {
            if (!orders) return;

            if (!Array.isArray(orders) || orders.length === 0) {
                container.innerHTML = '<div class="mp-empty">주문 내역이 없어요 🌿</div>';
                return;
            }

            var html = '';

            orders.forEach(function(order) {
                html += '<div style="border:1px solid var(--sand);border-radius:14px;padding:16px;margin-bottom:14px;background:#fff">'
                    + '<div style="display:flex;justify-content:space-between;gap:10px;margin-bottom:12px">'
                    + '<div>'
                    + '<div style="font-weight:800;color:var(--dark);font-size:.9rem">주문번호 ' + escapeMpHtml(order.orderNumber || '') + '</div>'
                    + '<div style="font-size:.72rem;color:var(--muted);margin-top:4px">' + formatMpDate(order.createdAt) + '</div>'
                    + '</div>'
                    + '<div style="font-size:.78rem;font-weight:700;color:var(--green)">' + getOrderStatusText(order.status) + '</div>'
                    + '</div>';

                if (Array.isArray(order.items)) {
                    order.items.forEach(function(item) {
                        var productId = item.productId;

                        var thumb = item.thumbnailUrl
                            ? '<img src="' + escapeMpHtml(item.thumbnailUrl) + '" alt="상품 이미지" style="width:58px;height:58px;object-fit:cover;border-radius:12px">'
                            : '<div style="width:58px;height:58px;border-radius:12px;background:var(--beige2);display:flex;align-items:center;justify-content:center">🥬</div>';

                        var buttonHtml = '';

                        if (item.reviewed) {
                            buttonHtml = '<button type="button" disabled style="width:86px;height:28px;border:none;border-radius:8px;background:#ddd;color:#777;font-size:.66rem;font-weight:700;margin-left:auto;flex-shrink:0;font-family:inherit">리뷰 완료</button>';
                        } else if (item.status === 'DELIVERED') {
                            buttonHtml = '<button type="button" style="width:74px;height:28px;border:none;border-radius:8px;background:var(--green);color:#fff;font-size:.66rem;font-weight:700;cursor:pointer;margin-left:auto;flex-shrink:0;font-family:inherit" onclick="event.stopPropagation();openReviewModal('
                                + productId + ', '
                                + item.orderItemId + ', \''
                                + escapeMpAttr(item.productName || '') + '\')">리뷰 작성</button>';
                        } else {
                            buttonHtml = '<button type="button" disabled style="width:96px;height:28px;border:none;border-radius:8px;background:#ddd;color:#777;font-size:.66rem;font-weight:700;margin-left:auto;flex-shrink:0;font-family:inherit">작성 불가</button>';
                        }

                        html += '<div onclick="goProductDetail(' + productId + ')" title="상품 상세보기" style="display:flex;gap:12px;align-items:center;border-top:1px dashed var(--sand);padding-top:12px;margin-top:12px;width:100%;cursor:pointer">'
                            + thumb
                            + '<div style="flex:1;min-width:0">'
                            + '<div style="font-weight:700;color:var(--dark);font-size:.86rem;text-decoration:underline;text-underline-offset:3px">' + escapeMpHtml(item.productName || '') + '</div>'
                            + '<div style="font-size:.74rem;color:var(--muted);margin-top:4px">'
                            + Number(item.price || 0).toLocaleString() + '원 · ' + escapeMpHtml(item.quantity || 0) + '개 · ' + getOrderStatusText(item.status)
                            + '</div>'
                            + '</div>'
                            + buttonHtml
                            + '</div>';
                    });
                }

                html += '</div>';
            });

            container.innerHTML = html;
        })
        .catch(function() {
            container.innerHTML = '<div class="mp-empty">주문 내역을 불러오지 못했어요 ㅠㅠ</div>';
        });
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
                var productId = p.productId || p.id;

                var thumb = p.thumbnailUrl
                    ? '<img src="' + escapeMpHtml(p.thumbnailUrl) + '" alt="' + escapeMpHtml(p.name || '상품 이미지') + '"/>'
                    : '🥬';

                var priceHtml = p.discountPrice
                    ? '<span class="wl-price">' + Number(p.discountPrice).toLocaleString() + '원</span>'
                    + '<span class="wl-og">' + Number(p.price).toLocaleString() + '원</span>'
                    : '<span class="wl-price">' + Number(p.price).toLocaleString() + '원</span>';

                html += '<div class="wl-card" id="wl-' + productId + '" onclick="goProductDetail(' + productId + ')" title="상품 상세보기" style="cursor:pointer">'
                    + '<div class="wl-thumb">' + thumb + '</div>'
                    + '<div class="wl-body">'
                    + '<div class="wl-cat">' + escapeMpHtml(p.category || '') + '</div>'
                    + '<div class="wl-name" style="text-decoration:underline;text-underline-offset:3px">' + escapeMpHtml(p.name || '') + '</div>'
                    + '<div class="wl-price-row">' + priceHtml + '</div>'
                    + '<button class="wl-remove" onclick="event.stopPropagation();removeWish(' + productId + ')">❤️ 찜 해제</button>'
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
        .catch(function() {
            showToast('오류가 발생했어요 ㅠㅠ');
        });
}

// 내 리뷰 조회
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
                var productId = review.productId;

                var thumb = review.thumbnailUrl
                    ? '<img onclick="event.stopPropagation();goProductDetail(' + productId + ')" src="' + escapeMpHtml(review.thumbnailUrl) + '" alt="상품 이미지" title="상품 상세보기" style="width:58px;height:58px;object-fit:cover;border-radius:12px;cursor:pointer">'
                    : '<div onclick="event.stopPropagation();goProductDetail(' + productId + ')" title="상품 상세보기" style="width:58px;height:58px;border-radius:12px;background:var(--beige2);display:flex;align-items:center;justify-content:center;cursor:pointer">🥬</div>';

                html += '<div onclick="openMyReviewDetail(' + review.reviewId + ')" title="리뷰 상세보기" style="border:1px solid var(--sand);border-radius:14px;padding:16px;margin-bottom:14px;background:#fff;cursor:pointer">'
                    + '<div style="display:flex;gap:12px;align-items:flex-start">'
                    + thumb
                    + '<div style="flex:1;min-width:0">'
                    + '<div onclick="event.stopPropagation();goProductDetail(' + productId + ')" title="상품 상세보기" style="font-weight:800;color:var(--dark);font-size:.9rem;cursor:pointer;text-decoration:underline;text-underline-offset:3px">' + escapeMpHtml(review.productName || '') + '</div>'
                    + '<div style="font-size:.78rem;color:#f5a400;margin:5px 0">' + renderMpStars(review.rating) + '</div>'
                    + '<div style="font-size:.8rem;color:var(--mid);line-height:1.6;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden">' + escapeMpHtml(review.content || '') + '</div>'
                    + '<div style="font-size:.68rem;color:var(--muted);margin-top:8px">' + formatMpDate(review.createdAt) + '</div>'
                    + '</div>'
                    + '</div>'
                    + '</div>';
            });

            container.innerHTML = html;
        })
        .catch(function() {
            container.innerHTML = '<div class="mp-empty">리뷰를 불러오지 못했어요 ㅠㅠ</div>';
        });
}

// 리뷰 작성 모달 열기
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

// 리뷰 상세보기
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
        + '<div onclick="goProductDetail(' + review.productId + ')" style="font-size:.95rem;font-weight:800;color:var(--dark);cursor:pointer;text-decoration:underline;text-underline-offset:3px">' + escapeMpHtml(review.productName || '') + '</div>'
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
        + '<button type="button" class="mp-btn outline" onclick="goProductDetail(' + review.productId + ')">상품으로 이동</button>'
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

// 비밀번호 변경
function changePassword() {
    var token = getToken();

    if (!token) {
        location.href = '/login';
        return;
    }

    var currentPw = document.getElementById('cp-current').value;
    var newPw = document.getElementById('cp-new').value;
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
        body: JSON.stringify({
            currentPassword: currentPw,
            newPassword: newPw,
            confirmPassword: confirmPw
        })
    })
        .then(function(res) {
            return res.json();
        })
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
        .catch(function() {
            showToast('오류가 발생했습니다.');
        });
}

// 회원 탈퇴
function withdrawAccount() {
    var token = getToken();

    if (!token) {
        location.href = '/login';
        return;
    }

    var pw = document.getElementById('wd-pw').value;
    var reason = document.getElementById('wd-reason').value;

    if (!pw) {
        showToast('비밀번호를 입력해주세요.');
        return;
    }

    if (!confirm('정말 탈퇴하시겠어요? 탈퇴 후에는 복구가 어렵습니다.')) return;

    fetch('/mypage/withdraw', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
        body: JSON.stringify({
            password: pw,
            reason: reason
        })
    })
        .then(function(res) {
            return res.json();
        })
        .then(function(data) {
            if (data.message) {
                localStorage.clear();
                sessionStorage.clear();
                showToast('탈퇴가 완료되었어요. 이용해주셔서 감사합니다 🌿');

                setTimeout(function() {
                    location.href = '/';
                }, 2000);
            } else {
                showToast(data.error);
            }
        })
        .catch(function() {
            showToast('오류가 발생했습니다.');
        });
}

function getOrderStatusText(status) {
    switch (status) {
        case 'PAYMENT_COMPLETE':
            return '결제 완료';
        case 'PREPARING':
            return '배송 준비';
        case 'SHIPPING':
            return '배송 중';
        case 'DELIVERED':
            return '배송 완료';
        case 'CANCELLED':
            return '취소';
        case 'REFUNDED':
            return '환불';
        default:
            return status || '';
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