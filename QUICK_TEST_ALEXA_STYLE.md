# Quick Test Guide - Alexa Style Ramu 🎤

## Install करें (Install)

```bash
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## Test Commands (बिना "Ramu" बोले)

### Basic Commands:
```
1. "WhatsApp kholo"
2. "scroll down"
3. "scroll up"
4. "go back"
5. "go home"
```

### WhatsApp Commands:
```
6. "open any chat"
7. "koi bhi chat kholo"
8. "WhatsApp se bahar nikalo"
9. "Soheb ko message bhejo"
10. "search Soheb"
```

### Story/Reel Commands:
```
11. "next story"
12. "next reel"
13. "story aage karo"
14. "scroll right"
15. "scroll left"
```

### App Control:
```
16. "Instagram kholo"
17. "YouTube kholo"
18. "camera kholo"
19. "settings kholo"
20. "Chrome kholo"
```

### Call Commands:
```
21. "Soheb ko call karo"
22. "call answer karo"
23. "call kaat do"
```

## Test with Wake Word (Optional):
```
1. "Ramu WhatsApp kholo"
2. "Ramu scroll down"
3. "Ramu open any chat"
4. "Ramu next story"
5. "Ramu bahar nikalo"
```

## Expected Behavior:

### ✅ Should Work:
- Commands WITHOUT "Ramu" wake word
- Commands WITH "Ramu" wake word
- Natural Hindi/English/Hinglish
- Fast response
- Continuous listening
- No "No wake word, ignoring" messages

### ✅ Notification Should Show:
```
"Listening for all commands..."
```

### ✅ Logs Should Show:
```
"No wake word, but processing anyway: [command]"
OR
"Wake word found and removed. Processing: [command]"
```

## Debugging:

### Check Logs:
```bash
adb logcat | findstr "VoiceListeningService CommandProcessor"
```

### Check Service Status:
```bash
adb shell dumpsys accessibility | findstr "Ramu"
```

### Restart Service:
1. Open Ramu app
2. Click "Stop Service"
3. Click "Start Service"

## Success Criteria:

- [ ] App installs without errors
- [ ] Service starts and shows notification
- [ ] Commands work WITHOUT "Ramu"
- [ ] Commands work WITH "Ramu"
- [ ] WhatsApp automation works
- [ ] Scroll commands work
- [ ] Back/Exit commands work
- [ ] No Speech Error 7
- [ ] Fast and responsive

## Common Issues:

### Issue 1: Commands not working
**Solution:** Enable Accessibility Service
```
Settings → Accessibility → Ramu → ON
```

### Issue 2: Speech Error 7
**Solution:** Already fixed! Reduced timeouts.

### Issue 3: "No wake word, ignoring"
**Solution:** Already fixed! Wake word is now optional.

## Test Results:

Date: _____________
Time: _____________

| Command | Without "Ramu" | With "Ramu" | Status |
|---------|---------------|-------------|--------|
| WhatsApp kholo | ⬜ | ⬜ | ⬜ |
| scroll down | ⬜ | ⬜ | ⬜ |
| open any chat | ⬜ | ⬜ | ⬜ |
| bahar nikalo | ⬜ | ⬜ | ⬜ |
| next story | ⬜ | ⬜ | ⬜ |
| search Soheb | ⬜ | ⬜ | ⬜ |

✅ = Working
❌ = Not Working
⬜ = Not Tested

## Notes:
_________________________________
_________________________________
_________________________________
