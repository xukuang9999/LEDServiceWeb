#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
RESOURCES_DIR="${REPO_ROOT}/zhglxt-web/src/main/resources"
LOCAL_TEMPLATES_DIR="${RESOURCES_DIR}/templates/cms"
LOCAL_STATIC_DIR="${RESOURCES_DIR}/static/cms"
LOCAL_TMP_DIR="${REPO_ROOT}/.tmp"

HOST=""
USER_NAME="root"
PORT="22"
REMOTE_ROOT="/opt/tomcat/webapps/zhglxt/WEB-INF/classes"
REMOTE_UPLOAD_DIR="/root"
BACKUP_DIR="/root/backups"
PASSWORD_ENV=""
IDENTITY_FILE=""
NO_BACKUP=0
KEEP_ARCHIVE=0
DRY_RUN=0

declare -a VERIFY_URLS=()

RUN_ID="$(date +%Y%m%d-%H%M%S)-$$"
ARCHIVE_NAME="cms-deploy-${RUN_ID}.tgz"
REMOTE_HELPER_NAME="cms-deploy-${RUN_ID}-remote.sh"
LOCAL_ARCHIVE_PATH="${LOCAL_TMP_DIR}/${ARCHIVE_NAME}"
LOCAL_REMOTE_HELPER_PATH="${LOCAL_TMP_DIR}/${REMOTE_HELPER_NAME}"
REMOTE_ARCHIVE_PATH=""
REMOTE_HELPER_PATH=""
BACKUP_RESULT_PATH=""
PASSWORD_VALUE=""
AUTH_MODE="plain"

declare -a TAR_CREATE_FLAGS=()

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $*${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $*${NC}"
}

die() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $*${NC}" >&2
    exit 1
}

usage() {
    cat <<'EOF'
Usage:
  scripts/deploy-cms-assets.sh --host <host> [options]

Required:
  --host <host>                Remote host or IP.

Optional:
  --user <user>                SSH user. Default: root
  --port <port>                SSH port. Default: 22
  --remote-root <path>         Exploded webapp classes root.
                               Default: /opt/tomcat/webapps/zhglxt/WEB-INF/classes
  --upload-dir <path>          Remote temp upload directory. Default: /root
  --backup-dir <path>          Remote backup directory. Default: /root/backups
  --password-env <ENV_NAME>    Read SSH password from environment variable.
  --identity-file <path>       SSH private key path.
  --verify-url <url>           Verify a public URL after deployment. Repeatable.
  --no-backup                  Skip remote backup creation.
  --keep-archive               Keep the uploaded archive on the remote host.
  --dry-run                    Build the archive and print remote actions only.
  -h, --help                   Show this help.

Examples:
  export CMS_DEPLOY_PASSWORD='your-password'
  scripts/deploy-cms-assets.sh \
    --host 47.237.142.65 \
    --user root \
    --port 22 \
    --password-env CMS_DEPLOY_PASSWORD \
    --verify-url https://www.ledservice.com.au/zhglxt/cms/index.html \
    --verify-url https://www.ledservice.com.au/zhglxt/cms/projects.html

  scripts/deploy-cms-assets.sh \
    --host example.com \
    --user deploy \
    --identity-file ~/.ssh/id_ed25519
EOF
}

require_cmd() {
    command -v "$1" >/dev/null 2>&1 || die "Missing required command: $1"
}

supports_tar_flag() {
    local flag="$1"
    tar "${flag}" -cf /dev/null "${REPO_ROOT}/README.md" >/dev/null 2>&1
}

quote_for_sh() {
    printf "'%s'" "$(printf '%s' "$1" | sed "s/'/'\\\\''/g")"
}

cleanup_local() {
    rm -f "${LOCAL_REMOTE_HELPER_PATH}"
    if [[ ${KEEP_ARCHIVE} -ne 1 ]]; then
        rm -f "${LOCAL_ARCHIVE_PATH}"
    fi
}

trap cleanup_local EXIT

