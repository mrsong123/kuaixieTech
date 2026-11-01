#!/usr/bin/env python3
"""Deterministically document explicit Spring RPC compatibility routes; no runtime/API calls."""
import argparse
import csv
import hashlib
import io
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / 'server/src/main/java/com/kuaixietech'
OUT = ROOT / 'docs/api'


def closing(text, start, left='(', right=')'):
    depth, quote, escape, i = 0, None, False, start
    while i < len(text):
        c = text[i]
        if quote:
            if escape:
                escape = False
            elif c == '\\':
                escape = True
            elif c == quote:
                quote = None
        elif text.startswith('//', i):
            end = text.find('\n', i)
            i = len(text) if end < 0 else end
            continue
        elif text.startswith('/*', i):
            end = text.find('*/', i + 2)
            i = len(text) if end < 0 else end + 2
            continue
        elif c in ('"', "'"):
            quote = c
        elif c == left:
            depth += 1
        elif c == right:
            depth -= 1
            if depth == 0:
                return i
        i += 1
    raise ValueError('Unbalanced Java declaration')


def locate_method(path, name):
    text = path.read_text()
    found = re.search(r'\bpublic\s+[\w<>,?\[\].\s]+?\s+' + re.escape(name) + r'\s*\(', text)
    if not found:
        return {'path': str(path.relative_to(ROOT)), 'line': 1}, ''
    end = closing(text, found.end() - 1)
    brace = text.find('{', end)
    if brace < 0:
        return {'path': str(path.relative_to(ROOT)), 'line': text.count('\n', 0, found.start()) + 1}, ''
    body = text[brace + 1:closing(text, brace, '{', '}')]
    return {'path': str(path.relative_to(ROOT)), 'line': text.count('\n', 0, found.start()) + 1}, body


BOUNDARIES = {
    'admin_issue_invoice': 'approve=true 固定拒绝 INVOICE_ISSUER_UNAVAILABLE；目前只允许真实申请和驳回，不能伪开票。',
    'request_invoice': '仅登记开票申请；没有税务服务商的实际开具、验真、交付回执。',
    'get_order_evidence': '管理员取证聚合读取；外部图像/生物识别结论不由此产生。',
    'log_privacy_call': '客户端提交 dialed/connected/unreachable 日志；不是运营商回执或真实通话证明。',
    'issue_face_challenge': '生成应用内留证口令，返回 challengeId/code；不是人脸核验供应商认证。',
    'record_face_verification': '保存截图、设备和挑战留证；记录ID不表示供应商人脸比对通过。',
    'upsert_kyc_doc_with_challenge': '仅保存认证资料与留证口令；人工/正式供应商认证另行进行。',
    'submit_buyer_kyc': '提交人工审核资料，不能当作真实第三方实名通过。',
    'batch12/submit_carrier_kyc': '校验资料与本地验证码后进入人工审核，不含供应商实名核验。',
    'batch12/submit_enterprise_verify': '保存执照/附件并提交人工审核，不含工商/持牌服务商自动核验。',
    'batch12/request_verification_code': '仅生成验证码并入联系方式 outbox，返回OK不等于供应商受理或送达。',
    'batch12/start_order_call': '只有订单通话信令和站内通知；无真实RTC入会、媒体连接或录音验签回执。',
    'batch12/accept_order_call': '仅接受邀请，返回room_id；不是媒体已连通。',
    'batch12/reject_order_call': '仅拒绝/撤回应用信令。',
    'batch12/hangup_order_call': '应用信令关闭；duration_sec不是供应商真实媒体计费时长，未产生录音回执。',
    'batch12/get_or_bind_privacy_number': '仅本地号码池占用/复用，mode=local_pool；没有运营商绑定API闭环。',
    'batch12/screen_chat_content': '本地规则引擎筛查，非第三方内容风控调用。',
    'admin_upsert_recipe': '后台模板CRUD存在，但当前采购取证服务未读取此模板驱动流程。',
    'admin_toggle_recipe': '仅切换后台模板active状态；模板消费链尚未闭环。',
    'admin_toggle_channel': '旧专业配置enabled切换不等于47类耦合器目录均已接业务消费者。',
    'admin_adjust_balance': '带管理员审计的内部钱包调整；不是充值到账、提现打款或银行回单。',
    'create_publish_deposit': '内部钱包冻结发布保证金；无真实外部收款动作。',
    'refund_publish_deposit': '内部钱包解冻发布保证金；无原支付渠道退款动作。',
    'award_pk': '使用后端确认报价与钱包托管；保险选择yes还需实际保险报价，零生产保险适配器时不可投保。',
    'quote_pk_award': '返回人民币托管报价；新版本平台insurancePremium=0，保险由外部机构报价并直接收费。',
    'admin_broadcast': '写入站内通知；返回SENT计数不等于短信/邮件/微信订阅外部送达。',
    'identity/change_password': '依赖已配置IdentityProviderClient；与/auth/login本地BCrypt登录实现不同，不能据本地登录成功推定可用。',
    'identity/reset_own_password_by_code': '依赖已配置IdentityProviderClient及当前用户匹配的真实OTP，非公开任意用户重置。',
    'identity/rebind_contact': '依赖已配置IdentityProviderClient、目标唯一性和真实OTP；无provider时不可承诺换绑完成。',
}

