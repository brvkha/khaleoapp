#!/bin/bash
set -e

echo "=== Docker Containers ==="
docker ps -a

echo ""
echo "=== Backend Container Logs (last 100 lines) ==="
docker logs --tail 100 khaleo-backend 2>&1 || echo "Container not found"

echo ""
echo "=== Listening Ports ==="
netstat -tlnp 2>/dev/null | grep -E ":(8080|80|443)" || echo "No services on key ports"

echo ""
echo "=== Nginx Status ==="
systemctl is-active nginx && echo "Nginx is running" || echo "Nginx is stopped"

echo ""
echo "=== Nginx Error Log (last 50 lines) ==="
tail -50 /var/log/nginx/error.log 2>/dev/null || echo "No error log"

echo ""
echo "=== Nginx Access Log (last 50 lines) ==="
tail -50 /var/log/nginx/access.log 2>/dev/null || echo "No access log"

echo ""
echo "=== Local Backend Health Check ==="
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://127.0.0.1:8080/actuator/health || echo "Connection failed"

echo ""
echo "=== Backend Container Health ==="
docker inspect khaleo-backend 2>/dev/null | grep -A 10 '"Health"' || echo "No health data"

