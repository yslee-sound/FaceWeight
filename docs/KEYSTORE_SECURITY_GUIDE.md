# 키스토어 파일 보안 관리 가이드

## 🔐 키스토어 파일의 중요성

**alcoholic-timer-upload.jks** 파일은:
- ✅ 앱 업데이트의 **유일한 열쇠**
- ✅ 분실 시 **앱 업데이트 영구 불가능**
- ✅ Google도 복구해줄 수 없음
- ✅ 노출 시 **앱 보안 위협**

**현재 위치**: `G:/secure/alcoholic-timer-upload.jks` ✅

---

## 📋 백업 전략: 3-2-1 규칙

### 3-2-1 규칙
- **3개의 복사본**: 원본 + 백업 2개
- **2종류의 저장 매체**: 로컬 드라이브 + 클라우드
- **1개의 오프사이트**: 물리적으로 다른 위치

---

## 🗂️ 권장 보관 장소 (5곳)

### ✅ 1순위: 원본 (현재)
```
📁 G:/secure/alcoholic-timer-upload.jks
```
- ✅ 이미 안전한 위치에 보관 중
- ✅ VCS(Git) 외부 위치
- ⚠️ 단, 로컬 드라이브만 있으면 위험

### ✅ 2순위: 클라우드 암호화 저장소 (필수!)

**추천 서비스**:

#### Option A: Google Drive (암호화 필수)
```
📁 Google Drive/AlcoholicTimer_Secure/
   ├─ alcoholic-timer-upload.jks.enc (암호화)
   ├─ keystore-info.txt.enc (비밀번호 정보)
   └─ backup-2025-10-09.zip.enc (ZIP 압축 + 암호화)
```

**암호화 방법**:
- 7-Zip으로 암호화 ZIP 생성
- VeraCrypt로 암호화 컨테이너 생성
- Cryptomator 사용 (추천)

#### Option B: OneDrive Personal Vault
```
📁 OneDrive/개인 자격 증명 모음/
   └─ alcoholic-timer-upload.jks
```
- 생체 인증 필요
- 자동 암호화

#### Option C: Dropbox (Dropbox Passwords 사용)
```
📁 Dropbox/AlcoholicTimer_Keys/
   └─ alcoholic-timer-upload.jks
```

### ✅ 3순위: 외장 USB/하드 드라이브

```
📁 USB Drive/AlcoholicTimer_Backup_2025-10-09/
   ├─ alcoholic-timer-upload.jks
   ├─ keystore-passwords.txt
   └─ README.txt (복구 방법)
```

**권장 USB 드라이브**:
- 암호화 기능 내장 USB (예: Kingston IronKey)
- BitLocker로 암호화된 USB
- 최소 2개 준비 (서로 다른 물리적 위치)

### ✅ 4순위: 비밀번호 관리자

**추천 서비스**:
- **1Password**: Secure Notes에 파일 첨부 가능
- **Bitwarden**: Secure Notes + 파일 첨부
- **LastPass**: Secure Notes

**저장 방법**:
```
항목 이름: AlcoholicTimer Keystore
형식: Secure Note
첨부 파일: alcoholic-timer-upload.jks
필드:
  - 파일 경로: G:/secure/alcoholic-timer-upload.jks
  - Keystore 비밀번호: [실제 비밀번호]
  - Key Alias: alcoholictimeruploadkey
  - Key 비밀번호: [실제 키 비밀번호]
  - SHA-256: [지문]
  - 생성일: 2025-10-09
  - 용도: Play Store 업로드 서명
```

### ✅ 5순위: 물리적 보관 (종이 + 금고)

**종이에 기록할 정보**:
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
AlcoholicTimer 키스토어 복구 정보
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
파일명: alcoholic-timer-upload.jks
원본 위치: G:/secure/alcoholic-timer-upload.jks
백업 위치:
  1. Google Drive: AlcoholicTimer_Secure/
  2. USB Drive #1: [보관 장소]
  3. USB Drive #2: [보관 장소]

Keystore 비밀번호: [여기에 기록]
Key Alias: alcoholictimeruploadkey
Key 비밀번호: [여기에 기록]

SHA-256 지문: [keytool 명령어 결과]

생성일: 2025-10-09
작성자: [이름]

⚠️ 이 정보는 앱 업데이트의 유일한 열쇠입니다.
   분실 시 앱 업데이트가 영구 불가능합니다.
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

**보관 장소**:
- 집 금고/서랍 (잠금 가능)
- 은행 안전 금고 (중요 문서함)
- 회사 서랍 (다른 물리적 위치)