OBJECT_FIELDS = {
    'batch12/start_order_call': ['call_id', 'room_id', 'peer'],
    'batch12/accept_order_call': ['ok', 'room_id'],
    'batch12/hangup_order_call': ['ok', 'duration_sec'],
    'batch12/get_or_bind_privacy_number': ['binding_id', 'virtual_number', 'expire_at', 'reused', 'mode'],
    'batch12/screen_chat_content': ['decision', 'codes'],
    'get_order_evidence': ['order', 'trip', 'faces'],
    'log_privacy_call': ['ok', 'log_id'],
    'open_peer_thread': ['thread_id', 'peer_id', 'peer_nickname', 'reused'],
    'get_peer_profile': ['id', 'nickname', 'avatar_url', 'city', 'performance_score', 'star_avg', 'star_count',
                         'deal_count', 'carrier_verify_status', 'buyer_verify_status', 'carrier_tier',
                         'invite_tier', 'user_role', 'created_at', 'phone_masked'],
    'issue_face_challenge': ['challengeId', 'code'],
    'quote_pk_award': ['goodsAmount', 'serviceFee', 'insurancePremium', 'buyerEscrow', 'vendorDeposit', 'currency', 'pricingVersion'],
}


def permission(key):
    if key.startswith('admin_') or key in {'get_order_evidence', 'batch12/match_join_application'}:
        return '已认证活跃管理员；服务内部校验，不能由请求isAdmin/userId替代'
    if key == 'list_team_members':
        return '本人root或管理员；非授权root返回空数组'
    if key == 'get_peer_profile':
        return '本人或存在订单/会话关系的对象'
    if key.startswith('identity/'):
        return '已认证本人；另需IdentityProviderClient配置'
    return '已认证活跃用户；角色、对象归属与状态按实际service逐项校验'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true', help='Verify generated artifacts without writing files')
    args = parser.parse_args()
    stale = []
    def output(name, value):
        path = OUT / name
        content = value.encode()
        if args.check:
            if not path.exists() or path.read_bytes() != content:
                stale.append(name)
        else:
            path.write_bytes(content)
    OUT.mkdir(parents=True, exist_ok=True)
    files = sorted(JAVA.rglob('*.java'))
    by_name = {p.stem: p for p in files}
    rows, source_files = [], set()
    for path in files:
        text = path.read_text()
        base = re.search(r'@RequestMapping\("(/api/rpc[^"\n]*)"\)', text)
        if not base:
            continue
        source_files.add(path)
        fields = dict((name, kind.split('.')[-1]) for kind, name in re.findall(r'private\s+final\s+([\w.]+)\s+(\w+)\s*;', text))
        pattern = r'@PostMapping\("([^"\n]+)"\)\s*public\s+([\w<>,?\[\].\s]+?)\s+(\w+)\s*\('
        for match in re.finditer(pattern, text):
            params_end = closing(text, match.end() - 1)
            params = text[match.end():params_end]
            brace = text.find('{', params_end)
            body = text[brace + 1:closing(text, brace, '{', '}')]
            route = base[1] + match[1]
            key = route.removeprefix('/api/rpc/')
            row = {'key': key, 'method': 'POST', 'canonicalPath': route, 'clientPath': route.removeprefix('/api'),
                   'controllerMethod': path.stem + '.' + match[3], 'requestDeclaration': params.strip(),
                   'returnJavaType': ' '.join(match[2].split()),
                   'source': {'path': str(path.relative_to(ROOT)), 'line': text.count('\n', 0, match.start()) + 1},
                   'access': permission(key), 'implementationState': 'explicit_http_route_with_local_service; real_acceptance_pending',
                   'delegates': [], 'literalFailureMarkers': [],
                   'failureEnumerationComplete': False,
                   'boundary': BOUNDARIES.get(key, '存在显式controller到service调用；需按状态/权限/持久化与真实回执验收，不能仅凭路由认定业务闭环。')}
            all_bodies = body
            for field, method in re.findall(r'\b(\w+)\.(\w+)\s*\(', body):
                if field not in fields:
                    continue
                kind = fields[field]
                target = by_name.get(kind)
                if target and 'public interface ' in target.read_text():
                    target = by_name.get(kind + 'Impl', target)
                delegate = {'type': kind, 'method': method}
                if target:
                    source_files.add(target)
                    delegate['source'], method_body = locate_method(target, method)
                    all_bodies += '\n' + method_body
                row['delegates'].append(delegate)
            markers = set(re.findall(r'"(ERR:[A-Z0-9_:]+)', all_bodies))
            markers.update('err:' + value for value in re.findall(r'(?:Map\.of\("err",|\berr\()\s*"([A-Z0-9_]+)"', all_bodies))
            row['literalFailureMarkers'] = sorted(markers)
            if key in OBJECT_FIELDS:
                row['successObjectFields'] = OBJECT_FIELDS[key]
                row['successObjectFieldsBasis'] = 'manually_checked_explicit_service_return_or_record; failure_branch_can_return_err'
            rows.append(row)
    rows.sort(key=lambda row: row['canonicalPath'])
    assert len(rows) == len({row['canonicalPath'] for row in rows}), 'Duplicate explicit RPC route'
    sdk_files = sorted((ROOT / 'client').rglob('*.ts')) + sorted((ROOT / 'client').rglob('*.vue'))
    client_literals = {}
    for path in sdk_files:
        if any(part in {'node_modules', 'dist', 'unpackage'} for part in path.parts):
            continue
        text = path.read_text()
        hits = [(m[1], m.start()) for m in re.finditer(r'[\'"`](/rpc/[^\'"`\n]+)[\'"`]', text)]
        hits += [('/rpc/' + m[1].lstrip('/'), m.start()) for m in re.finditer(r'\.rpc\(\s*[\'"`]([^\'"`\n]+)[\'"`]', text)]
        for value, offset in hits:
            if '${' in value or any(char in value for char in '{}()*+?|\\'):
                continue
            client_literals.setdefault(value, []).append({'path': str(path.relative_to(ROOT)), 'line': text.count('\n', 0, offset) + 1})
    paths = {row['clientPath'] for row in rows}
    absent = [{'clientPath': value, 'sources': client_literals[value]} for value in sorted(client_literals) if value not in paths]
    digest = hashlib.sha256()
    for path in sorted(source_files):
        digest.update(str(path.relative_to(ROOT)).encode() + b'\0' + hashlib.sha256(path.read_bytes()).digest())
    result = {'schemaVersion': 1, 'sourceSha256': digest.hexdigest(), 'count': len(rows),
              'scope': 'Explicit @PostMapping under /api/rpc; route presence is not external provider or complete business acceptance.',
              'failureMarkerScope': 'Literal direct controller/service returns only; delegated helper errors and thrown exceptions are not exhaustive.',
              'routes': rows, 'unmappedClientLiteralRoutes': absent}
    output('RPC_INDEX.json', json.dumps(result, ensure_ascii=False, indent=2) + '\n')
    stream = io.StringIO(newline='')
    writer = csv.writer(stream)
    writer.writerow(['key', 'method', 'canonicalPath', 'clientPath', 'returnJavaType', 'access', 'delegates', 'boundary', 'source'])
    for row in rows:
        writer.writerow([row['key'], row['method'], row['canonicalPath'], row['clientPath'], row['returnJavaType'], row['access'],
                         '; '.join(d['type'] + '.' + d['method'] for d in row['delegates']), row['boundary'],
                         row['source']['path'] + ':' + str(row['source']['line'])])
    output('RPC_INDEX.csv', stream.getvalue())
    md = ['# RPC兼容路由逐项索引', '', f'由真实Spring controller确定性提取 **{len(rows)}** 个显式POST路由。存在controller/service调用不等于完整业务或外部供应商验收。', '',
          '请求/响应Java类型详见同目录HTTP_INDEX与JAVA_TYPES；机器表为[RPC_INDEX.json](RPC_INDEX.json)、[RPC_INDEX.csv](RPC_INDEX.csv)。静态失败标记仅列直接方法体字面值，非完整错误码表。', '',
          '| RPC key（均为POST） | 入口/实现 | 响应Java类型 | 权限/边界 |', '| --- | --- | --- | --- |']
    for row in rows:
        source = row['source']; location = '../../' + source['path']
        delegates = ', '.join('`' + d['type'] + '.' + d['method'] + '`' for d in row['delegates'])
        md.append('| `' + row['key'] + '` | [' + source['path'].split('/')[-1] + ':' + str(source['line']) + '](' + location + '#L' + str(source['line']) + ')<br>' + delegates + ' | `' + row['returnJavaType'] + '` | ' + row['access'] + '<br>' + row['boundary'] + ' |')
    md.extend(['', '## 客户端字面RPC路径未映射项', '', '以下只是扫描客户端字面调用后未找到显式controller，不能当作可调用接口；动态路径由主HTTP索引另审。', ''])
    md += ['- `' + item['clientPath'] + '`：' + ', '.join(source['path'] + ':' + str(source['line']) for source in item['sources'][:4]) for item in absent]
    if not absent:
        md.append('未发现。')
    output('RPC_INDEX.md', '\n'.join(md) + '\n')
    print(json.dumps({'rpcRoutes': len(rows), 'unmappedClientLiterals': len(absent), 'sourceSha256': digest.hexdigest(), 'check': args.check, 'stale': stale}))
    if stale:
        raise SystemExit(1)


if __name__ == '__main__':
    main()
