#!/bin/bash
# CloudMall 微服务启动脚本 (内存优化版)
# JVM 参数: 堆64-128MB, 元空间最大64MB, 串行GC

BASE_DIR="/home/baidu/webProject"
JVM_OPTS="-Xms48m -Xmx128m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -XX:+HeapDumpOnOutOfMemoryError"
LOG_DIR="$BASE_DIR/logs"
mkdir -p "$LOG_DIR"

echo "========================================="
echo "  CloudMall 微服务启动 (低内存模式)"
echo "  JVM: $JVM_OPTS"
echo "========================================="

start_service() {
    local name=$1
    local jar="$BASE_DIR/$name/target/$name-2.0.0.jar"
    local log="$LOG_DIR/$name.log"

    if [ ! -f "$jar" ]; then
        echo "[SKIP] $name: JAR 不存在"
        return
    fi

    echo -n "[START] $name ... "
    nohup java $JVM_OPTS -jar "$jar" > "$log" 2>&1 &
    echo "PID=$!"
}

# 启动顺序: gateway → user → product → order → marketing
start_service "cloudmall-gateway"
sleep 8

start_service "cloudmall-user"
sleep 8

start_service "cloudmall-product"
sleep 8

start_service "cloudmall-order"
sleep 8

start_service "cloudmall-marketing"

echo ""
echo "========================================="
echo "  所有服务已启动，等待注册到 Nacos..."
echo "  日志目录: $LOG_DIR"
echo "========================================="