parse_args() {
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
            --remote-root)
                REMOTE_ROOT="${2:-}"
                shift 2
                ;;
            --upload-dir)
                REMOTE_UPLOAD_DIR="${2:-}"
                shift 2
                ;;
            --backup-dir)
                BACKUP_DIR="${2:-}"
                shift 2
                ;;
            --password-env)
                PASSWORD_ENV="${2:-}"
                shift 2
                ;;
            --identity-file)
                IDENTITY_FILE="${2:-}"
                shift 2
                ;;
            --verify-url)
                VERIFY_URLS+=("${2:-}")
                shift 2
                ;;
            --no-backup)
                NO_BACKUP=1
                shift
                ;;
            --keep-archive)
                KEEP_ARCHIVE=1
                shift
                ;;
            --dry-run)
                DRY_RUN=1
                shift
                ;;
            -h|--help)
                usage
                exit 0
                ;;
            *)
                die "Unknown argument: $1"
                ;;
        esac
    done
}

resolve_auth() {
    if [[ -n "${IDENTITY_FILE}" ]]; then
        [[ -f "${IDENTITY_FILE}" ]] || die "Identity file not found: ${IDENTITY_FILE}"
        AUTH_MODE="key"
        return
    fi

    if [[ -n "${PASSWORD_ENV}" ]]; then
        PASSWORD_VALUE="${!PASSWORD_ENV-}"
        [[ -n "${PASSWORD_VALUE}" ]] || die "Environment variable ${PASSWORD_ENV} is empty or unset"

        if command -v sshpass >/dev/null 2>&1; then
            AUTH_MODE="sshpass"
        elif command -v expect >/dev/null 2>&1; then
            AUTH_MODE="expect"
        else
            die "Password auth requested but neither sshpass nor expect is installed"
        fi
        return
    fi

    AUTH_MODE="plain"
}

check_prerequisites() {
    require_cmd tar
    require_cmd ssh
    require_cmd scp

    [[ -d "${LOCAL_TEMPLATES_DIR}" ]] || die "Missing local templates directory: ${LOCAL_TEMPLATES_DIR}"
    [[ -d "${LOCAL_STATIC_DIR}" ]] || die "Missing local static directory: ${LOCAL_STATIC_DIR}"
    [[ -n "${HOST}" ]] || die "--host is required"

    if [[ ${#VERIFY_URLS[@]} -gt 0 ]]; then
        require_cmd curl
    fi

    mkdir -p "${LOCAL_TMP_DIR}"

    TAR_CREATE_FLAGS=(--exclude='.DS_Store' --exclude='._*')
    for flag in --no-mac-metadata --disable-copyfile --no-xattrs --no-acls --no-fflags; do
        if supports_tar_flag "${flag}"; then
            TAR_CREATE_FLAGS+=("${flag}")
        fi
    done

    REMOTE_ARCHIVE_PATH="${REMOTE_UPLOAD_DIR%/}/${ARCHIVE_NAME}"
    REMOTE_HELPER_PATH="${REMOTE_UPLOAD_DIR%/}/${REMOTE_HELPER_NAME}"

    resolve_auth
}

ssh_run_expect() {
    local remote_cmd="$1"
    CMS_DEPLOY_PASSWORD_VALUE="${PASSWORD_VALUE}" \
    CMS_DEPLOY_PORT="${PORT}" \
    CMS_DEPLOY_TARGET="${USER_NAME}@${HOST}" \
    CMS_DEPLOY_REMOTE_CMD="${remote_cmd}" \
    expect <<'EOF'
set timeout -1
spawn ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -p $env(CMS_DEPLOY_PORT) $env(CMS_DEPLOY_TARGET) $env(CMS_DEPLOY_REMOTE_CMD)
expect {
    -re ".*assword:.*" {
        send -- "$env(CMS_DEPLOY_PASSWORD_VALUE)\r"
        exp_continue
    }
    eof {
        catch wait result
        exit [lindex $result 3]
    }
}
EOF
}

scp_put_expect() {
    local local_path="$1"
    local remote_path="$2"
    CMS_DEPLOY_PASSWORD_VALUE="${PASSWORD_VALUE}" \
    CMS_DEPLOY_PORT="${PORT}" \
    CMS_DEPLOY_LOCAL_PATH="${local_path}" \
    CMS_DEPLOY_REMOTE_TARGET="${USER_NAME}@${HOST}:${remote_path}" \
    expect <<'EOF'
set timeout -1
spawn scp -O -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -P $env(CMS_DEPLOY_PORT) $env(CMS_DEPLOY_LOCAL_PATH) $env(CMS_DEPLOY_REMOTE_TARGET)
expect {
    -re ".*assword:.*" {
        send -- "$env(CMS_DEPLOY_PASSWORD_VALUE)\r"
        exp_continue
    }
    eof {
        catch wait result
        exit [lindex $result 3]
    }
}
EOF
}

ssh_run() {
    local remote_cmd="$1"

    if [[ ${DRY_RUN} -eq 1 ]]; then
        log "[dry-run] ssh ${USER_NAME}@${HOST}:${PORT} ${remote_cmd}"
        return 0
    fi

    case "${AUTH_MODE}" in
        key)
            ssh -i "${IDENTITY_FILE}" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -p "${PORT}" "${USER_NAME}@${HOST}" "${remote_cmd}"
            ;;
        sshpass)
            SSHPASS="${PASSWORD_VALUE}" sshpass -e ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -p "${PORT}" "${USER_NAME}@${HOST}" "${remote_cmd}"
            ;;
        expect)
            ssh_run_expect "${remote_cmd}"
            ;;
        plain)
            ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -p "${PORT}" "${USER_NAME}@${HOST}" "${remote_cmd}"
            ;;
        *)
            die "Unsupported auth mode: ${AUTH_MODE}"
            ;;
    esac
}

