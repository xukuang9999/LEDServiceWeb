#!/usr/bin/env bash

set -euo pipefail

HOST=""
USER_NAME="root"
PORT="22"
PASSWORD_ENV=""

usage() {
    cat <<'EOF'
Usage:
  scripts/update-domain-routing.sh --host <host> --password-env <ENV_NAME> [options]

Options:
  --host <host>          Remote host or IP.
  --user <user>          SSH user. Default: root
  --port <port>          SSH port. Default: 22
  --password-env <name>  Environment variable holding the SSH password.
  -h, --help             Show this help.
EOF
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --host)
            HOST="${2:-}"
            shift 2
            ;;
        --user)
            USER_NAME="${2:-}"
            shift 2
            ;;
        --port)
            PORT="${2:-}"
            shift 2
            ;;
        --password-env)
            PASSWORD_ENV="${2:-}"
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo "Unknown argument: $1" >&2
            usage >&2
            exit 1
            ;;
    esac
done

[[ -n "${HOST}" ]] || { echo "--host is required" >&2; exit 1; }
[[ -n "${PASSWORD_ENV}" ]] || { echo "--password-env is required" >&2; exit 1; }
[[ -n "${!PASSWORD_ENV-}" ]] || { echo "Environment variable ${PASSWORD_ENV} is empty or unset" >&2; exit 1; }
command -v expect >/dev/null 2>&1 || { echo "expect is required" >&2; exit 1; }
command -v base64 >/dev/null 2>&1 || { echo "base64 is required" >&2; exit 1; }

read -r -d '' REMOTE_SCRIPT <<'EOS' || true
set -euo pipefail

conf=/usr/local/nginx/conf/nginx.conf
backup=/root/backups/nginx.conf-ledservice-domain-$(date +%Y%m%d-%H%M%S).bak

if grep -q 'location = /projects.html' "$conf"; then
    nginx -t >/tmp/nginx-test.out 2>&1
    nginx -s reload
    printf 'backup=unchanged\n'
    printf 'reload=ok\n'
    exit 0
fi

cp "$conf" "$backup"

perl -0pi -e 's~\n\s*# 根路径重定向\n\s*location = / \{\n\s*return 301 https://\$server_name/zhglxt/cms/index\.html;\n\s*\}\n\s*\n\s*# 管理后台入口~

    # 主域名公开页面
    location = / {
        proxy_pass http://zhglxt_backend/zhglxt/cms/index.html;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Host \$host;
        proxy_set_header X-Forwarded-Port \$server_port;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        expires 1h;
        add_header Cache-Control "public";
    }

    location = /index.html {
        return 301 https://\$server_name/;
    }

    location = /projects.html {
        proxy_pass http://zhglxt_backend/zhglxt/cms/projects.html;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Host \$host;
        proxy_set_header X-Forwarded-Port \$server_port;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        expires 1h;
        add_header Cache-Control "public";
    }

    location = /rental-service.html {
        proxy_pass http://zhglxt_backend/zhglxt/cms/rental-service.html;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Host \$host;
        proxy_set_header X-Forwarded-Port \$server_port;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        expires 1h;
        add_header Cache-Control "public";
    }

    location = /about.html {
        proxy_pass http://zhglxt_backend/zhglxt/cms/about.html;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Host \$host;
        proxy_set_header X-Forwarded-Port \$server_port;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        expires 1h;
        add_header Cache-Control "public";
    }

    location = /contact.html {
        proxy_pass http://zhglxt_backend/zhglxt/cms/contact.html;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Host \$host;
        proxy_set_header X-Forwarded-Port \$server_port;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        expires 1h;
        add_header Cache-Control "public";
    }

    # 主域名公开抓取入口
    location = /robots.txt {
        proxy_pass http://zhglxt_backend/zhglxt/robots.txt;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Host \$host;
        proxy_set_header X-Forwarded-Port \$server_port;
        add_header Cache-Control "no-cache, no-store, must-revalidate";
        add_header Pragma "no-cache";
        add_header Expires "0";
    }

    location = /llms.txt {
        proxy_pass http://zhglxt_backend/zhglxt/llms.txt;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Host \$host;
        proxy_set_header X-Forwarded-Port \$server_port;
        add_header Cache-Control "no-cache, no-store, must-revalidate";
        add_header Pragma "no-cache";
        add_header Expires "0";
    }

    location = /llms-full.txt {
        proxy_pass http://zhglxt_backend/zhglxt/llms-full.txt;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Host \$host;
        proxy_set_header X-Forwarded-Port \$server_port;
        add_header Cache-Control "no-cache, no-store, must-revalidate";
        add_header Pragma "no-cache";
        add_header Expires "0";
    }

    location = /sitemap.xml {
        proxy_pass http://zhglxt_backend/zhglxt/sitemap.xml;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Host \$host;
        proxy_set_header X-Forwarded-Port \$server_port;
        add_header Cache-Control "no-cache, no-store, must-revalidate";
        add_header Pragma "no-cache";
        add_header Expires "0";
    }

    # 管理后台入口~s' "$conf"

if ! grep -q 'location = /projects.html' "$conf"; then
    cp "$backup" "$conf"
    echo "Failed to inject domain routing block" >&2
    exit 1
fi

nginx -t >/tmp/nginx-test.out 2>&1 || {
    cat /tmp/nginx-test.out
    cp "$backup" "$conf"
    exit 1
}

nginx -s reload
printf 'backup=%s\n' "$backup"
printf 'reload=ok\n'
EOS

REMOTE_SCRIPT_B64="$(printf '%s' "${REMOTE_SCRIPT}" | base64 | tr -d '\n')"

export DOMAIN_ROUTING_PASSWORD="${!PASSWORD_ENV}"
export DOMAIN_ROUTING_B64="${REMOTE_SCRIPT_B64}"
export DOMAIN_ROUTING_HOST="${HOST}"
export DOMAIN_ROUTING_PORT="${PORT}"
export DOMAIN_ROUTING_TARGET="${USER_NAME}@${HOST}"

expect <<'EOF'
set timeout -1
spawn ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -p $env(DOMAIN_ROUTING_PORT) $env(DOMAIN_ROUTING_TARGET) "echo $env(DOMAIN_ROUTING_B64) | base64 -d | bash"
expect {
    -re ".*assword:.*" {
        send -- "$env(DOMAIN_ROUTING_PASSWORD)\r"
        exp_continue
    }
    eof {
        catch wait result
        exit [lindex $result 3]
    }
}
EOF
