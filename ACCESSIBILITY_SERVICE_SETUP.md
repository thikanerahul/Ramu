# 🔧 Accessibility Service Setup Guide

## ⚠️ CRITICAL: Accessibility Service MUST be enabled!

Logcat se pata chala ki **commands sun rahe hain** but **actions execute nahi ho rahe** kyunki **Accessibility Service enabled nahi hai**.

---

## 📱 Accessibility Service Enable Kaise Karein:

### Method 1: Manual Setup (Recommended)
1. **Settings** kholo
2. **Accessibility** ya **सुलभता** search karo
3. **Installed Services** ya **Installed Apps** section mein jao
4. **Ramu** app dhundo
5. **Ramu Accessibility Service** ko **ON** karo
6. Confirmation dialog mein **Allow** ya **OK** press karo

### Method 2: Direct Command
```bash
# Enable via ADB (for testing)
adb shell settings put secure enabled_accessibility_services com.example.ramu/com.example.ramu.service.RamuAccessibilityService
adb shell settings put secure accessibility_enabled 1
```

---

## ✅ Verify Service is Running:

### Check via Logcat:
```bash
adb logcat | findstr "RamuAccessibilityService"
```

**Expected Output**:
```
RamuAccessibilityService: ✅ Service CREATED and INSTANCE SET
```

### Check via Settings:
- Settings → Accessibility → Ramu
- Should show **ON** or **Enabled**

---

## 🔍 Troubleshooting:

### Problem 1: Service Not Showing in Settings
**Solution**: 
- Reinstall app: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- Restart phone
- Check if `accessibility_service_config.xml` exists

### Problem 2: Service Keeps Turning Off
**Solution**:
- Check battery optimization settings
- Disable battery saver for Ramu app
- Settings → Apps → Ramu → Battery → Unrestricted

### Problem 3: Commands Heard But Not Executing
**Symptoms**:
```
CommandProcessor: Intent action: SEARCH ✅
CommandProcessor: Accessibility Service NOT FOUND! ❌
```

**Solution**:
1. Enable Accessibility Service (see above)
2. Restart app
3. Test with: "Ramu go back"
4. Check logcat for: `RamuAccessibilityService: ✅ Service CREATED`

---

## 🎯 Test After Enabling:

### Test Commands:
```
1. "Ramu go back" → Should go back
2. "Ramu scroll down" → Should scroll
3. "Ramu search test" → Should search in current app
```

### Expected Logcat:
```
CommandProcessor: performGlobalAction called with action: back
CommandProcessor: Accessibility Service found, performing action
RamuAccessibilityService: performBack called
CommandProcessor: Back action performed
```

---

## 📋 Complete Setup Checklist:

- [ ] App installed
- [ ] Microphone permission granted
- [ ] Contacts permission granted
- [ ] Phone permission granted
- [ ] **Accessibility Service enabled** ⭐ MOST IMPORTANT
- [ ] Service showing in Settings → Accessibility
- [ ] Test command works ("Ramu go back")
- [ ] Logcat shows service logs

---

## 🚨 Common Mistakes:

1. ❌ **Forgetting to enable Accessibility Service**
   - This is the #1 reason commands don't work!
   
2. ❌ **Enabling wrong service**
   - Make sure it says "Ramu Accessibility Service"
   
3. ❌ **Not restarting app after enabling**
   - Close and reopen Ramu app after enabling service

---

## 💡 Pro Tips:

1. **Always check logcat first**:
   ```bash
   adb logcat | findstr "CommandProcessor\|RamuAccessibilityService"
   ```

2. **If you see "Accessibility Service NOT FOUND!"**:
   - Service is not enabled
   - Go to Settings → Accessibility → Enable Ramu

3. **Test with simple command first**:
   - "Ramu go back" is easiest to test
   - Should immediately go back if service is working

---

## 🎉 Success Indicators:

✅ Logcat shows: `RamuAccessibilityService: ✅ Service CREATED`
✅ Commands execute (back, scroll, search work)
✅ No "Accessibility Service NOT FOUND!" errors
✅ Settings shows Ramu service as ON

---

## 📞 Still Not Working?

Share logcat output:
```bash
adb logcat -d > logcat.txt
```

Look for these lines:
- `RamuAccessibilityService: ✅ Service CREATED` (should be present)
- `CommandProcessor: Accessibility Service NOT FOUND!` (should NOT be present)
- `CommandProcessor: Accessibility Service found` (should be present)