scp_put() {
    local local_path="$1"
    local remote_path="$2"

    if [[ ${DRY_RUN} -eq 1 ]]; then
        log "[dry-run] scp ${local_path} -> ${USER_NAME}@${HOST}:${remote_path}"
        return 0
    fi

    case "${AUTH_MODE}" in
        key)
            scp -O -i "${IDENTITY_FILE}" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -P "${PORT}" "${local_path}" "${USER_NAME}@${HOST}:${remote_path}"
            ;;
        sshpass)
            SSHPASS="${PASSWORD_VALUE}" sshpass -e scp -O -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -P "${PORT}" "${local_path}" "${USER_NAME}@${HOST}:${remote_path}"
            ;;
        expect)
            scp_put_expect "${local_path}" "${remote_path}"
            ;;
        plain)
            scp -O -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -P "${PORT}" "${local_path}" "${USER_NAME}@${HOST}:${remote_path}"
            ;;
        *)
            die "Unsupported auth mode: ${AUTH_MODE}"
            ;;
    esac
}

make_archive() {
    log "Creating CMS archive: ${LOCAL_ARCHIVE_PATH}"
    (
        cd "${RESOURCES_DIR}"
        COPYFILE_DISABLE=1 COPY_EXTENDED_ATTRIBUTES_DISABLE=1 \
        tar "${TAR_CREATE_FLAGS[@]}" -czf "${LOCAL_ARCHIVE_PATH}" templates/cms static/cms
    )
}

