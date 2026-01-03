# Complete Feature Requirements - Ramu Voice Assistant

## Current Status (From Logs):

### ✅ Working:
- Apps opening (Instagram, Contacts, etc.)
- Wake word detection ("Ramu")
- Continuous listening
- Auto-restart

### ❌ Not Working:
1. **Search commands** → Going to GENERAL_QUERY instead of search action
2. **Go back** → Accessibility service not enabled
3. **Scroll** → Not implemented properly
4. **Call receive** → Not implemented
5. **Contact search** → Not working in contacts app
6. **Navigation** → Home, back not working

## Required Features:

### 1. Search Commands:
```
"Ramu search Aditya" → Should search in current app
"Ramu dhundo Rahul" → Should search
"Ramu Soheb khojo" → Should search
```

### 2. Navigation Commands:
```
"Ramu go back" → Go back
"Ramu go home" → Go to home screen
"Ramu go to main page" → Go to home/main
```

### 3. Scroll Commands:
```
"Ramu scroll down" → Scroll down
"Ramu scroll up" → Scroll up
"Ramu niche jao" → Scroll down
"Ramu upar jao" → Scroll up
```

### 4. Call Commands:
```
"Ramu call Aditya" → Make call
"Ramu Aditya ko call karo" → Make call
"Ramu call receive karo" → Answer incoming call
"Ramu call cut karo" → End call
```

### 5. Contact Search:
```
"Ramu search Aditya in contacts" → Search in contacts
"Ramu contacts mein Rahul dhundo" → Search in contacts
```

### 6. App-Specific Actions:
```
"Ramu Instagram mein Soheb search karo" → Search in Instagram
"Ramu WhatsApp mein Rahul ka chat kholo" → Open chat
"Ramu YouTube pe songs search karo" → Search in YouTube
```

## Implementation Plan:

### Phase 1: Fix Search Commands ✅
- Add SEARCH action to NLUProcessor
- Detect "search", "dhundo", "khojo" patterns
- Extract search query properly

### Phase 2: Enable Accessibility Features ✅
- Ensure Accessibility Service is enabled
- Implement go back, go home
- Implement scroll up/down
- Implement click actions

### Phase 3: Call Management ⏳
- Implement call receive
- Implement call end
- Implement call reject

### Phase 4: App-Specific Search ⏳
- Search in contacts
- Search in Instagram
- Search in WhatsApp
- Search in YouTube

### Phase 5: Advanced Navigation ⏳
- Swipe left/right
- Long press
- Double tap
- Pinch zoom

## Priority Order:

1. **HIGH**: Search commands (search Aditya)
2. **HIGH**: Navigation (go back, go home)
3. **HIGH**: Scroll (scroll down/up)
4. **MEDIUM**: Call management (receive, end)
5. **MEDIUM**: Contact search
6. **LOW**: Advanced gestures

## Next Steps:

1. Add SEARCH action to NLUProcessor
2. Fix search pattern matching
3. Implement search in RamuAccessibilityService
4. Add call management actions
5. Test all features

## User Requirements (From Feedback):

> "proper jo bhi kahu suna cahiyee"
> "kisi ka name search karne ko kahu"
> "main page pr jane ko kahu"
> "scroll karne ko kahu down, right, left"
> "kisi ko call karne ko kahu"
> "kisi ka call recive karne ka bolo"

All these need to work properly!

---

**This is a MAJOR UPDATE needed. Should I proceed with implementing all these features?**
