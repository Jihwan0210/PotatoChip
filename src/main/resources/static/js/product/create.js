// 상품명
document.querySelector('[name=name]').addEventListener('input', function() {
    document.querySelector('.preview-name').textContent = this.value || '못난이 감자 3kg';
});

// 원산지
document.querySelector('[name=origin]').addEventListener('input', function() {
    document.querySelector('.preview-origin').textContent = this.value || '강원 홍천 · 감자 농가';
});

// 판매 가격 / 할인 가격
document.querySelector('[name=price]').addEventListener('input', updatePrice);
document.querySelector('[name=discountPrice]').addEventListener('input', updatePrice);

function updatePrice() {
    const price = document.querySelector('[name=price]').value;
    const discount = document.querySelector('[name=discountPrice]').value;
    if (price && discount) {
        const rate = Math.round((1 - discount / price) * 100);
        document.querySelector('.preview-discount').textContent = `-${rate}%`;
        document.querySelector('.preview-main').textContent = Number(discount).toLocaleString() + '원';
    } else if (price) {
        document.querySelector('.preview-discount').textContent = '';
        document.querySelector('.preview-main').textContent = Number(price).toLocaleString() + '원';
    }
}

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
togglePickup(); // 초기 상태 적용

// 주소 검색 + 지도 미리보기
function searchAddress() {
    new daum.Postcode({
        oncomplete: function(data) {

            // 주소 input에 입력
            document.getElementById('pickupAddressInput').value = data.roadAddress;

            // 주소 → 위도경도 변환 후 지도 표시
            const geocoder = new kakao.maps.services.Geocoder();
            geocoder.addressSearch(data.roadAddress, function(result, status) {
                if (status === kakao.maps.services.Status.OK) {
                    const lat = result[0].y;
                    const lng = result[0].x;

                    // hidden input에 위도경도 저장
                    document.getElementById('latitudeInput').value = lat;
                    document.getElementById('longitudeInput').value = lng;

                    // 지도 그리기
                    const container = document.getElementById('kakaoMap');
                    const options = { center: new kakao.maps.LatLng(lat, lng), level: 3 };
                    const map = new kakao.maps.Map(container, options);

                    // 마커 표시
                    const marker = new kakao.maps.Marker({
                        position: new kakao.maps.LatLng(lat, lng)
                    });
                    marker.setMap(map);
                }
            });
        }
    }).open();
}