---

## 🚀 즉시 실행 가능한 백업 스크립트

### PowerShell 자동 백업 스크립트

백업 스크립트를 실행하면:
1. ✅ USB 드라이브에 복사
2. ✅ 날짜별 폴더에 정리
3. ✅ 암호화 ZIP 생성 (Google Drive 업로드용)
4. ✅ SHA-256 해시 기록
5. ✅ 백업 로그 생성

**즉시 실행**:
```powershell
# scripts/backup_keystore.ps1 실행
.\scripts\backup_keystore.ps1
```

---

## 📝 백업 체크리스트

### 즉시 해야 할 것 (오늘!)

- [ ] **클라우드 백업** (Google Drive/OneDrive)
  - [ ] 암호화 ZIP 생성
  - [ ] 클라우드에 업로드
  - [ ] 다운로드 테스트

- [ ] **USB 백업 #1**
  - [ ] USB 드라이브 준비
  - [ ] 키스토어 복사
  - [ ] README.txt 작성
  - [ ] 안전한 곳에 보관

- [ ] **USB 백업 #2** (다른 물리적 위치)
  - [ ] 두 번째 USB 준비
  - [ ] 키스토어 복사
  - [ ] 다른 장소에 보관 (회사/부모님 댁)

- [ ] **비밀번호 관리자**
  - [ ] 1Password/Bitwarden 계정 생성
  - [ ] Secure Note에 정보 저장
  - [ ] 파일 첨부
  - [ ] 테스트 복구

- [ ] **종이 기록**
  - [ ] 정보 종이에 작성
  - [ ] 금고/서랍에 보관
  - [ ] 사진 찍어 안전한 곳에 보관

### 매월 확인 (월 1회)

- [ ] 클라우드 백업 존재 확인
- [ ] USB 드라이브 정상 작동 확인
- [ ] 비밀번호 관리자 접근 확인
- [ ] SHA-256 해시 일치 확인

### 분기별 확인 (3개월마다)

- [ ] 모든 백업에서 파일 복원 테스트
- [ ] 비밀번호로 실제 서명 테스트
- [ ] 새 백업 생성 (날짜별 보관)

---

## 🔒 보안 권장사항

### ✅ 해야 할 것

1. **암호화 필수**
   - 클라우드 업로드 시 항상 암호화
   - ZIP 파일에 강력한 비밀번호 설정
   - BitLocker/VeraCrypt 사용

2. **접근 제어**
   - 키스토어 폴더 권한 설정 (관리자만)
   - 공유 컴퓨터에 저장 금지
   - 공개 클라우드에 평문 업로드 금지

3. **버전 관리**
   - 날짜별 백업 유지
   - 최소 3개 버전 보관
   - 오래된 백업도 삭제하지 말 것

4. **정기 검증**
   - 월 1회 백업 확인
   - SHA-256 해시 비교
   - 복원 테스트

### ❌ 하지 말아야 할 것

1. **절대 금지**
   - ❌ Git/GitHub에 커밋
   - ❌ 이메일로 평문 전송
   - ❌ 카카오톡/메신저로 공유
   - ❌ 공개 클라우드에 암호화 없이 업로드
   - ❌ 암호화되지 않은 USB에 저장

2. **위험한 행동**
   - ❌ 비밀번호를 같은 파일에 평문 저장
   - ❌ "keystore_backup.jks" 같은 명확한 이름
   - ❌ 임시 폴더/다운로드 폴더에 보관
   - ❌ 백업 없이 원본만 보관

---

## 🆘 긴급 복구 시나리오

### 시나리오 1: PC 고장

**백업 위치**:
1. ✅ Google Drive에서 다운로드
2. ✅ USB 드라이브에서 복사
3. ✅ 비밀번호 관리자에서 다운로드

**복구 시간**: 10분

### 시나리오 2: 랜섬웨어 공격

**백업 위치**:
1. ✅ 클라우드 백업 (감염 안 됨)
2. ✅ 오프라인 USB (감염 안 됨)

**복구 시간**: 20분

### 시나리오 3: 실수로 삭제

**백업 위치**:
1. ✅ 휴지통 복구
2. ✅ Windows 파일 히스토리 복구
3. ✅ 클라우드/USB 백업

**복구 시간**: 5분

### 시나리오 4: 모든 백업 동시 손실 (최악)

**대응**:
1. ⚠️ Play Console → App Integrity → 업로드 키 교체 요청
2. ⚠️ 새 키스토어 생성
3. ⚠️ Google 승인 후 새 키로 업로드

**복구 시간**: 1-2주 (Google 검토 시간)

