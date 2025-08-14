#!/bin/sh

# Redis 노드 리스트 (공백 구분)
NODES="host.docker.internal:7001 host.docker.internal:7002 host.docker.internal:7003 host.docker.internal:7004 host.docker.internal:7005 host.docker.internal:7006"

# 기본 데이터 디렉토리
BASE_DIR="/var/lib/redis"

# Redis 서버 시작 (각 노드별 디렉토리)
for NODE in $NODES; do
  PORT=$(echo $NODE | cut -d: -f2)
  NODE_DIR="$BASE_DIR/$PORT"

  # 데이터 디렉토리 생성 및 권한 설정
  mkdir -p $NODE_DIR
  chown redis:redis $NODE_DIR

  # Redis 서버 시작
  redis-server /etc/redis.conf \
    --port $PORT \
    --dir $NODE_DIR \
    --appendonly yes &
done

# 서버들이 완전히 시작될 때까지 잠시 대기
sleep 5

# 클러스터 생성: 전체 6노드 + 3마스터 + 3슬레이브
redis-cli --cluster create \
  host.docker.internal:7001 \
  host.docker.internal:7002 \
  host.docker.internal:7003 \
  host.docker.internal:7004 \
  host.docker.internal:7005 \
  host.docker.internal:7006 \
  --cluster-replicas 1 --cluster-yes

echo "Redis 6노드 클러스터 설정 완료."

# 스크립트가 종료되지 않도록 유지
tail -f /dev/null
