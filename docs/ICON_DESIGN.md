# AlcoholicTimer 아이콘 디자인 가이드

이 문서는 앱 런처 아이콘(Adaptive / Monochrome) 및 관련 리소스를 유지·개선하기 위한 **단일 참조 문서**입니다. 향후 디자인 변경, 다크 모드/Material You 실험, 브랜드 정체성 확장 시 본 가이드를 기반으로 합니다.

---
## 1. 목적
| 목표 | 설명 |
|------|------|
| 플랫폼 적합성 | Android 8.0+ Adaptive Icon / Android 13 테마 아이콘 대응 |
| 시각적 일관성 | 다양한 런처 마스크(원형, 둥근사각, 물방울, 사각)에서 형태 유지 |
| 인지성 | 축소(24dp급)에서도 금주/타이머 컨셉 직관적 전달 |
| 유지보수 용이성 | Foreground/Monochrome 벡터 단일 경량 path 구조 |
| 대비 & 접근성 | Light/Dark 및 Wallpaper 변화 상황에서도 식별성 확보 |

---
## 2. 현재 구조 개요
| 레이어 | 리소스 | 용도 | 노트 |
|--------|--------|------|------|
| Background | `@color/icon_launcher_bg` (#FFFFFF) | 중립 배경 | Material You 에서 다이내믹 적용 실험 여지 |
| Foreground(Vector) | (XML Vector) | Hourglass + 강조 요소 | path 단순화, 스케일 1.43 적용 |
| Monochrome | `ic_launcher_monochrome.xml` | Android 13 테마 아이콘 | Hourglass 실루엣 단일 path |
| (Backup) | `ic_launcher_monochrome_full.xml` | 과거 복잡 버전 보관 | 회귀/실험 참고 |

Foreground는 Adaptive 캔버스(108x108dp 가상) 기준 inset 18dp 여백을 확보했습니다.

---
## 3. 색상 팔레트
| 역할 | 값 | 비고 |
|------|----|------|
| Foreground 메인 | #C6283A | 강한 레드톤 (절제/경고/동기 부여) |
| Foreground 보조(선택) | #8E1C2B | SHADE (입체감/음영 표현 시) |
| Background | #FFFFFF | 단순/중립, 대비 극대화 |
| 다크모드 실험안(제안) | #121212 / 동적 | Material You 동적 색 추출 고려 |

---
## 4. 스케일 & 안전 영역
| 항목 | 값 | 근거 |
|------|-----|------|
| Inset | 18dp | Android 권장(108dp 기준 여백 확보) |
| Foreground Group Scale | 1.43 | 기존 1.55 과밀 → 1.43 로 완화 |
| Stroke 사용 | 없음(도형 충실) | 스케일링 시 경계 alias 최소화 |
| Monochrome Path Count | 1 | 렌더 비용/테마 아이콘 정합성 |

---
## 5. Monochrome 설계 원칙
1. 실루엣 한 번에 인지: Hourglass 기본 윤곽 유지.
2. 미세 내부 디테일(모래 분할 등)은 축소 가독성을 해치면 제거.
3. Negative space(속 빈 부분) 대비 확보(>30%).
4. Fill path 최소: 1~2 (현재 1 유지).

---
## 6. 변경 이력(History)
| 버전/날짜 | 변경 | 이유 |
|-----------|------|------|
| 초기 | Foreground scale 1.55 | 1차 와이어프레임 |
| 조정 | Scale 1.43 / inset 확정 | 과밀 해소, 다양한 마스크 대응 |
| Monochrome 단순화 | 다중 path → 1 path | 테마 아이콘 대비 향상 |
| Round 아이콘 리소스 제거 | `roundIcon` manifest entry 삭제 | Adaptive 단일 전략 유지 |

---
## 7. 향후 개선 로드맵
| 우선순위 | 제안 | 기대 효과 |
|----------|------|-----------|
| 높음 | Material You 동적 배경 실험 | 시스템 톤 일체감 |
| 중간 | 다크 모드 전용 대비 튜닝 | 어두운 배경 wallpaper 가독성 |
| 중간 | Vector path snap (소수점 좌표 정리) | 렌더 품질 미세 향상 |
| 낮음 | 브랜드 팔레트 정의 문서화 | 확장 기능(위젯, 마케팅) 재사용 |

---
## 8. 업데이트 워크플로
1. 디자인 수정(Figma / Sketch) → 108x108 또는 432x432 px 아트보드.
2. Export → SVG (stroke → path outline 정규화).
3. Android Studio Vector Asset Import.
4. Inset / viewport 확인 (Width/Height 108, path 범위 <= 108 - 2*inset).
5. Monochrome 버전: 내부 디테일 정리 후 별도 Vector 생성.
6. XML diff 검토 (path 증가 여부, fillColor 일관성).
7. Debug 빌드 설치 후: 다양한 런처 마스크 / 다크 모드 / 테마 아이콘 체크.
8. README(요약) 혹은 ICON_DESIGN(본 문서) History 테이블 갱신.

---
## 9. QA 체크리스트
| 항목 | 합격 기준 |
|------|-----------|
| Adaptive 호환 | Pixel / Samsung / Xiaomi 기본 런처에서 잘림 없음 |
| 테마 아이콘 | Android 13+ 테마 아이콘 모드에서 실루엣 유지, 대비 양호 |
| 축소(24dp) | 윤곽 깨짐/ alias 뭉개짐 없음 |
| 대비 | 배경과 4.5:1 이상 권장(현재 밝은 배경 대비 충분) |
| 파일 크기 | Foreground/Monochrome 각각 2KB 내외 유지 |
| Path 수 | Foreground <= 5, Monochrome = 1 |

---
## 10. 문제 해결(Troubleshooting)
| 문제 | 원인 | 해결 |
|------|------|------|
| 모서리 잘림 | Scale 과다 | group scale 하향 / inset 증가 |
| 흐린 렌더링 | Fraction 좌표 다수 | 좌표 반올림 (정수 또는 .5) |
| 테마 아이콘 회색 단조 | Color 적용 잔존 | Monochrome vector 에 단일 색 유지, themeTint 위임 |
| 파일 커짐 | Path 중복/미사용 | Optimize path(불필요 anchor 삭제) |

---
## 11. 예시 리소스 구조
```
app/src/main/res/
  mipmap-anydpi-v26/
    ic_launcher.xml           (adaptive: background + foreground)
    ic_launcher_round.xml?    (삭제됨/비사용)
  drawable/
    ic_launcher_foreground.xml
    ic_launcher_monochrome.xml
    ic_launcher_monochrome_full.xml (backup)
  values/colors.xml (icon_launcher_bg, icon_launcher_fg)
```

---
## 12. 유지보수 원칙 요약
- 단일 소스(이 문서)만 수정; README에는 요약/링크만 유지.
- Path 추가 시 린트/사이즈 영향 여부 확인.
- 변경마다 QA 체크리스트 전체 수행.

---
## 13. 라이선스 / 저작권
- 내부 제작 자산. 외부 OSS 아이콘(예: Material Symbols) 차용 시 라이선스 문구 별도 추가.

---
문서 갱신 시 변경 이력에 행 추가 후 커밋 메시지: `docs: update icon design guide` 권장.

