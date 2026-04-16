#!/bin/bash
set -ex

echo "=== Docker Status ==="
docker ps -a

echo ""
echo "=== Backend Logs (last 100 lines) ==="
docker logs --tail 100 khaleo-backend 2>&1 || echo "No container"

echo ""
echo "=== Nginx Status ==="
systemctl status nginx || echo "Nginx not running"

echo ""
echo "=== Nginx Error Log ==="
tail -30 /var/log/nginx/error.log 2>/dev/null || echo "No error log"

echo ""
echo "=== Nginx Access Log (recent errors) ==="
tail -30 /var/log/nginx/access.log 2>/dev/null | grep "502\|500" || echo "No recent errors in access log"

echo ""
echo "=== Listening Ports ==="
netstat -tlnp 2>/dev/null | grep -E ":(80|443|8080)" || ss -tlnp | grep -E ":(80|443|8080)"

echo ""
echo "=== Health Check ==="
curl -v http://127.0.0.1:8080/actuator/health 2>&1 || echo "Health check failed"

echo ""
echo "=== Nginx Config ==="
cat /etc/nginx/conf.d/khaleo-api.conf | head -50