---

## 📊 백업 상태 확인

### SHA-256 해시 확인

모든 백업이 동일한지 확인:

```powershell
# 원본
Get-FileHash "G:/secure/alcoholic-timer-upload.jks" -Algorithm SHA256

# USB 백업
Get-FileHash "E:/AlcoholicTimer_Backup/alcoholic-timer-upload.jks" -Algorithm SHA256

# 해시가 동일하면 파일이 완전히 같음 ✅
```

### Keytool로 키스토어 정보 확인

```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" `
  -list -v `
  -keystore "G:/secure/alcoholic-timer-upload.jks" `
  -alias alcoholictimeruploadkey

# 비밀번호 입력 후 SHA-256 지문 확인
```

---

## 🎯 권장 보관 구성

### 최소 구성 (필수)

```
1. 원본: G:/secure/alcoholic-timer-upload.jks
2. 클라우드: Google Drive (암호화 ZIP)
3. USB: 외장 드라이브 #1
4. 비밀번호 관리자: 1Password
```

### 권장 구성 (안전)

```
1. 원본: G:/secure/alcoholic-timer-upload.jks
2. 클라우드 #1: Google Drive (암호화 ZIP)
3. 클라우드 #2: OneDrive Personal Vault
4. USB #1: 집 보관
5. USB #2: 회사/부모님 댁 보관
6. 비밀번호 관리자: 1Password
7. 종이 기록: 금고 보관
```

### 완벽 구성 (기업급)

```
1. 원본: G:/secure/ (BitLocker 암호화)
2. 클라우드 #1: Google Drive (Cryptomator)
3. 클라우드 #2: OneDrive Personal Vault
4. 클라우드 #3: Dropbox
5. USB #1 (암호화): 집 금고
6. USB #2 (암호화): 회사 서랍
7. USB #3 (암호화): 은행 안전 금고
8. 비밀번호 관리자: 1Password + Bitwarden
9. 종이 기록: 3곳 (집, 회사, 은행)
10. NAS 백업: 로컬 네트워크 저장소
```

---

## 🔄 자동화 권장사항

### Windows 작업 스케줄러 설정

매월 자동 백업:

1. **작업 스케줄러** 열기
2. **기본 작업 만들기**
3. 이름: `Keystore Monthly Backup`
4. 트리거: 매월 1일
5. 작업: `PowerShell.exe`
6. 인수: `-File "G:\Workspace\AlcoholicTimer\scripts\backup_keystore.ps1"`

---

## 📞 비상 연락처

### Google Play 지원

키스토어 관련 문제 발생 시:

```
Play Console → 도움말 및 의견 → 문의하기
카테고리: 앱 서명
```

### 업로드 키 교체 절차

1. Play Console 로그인
2. **설정** → **앱 무결성** → **업로드 키 관리**
3. **업로드 키 교체 요청**
4. 새 키스토어 생성 및 업로드
5. Google 검토 대기 (1-2주)

---

## ✅ 최종 체크리스트

### 오늘 바로 실행 (30분)

- [ ] 백업 스크립트 실행 (`.\scripts\backup_keystore.ps1`)
- [ ] Google Drive에 암호화 ZIP 업로드
- [ ] USB 드라이브에 복사 (최소 1개)
- [ ] SHA-256 해시 기록
- [ ] 비밀번호 관리자에 정보 저장

### 이번 주 내 실행

- [ ] 두 번째 USB 백업 (다른 장소)
- [ ] 종이에 정보 기록
- [ ] 금고/서랍에 보관
- [ ] 모든 백업에서 복원 테스트

### 매월 확인

- [ ] 백업 존재 확인
- [ ] SHA-256 해시 비교
- [ ] 새 백업 생성

---

## 🎉 결론

**현재 상태**: `G:/secure/` 위치는 좋은 시작 ✅

**즉시 해야 할 것**:
1. 🔒 암호화 ZIP 생성
2. ☁️ Google Drive 업로드
3. 💾 USB 백업 (최소 2개)
4. 🔑 비밀번호 관리자 저장
5. 📄 종이 기록 (금고 보관)

**목표**: 3-2-1 규칙 달성
- ✅ 3개 복사본
- ✅ 2종류 매체 (로컬 + 클라우드)
- ✅ 1개 오프사이트 (USB)

---

**⚠️ 기억하세요**: 
- 키스토어 분실 = 앱 업데이트 영구 불가능
- Google도 복구 불가능
- 백업은 선택이 아닌 **필수**

지금 바로 백업 스크립트를 실행하세요! 🚀

