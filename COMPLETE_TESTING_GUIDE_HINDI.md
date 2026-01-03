# पूर्ण टेस्टिंग गाइड - Ramu Voice Assistant

## 🎯 सभी Commands को Test करने का तरीका

### पहले ये करें (Setup):
1. **APK Install करें**: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
2. **सभी Permissions दें**:
   - Microphone ✅
   - Accessibility Service ✅
   - Contacts ✅
   - Phone ✅
   - Storage ✅
3. **Mic हमेशा ON रहेगा** - हर command के बाद automatically restart होगा

---

## 📱 1. APP OPENING COMMANDS

### English:
```
"Ramu open WhatsApp"
"Ramu open Instagram"
"Ramu open YouTube"
"Ramu open Chrome"
"Ramu open Camera"
```

### Short Forms (Aliases):
```
"Ramu open insta"      → Instagram खुलेगा
"Ramu open yt"         → YouTube खुलेगा
"Ramu open wa"         → WhatsApp खुलेगा
"Ramu open fb"         → Facebook खुलेगा
"Ramu open snap"       → Snapchat खुलेगा
"Ramu open cam"        → Camera खुलेगा
```

### Hindi/Marathi:
```
"Ramu कैमरा खोल"
"Ramu व्हाट्सएप खोलो"
"Ramu इंस्टा उघड"
```

**Expected Result**: App तुरंत खुलना चाहिए + "Opening [app name]" बोलना चाहिए

---

## 🔍 2. SEARCH COMMANDS (सबसे Important!)

### WhatsApp में Search:
```
"Ramu search soheb"
"Ramu dhundo soheb"
"Ramu खोज soheb"
"Ramu soheb ढूंढो"
```

**Expected Result**:
1. "Searching for soheb" बोलेगा
2. WhatsApp में search button click होगा
3. "soheb" type होगा
4. Search results दिखेंगे

### Instagram में Search:
```
"Ramu search virat kohli"
"Ramu dhundo cristiano"
```

**Expected Result**: Instagram search खुलेगा और query type होगी

### YouTube में Search:
```
"Ramu search funny videos"
"Ramu dhundo songs"
```

**Expected Result**: YouTube search bar में query type होगी

### किसी भी App में Search:
```
"Ramu search pizza"     (current app में search करेगा)
"Ramu find restaurants"
```

---

## 📞 3. CALL COMMANDS

### Normal Phone Call:
```
"Ramu call soheb"
"Ramu soheb को कॉल करो"
"Ramu phone soheb"
```

**Expected Result**: Contact search होगा और call dial होगा

### WhatsApp Call:
```
"Ramu WhatsApp call soheb"
"Ramu soheb को WhatsApp पे कॉल करो"
```

**Expected Result**: WhatsApp खुलेगा, contact search होगा, call button click होगा

### WhatsApp Video Call:
```
"Ramu WhatsApp video call soheb"
"Ramu soheb को video call करो"
```

**Expected Result**: Video call button click होगा

---

## 📲 4. CALL MANAGEMENT (Answer/End)

### Call Answer करना:
```
"Ramu answer call"
"Ramu call उठाओ"
"Ramu receive call"
"Ramu pick up"
```

**Expected Result**: 
- "Answering call" बोलेगा
- Green answer button click होगा
- Call connect होगा

### Call End करना:
```
"Ramu end call"
"Ramu call काटो"
"Ramu hang up"
"Ramu disconnect call"
```

**Expected Result**:
- "Ending call" बोलेगा
- Red end button click होगा
- Call disconnect होगा

---

## 💬 5. MESSAGE COMMANDS

### WhatsApp Message:
```
"Ramu send WhatsApp message to soheb"
"Ramu soheb को WhatsApp message भेजो"
"Ramu message soheb on WhatsApp"
```

**Expected Result**: WhatsApp खुलेगा, contact search होगा, message box खुलेगा

### SMS:
```
"Ramu send SMS to soheb"
"Ramu soheb को message भेजो"
```

**Expected Result**: SMS app खुलेगा with contact selected

---

## 🔄 6. SCROLL COMMANDS (सभी 4 Directions)

### Scroll Down (Default):
```
"Ramu scroll down"
"Ramu नीचे scroll करो"
```

### Scroll Up:
```
"Ramu scroll up"
"Ramu ऊपर scroll करो"
```

### Scroll Left:
```
"Ramu scroll left"
"Ramu बाएं scroll करो"
```

### Scroll Right:
```
"Ramu scroll right"
"Ramu दाएं scroll करो"
```

**Expected Result**: Screen तुरंत scroll होनी चाहिए (बिना speech के, instant feel के लिए)

---

## 🏠 7. NAVIGATION COMMANDS

### Go Back:
```
"Ramu go back"
"Ramu पीछे जाओ"
"Ramu back"
```

**Expected Result**: "Going back" + back button press होगा

### Go Home:
```
"Ramu go home"
"Ramu home जाओ"
"Ramu घर जाओ"
```

**Expected Result**: "Going home" + home screen पर जाएगा

---

## 🔧 8. SYSTEM CONTROLS

### Bluetooth:
```
"Ramu turn on Bluetooth"
"Ramu Bluetooth चालू करो"
"Ramu turn off Bluetooth"
"Ramu Bluetooth बंद करो"
```

