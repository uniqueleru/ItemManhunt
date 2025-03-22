# PaperAPI에서 Sound 처리 가이드

## 개요

Minecraft 1.21.4 이후의 PaperAPI에서는 기존 `Sound.valueOf()` 방식이 Deprecated 되었습니다. 이 문서는 최신 PaperAPI에서 권장하는 소리 처리 방법에 대한 가이드입니다.

## 방법 비교

### 1. 기존 방식 (Deprecated)
```java
Sound sound = Sound.valueOf("ENTITY_PLAYER_LEVELUP");
player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
```

### 2. 최신 권장 방식
```java
// 필요한 임포트
import org.bukkit.Registry;
import org.bukkit.NamespacedKey;

// 소리 가져오기
var sound = Registry.SOUND_EVENT.get(NamespacedKey.minecraft("entity.player.levelup"));
player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
```

### 3. Adventure API 방식 (대체 옵션)
```java
// 필요한 임포트
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

// Adventure API로 소리 생성
Sound sound = Sound.sound(
    Key.key("minecraft:entity.player.levelup"),
    Sound.Source.MASTER,
    1.0f,  // 볼륨
    1.0f   // 피치
);

player.playSound(sound);
```

## 구현 패턴

### 기본 패턴
```java
String soundName = "entity.player.levelup"; // 소문자로 된 소리 이름
var sound = Registry.SOUND_EVENT.get(NamespacedKey.minecraft(soundName));
if (sound != null) {
    player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
}
```

### 폴백 패턴 (권장)
```java
String soundName = "entity.player.levelup";
try {
    // 기본 방식 시도
    var sound = Registry.SOUND_EVENT.get(NamespacedKey.minecraft(soundName));
    if (sound != null) {
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }
} catch (Exception e) {
    // Adventure API로 폴백
    try {
        Sound sound = Sound.sound(
            Key.key("minecraft:" + soundName),
            Sound.Source.MASTER,
            1.0f,
            1.0f
        );
        player.playSound(sound);
    } catch (Exception ex) {
        logger.warning("잘못된 사운드 이름: " + soundName);
    }
}
```

## 주의사항

1. **소리 이름 포맷**:
   - 최신 API에서는 소리 이름이 소문자이며 언더스코어(`_`) 대신 점(`.`)을 사용합니다.
   - 예: `ENTITY_PLAYER_LEVELUP` → `entity.player.levelup`

2. **이전 레거시 이름 변환**:
   ```java
   // 레거시 이름을 현대 형식으로 변환
   String modernName = legacyName.toLowerCase().replace("_", ".");
   ```

3. **Null 체크**:
   - `Registry.SOUND_EVENT.get()`는 해당 소리가 없을 경우 null을 반환할 수 있습니다.
   - 항상 null 체크를 수행하세요.

4. **예외 처리**:
   - 잘못된 소리 이름은 예외를 발생시킬 수 있습니다.
   - 적절한 예외 처리를 구현하세요.

## 소리 목록 가져오기

현재 등록된 모든 소리 목록을 가져오는 방법:

```java
// 모든 등록된 소리 목록 가져오기
for (var key : Registry.SOUND_EVENT.getKeys()) {
    System.out.println(key.toString());
}
```

## 서버 버전별 호환성

### 1.21.4 이상
- `Registry.SOUND_EVENT.get(NamespacedKey)` 사용 (권장)
- Adventure API 사용 가능

### 1.16.5 ~ 1.21.3
- `Sound.valueOf()` 사용 가능하지만 경고 표시
- Adventure API 사용 가능

### 1.16 미만
- `Sound.valueOf()` 사용

## 실제 구현 예시

```java
/**
 * 소리 재생 유틸리티
 * @param player 소리를 들을 플레이어
 * @param soundName 소리 이름 (소문자, 점 형식)
 * @param volume 볼륨 (0.0 ~ 1.0)
 * @param pitch 피치 (0.5 ~ 2.0)
 */
public void playSound(Player player, String soundName, float volume, float pitch) {
    if (soundName == null || soundName.isEmpty()) {
        return;
    }
    
    // 레거시 형식 변환 (필요한 경우)
    if (soundName.contains("_")) {
        soundName = soundName.toLowerCase().replace("_", ".");
    }
    
    try {
        // 기본 방식 시도
        var sound = Registry.SOUND_EVENT.get(NamespacedKey.minecraft(soundName));
        if (sound != null) {
            player.playSound(player.getLocation(), sound, volume, pitch);
            return;
        }
        
        // Adventure API 시도
        net.kyori.adventure.sound.Sound sound = net.kyori.adventure.sound.Sound.sound(
            Key.key("minecraft:" + soundName),
            net.kyori.adventure.sound.Sound.Source.MASTER,
            volume,
            pitch
        );
        player.playSound(sound);
    } catch (Exception e) {
        logger.warning("사운드 재생 실패: " + soundName + " - " + e.getMessage());
    }
}
```