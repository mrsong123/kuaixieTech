#!/usr/bin/env python3
"""Run real HTTP/SQL smoke checks ONLY against the explicitly confirmed disposable local audit DB.

No provider calls, real payment, real OTP delivery, or production credentials. Secrets are generated
in process memory, passed through environment variables, and never written to the receipt.
The Docker container and imported schema must already exist; this script never imports migrations.
"""
import argparse
import base64
import datetime as dt
import hashlib
import json
import os
from pathlib import Path
import re
import secrets
import signal
import socket
import subprocess
import tempfile
import time
import urllib.error
import urllib.request
import uuid

ROOT=Path(__file__).resolve().parents[2]

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--confirm-disposable-database',action='store_true',required=True)
    parser.add_argument('--container',default='kuaixie-audit-mysql-20261101')
    parser.add_argument('--db-port',type=int,default=13307)
    parser.add_argument('--port',type=int,default=18080)
    parser.add_argument('--maven',default='/workspace/tooling/apache-maven-3.9.11/bin/mvn')
    parser.add_argument('--java-home',default=None)
    parser.add_argument('--settings',default='/workspace/tooling/maven-settings.xml')
    parser.add_argument('--repository',default='/workspace/tooling/m2')
    parser.add_argument('--result',default='/tmp/kuaixie-runtime-smoke.json')
    parser.add_argument('--server-log',default='/tmp/kuaixie-runtime-smoke-server.log')
    args=parser.parse_args()
    if not args.container.startswith('kuaixie-audit-mysql-') or args.db_port!=13307:
        raise SystemExit('Refusing any database other than the designated disposable local audit container/port.')
    if not 1024<args.port<65536:raise SystemExit('Invalid local test HTTP port')
    with socket.socket() as s:
        if s.connect_ex(('127.0.0.1',args.port))==0:raise SystemExit('Test HTTP port already in use; do not stop an unrelated service.')
    docker_env=os.environ.copy()
    for key in ['DOCKER_HOST','DOCKER_CONTEXT','DOCKER_TLS','DOCKER_TLS_VERIFY','DOCKER_CERT_PATH']:
        docker_env.pop(key,None)
    docker=['docker','--host=unix:///var/run/docker.sock']
    def dock(*command,input=None):
        proc=subprocess.run([*docker,*command],input=input,text=True,capture_output=True,env=docker_env)
        if proc.returncode:
            match=re.search(r'ERROR\s+(\d+)\s+\(([A-Z0-9]+)\)',proc.stderr)
            raise RuntimeError('Disposable SQL/docker operation failed'+(' '+match[0] if match else ' (inspect local logs; values redacted)'))
        return proc.stdout.strip()
    image=dock('inspect','--format','{{.Config.Image}}',args.container)
    purpose=dock('inspect','--format','{{index .Config.Labels "kuaixie.purpose"}}',args.container)
    binding=dock('port',args.container,'3306/tcp')
    if image!='mysql:8.4' or purpose!='disposable-runtime-audit' or binding!='127.0.0.1:13307':
        raise SystemExit('Disposable database purpose label/image/local binding did not match the audit target.')
    def sql(statement):
        return dock('exec','-i',args.container,'mysql','-uroot','-N','-B','suixinggou',input=statement)
    def q(value):return "'"+str(value).replace('\\','\\\\').replace("'","''")+"'"
    table_count=int(sql("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='suixinggou';"))
    if table_count!=113:raise SystemExit('Expected the imported 113-table disposable schema before smoke testing.')
    report={'startedAt':dt.datetime.now(dt.timezone.utc).isoformat(),'scope':'disposable mysql:8.4 on 127.0.0.1:13307; synthetic data only',
            'baseCommit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip(),
            'httpPort':args.port,'tableCount':table_count,'providerCallsRequestedBySmoke':0,
            'externalProviderAcceptance':'pending; no provider traffic instrumentation or credentials used',
            'cases':[],'fixtures':[]}
    report['mysqlVersion']=sql('SELECT VERSION();')
    source=hashlib.sha256()
    for file in sorted((ROOT/'server/src/main').rglob('*')):
        if file.is_file():source.update(str(file.relative_to(ROOT)).encode()+b'\0'+hashlib.sha256(file.read_bytes()).digest())
    report['workingTreeSourceSha256']=source.hexdigest()
    report['workingTreeHasChanges']=subprocess.run(['git','diff','--quiet'],cwd=ROOT).returncode!=0
    opener=urllib.request.build_opener(urllib.request.ProxyHandler({}))
    def request(method,path,body=None,token=None):
        headers={'Accept':'application/json'}
        if body is not None:headers['Content-Type']='application/json'
        if token:headers['Authorization']='Bearer '+token
        req=urllib.request.Request('http://127.0.0.1:'+str(args.port)+path,
            data=json.dumps(body).encode() if body is not None else None,headers=headers,method=method)
        try:
            response=opener.open(req,timeout=15)
        except urllib.error.HTTPError as e:response=e
        raw=response.read(2*1024*1024)
        try:value=json.loads(raw) if raw else None
        except (ValueError,UnicodeDecodeError):value=None
        return response.status,value
    def check(name,method,path,body=None,token=None,expected=(200,),predicate=lambda value:True):
        status,value=request(method,path,body,token)
        passed=status in expected and bool(predicate(value))
        row={'name':name,'method':method,'path':path,'expectedStatuses':list(expected),'status':status,'passed':passed}
        if isinstance(value,dict):row['responseFields']=sorted(k for k in value if k not in ['token','accessToken'])
        report['cases'].append(row)
        print(json.dumps({'case':name,'status':status,'passed':passed}),flush=True)
        if not passed:raise RuntimeError('HTTP smoke assertion failed: '+name+' (no sensitive body recorded)')
        return value
    env=os.environ.copy()
    bootstrap='Audit'+secrets.token_hex(5)+'!9'
    env.update(DB_URL='jdbc:mysql://127.0.0.1:13307/suixinggou?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false',
        DB_USER='root',DB_PASSWORD='',APP_JWT_SECRET=secrets.token_urlsafe(48),
        INTEGRATION_CONFIG_KEY=base64.b64encode(secrets.token_bytes(32)).decode(),ADMIN_BOOTSTRAP_PASSWORD=bootstrap,
        APP_ENVIRONMENT='local',SERVER_PORT=str(args.port))
    # Ambient legacy migration/provider toggles must not dispatch real outbound work during a local smoke.
    for key in list(env):
        if (key.startswith('CLOUD_') or key.startswith('MIGRATION_')) and key.endswith('_ENABLED'):
            env[key]='false'
    env.update(IDENTITY_PROVIDER_ENABLED='false',AUTH_JWT_ENABLED='false',
               MIGRATION_DELIVER_CONTACT_CODES_ENABLED='false',MIGRATION_NOTIFY_OUTBOX_ENABLED='false',
               MIGRATION_ORDER_TIMEOUTS_ENABLED='false',MIGRATION_REVEAL_DUE_DEMANDS_ENABLED='false')
    env['JAVA_HOME']=args.java_home or Path('/workspace/tooling/jdk21-path').read_text().strip()
    log=Path(args.server_log).open('w')
    with tempfile.NamedTemporaryFile(prefix='kuaixie-audit-classpath-',suffix='.txt',delete=False) as out:
        classpath_file=Path(out.name)
    cmd=[args.maven,'-f',str(ROOT/'server/pom.xml'),'-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts',
         '-s',args.settings,'-Dmaven.repo.local='+args.repository,'-Pcurrent-app-tests','-DskipTests',
         '-Dmdep.outputFile='+str(classpath_file),'-DincludeScope=runtime','compile','dependency:build-classpath']
    print('Compiling current sources and resolving the local runtime classpath; no tests or migrations executed by Maven.',flush=True)
    build=subprocess.run(cmd,cwd=ROOT,env=env,stdout=log,stderr=subprocess.STDOUT)
    if build.returncode:
        log.close();classpath_file.unlink(missing_ok=True)
        report.update(success=False,failure={'type':'BuildFailure','message':'Runtime classpath build failed; inspect local log.'})
        Path(args.result).write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
        raise SystemExit(1)
    classpath=str(ROOT/'server/target/classes')+os.pathsep+classpath_file.read_text().strip()
    classpath_file.unlink()
    process=subprocess.Popen([str(Path(env['JAVA_HOME'])/'bin/java'),'-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts',
        '-cp',classpath,'com.kuaixietech.KuaixiesuixinggouApplication'],cwd=ROOT,env=env,stdout=log,stderr=subprocess.STDOUT,start_new_session=True)
    success=False
    try:
        print('Starting actual Spring Boot application with generated local-only credentials; no values logged.',flush=True)
        deadline=time.monotonic()+90
        while time.monotonic()<deadline:
            if process.poll() is not None:raise RuntimeError('Spring Boot process exited during startup; inspect sanitized server log.')
            try:
                status,value=request('GET','/public/categories')
                if status==200:break
                if status>=500:raise RuntimeError('Application answered public SQL request with '+str(status)+'; inspect local server log.')
            except (urllib.error.URLError,TimeoutError,ConnectionError):pass
            time.sleep(.5)
        else:raise RuntimeError('Application failed functional startup readiness within 90 seconds.')
        report['startupReady']=True
        check('anonymous catalog through gateway','GET','/public/categories',predicate=lambda x:isinstance(x,list))
        check('canonical catalog','GET','/api/public/categories',predicate=lambda x:isinstance(x,list))
        check('private profile denies anonymous','GET','/me/profile',expected=(401,403))
        check('invalid bearer rejected','GET','/me/profile',token='not-a-valid-jwt',expected=(401,403))
        stamp=uuid.uuid4().hex[:12]
        admin_mail='audit-admin-'+stamp+'@example.invalid'
        if int(sql('SELECT COUNT(*) FROM t_profiles WHERE is_admin=1;')):
            admin_id=str(uuid.uuid4())
            sql('INSERT INTO t_profiles(id,username,nickname,email,is_admin,user_role) VALUES('+','.join(map(q,[admin_id,'a'+stamp,'Synthetic runtime admin',admin_mail]))+",1,'buyer');")
            report['fixtures'].append('Synthetic isolated admin profile inserted because a previous local audit admin already existed; password/JWT still issued by actual login HTTP endpoint.')
        else:report['fixtures'].append('First administrator created through actual local bootstrap login HTTP endpoint.')
        admin=check('actual administrator password login','POST','/auth/login',{'channel':'email','contact':admin_mail,'password':bootstrap},predicate=lambda x:x.get('isAdmin') is True and bool(x.get('accessToken')))
        admin_token=admin['accessToken']
        check('administrator own profile','GET','/me/profile',token=admin_token,predicate=lambda x:x.get('id')==admin['userId'])
        check('contact delivery diagnostics','GET','/portal/admin/contact-delivery/health',token=admin_token,predicate=lambda x:x.get('state') in ['paused','idle'] and x.get('providerDeliveryVerified') is False)
        check('coupler configuration window','GET','/portal/admin/integration-center',token=admin_token,predicate=lambda x:len(x.get('categories',[]))==21 and x.get('runtime',{}).get('mode')=='SANDBOX')
        check('professional payment settings','GET','/portal/admin/payments/settings',token=admin_token,predicate=lambda x:x.get('settlementCurrency')=='CNY')
        check('insurance settings honestly empty consumers','GET','/portal/admin/insurance/settings',token=admin_token,predicate=lambda x:x.get('adapters')==[])
        identities={}
        for role in ['buyer','carrier']:
            mail='audit-'+role+'-'+stamp+'@example.invalid';code=str(secrets.randbelow(900000)+100000)
            sql("INSERT INTO t_verification_codes(id,target,channel,code,purpose,expires_at,used) VALUES("+','.join(map(q,[str(uuid.uuid4()),'auth:email:'+mail,'email',code,'signup']))+",TIMESTAMPADD(MINUTE,10,CURRENT_TIMESTAMP(3)),0);")
            report['fixtures'].append('Synthetic signup OTP inserted directly for '+role+'; does not test SMS/email delivery.')
            response=check('register '+role+' using synthetic OTP','POST','/auth/register',{'channel':'email','contact':mail,'password':bootstrap,'code':code,'role':role},predicate=lambda x:bool(x.get('accessToken')) and x.get('isAdmin') is False)
            identities[role]=response
            check(role+' password login','POST','/auth/login',{'channel':'email','contact':mail,'password':bootstrap},predicate=lambda x:x.get('userId')==response['userId'])
        buyer,carrier=identities['buyer'],identities['carrier']
        check('buyer cannot inspect administrator diagnostics','GET','/portal/admin/contact-delivery/health',token=buyer['accessToken'],expected=(403,))
        check('buyer cannot list administrator review queue','GET','/portal/admin/reviews',token=buyer['accessToken'],expected=(403,))
        address_input={'contactName':'Synthetic buyer','contactPhone':'13800009999','province':'Synthetic province',
                       'city':'Synthetic city','district':'Synthetic district','isDefault':True}
        first_address=check('buyer creates first default address through actual HTTP','POST','/me/addresses',
            {**address_input,'detail':'Disposable synthetic address A'},buyer['accessToken'],predicate=lambda x:bool(x.get('id')))
        second_address=check('buyer creates second default address and switches default','POST','/me/addresses',
            {**address_input,'detail':'Disposable synthetic address B'},buyer['accessToken'],
            predicate=lambda x:bool(x.get('id')) and x.get('id')!=first_address['id'])
        check('buyer reads exactly two addresses with only second default','GET','/me/addresses',token=buyer['accessToken'],
            predicate=lambda x:isinstance(x,list) and len(x)==2 and
                {row.get('id') for row in x}=={first_address['id'],second_address['id']} and
                [row.get('id') for row in x if row.get('is_default') in (True,1)]==[second_address['id']])
        check('carrier cannot see buyer addresses','GET','/me/addresses',token=carrier['accessToken'],predicate=lambda x:x==[])
        address_result=sql('SELECT COUNT(*),SUM(is_default),MAX(CASE WHEN is_default=1 THEN id END) FROM t_addresses WHERE user_id='+q(buyer['userId'])+';').split('\t')
        addresses_passed=address_result==['2','1',second_address['id']]
        report['cases'].append({'name':'actual SQL confirms HTTP address switch preserves one default','passed':addresses_passed,
            'values':{'addressCount':address_result[0],'defaultCount':address_result[1],'defaultAddressId':address_result[2]}})
        if not addresses_passed:raise RuntimeError('SQL default-address invariant failed')
        report['fixtures'].append('Two synthetic buyer addresses created and read through actual authenticated HTTP; carrier listing was empty. Rows retained only in disposable database.')
        sql("UPDATE t_profiles SET buyer_verify_status='approved',buyer_verified_at=CURRENT_TIMESTAMP(3),balance=1000.00 WHERE id="+q(buyer['userId'])+';')
        sql("UPDATE t_profiles SET carrier_verify_status='approved',verified_at=CURRENT_TIMESTAMP(3) WHERE id="+q(carrier['userId'])+';')
        sql("INSERT INTO t_balance_ledger(id,user_id,kind,amount,note) VALUES("+','.join(map(q,[str(uuid.uuid4()),buyer['userId'],'test_seed']))+",1000.00,'Synthetic isolated opening balance; not a real payment');")
        report['fixtures'].append('Buyer/carrier verification and 1000.00 CNY opening balance are explicit SQL fixtures, not KYC or payment-provider receipts.')
        tomorrow=dt.date.today()+dt.timedelta(days=1);later=tomorrow+dt.timedelta(days=7)
        trip=check('carrier publishes trip awaiting review','POST','/portal/trips',{'originCity':'Synthetic origin','destination':'Synthetic destination','departDate':str(tomorrow),'returnDate':str(later),'tripType':'domestic','transportMode':'road','capacityQty':2},carrier['accessToken'],predicate=lambda x:x.get('status')=='pending_review')
        check('administrator approves actual trip','POST','/portal/admin/trips/review',{'tripId':trip['id'],'approved':True},admin_token,expected=(200,204))
        product=check('carrier publishes product initially off','POST','/me/products',{'name':'Synthetic smoke product','spec':'runtime-only','region':'Synthetic','price':'100.00','stock':3,'imageUrls':[],'note':'disposable test data'},carrier['accessToken'],predicate=lambda x:x.get('status')=='off')
        check('owner lists product','PUT','/me/products/'+product['id']+'/status',{'status':'on'},carrier['accessToken'],predicate=lambda x:x.get('status')=='on')
        check('public product detail from actual SQL','GET','/public/products/'+product['id'],predicate=lambda x:x.get('id')==product['id'])
        check('product exposes eligible seller trip','GET','/public/products/'+product['id']+'/trips',predicate=lambda x:any(row.get('id')==trip['id'] for row in x))
        order_input={'productId':product['id'],'tripId':trip['id'],'quantity':1,'contactName':'Synthetic buyer','contactPhone':'13800009999','address':'Disposable runtime fixture address','insuranceChoice':'waived'}
        quote=check('buyer obtains server quote','POST','/me/product-orders/quote',order_input,buyer['accessToken'],predicate=lambda x:x.get('buyerEscrow')=='105.00' and x.get('currency')=='CNY')
        confirm={**order_input,'requestId':str(uuid.uuid4()),'expectedUnitPrice':quote['unitPrice'],'expectedBuyerEscrow':quote['buyerEscrow'],'expectedProductFingerprint':quote['productFingerprint']}
        order=check('confirm order actually reserves funds and stock','POST','/me/product-orders',confirm,buyer['accessToken'],predicate=lambda x:x.get('status')=='requesting')
        check('identical retry returns original order','POST','/me/product-orders',confirm,buyer['accessToken'],predicate=lambda x:x.get('id')==order['id'])
        check('buyer reads own actual order','GET','/me/orders/'+order['id'],token=buyer['accessToken'],predicate=lambda x:x.get('id')==order['id'])
        check('unrelated administrator own-order endpoint cannot read buyer order','GET','/me/orders/'+order['id'],token=admin_token,expected=(404,))
        result=sql('SELECT p.stock,(SELECT balance FROM t_profiles WHERE id='+q(buyer['userId'])+'),(SELECT COUNT(*) FROM t_product_order_reservations WHERE order_id='+q(order['id'])+'),(SELECT amount FROM t_deposits WHERE order_id='+q(order['id'])+" AND party_role='buyer') FROM t_products p WHERE p.id="+q(product['id'])+';').split('\t')
        passed=result==['2','895.00','1','105.00']
        report['cases'].append({'name':'actual SQL stock/wallet/reservation/escrow invariants after duplicate confirm','passed':passed,'values':{'stock':result[0],'buyerBalance':result[1],'reservations':result[2],'buyerEscrow':result[3]}})
        if not passed:raise RuntimeError('SQL escrow/stock invariant failed')
        report['flowIds']={'product':product['id'],'trip':trip['id'],'order':order['id'],
                           'addresses':[first_address['id'],second_address['id']]}
        success=True
    except Exception as e:
        report['failure']={'type':type(e).__name__,'message':str(e)}
        print(json.dumps({'failure':report['failure']}),flush=True)
    finally:
        if process.poll() is None:
            os.killpg(process.pid,signal.SIGTERM)
            try:process.wait(timeout=10)
            except subprocess.TimeoutExpired:os.killpg(process.pid,signal.SIGKILL);process.wait(timeout=5)
        log.close()
        startup=re.search(r'Started KuaixiesuixinggouApplication in ([0-9.]+) seconds',Path(args.server_log).read_text())
        if startup:report['applicationStartupSeconds']=float(startup[1])
        report['processStoppedAfterChecks']=True
        report['expectedShutdownSignal']='SIGTERM after completed checks (no Maven run-plugin failure)'
        report['completedAt']=dt.datetime.now(dt.timezone.utc).isoformat();report['success']=success
        report['passed']=sum(row['passed'] for row in report['cases']);report['failed']=sum(not row['passed'] for row in report['cases'])
        Path(args.result).write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
        print(json.dumps({'success':success,'passed':report['passed'],'failed':report['failed'],'receipt':args.result,'serverLog':args.server_log}),flush=True)
    raise SystemExit(0 if success else 1)

if __name__=='__main__':main()
