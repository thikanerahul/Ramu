# Ramu Voice Assistant - Testing Checklist

## ✅ Pre-Testing Setup

- [ ] App installed on Android device (API 24+)
- [ ] All permissions granted:
  - [ ] Microphone
  - [ ] Contacts
  - [ ] Phone
  - [ ] Bluetooth Connect (Android 12+)
- [ ] Accessibility Service enabled for Ramu
- [ ] Internet connection active
- [ ] At least one contact saved in phone

## 🎤 Voice Recognition Tests

### Basic Listening
- [ ] Mic button shows animation when listening
- [ ] Status text updates to "Listening..."
- [ ] Partial results show in real-time
- [ ] Mic stays open for 10 seconds
- [ ] Auto-restarts on "No match" error

### Speech Quality
- [ ] Recognizes clear English commands
- [ ] Handles Indian English accent
- [ ] Works with background noise (moderate)
- [ ] Captures complete sentences

## 📱 App Control Tests

### Open Apps
- [ ] "Open WhatsApp" → Opens WhatsApp
- [ ] "Launch Chrome" → Opens Chrome browser
- [ ] "Start YouTube" → Opens YouTube
- [ ] "Open Gmail" → Opens Gmail
- [ ] "Launch Maps" → Opens Google Maps
- [ ] "Open Camera" → Opens camera app

## 📞 Communication Tests

### Phone Calls
- [ ] "Call [Contact Name]" → Initiates call
- [ ] "Phone [Contact Name]" → Initiates call
- [ ] "Dial [Contact Name]" → Initiates call
- [ ] Shows error if contact not found
- [ ] Speaks confirmation before calling

### WhatsApp Messages
- [ ] "Send WhatsApp message to [Name]" → Opens WhatsApp
- [ ] Accessibility service finds contact
- [ ] Types message automatically
- [ ] Clicks send button
- [ ] Speaks confirmation

### SMS Messages
- [ ] "Send SMS to [Name]" → Opens SMS app
- [ ] Pre-fills contact number
- [ ] Pre-fills message text
- [ ] User can review before sending

## 🔧 System Control Tests

### Bluetooth
- [ ] "Turn on Bluetooth" → Shows Bluetooth enable dialog
- [ ] "Enable Bluetooth" → Shows Bluetooth enable dialog
- [ ] "Bluetooth on" → Shows Bluetooth enable dialog
- [ ] "Turn off Bluetooth" → Disables Bluetooth
- [ ] "Disable Bluetooth" → Disables Bluetooth
- [ ] Speaks confirmation for each action

### WiFi
- [ ] "Turn on WiFi" → Opens WiFi settings (Android 10+)
- [ ] "Enable WiFi" → Opens WiFi settings (Android 10+)
- [ ] "Turn off WiFi" → Opens WiFi settings (Android 10+)
- [ ] Speaks confirmation

### Camera
- [ ] "Open Camera" → Opens camera
- [ ] "Take a photo" → Opens camera
- [ ] "Take a selfie" → Opens camera

## 🔄 Background Service Tests

### Service Lifecycle
- [ ] "Start Service" button → Service starts
- [ ] Notification appears in status bar
- [ ] Button text changes to "Stop Service"
- [ ] Service continues after closing app
- [ ] "Stop Service" button → Service stops
- [ ] Notification disappears

### Continuous Listening
- [ ] Service listens continuously
- [ ] Auto-restarts after each command
- [ ] Notification updates with status
- [ ] Commands work from any screen
- [ ] Service survives screen off (with proper settings)

## 🎯 Command Processing Tests

### Natural Language Understanding
- [ ] Recognizes "open" variations (launch, start, run)
- [ ] Recognizes "call" variations (phone, dial)
- [ ] Recognizes "turn on/off" variations (enable, disable, switch)
- [ ] Extracts contact names correctly
- [ ] Extracts message text correctly
- [ ] Handles commands with extra words

### Error Handling
- [ ] Unknown command → "I didn't understand"
- [ ] Missing permission → Requests permission
- [ ] Contact not found → Speaks error message
- [ ] App not installed → Speaks error message
- [ ] Network error → Speaks error message

## 🔊 Text-to-Speech Tests

### Voice Feedback
- [ ] Speaks confirmation for each action
- [ ] Clear and understandable voice
- [ ] Appropriate volume level
- [ ] No overlapping speech
- [ ] Speaks error messages clearly

## 🎨 UI/UX Tests

### Main Screen
- [ ] Title and subtitle visible
- [ ] Mic button centered and clickable
- [ ] Status text updates correctly
- [ ] Service toggle button works
- [ ] Settings button opens permissions screen
- [ ] Instructions text helpful

### Permission Screen
- [ ] All 4 permission cards visible
- [ ] Status indicators accurate (✓/✗)
- [ ] Grant buttons work
- [ ] Buttons disable after granting
- [ ] Continue button validates all permissions
- [ ] Back navigation works

### Animations
- [ ] Mic button scales when listening
- [ ] Animation stops when done
- [ ] Smooth transitions
- [ ] No UI lag

## 🔋 Performance Tests

### Battery Usage
- [ ] Service doesn't drain battery excessively
- [ ] App doesn't overheat device
- [ ] Background service efficient

### Memory Usage
- [ ] No memory leaks
- [ ] App doesn't crash on long usage
- [ ] Service restarts if killed by system

### Response Time
- [ ] Commands execute within 2 seconds
- [ ] Voice recognition starts immediately
- [ ] No noticeable lag in UI

## 🐛 Edge Cases

### Unusual Scenarios
- [ ] Works with airplane mode off (needs internet)
- [ ] Handles multiple rapid commands
- [ ] Recovers from speech recognizer crash
- [ ] Works after phone restart
- [ ] Handles permission revocation gracefully
- [ ] Works with different Android versions

### Stress Tests
- [ ] 10 consecutive commands
- [ ] Service running for 1 hour
- [ ] Multiple app switches
- [ ] Low battery mode
- [ ] Low storage space

## 📊 Test Results Summary

**Date:** _____________
**Device:** _____________
**Android Version:** _____________
**App Version:** 1.0

**Overall Status:** ⬜ Pass ⬜ Fail ⬜ Partial

**Critical Issues Found:**
1. _____________
2. _____________
3. _____________

**Minor Issues Found:**
1. _____________
2. _____________
3. _____________

**Notes:**
_____________________________________________
_____________________________________________
_____________________________________________
