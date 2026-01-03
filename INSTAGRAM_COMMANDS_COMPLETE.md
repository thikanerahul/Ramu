# Instagram Voice Commands - Complete Guide 📸

## Current Status ✅

Based on logs, basic commands are working:
- ✅ Scroll up/down working
- ✅ Go back working
- ✅ Wake word optional
- ✅ Service persistent

## Instagram Commands to Add 🎯

### 1. Navigation Commands

#### Stories:
```
"stories dikhao"
"stories show karo"
"stories pe jao"
"open stories"
"story dekho"
```

#### Reels:
```
"reels pe jao"
"reels dikhao"
"reels show karo"
"open reels"
"reel dekho"
```

#### Posts/Feed:
```
"posts dikhao"
"feed dikhao"
"home pe jao"
"feed show karo"
```

#### Profile:
```
"profile dikhao"
"mera profile"
"my profile"
"profile kholo"
```

### 2. Search Commands

#### Search User:
```
"Virat search karo"
"Virat ka page kholo"
"Virat kohli dhundo"
"search for Virat"
```

### 3. Action Commands

#### Like:
```
"like karo"
"like this"
"heart karo"
```

#### Comment:
```
"comment karo"
"comment likho"
```

#### Next/Previous:
```
"next story"
"next reel"
"agle story"
"pichli story"
"previous story"
```

## How Instagram Navigation Works 🔍

### Instagram Bottom Navigation:
```
Position 1: Home/Feed (🏠)
Position 2: Search (🔍)
Position 3: Reels (🎬)
Position 4: Shop (🛍️)
Position 5: Profile (👤)
```

### Stories Location:
- Top of feed
- Horizontal scroll
- Click on story circle

## Implementation Strategy 📝

### Method 1: Click by Content Description
```java
// Instagram uses content descriptions
findAndClickByContentDescription(rootNode, "Reels")
findAndClickByContentDescription(rootNode, "Profile")
findAndClickByContentDescription(rootNode, "Search and Explore")
```

### Method 2: Click by Position
```java
// Bottom navigation bar positions
clickBottomNavigation(position) {
    // Position 0 = Home
    // Position 1 = Search
    // Position 2 = Reels
    // Position 3 = Shop
    // Position 4 = Profile
}
```

### Method 3: Swipe for Stories
```java
// Horizontal swipe for next/previous story
performSwipeHorizontal(direction) {
    // Left = Previous story
    // Right = Next story
}
```

## Current Implementation Status 📊

### Already Working:
```
✅ Scroll up/down (for feed)
✅ Go back
✅ Search (basic)
✅ Next story (scroll right)
```

### Need to Add:
```
⏳ Click Reels tab
⏳ Click Profile tab
⏳ Click Stories section
⏳ Better story navigation
⏳ Like/Comment actions
```

## Testing Instagram Commands 🧪

### Test Sequence 1: Navigation
```
1. "Instagram kholo"
2. "reels pe jao" → Should click Reels tab
3. "scroll down" → Should scroll reels
4. "profile dikhao" → Should click Profile tab
5. "go back" → Should go to feed
```

### Test Sequence 2: Stories
```
1. "Instagram kholo"
2. "stories dikhao" → Should click first story
3. "next story" → Should swipe right
4. "previous story" → Should swipe left
5. "go back" → Should exit stories
```

### Test Sequence 3: Search
```
1. "Instagram kholo"
2. "Virat search karo" → Should open search, type "Virat"
3. "Virat ka page kholo" → Should click on profile
4. "go back" → Should return to search
```

## NLU Patterns to Add 🎯

### Stories Pattern:
```java
if (matchesPattern(command, ".*(stories|story).*(dikhao|show|pe jao|open|dekho).*") ||
    matchesPattern(command, ".*(open|show|dikhao).*(stories|story).*")) {
    intent.action = CommandAction.INSTAGRAM_STORIES;
    return intent;
}
```

### Reels Pattern:
```java
if (matchesPattern(command, ".*(reels|reel).*(pe jao|dikhao|show|open|dekho).*") ||
    matchesPattern(command, ".*(open|show|dikhao).*(reels|reel).*")) {
    intent.action = CommandAction.INSTAGRAM_REELS;
    return intent;
}
```

### Profile Pattern:
```java
if (matchesPattern(command, ".*(profile|प्रोफाइल).*(dikhao|show|kholo|open).*") ||
    matchesPattern(command, ".*(mera|my).*(profile|प्रोफाइल).*")) {
    intent.action = CommandAction.INSTAGRAM_PROFILE;
    return intent;
}
```

## Expected Behavior 🎬

### Command: "reels pe jao"
```
1. Detect Instagram is open
2. Find Reels tab (bottom navigation)
3. Click Reels tab
4. Speak: "Reels pe ja raha hoon"
```

### Command: "stories dikhao"
```
1. Detect Instagram is open
2. Find first story circle (top of feed)
3. Click story
4. Speak: "Stories dikha raha hoon"
```

### Command: "Virat ka page kholo"
```
1. Detect Instagram is open
2. Click search tab
3. Type "Virat" in search
4. Click first result
5. Speak: "Virat ka page khol raha hoon"
```

## Logs to Check 📝

### Instagram Navigation:
```
D/RamuAccessibilityService: Current app package: com.instagram.android
D/RamuAccessibilityService: Routing to Instagram handler
I/RamuAccessibilityService: Clicking Reels tab
```

### Story Navigation:
```
D/CommandProcessor: Intent action: INSTAGRAM_STORIES
I/RamuAccessibilityService: Opening stories
I/RamuAccessibilityService: Swiping to next story
```

## Current Working Commands (From Logs) ✅

```
✅ "scroll down" → Feed scroll
✅ "scroll up" → Feed scroll
✅ "go back" → Exit current view
✅ "Ramu scroll down" → With wake word
✅ "next story" → Swipe right (already working!)
```

## Summary 📊

### What's Working Now:
- Basic navigation (scroll, back)
- Wake word optional
- Service persistent
- Story swiping (via scroll right)

### What Needs Enhancement:
- Tab clicking (Reels, Profile, Search)
- Story opening (click first story)
- Better search integration
- Like/Comment actions

### Next Steps:
1. Add NLU patterns for Instagram commands
2. Enhance Instagram automation handler
3. Add tab clicking functionality
4. Test all commands

## Installation & Testing 📱

```bash
# Current APK already has basic functionality
adb install -r app\build\outputs\apk\debug\app-debug.apk

# Test basic commands (already working):
1. "Instagram kholo"
2. "scroll down"
3. "scroll up"
4. "go back"
5. "next story" (scroll right)

# Test new commands (after enhancement):
6. "reels pe jao"
7. "profile dikhao"
8. "stories dikhao"
9. "Virat search karo"
```

## Conclusion 🎯

**Current Status:** Basic commands working perfectly! ✅
**Logs Show:** Service running, commands executing, wake word optional ✅
**Next:** Add Instagram-specific tab navigation and enhanced story controls

**Abhi jo hai wo kaam kar raha hai. Instagram ke specific commands add karne hain for better experience! 🚀**