### WiFi:
```
"Ramu turn on WiFi"
"Ramu WiFi चालू करो"
"Ramu turn off WiFi"
"Ramu WiFi बंद करो"
```

### Camera:
```
"Ramu open camera"
"Ramu कैमरा खोल"
"Ramu take a photo"
```

---

## 💬 9. CHAT COMMANDS

### Open Specific Chat:
```
"Ramu open soheb chat"
"Ramu soheb की chat खोल"
"Ramu open chat with soheb"
```

**Expected Result**: WhatsApp में directly soheb की chat खुलेगी

---

## 🤖 10. GENERAL QUERIES

### Time:
```
"Ramu what time is it"
"Ramu समय क्या है"
```

### Date:
```
"Ramu what's the date"
"Ramu आज की तारीख"
```

### Battery:
```
"Ramu battery level"
"Ramu बैटरी कितनी है"
```

### Help:
```
"Ramu help"
"Ramu what can you do"
```

---

## 🎭 11. GREETINGS

```
"Ramu hello"
"Ramu hi"
"Ramu namaste"
"Ramu नमस्ते"
```

**Expected Result**: Random greeting response

---

## 🛑 12. STOP SERVICE

```
"Ramu stop"
"Ramu sleep"
"Ramu band ho jao"
"Ramu go to sleep"
```

**Expected Result**: "Goodbye friend! Take care." + service stop होगी

---

## ✅ TESTING CHECKLIST

### हर Command के लिए Check करें:

1. **Wake Word Detection** ✅
   - "Ramu" बोलना जरूरी है
   - बिना "Ramu" के command ignore होगी

2. **Speech Recognition** ✅
   - 10 seconds listening window
   - Clear audio capture
   - Proper transcription

3. **Command Execution** ✅
   - Correct action trigger
   - Proper TTS feedback
   - Expected result achieved

4. **Continuous Listening** ✅
   - Mic auto-restart after command (1.5s delay)
   - Mic auto-restart after error (1s delay)
   - Always-on feeling

5. **Multi-Language Support** ✅
   - English commands work
   - Hindi commands work
   - Marathi commands work
   - Hinglish mix works

---

## 🐛 DEBUGGING TIPS

### अगर Command काम नहीं कर रहा:

1. **Check Logcat**:
```bash
adb logcat | findstr "CommandProcessor"
adb logcat | findstr "NLUProcessor"
adb logcat | findstr "VoiceListening"
```

2. **Check Permissions**:
   - Settings → Apps → Ramu → Permissions
   - Settings → Accessibility → Ramu Accessibility Service

3. **Check Mic**:
   - Status text में "🎤 Listening..." दिखना चाहिए
   - Mic button green होनी चाहिए

4. **Check Wake Word**:
   - हमेशा "Ramu" से start करें
   - "Ram", "Ramoo" भी काम करेगा

---

## 🎯 EXPECTED BEHAVIOR

### Perfect Working Scenario:

1. **App खोलते ही**: Mic ON, "🎤 Listening..." दिखेगा
2. **Command बोलें**: "Ramu open WhatsApp"
3. **Recognition**: "✅ Heard: ramu open whatsapp"
4. **Processing**: "Processing command after wake word removal: open whatsapp"
5. **Action**: WhatsApp खुलेगा
6. **Feedback**: "Opening WhatsApp" (TTS)
7. **Auto-Restart**: 1.5 seconds बाद mic फिर से ON
8. **Ready**: अगली command के लिए ready

---

## 🚀 ADVANCED TESTING

### Multi-Step Commands:
```
1. "Ramu open WhatsApp"
2. Wait for app to open
3. "Ramu search soheb"
4. Wait for search
5. "Ramu scroll down"
6. "Ramu go back"
```

### App-Specific Actions:
```
1. "Ramu open Instagram"
2. "Ramu search virat kohli"
3. "Ramu scroll down"
4. "Ramu like"
```

---

## 💡 PRO TIPS

1. **Clear Speech**: Clearly बोलें, background noise कम रखें
2. **Pause After Wake Word**: "Ramu" बोलने के बाद slight pause
3. **Natural Language**: "Ramu insta khol" भी काम करेगा
4. **Contact Names**: Exactly वैसे बोलें जैसे contacts में saved है
5. **Continuous Mode**: हर command के बाद wait करें, mic auto-restart होगा

---

## 🎉 SUCCESS CRITERIA

✅ सभी apps खुल रहे हैं  
✅ Search हर app में काम कर रहा है  
✅ Scroll सभी 4 directions में काम कर रहा है  
✅ Calls answer/end हो रहे हैं  
✅ Navigation (back/home) काम कर रहा है  
✅ Multi-language support काम कर रहा है  
✅ Continuous listening काम कर रहा है  
✅ TTS feedback proper मिल रहा है  

---

## 📞 CONTACT TESTING EXAMPLES

अपने real contacts के साथ test करें:
```
"Ramu call [your friend's name]"
"Ramu search [your friend's name]"
"Ramu open [your friend's name] chat"
"Ramu send WhatsApp message to [your friend's name]"
```

---

**सब कुछ properly काम करना चाहिए! 🚀**

अगर कोई भी command काम नहीं कर रहा, तो logcat check करें और बताएं कि क्या error आ रहा है.
