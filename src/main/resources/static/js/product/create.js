// 상품명
document.querySelector('[name=name]').addEventListener('input', function() {
    document.querySelector('.preview-name').textContent = this.value || '못난이 감자 3kg';
});

// 원산지
document.querySelector('[name=origin]').addEventListener('input', function() {
    document.querySelector('.preview-origin').textContent = this.value || '강원 홍천 · 감자 농가';
});

// 판매 가격 / 할인 가격
document.querySelector('[name=price]').addEventListener('input', updateDiscountPreview);
document.getElementById('discountRateInput').addEventListener('input', updateDiscountPreview);

// 카테고리
document.querySelector('[name=category]').addEventListener('change', function() {
    document.querySelector('.preview-chip').textContent = '🥔 ' + (this.value || '채소');
});

// 재고
document.querySelector('[name=stockQuantity]').addEventListener('input', function() {
    document.querySelectorAll('.preview-chip')[1].textContent = '📦 재고 ' + (this.value || '0') + '개';
});

// 픽업
document.querySelector('[name=isPickupAvailable]').addEventListener('change', function() {
    document.querySelectorAll('.preview-chip')[2].textContent =
        this.value === 'true' ? '🚚 픽업 가능' : '🚚 픽업 불가';
});

// 이미지 미리보기
document.querySelector('[name=thumbnailFile]').addEventListener('change', function() {
    const file = this.files[0];
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

// 설명
document.querySelector('[name=description]').addEventListener('input', function() {
    document.querySelector('.preview-desc').textContent = this.value || '설명란을 입력해주세요.';
});

// 픽업 가능 여부 토글
const pickupSelect = document.getElementById('pickupSelect');
const pickupDetail = document.getElementById('pickupDetail');

function togglePickup() {
    if (pickupSelect.value === 'true') {
        pickupDetail.style.display = 'block';
    } else {
        pickupDetail.style.display = 'none';
    }
}

pickupSelect.addEventListener('change', togglePickup);
togglePickup();

// 할인 날짜 활성화/비활성화
const discountRateEl = document.getElementById('discountRateInput');
const discountStartAt = document.querySelector('[name=discountStartAt]');
const discountEndAt = document.querySelector('[name=discountEndAt]');

function toggleDiscountDates() {
    const hasDiscount = discountRateEl.value.trim() !== '' && Number(discountRateEl.value) > 0;
    discountStartAt.disabled = !hasDiscount;
    discountEndAt.disabled = !hasDiscount;
    discountStartAt.style.opacity = hasDiscount ? '1' : '0.5';
    discountEndAt.style.opacity = hasDiscount ? '1' : '0.5';
    document.getElementById('discountDateHint').style.display = hasDiscount ? 'none' : 'block';
    if (!hasDiscount) {
        discountStartAt.value = '';
        discountEndAt.value = '';
    }
}

discountRateEl.addEventListener('input', toggleDiscountDates);
toggleDiscountDates();

// 주소 검색 + 지도 미리보기
function searchAddress() {
    new daum.Postcode({
        oncomplete: function(data) {
            document.getElementById('pickupAddressInput').value = data.roadAddress;
            const geocoder = new kakao.maps.services.Geocoder();
            geocoder.addressSearch(data.roadAddress, function(result, status) {
                if (status === kakao.maps.services.Status.OK) {
                    const lat = result[0].y;
                    const lng = result[0].x;
                    document.getElementById('latitudeInput').value = lat;
                    document.getElementById('longitudeInput').value = lng;
                    const container = document.getElementById('kakaoMap');
                    const options = { center: new kakao.maps.LatLng(lat, lng), level: 3 };
                    const map = new kakao.maps.Map(container, options);
                    const marker = new kakao.maps.Marker({
                        position: new kakao.maps.LatLng(lat, lng)
                    });
                    marker.setMap(map);
                }
            });
        }
    }).open();
}