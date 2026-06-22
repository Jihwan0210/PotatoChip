// 할인 미리보기
function updateDiscountPreview() {
    var price     = Number(document.getElementById('priceInput').value);
    var rate      = Number(document.getElementById('discountRateInput').value);
    var pvMain    = document.querySelector('.pv-main');
    var pvRate    = document.querySelector('.pv-rate');
    var hidden    = document.getElementById('discountPriceHidden');
    var startInput = document.getElementById('discountStartAtInput');
    var endInput   = document.getElementById('discountEndAtInput');
    var hasDiscount = rate > 0 && rate <= 99;

    startInput.disabled = !hasDiscount;
    endInput.disabled   = !hasDiscount;
    var disabledStyle = 'opacity:.4;cursor:not-allowed;background:var(--beige2)';
    var enabledStyle  = '';
    startInput.style.cssText = hasDiscount ? enabledStyle : disabledStyle;
    endInput.style.cssText   = hasDiscount ? enabledStyle : disabledStyle;
    if (!hasDiscount) {
        startInput.value = '';
        endInput.value   = '';
    }

    if (!price) return;
    if (hasDiscount) {
        var discounted = Math.round(price * (1 - rate / 100));
        pvRate.textContent = '-' + rate + '%';
        pvMain.textContent = discounted.toLocaleString() + '원';
        hidden.value       = discounted;
    } else {
        pvRate.textContent = '';
        pvMain.textContent = price.toLocaleString() + '원';
        hidden.value       = '';
    }
}

// 미리보기 칩 업데이트
var catEmoji = { '채소':'🥬','과일':'🍎','곡류':'🌾','버섯':'🍄','뿌리채소':'🥕' };
function updateChips() {
    var cat    = document.querySelector('[name=category]').value;
    var stock  = document.querySelector('[name=stockQuantity]').value;
    var pickup = document.getElementById('pickupSelect').value === 'true';
    document.getElementById('pvChips').innerHTML =
        '<div class="pv-chip">' + (catEmoji[cat] || '🥔') + ' ' + (cat || '채소') + '</div>' +
        '<div class="pv-chip">📦 재고 ' + (stock || 0) + '개</div>' +
        (pickup ? '<div class="pv-chip">🚚 픽업 가능</div>'
                : '<div class="pv-chip" style="color:var(--muted)">픽업 불가</div>');
}

// 픽업 섹션 show/hide
function togglePickup() {
    var show = document.getElementById('pickupSelect').value === 'true';
    document.getElementById('pickupDetail').style.display = show ? 'block' : 'none';
}

// 지도 인스턴스 (한 번만 생성)
var _map = null;
var _marker = null;

function showMap(lat, lng) {
    document.getElementById('mapPlaceholder').style.display = 'none';
    var container = document.getElementById('kakaoMap');
    container.style.display = 'block';
    var latlng = new kakao.maps.LatLng(lat, lng);
    if (!_map) {
        _map    = new kakao.maps.Map(container, { center: latlng, level: 3 });
        _marker = new kakao.maps.Marker({ map: _map, position: latlng });
    } else {
        _map.setCenter(latlng);
        _marker.setPosition(latlng);
    }
}

// 주소 검색
function searchAddress() {
    new daum.Postcode({
        oncomplete: function (data) {
            document.getElementById('pickupAddressInput').value = data.roadAddress;
            var geocoder = new kakao.maps.services.Geocoder();
            geocoder.addressSearch(data.roadAddress, function (result, status) {
                if (status === kakao.maps.services.Status.OK) {
                    var lat = result[0].y;
                    var lng = result[0].x;
                    document.getElementById('latitudeInput').value  = lat;
                    document.getElementById('longitudeInput').value = lng;
                    showMap(lat, lng);
                }
            });
        }
    }).open();
}

// 대표 이미지 변경 미리보기
document.getElementById('thumbFile').addEventListener('change', function (e) {
    var file = e.target.files[0];
    if (!file) return;
    var reader = new FileReader();
    reader.onload = function (ev) {
        var img = document.getElementById('thumbPreview');
        img.src = ev.target.result;
        img.style.display = 'block';
        document.querySelector('.pv-img').childNodes.forEach(function (n) {
            if (n.nodeType === 3) n.textContent = '';
        });
    };
    reader.readAsDataURL(file);
});

// 새 상세 이미지 미리보기 (그리드 통합)
document.getElementById('newImageFiles').addEventListener('change', function () {
    document.querySelectorAll('.img-new-preview').forEach(function (el) { el.remove(); });
    var addBtn = document.querySelector('.img-add-btn');
    var grid   = document.getElementById('imageGrid');
    Array.from(this.files).forEach(function (file) {
        var reader = new FileReader();
        reader.onload = function (e) {
            var div = document.createElement('div');
            div.className = 'img-new-preview';
            var img = document.createElement('img');
            img.src = e.target.result;
            div.appendChild(img);
            grid.insertBefore(div, addBtn);
        };
        reader.readAsDataURL(file);
    });
});

// 즉시 실행
updateDiscountPreview();
togglePickup();

// 기존 좌표가 있으면 지도 바로 초기화
if (typeof initLat !== 'undefined' && initLat && initLng) {
    showMap(initLat, initLng);
}
