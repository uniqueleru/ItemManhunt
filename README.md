# Item Manhunt

마인크래프트 서버에서 아이템을 목표로 한 추격 게임을 즐길 수 있는 플러그인입니다.

## 게임 설명

Item Manhunt는 도망자와 추격자로 나뉘어서 플레이하는 게임입니다:

- **도망자**: 게임이 시작될 때 랜덤하게 선택되는 '목표 아이템'을 한 번도 죽지 않고 획득하면 승리합니다.
- **추격자**: 도망자가 목표 아이템을 얻기 전에 도망자를 처치하면 승리합니다.

## 게임 규칙

1. 게임 시작 전, 관리자가 도망자 한 명을 지정합니다.
2. 게임이 시작되면 서버의 모든 플레이어에게 도망자의 '목표 아이템'이 타이틀로 공지됩니다.
3. 도망자에게는 먼저 도망갈 시간이 주어지며, 이 시간 동안 추격자들은 움직일 수 없습니다.
4. 추격자들의 추격이 시작되면 모든 추격자에게 도망자의 위치를 추적할 수 있는 나침반이 주어집니다.
5. 도망자가 '목표 아이템'을 획득하면 즉시 도망자의 승리로 게임이 종료됩니다.
6. 도망자가 '목표 아이템' 획득 전에 사망하면 추격자들의 승리로 게임이 종료됩니다.

## 기능

- 게임 시작/종료 관리
- 도망자 지정 및 목표 아이템 랜덤 선택
- 추격자를 위한 도망자 추적 나침반
- 목표 아이템 설정 GUI
- 모든 메시지 및 타이틀 커스터마이징 가능
- 게임 중 효과음 설정

## 명령어

| 명령어 | 설명 | 권한 |
|--------|------|------|
| `/imh` | 플러그인 명령어 도움말을 표시합니다. | 없음 |
| `/imh setrunner [닉네임]` | 다음 게임의 도망자를 지정합니다. | itemmanhunt.admin |
| `/imh start` | 게임을 시작합니다. | itemmanhunt.admin |
| `/imh stop` | 게임을 강제 종료합니다. | itemmanhunt.admin |
| `/imh reload` | 플러그인의 설정 파일을 리로드합니다. | itemmanhunt.admin |
| `/imh config` | 목표 아이템 설정 GUI를 엽니다. | itemmanhunt.admin |

## 설정

### config.yml
```yaml
# 초기 도망 시간 (초)
head-start-time: 60

# 나침반 업데이트 주기 (틱, 20틱 = 1초)
compass-update-interval: 20

# 게임 중 사용되는 소리 효과
sounds:
  game-start: ENTITY_ENDER_DRAGON_GROWL
  game-end: ENTITY_WITHER_DEATH
  target-item-found: ENTITY_PLAYER_LEVELUP
  
# 게임 시작시 목표 아이템 후보들
# 이 옵션은 /imh config 명령어로 설정하는 GUI에서 설정한 값이 저장됩니다.
enabled-items: []
```

### lang.yml
모든 메시지, 타이틀, 액션바 등의 텍스트를 커스터마이징할 수 있습니다.

## 요구사항

- Bukkit/Spigot/Paper 서버 (1.21 이상)
- Java 17 이상

## 설치 방법

1. 플러그인 JAR 파일을 서버의 `plugins` 폴더에 넣습니다.
2. 서버를 재시작하거나 `/reload` 명령어를 실행합니다.
3. 플러그인의 설정 파일을 필요에 따라 수정합니다.

## 라이선스

이 프로젝트는 [GPL-3.0 라이선스](https://www.gnu.org/licenses/gpl-3.0.html)를 따릅니다.