create_remote_helper() {
    local q_remote_root q_backup_dir q_remote_archive q_keep_archive q_no_backup
    q_remote_root="$(quote_for_sh "${REMOTE_ROOT}")"
    q_backup_dir="$(quote_for_sh "${BACKUP_DIR}")"
    q_remote_archive="$(quote_for_sh "${REMOTE_ARCHIVE_PATH}")"
    q_keep_archive="$(quote_for_sh "${KEEP_ARCHIVE}")"
    q_no_backup="$(quote_for_sh "${NO_BACKUP}")"

    cat > "${LOCAL_REMOTE_HELPER_PATH}" <<EOF
#!/usr/bin/env bash
set -euo pipefail

REMOTE_ROOT=${q_remote_root}
BACKUP_DIR=${q_backup_dir}
REMOTE_ARCHIVE=${q_remote_archive}
KEEP_ARCHIVE=${q_keep_archive}
NO_BACKUP=${q_no_backup}
STAGE_DIR="/tmp/cms-deploy-stage-\$(date +%Y%m%d-%H%M%S)-\$\$"

log() {
    printf '[%s] %s\n' "\$(date +'%Y-%m-%d %H:%M:%S')" "\$*"
}

cleanup() {
    rm -rf "\${STAGE_DIR}"
    if [[ "\${KEEP_ARCHIVE}" != "1" ]]; then
        rm -f "\${REMOTE_ARCHIVE}"
    fi
}

trap cleanup EXIT

mkdir -p "\${STAGE_DIR}" "\${REMOTE_ROOT}/templates" "\${REMOTE_ROOT}/static"

if [[ "\${NO_BACKUP}" != "1" ]]; then
    mkdir -p "\${BACKUP_DIR}"
    backup_file="\${BACKUP_DIR}/zhglxt-cms-backup-\$(date +%Y%m%d-%H%M%S).tgz"
    backup_items=()
    [[ -d "\${REMOTE_ROOT}/templates/cms" ]] && backup_items+=(templates/cms)
    [[ -d "\${REMOTE_ROOT}/static/cms" ]] && backup_items+=(static/cms)
    if [[ "\${#backup_items[@]}" -gt 0 ]]; then
        tar -C "\${REMOTE_ROOT}" -czf "\${backup_file}" "\${backup_items[@]}"
        log "Backup created: \${backup_file}"
    else
        log "Backup skipped because no existing CMS directories were found"
    fi
fi

template_owner="\$(stat -c '%U:%G' "\${REMOTE_ROOT}/templates/cms" 2>/dev/null || stat -c '%U:%G' "\${REMOTE_ROOT}/templates" 2>/dev/null || true)"
static_owner="\$(stat -c '%U:%G' "\${REMOTE_ROOT}/static/cms" 2>/dev/null || stat -c '%U:%G' "\${REMOTE_ROOT}/static" 2>/dev/null || true)"

tar -C "\${STAGE_DIR}" -xzf "\${REMOTE_ARCHIVE}"
[[ -d "\${STAGE_DIR}/templates/cms" ]] || { echo "Missing templates/cms in archive" >&2; exit 1; }
[[ -d "\${STAGE_DIR}/static/cms" ]] || { echo "Missing static/cms in archive" >&2; exit 1; }

rm -rf "\${REMOTE_ROOT}/templates/cms" "\${REMOTE_ROOT}/static/cms"
mv "\${STAGE_DIR}/templates/cms" "\${REMOTE_ROOT}/templates/cms"
mv "\${STAGE_DIR}/static/cms" "\${REMOTE_ROOT}/static/cms"

if [[ -n "\${template_owner}" ]]; then
    chown -R "\${template_owner}" "\${REMOTE_ROOT}/templates/cms"
fi
if [[ -n "\${static_owner}" ]]; then
    chown -R "\${static_owner}" "\${REMOTE_ROOT}/static/cms"
fi

find "\${REMOTE_ROOT}/templates/cms" -type d -exec chmod 755 {} +
find "\${REMOTE_ROOT}/templates/cms" -type f -exec chmod 644 {} +
find "\${REMOTE_ROOT}/static/cms" -type d -exec chmod 755 {} +
find "\${REMOTE_ROOT}/static/cms" -type f -exec chmod 644 {} +

log "Deployment complete"
log "Templates: \${REMOTE_ROOT}/templates/cms"
log "Static assets: \${REMOTE_ROOT}/static/cms"
EOF
}

deploy() {
    local q_upload_dir q_remote_helper
    q_upload_dir="$(quote_for_sh "${REMOTE_UPLOAD_DIR}")"
    q_remote_helper="$(quote_for_sh "${REMOTE_HELPER_PATH}")"

    log "Ensuring remote upload directory exists"
    ssh_run "mkdir -p ${q_upload_dir}"

    log "Uploading archive"
    scp_put "${LOCAL_ARCHIVE_PATH}" "${REMOTE_ARCHIVE_PATH}"

    log "Uploading remote deploy helper"
    scp_put "${LOCAL_REMOTE_HELPER_PATH}" "${REMOTE_HELPER_PATH}"

    log "Running remote deployment"
    ssh_run "bash ${q_remote_helper}"

    if [[ ${DRY_RUN} -ne 1 ]]; then
        ssh_run "rm -f ${q_remote_helper}"
    fi
}

verify_urls() {
    local url
    for url in "${VERIFY_URLS[@]}"; do
        log "Verifying ${url}"
        curl -fsSIL "${url}" >/dev/null
    done
}

main() {
    parse_args "$@"
    check_prerequisites
    make_archive
    create_remote_helper
    deploy
    verify_urls

    log "CMS deployment script finished successfully"
    log "Host: ${HOST}"
    log "Remote root: ${REMOTE_ROOT}"
    if [[ ${NO_BACKUP} -eq 0 ]]; then
        log "Backup directory: ${BACKUP_DIR}"
    fi
}

main "$@"
