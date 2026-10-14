#!/usr/bin/env bash
# EC2 배포 스크립트. 새 이미지가 응답하지 않으면 직전에 실행 중이던 이미지로 되돌린다.
# 사용: deploy.sh <이미지>
set -u

IMAGE="$1"
NAME=potatochip
ENV_FILE="${ENV_FILE:-/home/ubuntu/.env}"
UPLOAD_DIR="${UPLOAD_DIR:-/home/ubuntu/uploads}"
TRIES="${HEALTH_TRIES:-30}"
INTERVAL="${HEALTH_INTERVAL:-10}"

# .env 가 없으면 기존 컨테이너를 건드리지 않고 중단
test -f "$ENV_FILE" || { echo "::error::$ENV_FILE 없음"; exit 1; }

run() {
  docker rm -f "$NAME" >/dev/null 2>&1 || true
  docker run -d --name "$NAME" --restart unless-stopped \
    -p 127.0.0.1:8080:8080 \
    --env-file "$ENV_FILE" \
    -v "$UPLOAD_DIR":/app/uploads \
    "$1" >/dev/null
}

healthy() {
  for i in $(seq 1 "$TRIES"); do
    code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 3 http://127.0.0.1:8080/ || true)
    case "$code" in 2*|3*) echo "기동 확인 (HTTP $code)"; return 0 ;; esac
    sleep "$INTERVAL"
  done
  return 1
}

# 롤백 대상: 지금 실행 중인 컨테이너의 이미지 ID. latest 태그가 새 이미지로 옮겨가도 ID는 남는다.
PREV=$(docker inspect --format '{{.Image}}' "$NAME" 2>/dev/null || true)

if [ "${SKIP_PULL:-}" != 1 ]; then
  docker pull "$IMAGE" || exit 1
fi
NEW=$(docker image inspect --format '{{.Id}}' "$IMAGE")

echo "새 버전 기동 대기..."
run "$NEW"
if healthy; then
  # 성공한 뒤에만 이전 이미지를 정리한다. 먼저 지우면 되돌릴 이미지가 없어진다.
  docker image prune -f >/dev/null
  exit 0
fi

echo "::error::새 버전이 기동하지 못했습니다"
docker logs --tail 50 "$NAME"

if [ -n "$PREV" ] && [ "$PREV" != "$NEW" ]; then
  echo "이전 버전으로 되돌립니다: $PREV"
  run "$PREV"
  if healthy; then
    echo "::error::이전 버전으로 복구했습니다. 새 버전 배포는 실패입니다"
  else
    echo "::error::이전 버전도 기동하지 못했습니다"
  fi
fi
exit 1
