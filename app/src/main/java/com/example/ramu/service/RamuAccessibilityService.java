package com.example.ramu.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class RamuAccessibilityService extends AccessibilityService {

    private static RamuAccessibilityService instance;
    private String targetContact = null;
    private String targetMessage = null;
    private String targetAction = null;
    private String targetSearchQuery = null;
    private boolean isAutomating = false;
    private boolean isWhatsAppCall = false;
    private boolean isVideoCall = false;
    private String currentApp = "";

    public static RamuAccessibilityService getInstance() {
        return instance;
    }

    // Reset all automation state - call this before starting new automation
    private void resetAutomationState() {
        android.util.Log.d("RamuAccessibilityService", "Resetting automation state");
        targetContact = null;
        targetMessage = null;
        targetAction = null;
        targetSearchQuery = null;
        isAutomating = false;
        isWhatsAppCall = false;
        isVideoCall = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        android.util.Log.i("RamuAccessibilityService", "✅ Service CREATED and INSTANCE SET");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isAutomating) {
            return;
        }

        android.util.Log.d("RamuAccessibilityService", "onAccessibilityEvent - isAutomating=true");

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            android.util.Log.w("RamuAccessibilityService", "Root node is null");
            return;
        }

        String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
        android.util.Log.d("RamuAccessibilityService", "Current package: " + packageName);

        // Universal app automation based on package name
        if (packageName.contains("whatsapp")) {
            android.util.Log.d("RamuAccessibilityService", "Handling WhatsApp automation");
            handleWhatsAppAutomation(rootNode);
        } else if (packageName.contains("instagram")) {
            android.util.Log.d("RamuAccessibilityService", "Handling Instagram automation");
            handleInstagramAutomation(rootNode);
        } else if (packageName.contains("youtube")) {
            android.util.Log.d("RamuAccessibilityService", "Handling YouTube automation");
            handleYouTubeAutomation(rootNode);
        } else if (packageName.contains("gmail") || packageName.contains("mail")) {
            android.util.Log.d("RamuAccessibilityService", "Handling Gmail automation");
            handleGmailAutomation(rootNode);
        } else if (packageName.contains("chrome") || packageName.contains("browser")) {
            android.util.Log.d("RamuAccessibilityService", "Handling Browser automation");
            handleBrowserAutomation(rootNode);
        } else if (packageName.contains("facebook")) {
            android.util.Log.d("RamuAccessibilityService", "Handling Facebook automation");
            handleFacebookAutomation(rootNode);
        } else if (packageName.contains("twitter") || packageName.contains("x.com")) {
            android.util.Log.d("RamuAccessibilityService", "Handling Twitter automation");
            handleTwitterAutomation(rootNode);
        } else {
            android.util.Log.d("RamuAccessibilityService", "Handling Generic app automation");
            // Generic automation for any app
            handleGenericAppAutomation(rootNode);
        }

        rootNode.recycle();
    }

    private void handleWhatsAppAutomation(AccessibilityNodeInfo rootNode) {
        if (targetContact != null) {
            // Special case: "any" means open first available chat
            if (targetContact.equalsIgnoreCase("any")) {
                android.util.Log.d("RamuAccessibilityService", "Opening any available chat");
                // Try to click on first chat in the list
                if (findAndClickFirstChat(rootNode)) {
                    targetContact = null;
                    isAutomating = false;
                    return;
                }
            }

            // Step 1: Find and click search button
            if (findAndClickByText(rootNode, "Search") ||
                    findAndClickByContentDescription(rootNode, "Search")) {
                return;
            }

            // Step 2: Type contact name in search
            if (findAndTypeInEditText(rootNode, targetContact)) {
                return;
            }

            // Step 3: Click on contact from search results
            if (findAndClickByText(rootNode, targetContact)) {
                targetContact = null; // Contact found
                return;
            }
        }

        // WhatsApp Call handling
        if (isWhatsAppCall && targetContact == null) {
            // Contact found, now click call button
            if (isVideoCall) {
                if (findAndClickByContentDescription(rootNode, "Video call") ||
                        findAndClickByText(rootNode, "Video call")) {
                    isAutomating = false;
                    isWhatsAppCall = false;
                    return;
                }
            } else {
                if (findAndClickByContentDescription(rootNode, "Voice call") ||
                        findAndClickByContentDescription(rootNode, "Call") ||
                        findAndClickByText(rootNode, "Call")) {
                    isAutomating = false;
                    isWhatsAppCall = false;
                    return;
                }
            }
        }

        if (targetMessage != null && targetContact == null && !isWhatsAppCall) {
            // Step 4: Type message
            if (findAndTypeInMessageBox(rootNode, targetMessage)) {
                targetMessage = null; // Message typed
                return;
            }

            // Step 5: Click send button
            if (findAndClickSendButton(rootNode)) {
                isAutomating = false; // Done
            }
        }
    }

    // Helper to find and click first available chat
    private boolean findAndClickFirstChat(AccessibilityNodeInfo node) {
        if (node == null)
            return false;

        // Look for chat items - they usually have specific view IDs or class names
        // Try to find RecyclerView or ListView containing chats
        if (node.getClassName() != null) {
            String className = node.getClassName().toString();
            if (className.contains("RecyclerView") || className.contains("ListView")) {
                // Found the chat list, click on first child
                if (node.getChildCount() > 0) {
                    AccessibilityNodeInfo firstChild = node.getChild(0);
                    if (firstChild != null && firstChild.isClickable()) {
                        firstChild.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        android.util.Log.d("RamuAccessibilityService", "Clicked first chat");
                        return true;
                    }
                }
            }
        }

        // Recursively search children
        for (int i = 0; i < node.getChildCount(); i++) {
            if (findAndClickFirstChat(node.getChild(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean findAndClickByText(AccessibilityNodeInfo node, String text) {
        if (node == null)
            return false;

        List<AccessibilityNodeInfo> nodes = node.findAccessibilityNodeInfosByText(text);
        for (AccessibilityNodeInfo n : nodes) {
            if (n.isClickable()) {
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return true;
            }
        }
        return false;
    }

    private boolean findAndClickByContentDescription(AccessibilityNodeInfo node, String desc) {
        if (node == null)
            return false;

        if (node.getContentDescription() != null &&
                node.getContentDescription().toString().contains(desc)) {
            if (node.isClickable()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return true;
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (findAndClickByContentDescription(node.getChild(i), desc)) {
                return true;
            }
        }
        return false;
    }

    private boolean findAndTypeInEditText(AccessibilityNodeInfo node, String text) {
        if (node == null)
            return false;

        if ("android.widget.EditText".equals(node.getClassName())) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (findAndTypeInEditText(node.getChild(i), text)) {
                return true;
            }
        }
        return false;
    }

    private boolean findAndTypeInMessageBox(AccessibilityNodeInfo node, String text) {
        if (node == null)
            return false;

        // Look for message input field
        List<AccessibilityNodeInfo> editTexts = node.findAccessibilityNodeInfosByViewId(
                "com.whatsapp:id/entry");

        if (editTexts.isEmpty()) {
            // Fallback to any EditText
            return findAndTypeInEditText(node, text);
        }

        for (AccessibilityNodeInfo editText : editTexts) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            editText.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            return true;
        }
        return false;
    }

    // Global Actions
    public boolean performBack() {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    public boolean performHome() {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    // Scrolling with gesture support
    public void performScroll(boolean scrollDown) {
        android.util.Log.d("RamuAccessibilityService", "performScroll called - scrollDown: " + scrollDown);

        // CRITICAL FIX: Use gesture scrolling DIRECTLY for modern apps
        // Traditional scrollable nodes return true but don't actually scroll
        android.util.Log.d("RamuAccessibilityService", "Using gesture scroll for reliable scrolling");
        performGestureScroll(scrollDown);
    }

    // Horizontal scrolling (left/right) with gesture support
    public void performScrollHorizontal(boolean scrollRight) {
        android.util.Log.d("RamuAccessibilityService", "performScrollHorizontal called - scrollRight: " + scrollRight);

        // CRITICAL FIX: Use gesture scrolling DIRECTLY for modern apps
        // Traditional scrollable nodes return true but don't actually scroll
        android.util.Log.d("RamuAccessibilityService", "Using gesture horizontal scroll for reliable scrolling");
        performGestureScrollHorizontal(scrollRight);
    }

    // Gesture-based vertical scroll (for modern apps)
    private void performGestureScroll(boolean scrollDown) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            android.util.Log.d("RamuAccessibilityService", "Performing gesture scroll - down: " + scrollDown);

            // Get screen dimensions
            android.graphics.Rect bounds = new android.graphics.Rect();
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) {
                root.getBoundsInScreen(bounds);
                root.recycle();
            }

            // Calculate swipe coordinates (middle of screen, vertical swipe)
            int centerX = bounds.centerX();
            int startY = scrollDown ? bounds.bottom - 200 : bounds.top + 200;
            int endY = scrollDown ? bounds.top + 200 : bounds.bottom - 200;

            // Create swipe gesture path
            android.graphics.Path path = new android.graphics.Path();
            path.moveTo(centerX, startY);
            path.lineTo(centerX, endY);

            // Create gesture description
            android.accessibilityservice.GestureDescription.StrokeDescription stroke = new android.accessibilityservice.GestureDescription.StrokeDescription(
                    path, 0, 100);

            android.accessibilityservice.GestureDescription.Builder builder = new android.accessibilityservice.GestureDescription.Builder();
            builder.addStroke(stroke);

            // Dispatch gesture
            boolean dispatched = dispatchGesture(builder.build(), new GestureResultCallback() {
                @Override
                public void onCompleted(android.accessibilityservice.GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    android.util.Log.d("RamuAccessibilityService", "Gesture scroll completed successfully");
                }

                @Override
                public void onCancelled(android.accessibilityservice.GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    android.util.Log.e("RamuAccessibilityService", "Gesture scroll cancelled");
                }
            }, null);

            android.util.Log.d("RamuAccessibilityService", "Gesture dispatched: " + dispatched);
        } else {
            android.util.Log.w("RamuAccessibilityService", "Gesture API not available on this Android version");
        }
    }

    // Gesture-based horizontal scroll (for stories/reels)
    private void performGestureScrollHorizontal(boolean scrollRight) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            android.util.Log.d("RamuAccessibilityService",
                    "Performing gesture horizontal scroll - right: " + scrollRight);

            // Get screen dimensions
            android.graphics.Rect bounds = new android.graphics.Rect();
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) {
                root.getBoundsInScreen(bounds);
                root.recycle();
            }

            // Calculate swipe coordinates (middle of screen, horizontal swipe)
            int centerY = bounds.centerY();
            int startX = scrollRight ? bounds.right - 200 : bounds.left + 200;
            int endX = scrollRight ? bounds.left + 200 : bounds.right - 200;

            // Create swipe gesture path
            android.graphics.Path path = new android.graphics.Path();
            path.moveTo(startX, centerY);
            path.lineTo(endX, centerY);

            // Create gesture description
            android.accessibilityservice.GestureDescription.StrokeDescription stroke = new android.accessibilityservice.GestureDescription.StrokeDescription(
                    path, 0, 100);

            android.accessibilityservice.GestureDescription.Builder builder = new android.accessibilityservice.GestureDescription.Builder();
            builder.addStroke(stroke);

            // Dispatch gesture
            boolean dispatched = dispatchGesture(builder.build(), new GestureResultCallback() {
                @Override
                public void onCompleted(android.accessibilityservice.GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    android.util.Log.d("RamuAccessibilityService", "Gesture horizontal scroll completed successfully");
                }

                @Override
                public void onCancelled(android.accessibilityservice.GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    android.util.Log.e("RamuAccessibilityService", "Gesture horizontal scroll cancelled");
                }
            }, null);

            android.util.Log.d("RamuAccessibilityService", "Horizontal gesture dispatched: " + dispatched);
        } else {
            android.util.Log.w("RamuAccessibilityService", "Gesture API not available on this Android version");
        }
    }

    private boolean findAndScroll(AccessibilityNodeInfo node, boolean scrollDown) {
        if (node == null)
            return false;

        if (node.isScrollable()) {
            int action = scrollDown ? AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                    : AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD;
            node.performAction(action);
            return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (findAndScroll(node.getChild(i), scrollDown)) {
                return true;
            }
        }
        return false;
    }

    private boolean findAndScrollHorizontal(AccessibilityNodeInfo node, boolean scrollRight) {
        if (node == null)
            return false;

        if (node.isScrollable()) {
            // Try horizontal scroll actions
            int action = scrollRight ? AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                    : AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD;
            node.performAction(action);
            return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (findAndScrollHorizontal(node.getChild(i), scrollRight)) {
                return true;
            }
        }
        return false;
    }

    // Clicking by Text
    public boolean performClick(String text) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null)
            return false;

        boolean clicked = findAndClickByText(root, text) ||
                findAndClickByContentDescription(root, text);
        root.recycle();
        return clicked;
    }

    // Open Specific Chat
    public void openWhatsAppChat(String contactName) {
        this.targetContact = contactName;
        this.targetMessage = null; // No message, just open chat
        this.isAutomating = true;

        android.util.Log.d("RamuAccessibilityService", "openWhatsAppChat called with: " + contactName);

        // If we are already in WhatsApp, trigger automation immediately
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null && root.getPackageName() != null &&
                root.getPackageName().toString().contains("whatsapp")) {
            handleWhatsAppAutomation(root);
            root.recycle();
        } else {
            // Otherwise, launch WhatsApp first
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    private boolean findAndClickSendButton(AccessibilityNodeInfo node) {
        if (node == null)
            return false;

        // Look for send button
        List<AccessibilityNodeInfo> sendButtons = node.findAccessibilityNodeInfosByViewId(
                "com.whatsapp:id/send");

        for (AccessibilityNodeInfo button : sendButtons) {
            if (button.isClickable()) {
                button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return true;
            }
        }

        // Fallback: look for content description
        return findAndClickByContentDescription(node, "Send");
    }

    public void sendWhatsAppMessage(String contact, String message) {
        resetAutomationState(); // Reset previous state
        this.targetContact = contact;
        this.targetMessage = message;
        this.isAutomating = true;
        this.isWhatsAppCall = false;
    }

    public void makeWhatsAppCall(String contact, boolean isVideo) {
        resetAutomationState(); // Reset previous state
        this.targetContact = contact;
        this.targetMessage = null;
        this.isAutomating = true;
        this.isWhatsAppCall = true;
        this.isVideoCall = isVideo;
        this.currentApp = "whatsapp";
    }

    // Type message in current chat (without sending)
    public void typeMessageInCurrentChat(String message) {
        android.util.Log.d("RamuAccessibilityService", "typeMessageInCurrentChat called with: " + message);
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            // Find message input box and type
            boolean typed = findAndTypeInEditText(root, message);
            if (typed) {
                android.util.Log.i("RamuAccessibilityService", "Successfully typed message: " + message);
            } else {
                android.util.Log.w("RamuAccessibilityService", "Could not find message input box");
            }
            root.recycle();
        }
    }

    // Send message in current chat (press send button)
    public void sendMessageInCurrentChat() {
        android.util.Log.d("RamuAccessibilityService", "sendMessageInCurrentChat called");
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            // Find and click send button
            boolean sent = findAndClickSendButton(root);
            if (sent) {
                android.util.Log.i("RamuAccessibilityService", "Successfully clicked send button");
            } else {
                android.util.Log.w("RamuAccessibilityService", "Could not find send button");
            }
            root.recycle();
        }
    }

    // UNIVERSAL IN-APP OPEN - Open anything within current app
    public void openInCurrentApp(String target) {
        android.util.Log.d("RamuAccessibilityService", "openInCurrentApp called with: " + target);

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            android.util.Log.w("RamuAccessibilityService", "Root node is null");
            return;
        }

        String packageName = root.getPackageName() != null ? root.getPackageName().toString() : "";
        android.util.Log.d("RamuAccessibilityService", "Current app: " + packageName + ", Opening: " + target);

        // Strategy 1: Try to find and click by text (exact match)
        if (findAndClickByText(root, target)) {
            android.util.Log.i("RamuAccessibilityService", "Clicked on: " + target + " (exact match)");
            root.recycle();
            return;
        }

        // Strategy 2: Try to find and click by content description
        if (findAndClickByContentDescription(root, target)) {
            android.util.Log.i("RamuAccessibilityService", "Clicked on: " + target + " (content description)");
            root.recycle();
            return;
        }

        // Strategy 3: Try common variations
        String[] variations = generateVariations(target);
        for (String variation : variations) {
            if (findAndClickByText(root, variation) || findAndClickByContentDescription(root, variation)) {
                android.util.Log.i("RamuAccessibilityService", "Clicked on: " + variation + " (variation)");
                root.recycle();
                return;
            }
        }

        android.util.Log.w("RamuAccessibilityService", "Could not find: " + target + " in current app");
        root.recycle();
    }

    // Generate common variations of target text
    private String[] generateVariations(String target) {
        String lower = target.toLowerCase();
        java.util.List<String> variations = new java.util.ArrayList<>();

        // Add original
        variations.add(target);

        // Add capitalized
        if (target.length() > 0) {
            variations.add(Character.toUpperCase(target.charAt(0)) + target.substring(1).toLowerCase());
        }

        // Add all caps
        variations.add(target.toUpperCase());

        // Add common mappings
        if (lower.contains("chat")) {
            variations.add("Chats");
            variations.add("Messages");
            variations.add("Direct");
            variations.add("DM");
            variations.add("Conversations");
        }

        if (lower.contains("location")) {
            variations.add("Location");
            variations.add("Map");
            variations.add("Share location");
            variations.add("Send location");
        }

        if (lower.contains("profile")) {
            variations.add("Profile");
            variations.add("My profile");
            variations.add("View profile");
            variations.add("Account");
        }

        if (lower.contains("settings")) {
            variations.add("Settings");
            variations.add("Options");
            variations.add("Preferences");
            variations.add("More");
        }

        if (lower.contains("camera")) {
            variations.add("Camera");
            variations.add("Take photo");
            variations.add("Capture");
        }

        if (lower.contains("story") || lower.contains("stories")) {
            variations.add("Story");
            variations.add("Stories");
            variations.add("Add story");
            variations.add("Your story");
        }

        if (lower.contains("search")) {
            variations.add("Search");
            variations.add("Find");
            variations.add("Search and Explore");
        }

        if (lower.contains("notification")) {
            variations.add("Notifications");
            variations.add("Activity");
            variations.add("Alerts");
        }

        return variations.toArray(new String[0]);
    }

    // UNIVERSAL CHAT OPENING - Works in ANY app (WhatsApp, Snapchat, Instagram,
    // Telegram, etc.)
    public void openChatInCurrentApp(String contactName) {
        android.util.Log.d("RamuAccessibilityService", "openChatInCurrentApp called with: " + contactName);

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            android.util.Log.w("RamuAccessibilityService", "Root node is null");
            return;
        }

        String packageName = root.getPackageName() != null ? root.getPackageName().toString() : "";
        android.util.Log.d("RamuAccessibilityService", "Current app package: " + packageName);

        // Detect which app is currently open and open chat accordingly
        if (packageName.contains("whatsapp")) {
            android.util.Log.d("RamuAccessibilityService", "Opening WhatsApp chat");
            openWhatsAppChat(contactName);
        } else if (packageName.contains("snapchat")) {
            android.util.Log.d("RamuAccessibilityService", "Opening Snapchat chat");
            openSnapchatChat(contactName);
        } else if (packageName.contains("instagram")) {
            android.util.Log.d("RamuAccessibilityService", "Opening Instagram chat");
            openInstagramChat(contactName);
        } else if (packageName.contains("telegram")) {
            android.util.Log.d("RamuAccessibilityService", "Opening Telegram chat");
            openTelegramChat(contactName);
        } else if (packageName.contains("facebook")) {
            android.util.Log.d("RamuAccessibilityService", "Opening Facebook Messenger chat");
            openFacebookChat(contactName);
        } else {
            // Generic chat opening for any messaging app
            android.util.Log.d("RamuAccessibilityService", "Opening chat in generic app");
            openGenericChat(root, contactName);
        }

        root.recycle();
    }

    // Snapchat specific chat opening
    private void openSnapchatChat(String contactName) {
        this.targetContact = contactName;
        this.targetMessage = null;
        this.isAutomating = true;
        this.currentApp = "snapchat";

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            // Try to find and click on contact name in Snapchat
            if (findAndClickByText(root, contactName)) {
                android.util.Log.i("RamuAccessibilityService", "Clicked on Snapchat contact: " + contactName);
                resetAutomationState();
            } else {
                // Try to open search and search for contact
                if (findAndClickByContentDescription(root, "Search") ||
                        findAndClickByText(root, "Search")) {
                    android.util.Log.i("RamuAccessibilityService", "Opened Snapchat search");
                    // Type contact name in search
                    new android.os.Handler().postDelayed(() -> {
                        AccessibilityNodeInfo searchRoot = getRootInActiveWindow();
                        if (searchRoot != null) {
                            findAndTypeInEditText(searchRoot, contactName);
                            // Click on first result
                            new android.os.Handler().postDelayed(() -> {
                                AccessibilityNodeInfo resultRoot = getRootInActiveWindow();
                                if (resultRoot != null) {
                                    findAndClickByText(resultRoot, contactName);
                                    resultRoot.recycle();
                                }
                                resetAutomationState();
                            }, 1000);
                            searchRoot.recycle();
                        }
                    }, 500);
                }
            }
            root.recycle();
        }
    }

    // Instagram specific chat opening
    private void openInstagramChat(String contactName) {
        this.targetContact = contactName;
        this.targetMessage = null;
        this.isAutomating = true;
        this.currentApp = "instagram";

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            // Try to find and click on contact name
            if (findAndClickByText(root, contactName)) {
                android.util.Log.i("RamuAccessibilityService", "Clicked on Instagram contact: " + contactName);
                resetAutomationState();
            } else {
                // Try to open DM/Messages first
                if (findAndClickByContentDescription(root, "Direct") ||
                        findAndClickByContentDescription(root, "Messages") ||
                        findAndClickByText(root, "Messages")) {
                    android.util.Log.i("RamuAccessibilityService", "Opened Instagram messages");
                    // Then search for contact
                    new android.os.Handler().postDelayed(() -> {
                        AccessibilityNodeInfo dmRoot = getRootInActiveWindow();
                        if (dmRoot != null) {
                            if (findAndClickByText(dmRoot, contactName)) {
                                android.util.Log.i("RamuAccessibilityService", "Found contact in DMs");
                            }
                            dmRoot.recycle();
                        }
                        resetAutomationState();
                    }, 1000);
                }
            }
            root.recycle();
        }
    }

    // Telegram specific chat opening
    private void openTelegramChat(String contactName) {
        this.targetContact = contactName;
        this.targetMessage = null;
        this.isAutomating = true;
        this.currentApp = "telegram";

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            // Try to find and click on contact name
            if (findAndClickByText(root, contactName)) {
                android.util.Log.i("RamuAccessibilityService", "Clicked on Telegram contact: " + contactName);
                resetAutomationState();
            } else {
                // Try to open search
                if (findAndClickByContentDescription(root, "Search") ||
                        findAndClickByText(root, "Search")) {
                    android.util.Log.i("RamuAccessibilityService", "Opened Telegram search");
                    new android.os.Handler().postDelayed(() -> {
                        AccessibilityNodeInfo searchRoot = getRootInActiveWindow();
                        if (searchRoot != null) {
                            findAndTypeInEditText(searchRoot, contactName);
                            new android.os.Handler().postDelayed(() -> {
                                AccessibilityNodeInfo resultRoot = getRootInActiveWindow();
                                if (resultRoot != null) {
                                    findAndClickByText(resultRoot, contactName);
                                    resultRoot.recycle();
                                }
                                resetAutomationState();
                            }, 1000);
                            searchRoot.recycle();
                        }
                    }, 500);
                }
            }
            root.recycle();
        }
    }

    // Facebook Messenger specific chat opening
    private void openFacebookChat(String contactName) {
        this.targetContact = contactName;
        this.targetMessage = null;
        this.isAutomating = true;
        this.currentApp = "facebook";

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            // Try to find and click on contact name
            if (findAndClickByText(root, contactName)) {
                android.util.Log.i("RamuAccessibilityService", "Clicked on Facebook contact: " + contactName);
                resetAutomationState();
            }
            root.recycle();
        }
    }

    // Generic chat opening for any messaging app
    private void openGenericChat(AccessibilityNodeInfo root, String contactName) {
        android.util.Log.d("RamuAccessibilityService", "Trying generic chat opening for: " + contactName);

        // Strategy 1: Try to find contact name directly on screen and click
        if (findAndClickByText(root, contactName)) {
            android.util.Log.i("RamuAccessibilityService", "Found and clicked contact name directly");
            return;
        }

        // Strategy 2: Try to find search button and search for contact
        if (findAndClickByContentDescription(root, "Search") ||
                findAndClickByText(root, "Search")) {
            android.util.Log.i("RamuAccessibilityService", "Opened search");
            new android.os.Handler().postDelayed(() -> {
                AccessibilityNodeInfo searchRoot = getRootInActiveWindow();
                if (searchRoot != null) {
                    if (findAndTypeInEditText(searchRoot, contactName)) {
                        android.util.Log.i("RamuAccessibilityService", "Typed contact name in search");
                        // Wait and click on result
                        new android.os.Handler().postDelayed(() -> {
                            AccessibilityNodeInfo resultRoot = getRootInActiveWindow();
                            if (resultRoot != null) {
                                findAndClickByText(resultRoot, contactName);
                                resultRoot.recycle();
                            }
                        }, 1000);
                    }
                    searchRoot.recycle();
                }
            }, 500);
        }
    }

    // Instagram Automation
    public void automateInstagram(String action, String target) {
        resetAutomationState(); // Reset previous state
        this.targetAction = action;
        this.targetContact = target;
        this.isAutomating = true;
        this.currentApp = "instagram";
    }

    // YouTube Automation
    public void automateYouTube(String action, String query) {
        resetAutomationState(); // Reset previous state
        this.targetAction = action;
        this.targetSearchQuery = query;
        this.isAutomating = true;
        this.currentApp = "youtube";
    }

    // Gmail Automation
    public void automateGmail(String action, String recipient, String message) {
        resetAutomationState(); // Reset previous state
        this.targetAction = action;
        this.targetContact = recipient;
        this.targetMessage = message;
        this.isAutomating = true;
        this.currentApp = "gmail";
    }

    // Browser Automation
    public void automateBrowser(String action, String url) {
        this.targetAction = action;
        this.targetSearchQuery = url;
        this.isAutomating = true;
        this.currentApp = "browser";
    }

    // Generic App Automation
    public void automateGenericApp(String action, String target) {
        this.targetAction = action;
        this.targetSearchQuery = target;
        this.isAutomating = true;
        this.currentApp = "generic";
    }

    // Instagram specific automation
    private void handleInstagramAutomation(AccessibilityNodeInfo rootNode) {
        if (targetAction == null)
            return;

        android.util.Log.d("RamuAccessibilityService", "Instagram automation - Action: " + targetAction);

        if (targetAction.equals("search") && targetContact != null) {
            // Try to type in search box first (if already open)
            if (findAndTypeInEditText(rootNode, targetContact)) {
                android.util.Log.i("RamuAccessibilityService", "Typed in Instagram search: " + targetContact);
                targetContact = null;
                targetAction = null;
                isAutomating = false;
                return;
            }

            // Click search icon if search box not found
            if (findAndClickByContentDescription(rootNode, "Search and Explore") ||
                    findAndClickByText(rootNode, "Search")) {
                android.util.Log.i("RamuAccessibilityService", "Clicked Instagram search button");
                targetAction = "search_typing";
                return;
            }

        } else if (targetAction.equals("search_typing") && targetContact != null) {
            // Type after clicking search
            if (findAndTypeInEditText(rootNode, targetContact)) {
                android.util.Log.i("RamuAccessibilityService",
                        "Typed in Instagram search after click: " + targetContact);
                targetContact = null;
                targetAction = null;
                isAutomating = false;
            }

        } else if (targetAction.equals("profile") && targetContact != null) {
            // Search and open profile
            if (findAndClickByText(rootNode, targetContact)) {
                targetContact = null;
                targetAction = null;
                isAutomating = false;
                return;
            }
        } else if (targetAction.equals("like")) {
            // Find and click like button
            if (findAndClickByContentDescription(rootNode, "Like") ||
                    findAndClickByText(rootNode, "Like")) {
                targetAction = null;
                isAutomating = false;
            }
        } else if (targetAction.equals("comment")) {
            // Click comment button
            if (findAndClickByContentDescription(rootNode, "Comment") ||
                    findAndClickByText(rootNode, "Comment")) {
                targetAction = "type_comment";
                return;
            }
        } else if (targetAction.equals("type_comment") && targetMessage != null) {
            // Type comment
            if (findAndTypeInEditText(rootNode, targetMessage)) {
                targetMessage = null;
                targetAction = null;
                isAutomating = false;
            }
        }
    }

    // YouTube specific automation
    private void handleYouTubeAutomation(AccessibilityNodeInfo rootNode) {
        if (targetAction == null)
            return;

        if (targetAction.equals("search") && targetSearchQuery != null) {
            // Click search icon
            if (findAndClickByContentDescription(rootNode, "Search") ||
                    findAndClickByText(rootNode, "Search")) {
                return;
            }

            // Type search query
            if (findAndTypeInEditText(rootNode, targetSearchQuery)) {
                targetSearchQuery = null;
                isAutomating = false;
                return;
            }
        } else if (targetAction.equals("play")) {
            // Click first video or play button
            if (findAndClickByContentDescription(rootNode, "Play") ||
                    findAndClickByText(rootNode, "Play")) {
                isAutomating = false;
            }
        } else if (targetAction.equals("pause")) {
            // Click pause button
            if (findAndClickByContentDescription(rootNode, "Pause")) {
                isAutomating = false;
            }
        } else if (targetAction.equals("like")) {
            // Click like button
            if (findAndClickByContentDescription(rootNode, "Like") ||
                    findAndClickByText(rootNode, "Like")) {
                isAutomating = false;
            }
        } else if (targetAction.equals("subscribe")) {
            // Click subscribe button
            if (findAndClickByText(rootNode, "Subscribe") ||
                    findAndClickByContentDescription(rootNode, "Subscribe")) {
                isAutomating = false;
            }
        }
    }

    // Gmail specific automation
    private void handleGmailAutomation(AccessibilityNodeInfo rootNode) {
        if (targetAction == null)
            return;

        if (targetAction.equals("compose")) {
            // Click compose button
            if (findAndClickByContentDescription(rootNode, "Compose") ||
                    findAndClickByText(rootNode, "Compose")) {
                targetAction = "enter_recipient";
                return;
            }
        } else if (targetAction.equals("enter_recipient") && targetContact != null) {
            // Type recipient email
            if (findAndTypeInEditText(rootNode, targetContact)) {
                targetContact = null;
                targetAction = "enter_message";
                return;
            }
        } else if (targetAction.equals("enter_message") && targetMessage != null) {
            // Type email message
            if (findAndTypeInEditText(rootNode, targetMessage)) {
                targetMessage = null;
                targetAction = "send";
                return;
            }
        } else if (targetAction.equals("send")) {
            // Click send button
            if (findAndClickByContentDescription(rootNode, "Send") ||
                    findAndClickByText(rootNode, "Send")) {
                isAutomating = false;
            }
        } else if (targetAction.equals("search") && targetSearchQuery != null) {
            // Search emails
            if (findAndClickByContentDescription(rootNode, "Search") ||
                    findAndClickByText(rootNode, "Search")) {
                return;
            }
            if (findAndTypeInEditText(rootNode, targetSearchQuery)) {
                targetSearchQuery = null;
                isAutomating = false;
            }
        }
    }

    // Browser specific automation
    private void handleBrowserAutomation(AccessibilityNodeInfo rootNode) {
        if (targetAction == null)
            return;

        if (targetAction.equals("search") && targetSearchQuery != null) {
            // Click address bar
            if (findAndClickByContentDescription(rootNode, "Address bar") ||
                    findAndClickByContentDescription(rootNode, "Search") ||
                    findAndTypeInEditText(rootNode, targetSearchQuery)) {
                targetSearchQuery = null;
                isAutomating = false;
            }
        } else if (targetAction.equals("back")) {
            performBack();
            isAutomating = false;
        } else if (targetAction.equals("forward")) {
            if (findAndClickByContentDescription(rootNode, "Forward")) {
                isAutomating = false;
            }
        } else if (targetAction.equals("refresh")) {
            if (findAndClickByContentDescription(rootNode, "Refresh")) {
                isAutomating = false;
            }
        }
    }

    // Facebook specific automation
    private void handleFacebookAutomation(AccessibilityNodeInfo rootNode) {
        if (targetAction == null)
            return;

        if (targetAction.equals("search") && targetSearchQuery != null) {
            if (findAndClickByContentDescription(rootNode, "Search") ||
                    findAndClickByText(rootNode, "Search")) {
                return;
            }
            if (findAndTypeInEditText(rootNode, targetSearchQuery)) {
                targetSearchQuery = null;
                isAutomating = false;
            }
        } else if (targetAction.equals("post") && targetMessage != null) {
            // Create post
            if (findAndClickByText(rootNode, "What's on your mind")) {
                targetAction = "type_post";
                return;
            }
        } else if (targetAction.equals("type_post") && targetMessage != null) {
            if (findAndTypeInEditText(rootNode, targetMessage)) {
                targetMessage = null;
                targetAction = "publish";
                return;
            }
        } else if (targetAction.equals("publish")) {
            if (findAndClickByText(rootNode, "Post") ||
                    findAndClickByText(rootNode, "Publish")) {
                isAutomating = false;
            }
        }
    }

    // Twitter/X specific automation
    private void handleTwitterAutomation(AccessibilityNodeInfo rootNode) {
        if (targetAction == null)
            return;

        if (targetAction.equals("tweet") && targetMessage != null) {
            // Click compose tweet
            if (findAndClickByContentDescription(rootNode, "Compose") ||
                    findAndClickByText(rootNode, "Tweet")) {
                targetAction = "type_tweet";
                return;
            }
        } else if (targetAction.equals("type_tweet") && targetMessage != null) {
            if (findAndTypeInEditText(rootNode, targetMessage)) {
                targetMessage = null;
                targetAction = "post_tweet";
                return;
            }
        } else if (targetAction.equals("post_tweet")) {
            if (findAndClickByText(rootNode, "Tweet") ||
                    findAndClickByText(rootNode, "Post")) {
                isAutomating = false;
            }
        } else if (targetAction.equals("search") && targetSearchQuery != null) {
            if (findAndClickByContentDescription(rootNode, "Search") ||
                    findAndClickByText(rootNode, "Search")) {
                return;
            }
            if (findAndTypeInEditText(rootNode, targetSearchQuery)) {
                targetSearchQuery = null;
                isAutomating = false;
            }
        }
    }

    // Generic app automation - works with any app
    private void handleGenericAppAutomation(AccessibilityNodeInfo rootNode) {
        if (targetAction == null) {
            android.util.Log.d("RamuAccessibilityService", "No target action in generic handler");
            return;
        }

        android.util.Log.d("RamuAccessibilityService",
                "Generic automation - Action: " + targetAction + ", Query: " + targetSearchQuery);

        if (targetAction.equals("search") && targetSearchQuery != null) {
            // Step 1: Try to find search field first (already open)
            if (findAndTypeInEditText(rootNode, targetSearchQuery)) {
                android.util.Log.i("RamuAccessibilityService", "Typed in search field: " + targetSearchQuery);
                // Clear state after successful typing
                targetSearchQuery = null;
                targetAction = null;
                isAutomating = false;
                return;
            }

            // Step 2: If no search field found, try to click search button
            if (findAndClickByContentDescription(rootNode, "Search") ||
                    findAndClickByText(rootNode, "Search") ||
                    findAndClickByContentDescription(rootNode, "search")) {
                android.util.Log.i("RamuAccessibilityService", "Clicked search button, waiting for field...");
                // Don't clear targetSearchQuery yet - we need to type after search opens
                // Change action to indicate we're waiting to type
                targetAction = "search_typing";
                return;
            }

            android.util.Log.w("RamuAccessibilityService", "Could not find search field or button");
            // Clear state if nothing found
            targetSearchQuery = null;
            targetAction = null;
            isAutomating = false;

        } else if (targetAction.equals("search_typing") && targetSearchQuery != null) {
            // We clicked search button, now type in the field
            if (findAndTypeInEditText(rootNode, targetSearchQuery)) {
                android.util.Log.i("RamuAccessibilityService",
                        "Typed in search field after clicking: " + targetSearchQuery);
                // Clear state after successful typing
                targetSearchQuery = null;
                targetAction = null;
                isAutomating = false;
            }

        } else if (targetAction.equals("click") && targetSearchQuery != null) {
            // Click any element by text
            if (findAndClickByText(rootNode, targetSearchQuery) ||
                    findAndClickByContentDescription(rootNode, targetSearchQuery)) {
                android.util.Log.i("RamuAccessibilityService", "Clicked: " + targetSearchQuery);
                targetSearchQuery = null;
                targetAction = null;
                isAutomating = false;
            }

        } else if (targetAction.equals("type") && targetMessage != null) {
            // Type in any edit text
            if (findAndTypeInEditText(rootNode, targetMessage)) {
                android.util.Log.i("RamuAccessibilityService", "Typed message: " + targetMessage);
                targetMessage = null;
                targetAction = null;
                isAutomating = false;
            }
        }
    }

    // Search in current app
    public void performSearch(String query) {
        android.util.Log.i("RamuAccessibilityService", "performSearch called with query: " + query);

        // Reset previous automation state first
        resetAutomationState();

        // Set new automation state
        this.targetAction = "search";
        this.targetSearchQuery = query;
        this.isAutomating = true;

        // Trigger automation immediately
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            String packageName = root.getPackageName() != null ? root.getPackageName().toString() : "";
            android.util.Log.d("RamuAccessibilityService", "Current app package: " + packageName);

            // Route to appropriate handler based on current app
            if (packageName.contains("whatsapp")) {
                android.util.Log.d("RamuAccessibilityService", "Routing to WhatsApp handler");
                handleWhatsAppAutomation(root);
            } else if (packageName.contains("instagram")) {
                android.util.Log.d("RamuAccessibilityService", "Routing to Instagram handler");
                handleInstagramAutomation(root);
            } else if (packageName.contains("youtube")) {
                android.util.Log.d("RamuAccessibilityService", "Routing to YouTube handler");
                handleYouTubeAutomation(root);
            } else if (packageName.contains("gmail")) {
                android.util.Log.d("RamuAccessibilityService", "Routing to Gmail handler");
                handleGmailAutomation(root);
            } else if (packageName.contains("chrome") || packageName.contains("browser")) {
                android.util.Log.d("RamuAccessibilityService", "Routing to Browser handler");
                handleBrowserAutomation(root);
            } else if (packageName.contains("facebook")) {
                android.util.Log.d("RamuAccessibilityService", "Routing to Facebook handler");
                handleFacebookAutomation(root);
            } else if (packageName.contains("twitter")) {
                android.util.Log.d("RamuAccessibilityService", "Routing to Twitter handler");
                handleTwitterAutomation(root);
            } else {
                android.util.Log.d("RamuAccessibilityService", "Routing to Generic handler");
                // Generic search for any app
                handleGenericAppAutomation(root);
            }
            root.recycle();
        } else {
            android.util.Log.e("RamuAccessibilityService", "Root node is NULL!");
        }
    }

    // Answer incoming call
    public void answerIncomingCall() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null)
            return;

        // Try to find and click answer button
        // Common text/descriptions: "Answer", "Accept", "Pick up", "उठाओ"
        boolean answered = findAndClickByText(root, "Answer") ||
                findAndClickByText(root, "Accept") ||
                findAndClickByText(root, "Pick up") ||
                findAndClickByText(root, "उठाओ") ||
                findAndClickByContentDescription(root, "Answer") ||
                findAndClickByContentDescription(root, "Accept") ||
                findAndClickByContentDescription(root, "answer call") ||
                findAndClickByContentDescription(root, "accept call");

        if (!answered) {
            // Try to find green button (answer button is usually green)
            findAndClickGreenButton(root);
        }

        root.recycle();
    }

    // End active call
    public void endActiveCall() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null)
            return;

        // Try to find and click end call button
        // Common text/descriptions: "End call", "Hang up", "Disconnect", "काटो"
        boolean ended = findAndClickByText(root, "End call") ||
                findAndClickByText(root, "Hang up") ||
                findAndClickByText(root, "Disconnect") ||
                findAndClickByText(root, "काटो") ||
                findAndClickByContentDescription(root, "End call") ||
                findAndClickByContentDescription(root, "end call") ||
                findAndClickByContentDescription(root, "hang up") ||
                findAndClickByContentDescription(root, "disconnect");

        if (!ended) {
            // Try to find red button (end call button is usually red)
            findAndClickRedButton(root);
        }

        root.recycle();
    }

    // Helper to find and click green button (answer call)
    private boolean findAndClickGreenButton(AccessibilityNodeInfo node) {
        if (node == null)
            return false;

        // Look for clickable nodes that might be the answer button
        if (node.isClickable() && node.getClassName() != null) {
            String className = node.getClassName().toString();
            if (className.contains("Button") || className.contains("ImageButton")) {
                // This might be the answer button, click it
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return true;
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (findAndClickGreenButton(node.getChild(i))) {
                return true;
            }
        }
        return false;
    }

    // Helper to find and click red button (end call)
    private boolean findAndClickRedButton(AccessibilityNodeInfo node) {
        if (node == null)
            return false;

        // Look for clickable nodes that might be the end call button
        if (node.isClickable() && node.getClassName() != null) {
            String className = node.getClassName().toString();
            if (className.contains("Button") || className.contains("ImageButton")) {
                // This might be the end call button, click it
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return true;
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (findAndClickRedButton(node.getChild(i))) {
                return true;
            }
        }
        return false;
    }

    // Erase last word from focused EditText
    public void eraseLastWord() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null)
            return;

        AccessibilityNodeInfo focusedNode = findFocusedEditText(root);
        if (focusedNode != null && focusedNode.getText() != null) {
            String text = focusedNode.getText().toString();
            if (!text.isEmpty()) {
                // Remove last word
                int lastSpace = text.lastIndexOf(' ');
                String newText = (lastSpace != -1) ? text.substring(0, lastSpace) : "";

                android.os.Bundle arguments = new android.os.Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, newText);
                focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                android.util.Log.i("RamuAccessibilityService", "Erased last word. New text: " + newText);
            }
            focusedNode.recycle();
        }
        root.recycle();
    }

    private AccessibilityNodeInfo findFocusedEditText(AccessibilityNodeInfo node) {
        if (node == null)
            return null;
        if (node.isFocused() && node.getClassName() != null &&
                node.getClassName().toString().contains("EditText")) {
            return AccessibilityNodeInfo.obtain(node);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo found = findFocusedEditText(node.getChild(i));
            if (found != null)
                return found;
        }
        return null;
    }

    // Show WhatsApp Status
    public void showStatus(String name) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null)
            return;

        // Try to find and click "Updates" or "Status" tab
        if (findAndClickByText(root, "Updates") ||
                findAndClickByText(root, "Status") ||
                findAndClickByContentDescription(root, "Updates") ||
                findAndClickByContentDescription(root, "Status")) {

            if (name != null && !name.isEmpty() && !name.equalsIgnoreCase("Unknown")) {
                // Wait for tab to switch then find person
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    AccessibilityNodeInfo newRoot = getRootInActiveWindow();
                    if (newRoot != null) {
                        findAndClickByText(newRoot, name);
                        newRoot.recycle();
                    }
                }, 1000);
            }
        }
        root.recycle();
    }

    // Show Instagram Story
    public void showStory(String name) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null)
            return;

        if (name != null && !name.isEmpty() && !name.equalsIgnoreCase("Unknown")) {
            // Find story by content description "Story by [name]" or text
            if (!findAndClickByContentDescription(root, "Story by " + name)) {
                findAndClickByText(root, name);
            }
        } else {
            // Just click first story if no name
            findAndClickByContentDescription(root, "Story by");
        }
        root.recycle();
    }

    @Override
    public void onInterrupt() {
        isAutomating = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }
}
