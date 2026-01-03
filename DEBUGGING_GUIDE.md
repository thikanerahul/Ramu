# Ramu Voice Assistant - Debugging Guide

## Voice Recognition Not Working? Follow These Steps:

### 1. Check Microphone Permission ✅
- Go to Android Settings → Apps → Ramu → Permissions
- Ensure "Microphone" permission is **ALLOWED**
- If denied, enable it and restart the app

### 2. Check Internet Connection 🌐
- Voice recognition requires **active internet connection**
- Google Speech API needs internet to work
- Test: Open browser and load a website
- Try both WiFi and mobile data

### 3. Check if Another App is Using Microphone 🎤
- Close all apps that might use microphone:
  - WhatsApp calls
  - Phone calls
  - Other voice assistants (Google Assistant, Alexa)
  - Recording apps
  - Video call apps (Zoom, Meet, etc.)
- Restart Ramu app

### 4. Test Speech Recognition 🧪
1. Open Ramu app
2. Tap the microphone button (should turn blue/animated)
3. **Immediately** say: "Ramu open camera"
4. Watch the status text at bottom:
   - Should show: "🎤 Listening..."
   - Then: "👂 Hearing you..."
   - Then: "Heard: ramu open camera"
   - Finally: "✓ Done"

### 5. Common Error Messages 🚨

#### "Didn't hear anything"
- **Cause**: You didn't speak or spoke too softly
- **Fix**: Tap mic again and speak louder and clearer

#### "No internet - Check connection"
- **Cause**: No internet connection
- **Fix**: Enable WiFi or mobile data

#### "Microphone error"
- **Cause**: Another app is using microphone
- **Fix**: Close other apps and try again

#### "Permission denied"
- **Cause**: Microphone permission not granted
- **Fix**: Go to Settings → Apps → Ramu → Permissions → Allow Microphone

#### "Speech recognizer not available"
- **Cause**: Google Speech Services not installed
- **Fix**: Install "Google" app from Play Store

### 6. Enable Accessibility Service (For Automation) ⚙️
For WhatsApp automation, Instagram, YouTube, etc.:
1. Go to Android Settings → Accessibility
2. Find "Ramu Accessibility Service"
3. Turn it **ON**
4. Grant permission

Without this, commands like "send WhatsApp message" won't work.

### 7. Check Logcat (For Developers) 💻
Connect phone to PC and run:
```bash
adb logcat | grep -E "MainActivity|CommandProcessor|RamuAccessibility"
```

Look for:
- "onReadyForSpeech" - Mic is ready
- "Speech recognized:" - Command was heard
- "Processing command:" - Command is being processed
- Any ERROR messages

### 8. Test Commands 🎯

#### Basic Commands (No Accessibility needed):
- "Ramu open camera"
- "Ramu open WhatsApp"
- "Ramu turn on Bluetooth"
- "Ramu what time is it"

#### Advanced Commands (Needs Accessibility):
- "Ramu send WhatsApp message to Rahul saying hello"
- "Ramu call Rahul on WhatsApp"
- "Ramu open Instagram and search Rahul"

### 9. Wake Word Required ⚠️
**IMPORTANT**: You MUST say "Ramu" before every command!

✅ Correct: "Ramu open WhatsApp"
❌ Wrong: "Open WhatsApp"

The app will ignore commands without "Ramu" wake word.

### 10. Still Not Working? 🔧

#### Reset Speech Recognizer:
1. Force stop Ramu app
2. Clear app cache (Settings → Apps → Ramu → Storage → Clear Cache)
3. Restart phone
4. Open Ramu app again

#### Check Google App:
1. Open Play Store
2. Search "Google"
3. Update to latest version
4. This provides speech recognition services

#### Factory Reset (Last Resort):
1. Uninstall Ramu app
2. Restart phone
3. Reinstall Ramu app
4. Grant all permissions

## Troubleshooting Specific Features

### WhatsApp Automation Not Working
1. Enable Accessibility Service (see step 6)
2. Open WhatsApp manually once to ensure it's installed
3. Try command: "Ramu send WhatsApp message to [contact name] saying hello"
4. Wait 2-3 seconds for automation to complete

### App Not Opening
- Ensure app is installed
- Try opening app manually first
- Some apps have different package names
- Example: Say "Ramu open Instagram" not "Ramu open Insta"

### Commands Not Understood
- Speak clearly and slowly
- Use simple commands
- Say "Ramu" first
- Wait for "Listening..." status before speaking
- Avoid background noise

## Debug Checklist ✓

- [ ] Microphone permission granted
- [ ] Internet connection active
- [ ] No other apps using microphone
- [ ] Google app installed and updated
- [ ] Accessibility service enabled (for automation)
- [ ] Speaking "Ramu" before commands
- [ ] Speaking immediately after mic button turns blue
- [ ] Speaking clearly and loudly enough

## Quick Test Script 🚀

Try these commands in order:

1. "Ramu hello" → Should respond with greeting
2. "Ramu what time is it" → Should tell time
3. "Ramu open camera" → Should open camera
4. "Ramu turn on Bluetooth" → Should open Bluetooth settings
5. "Ramu open WhatsApp" → Should open WhatsApp

If all 5 work, voice recognition is working perfectly!
If some fail, note which ones and check the specific troubleshooting section above.
