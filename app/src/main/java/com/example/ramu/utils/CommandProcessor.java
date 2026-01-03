package com.example.ramu.utils;

import static com.example.ramu.utils.CommandAction.GENERAL_QUERY;
import static com.example.ramu.utils.CommandAction.GREETING;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.example.ramu.service.RamuAccessibilityService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandProcessor {

    private Context context;
    private TextToSpeech textToSpeech;
    private NLUProcessor nluProcessor;
    private String lastCommandLanguage = "en"; // Track language of last command
    private String lastCommand = null; // Track last command for "again" feature

    public CommandProcessor(Context context, TextToSpeech textToSpeech) {
        this.context = context;
        this.textToSpeech = textToSpeech;
        this.nluProcessor = new NLUProcessor();
    }

    // Detect language from command
    private String detectLanguage(String command) {
        String lower = command.toLowerCase();

        // Count Hindi/Marathi words
        int hindiCount = 0;
        String[] hindiWords = { "खोल", "खोलो", "चालू", "करो", "भेज", "भेजो", "कॉल", "फोन",
                "को", "का", "की", "है", "नहीं", "हाँ", "बंद", "ऑन", "ऑफ",
                "नीचे", "ऊपर", "बाएं", "दाएं", "पीछे", "वापस", "घर", "मुख्य" };
        for (String word : hindiWords) {
            if (lower.contains(word)) {
                hindiCount++;
            }
        }

        // Count Marathi words
        int marathiCount = 0;
        String[] marathiWords = { "उघड", "सुरू", "बंद", "पाठव", "ला", "कॅमेरा", "फोटो",
                "काढ", "वायफाय", "शोध", "माघे", "पुढे" };
        for (String word : marathiWords) {
            if (lower.contains(word)) {
                marathiCount++;
            }
        }

        // Count Hinglish patterns
        int hinglishCount = 0;
        String[] hinglishWords = { "karo", "karo", "bhejo", "chalao", "dikhao", "bataao",
                "lagao", "bulao", "dhundo", "dekho" };
        for (String word : hinglishWords) {
            if (lower.contains(word)) {
                hinglishCount++;
            }
        }

        // Determine language
        if (hindiCount > 0 || hinglishCount > 1) {
            return "hi"; // Hindi/Hinglish
        } else if (marathiCount > 0) {
            return "mr"; // Marathi
        } else {
            return "en"; // English (default)
        }
    }

    public void processCommand(String command) {
        String lowerCommand = command.toLowerCase().trim();

        // ALEXA-STYLE: Wake word is OPTIONAL, not required
        // 1. Check if Wake Word "Ramu" is present (optional)
        Pattern wakeWordPattern = Pattern.compile("(^|\\s)(ramu|ram|ramoo|रामू|राम)(\\s|$)");
        Matcher matcher = wakeWordPattern.matcher(lowerCommand);

        boolean hasWakeWord = matcher.find();

        if (hasWakeWord) {
            // Remove wake word to clean up the command for NLU
            command = matcher.replaceAll(" ").trim();
            android.util.Log.d("CommandProcessor", "Wake word found and removed. Processing: " + command);
        } else {
            // No wake word, but still process (Alexa-style)
            android.util.Log.d("CommandProcessor", "No wake word, but processing anyway: " + command);
        }

        // CHECK FOR "AGAIN" COMMAND - Repeat last command
        String cleanedCommand = command.toLowerCase().trim();
        if (cleanedCommand.matches(".*(again|dobara|phir se|फिर से|दोबारा|repeat|ek baar aur|एक बार और).*")) {
            if (lastCommand != null && !lastCommand.isEmpty()) {
                android.util.Log.d("CommandProcessor", "Repeating last command: " + lastCommand);
                speakMultiLang("Repeating last command", "Pichla command dobara kar raha hoon",
                        "Magchya command punha karto");
                // Recursively call processCommand with last command
                processCommand(lastCommand);
                return;
            } else {
                speakMultiLang("No previous command to repeat", "Koi pichla command nahi hai",
                        "Kahi magcha command nahi");
                return;
            }
        }

        // CHECK FOR "EXIT" COMMAND - Close all running apps
        if (cleanedCommand.matches(
                ".*(exit all|close all|band karo sab|बंद करो सब|sab band karo|सब बंद करो|exit everything|close everything).*")) {
            android.util.Log.d("CommandProcessor", "Exit all apps command detected");
            closeAllApps();
            return;
        }

        // Save this command as last command (for "again" feature)
        // Don't save if it's "again", "exit", or stop service commands
        if (!cleanedCommand.contains("again") &&
                !cleanedCommand.contains("dobara") &&
                !cleanedCommand.contains("exit") &&
                !cleanedCommand.contains("stop") &&
                !cleanedCommand.contains("sleep") &&
                !cleanedCommand.contains("shutdown")) {
            lastCommand = command;
            android.util.Log.d("CommandProcessor", "Saved last command: " + lastCommand);
        }

        // 2. Detect language from command
        lastCommandLanguage = detectLanguage(command);
        android.util.Log.d("CommandProcessor", "Detected language: " + lastCommandLanguage);

        android.util.Log.d("CommandProcessor", "Processing command: " + command);

        // Parse command using NLU
        CommandIntent intent = nluProcessor.parseCommand(command);

        android.util.Log.d("CommandProcessor", "Intent action: " + intent.action + ", appName: " + intent.appName);

        switch (intent.action) {
            case GREETING:
                handleGreeting();
                break;
            case GENERAL_QUERY:
                handleGeneralQuery(intent.messageText);
                break;
            case OPEN_APP:
                openApp(intent.appName);
                break;
            case WHATSAPP_MESSAGE:
                sendWhatsAppMessage(intent.contactName, intent.messageText);
                break;
            case WHATSAPP_CALL:
                makeWhatsAppCall(intent.contactName, intent.messageText);
                break;
            case MAKE_CALL:
                makeCall(intent.contactName);
                break;
            case BLUETOOTH_ON:
                toggleBluetooth(true);
                break;
            case BLUETOOTH_OFF:
                toggleBluetooth(false);
                break;
            case WIFI_ON:
                toggleWifi(true);
                break;
            case WIFI_OFF:
                toggleWifi(false);
                break;
            case OPEN_CAMERA:
                openCamera();
                break;
            case SEND_SMS:
                sendSMS(intent.contactName, intent.messageText);
                break;
            case GO_BACK:
                performGlobalAction("back");
                break;
            case GO_HOME:
                performGlobalAction("home");
                break;
            case SCROLL:
                performScroll(intent.messageText);
                break;
            case SEARCH:
                performSearch(intent.searchQuery);
                break;
            case CALL_ANSWER:
                answerCall();
                break;
            case CALL_END:
                endCall();
                break;
            case CLICK:
                performClick(intent.messageText);
                break;
            case OPEN_CHAT:
                openSpecificChat(intent.contactName);
                break;
            case TYPE_MESSAGE:
                typeMessageInChat(intent.messageText);
                break;
            case SEND_MESSAGE:
                sendMessageInChat();
                break;
            case IN_APP_OPEN:
                openInCurrentApp(intent.messageText);
                break;
            case STOP_SERVICE:
                speak("Goodbye friend! Take care.");
                // Give TTS time to speak before killing process/service
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    com.example.ramu.service.VoiceListeningService.stopService(context);
                }, 2000);
                break;
            // Instagram actions
            case INSTAGRAM_SEARCH:
                openAppWithAction("instagram", "search", intent.searchQuery, null);
                break;
            case INSTAGRAM_LIKE:
                openAppWithAction("instagram", "like", null, null);
                break;
            case INSTAGRAM_COMMENT:
                openAppWithAction("instagram", "comment", null, intent.messageText);
                break;
            // YouTube actions
            case YOUTUBE_SEARCH:
                openAppWithAction("youtube", "search", intent.searchQuery, null);
                break;
            case YOUTUBE_PLAY:
                openAppWithAction("youtube", "play", null, null);
                break;
            case YOUTUBE_LIKE:
                openAppWithAction("youtube", "like", null, null);
                break;
            // Gmail actions
            case GMAIL_COMPOSE:
                openAppWithAction("gmail", "compose", null, null);
                break;
            case GMAIL_SEND:
                openAppWithAction("gmail", "compose", intent.contactName, intent.messageText);
                break;
            // Browser actions
            case BROWSER_SEARCH:
                openAppWithAction("chrome", "search", intent.searchQuery, null);
                break;
            // Facebook actions
            case FACEBOOK_POST:
                openAppWithAction("facebook", "post", null, intent.messageText);
                break;
            // Twitter actions
            case TWITTER_TWEET:
                openAppWithAction("twitter", "tweet", null, intent.messageText);
                break;
            case SHOW_STATUS:
                showStatus(intent.contactName);
                break;
            case SHOW_STORY:
                showStory(intent.contactName);
                break;
            case ERASE_WORD:
                eraseLastWord();
                break;
            // Generic app action
            case GENERIC_APP_ACTION:
                openAppWithAction(intent.appName, intent.targetAction, intent.searchQuery, intent.messageText);
                break;
            default:
                speak("I'm here and listening. What would you like me to do?");
                break;
        }
    }

    private void showStatus(String name) {
        RamuAccessibilityService service = RamuAccessibilityService.getInstance();
        if (service != null) {
            android.util.Log.d("CommandProcessor", "Showing status for: " + name);
            speakMultiLang("Opening status", "Status khol raha hoon", "Status ughadat aahe");
            service.showStatus(name);
        }
    }

    private void showStory(String name) {
        RamuAccessibilityService service = RamuAccessibilityService.getInstance();
        if (service != null) {
            android.util.Log.d("CommandProcessor", "Showing story for: " + name);
            speakMultiLang("Showing story", "Story dikha raha hoon", "Story dakhvat aahe");
            service.showStory(name);
        }
    }

    private void eraseLastWord() {
        RamuAccessibilityService service = RamuAccessibilityService.getInstance();
        if (service != null) {
            android.util.Log.d("CommandProcessor", "Erasing last word");
            service.eraseLastWord();
        }
    }

    private void handleGreeting() {
        String[] greetingsEn = {
                "Hello! I'm Ramu. What can I do for you?",
                "Hi there! How can I help?",
                "Hey! I'm here. Just tell me what you need.",
                "Hello friend! Ready to help.",
                "Hi! What would you like me to do?"
        };

        String[] greetingsHi = {
                "Namaste! Main Ramu hoon. Kya madad chahiye?",
                "Hello! Kaise madad kar sakta hoon?",
                "Namaste! Bol do, main sun raha hoon.",
                "Haan bolo, main tayyar hoon.",
                "Namaste dost! Kya karna hai?"
        };

        String[] greetingsMr = {
                "Namaskar! Mi Ramu. Kai madad karaychay?",
                "Hello! Kai karaycha aahe?",
                "Namaskar! Sanga, mi aiktoye.",
                "Haan sanga, mi tayar aahe."
        };

        String[] greetings;
        if (lastCommandLanguage.equals("hi")) {
            greetings = greetingsHi;
        } else if (lastCommandLanguage.equals("mr")) {
            greetings = greetingsMr;
        } else {
            greetings = greetingsEn;
        }

        int random = (int) (Math.random() * greetings.length);
        speak(greetings[random]);
    }

    private void handleGeneralQuery(String query) {
        query = query.toLowerCase();

        // Time queries
        if (query.contains("time") || query.contains("समय") || query.contains("samay")) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault());
            String time = sdf.format(new java.util.Date());
            speak("The current time is " + time);
            return;
        }

        // Date queries
        if (query.contains("date") || query.contains("today") || query.contains("तारीख") || query.contains("आज")) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE, MMMM d, yyyy",
                    java.util.Locale.getDefault());
            String date = sdf.format(new java.util.Date());
            speak("Today is " + date);
            return;
        }

        // Battery queries
        if (query.contains("battery") || query.contains("बैटरी")) {
            android.content.IntentFilter ifilter = new android.content.IntentFilter(
                    android.content.Intent.ACTION_BATTERY_CHANGED);
            android.content.Intent batteryStatus = context.registerReceiver(null, ifilter);
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);
                int batteryPct = (int) ((level / (float) scale) * 100);
                speak("Your battery is at " + batteryPct + " percent");
            }
            return;
        }

        // Name queries
        if (query.contains("your name") || query.contains("who are you") ||
                query.contains("तुम्हारा नाम") || query.contains("तुम कौन हो")) {
            speak("I'm Ramu, your personal voice assistant. I can help you with calls, messages, and controlling your phone.");
            return;
        }

        // Help queries
        if (query.contains("help") || query.contains("what can you do") ||
                query.contains("मदद") || query.contains("क्या कर सकते")) {
            speak("I can open apps, make calls, send WhatsApp messages, turn on Bluetooth or WiFi, open camera, and answer basic questions about time, date, and battery.");
            return;
        }

        // Weather queries
        if (query.contains("weather") || query.contains("मौसम")) {
            speak("I don't have access to weather information right now, but I can open a weather app for you if you'd like.");
            return;
        }

        // Thank you
        if (query.contains("thank") || query.contains("thanks") || query.contains("धन्यवाद")
                || query.contains("शुक्रिया")) {
            speak("You're welcome! Happy to help.");
            return;
        }

        // Default response for unknown queries - More friendly
        speak("I'm not sure about that. Try asking me to open an app, make a call, or search for something.");
    }

    private void openApp(String appName) {
        android.util.Log.d("CommandProcessor", "openApp() called with: " + appName);

        if (appName == null || appName.isEmpty() || appName.equals("unknown")) {
            android.util.Log.e("CommandProcessor", "Invalid app name: " + appName);
            speakMultiLang("Please tell me which app to open",
                    "Kaunsa app kholna hai bataiye",
                    "Konata app ughada sangaa");
            return;
        }

        try {
            PackageManager pm = context.getPackageManager();
            Intent intent = null;
            String lowerAppName = appName.toLowerCase().trim();

            android.util.Log.d("CommandProcessor", "Opening app: " + appName);

            // Direct package name mapping for common apps
            String packageName = null;

            if (lowerAppName.contains("whatsapp") || lowerAppName.contains("whats app")) {
                packageName = "com.whatsapp";
            } else if (lowerAppName.contains("camera") || lowerAppName.contains("cam")) {
                openCamera();
                return;
            } else if (lowerAppName.contains("chrome") || lowerAppName.contains("browser")) {
                packageName = "com.android.chrome";
            } else if (lowerAppName.contains("youtube")) {
                packageName = "com.google.android.youtube";
            } else if (lowerAppName.contains("insta")) {
                packageName = "com.instagram.android";
            } else if (lowerAppName.contains("facebook") || lowerAppName.contains("fb")) {
                packageName = "com.facebook.katana";
            } else if (lowerAppName.contains("twitter") || lowerAppName.equals("x")) {
                packageName = "com.twitter.android";
            } else if (lowerAppName.contains("gmail") || lowerAppName.contains("mail")) {
                packageName = "com.google.android.gm";
            } else if (lowerAppName.contains("maps") || lowerAppName.contains("map")) {
                packageName = "com.google.android.apps.maps";
            } else if (lowerAppName.contains("snapchat") || lowerAppName.contains("snap")) {
                packageName = "com.snapchat.android";
            } else if (lowerAppName.contains("telegram")) {
                packageName = "org.telegram.messenger";
            } else if (lowerAppName.contains("spotify")) {
                packageName = "com.spotify.music";
            } else if (lowerAppName.contains("netflix")) {
                packageName = "com.netflix.mediaclient";
            } else if (lowerAppName.contains("amazon")) {
                packageName = "in.amazon.mShop.android.shopping";
            } else if (lowerAppName.contains("flipkart")) {
                packageName = "com.flipkart.android";
            } else if (lowerAppName.contains("paytm")) {
                packageName = "net.one97.paytm";
            } else if (lowerAppName.contains("gpay") || lowerAppName.contains("google pay")) {
                packageName = "com.google.android.apps.nbu.paisa.user";
            } else if (lowerAppName.contains("phonepe") || lowerAppName.contains("phone pe")) {
                packageName = "com.phonepe.app";
            } else if (lowerAppName.contains("settings")) {
                packageName = "com.android.settings";
            } else if (lowerAppName.contains("calculator")) {
                packageName = "com.android.calculator2";
            } else if (lowerAppName.contains("clock")) {
                packageName = "com.android.deskclock";
            } else if (lowerAppName.contains("calendar")) {
                packageName = "com.android.calendar";
            } else if (lowerAppName.contains("contacts")) {
                packageName = "com.android.contacts";
            } else if (lowerAppName.contains("messages") || lowerAppName.contains("sms")) {
                packageName = "com.google.android.apps.messaging";
            } else if (lowerAppName.contains("photos")) {
                packageName = "com.google.android.apps.photos";
            } else if (lowerAppName.contains("gallery")) {
                packageName = "com.android.gallery3d";
            } else if (lowerAppName.contains("play store") || lowerAppName.contains("playstore")) {
                packageName = "com.android.vending";
            }

            // Try to get launch intent
            if (packageName != null) {
                intent = pm.getLaunchIntentForPackage(packageName);
                android.util.Log.d("CommandProcessor", "Package: " + packageName + ", Intent: " + (intent != null));
            }

            // Fallback to app cache if direct mapping didn't work
            if (intent == null) {
                android.util.Log.d("CommandProcessor", "Trying app cache for: " + lowerAppName);
                AppCacheManager cacheManager = AppCacheManager.getInstance(context);
                String cachedPackage = cacheManager.findPackageName(lowerAppName);
                if (cachedPackage != null) {
                    intent = pm.getLaunchIntentForPackage(cachedPackage);
                    android.util.Log.d("CommandProcessor", "Cache found: " + cachedPackage);
                }
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                speakMultiLang("Opening " + appName,
                        appName + " khol raha hoon",
                        appName + " ughad raha aahe");
                android.util.Log.d("CommandProcessor", "Successfully opened: " + appName);
            } else {
                speakMultiLang("I couldn't find " + appName,
                        appName + " nahi mila",
                        appName + " sapadata nahi");
                android.util.Log.e("CommandProcessor", "App not found: " + appName);
            }
        } catch (Exception e) {
            speakMultiLang("Sorry, I had trouble opening that app",
                    "Sorry, app kholne mein problem aayi",
                    "Sorry, app ughadata problem aali");
            android.util.Log.e("CommandProcessor", "Error opening app: " + appName, e);
        }
    }

    private void sendWhatsAppMessage(String contactName, String message) {
        try {
            // Open WhatsApp
            Intent intent = context.getPackageManager()
                    .getLaunchIntentForPackage("com.whatsapp");
            if (intent != null) {
                context.startActivity(intent);
                speak("Sending WhatsApp message to " + contactName);

                // Use Accessibility Service to automate
                new android.os.Handler().postDelayed(() -> {
                    RamuAccessibilityService service = RamuAccessibilityService.getInstance();
                    if (service != null) {
                        service.sendWhatsAppMessage(contactName, message);
                    } else {
                        speak("Please enable Accessibility Service");
                    }
                }, 2000);
            } else {
                speak("WhatsApp is not installed");
            }
        } catch (Exception e) {
            speak("Error opening WhatsApp");
        }
    }

    private void makeWhatsAppCall(String contactName, String callType) {
        try {
            // Open WhatsApp
            Intent intent = context.getPackageManager()
                    .getLaunchIntentForPackage("com.whatsapp");
            if (intent != null) {
                context.startActivity(intent);
                boolean isVideo = callType != null && callType.equals("video");
                speak((isVideo ? "Making video call" : "Calling") + " " + contactName + " on WhatsApp");

                // Use Accessibility Service to automate
                new android.os.Handler().postDelayed(() -> {
                    RamuAccessibilityService service = RamuAccessibilityService.getInstance();
                    if (service != null) {
                        service.makeWhatsAppCall(contactName, isVideo);
                    } else {
                        speak("Please enable Accessibility Service");
                    }
                }, 2000);
            } else {
                speak("WhatsApp is not installed");
            }
        } catch (Exception e) {
            speak("Error opening WhatsApp");
        }
    }

    private void makeCall(String contactName) {
        try {
            String phoneNumber = getContactNumber(contactName);
            if (phoneNumber != null) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                speakMultiLang("Calling " + contactName,
                        contactName + " ko call kar raha hoon",
                        contactName + " la call karto aahe");
            } else {
                speakMultiLang("Contact not found for " + contactName,
                        contactName + " ka contact nahi mila",
                        contactName + " cha contact sapadata nahi");
            }
        } catch (SecurityException e) {
            speakMultiLang("Call permission denied",
                    "Call permission nahi hai",
                    "Call permission nahi aahe");
        } catch (Exception e) {
            speakMultiLang("Error making call",
                    "Call karne mein error aayi",
                    "Call karata problem aali");
        }
    }

    private void toggleBluetooth(boolean enable) {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null) {
                if (enable) {
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        speak("Turning on Bluetooth");
                    } else {
                        speak("Bluetooth is already on");
                    }
                } else {
                    if (bluetoothAdapter.isEnabled()) {
                        bluetoothAdapter.disable();
                        speak("Turning off Bluetooth");
                    } else {
                        speak("Bluetooth is already off");
                    }
                }
            }
        } catch (Exception e) {
            speak("Error toggling Bluetooth");
        }
    }

    private void toggleWifi(boolean enable) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    // Android 10+ requires user interaction
                    Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    speak("Opening WiFi settings");
                } else {
                    wifiManager.setWifiEnabled(enable);
                    speak(enable ? "Turning on WiFi" : "Turning off WiFi");
                }
            }
        } catch (Exception e) {
            speak("Error toggling WiFi");
        }
    }

    private void openCamera() {
        try {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            speak("Opening camera");
        } catch (Exception e) {
            speak("Error opening camera");
        }
    }

    private void sendSMS(String contactName, String message) {
        try {
            String phoneNumber = getContactNumber(contactName);
            if (phoneNumber != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("sms:" + phoneNumber));
                intent.putExtra("sms_body", message);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                speak("Sending message to " + contactName);
            } else {
                speak("Contact not found for " + contactName);
            }
        } catch (Exception e) {
            speak("Error sending message");
        }
    }

    private String getContactNumber(String contactName) {
        try {
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String[] projection = new String[] {
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };
            String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = new String[] { "%" + contactName + "%" };

            Cursor cursor = context.getContentResolver().query(
                    uri, projection, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(numberIndex);
                cursor.close();
                return number;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void performGlobalAction(String action) {
        android.util.Log.d("CommandProcessor", "performGlobalAction called with action: " + action);
        RamuAccessibilityService service = RamuAccessibilityService.getInstance();
        if (service != null) {
            android.util.Log.d("CommandProcessor", "Accessibility Service found, performing action");
            if (action.equals("back")) {
                if (service.performBack()) {
                    speakMultiLang("Going back", "Piche ja raha hoon", "Maage jat aahe");
                    android.util.Log.d("CommandProcessor", "Back action performed");
                }
            } else if (action.equals("home")) {
                if (service.performHome()) {
                    speakMultiLang("Going home", "Ghar ja raha hoon", "Ghari jat aahe");
                    android.util.Log.d("CommandProcessor", "Home action performed");
                }
            }
        } else {
            android.util.Log.e("CommandProcessor", "Accessibility Service NOT FOUND!");
            speakMultiLang("Please enable Ramu Accessibility Service in Settings",
                    "Settings mein Ramu Accessibility Service enable karein",
                    "Settings madhe Ramu Accessibility Service enable kara");
            // Open accessibility settings
            android.content.Intent intent = new android.content.Intent(
                    android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private void performScroll(String direction) {
        android.util.Log.d("CommandProcessor", "performScroll called with direction: " + direction);
        RamuAccessibilityService service = RamuAccessibilityService.getInstance();
        if (service != null) {
            android.util.Log.d("CommandProcessor", "Accessibility Service found, performing scroll");
            // Support all 4 directions: up, down, left, right
            if (direction.equals("left")) {
                service.performScrollHorizontal(false); // Scroll left
                android.util.Log.d("CommandProcessor", "Scrolling left");
            } else if (direction.equals("right")) {
                service.performScrollHorizontal(true); // Scroll right
                android.util.Log.d("CommandProcessor", "Scrolling right");
            } else if (direction.equals("up")) {
                service.performScroll(false); // Scroll up
                android.util.Log.d("CommandProcessor", "Scrolling up");
            } else {
                service.performScroll(true); // Scroll down (default)
                android.util.Log.d("CommandProcessor", "Scrolling down");
            }
            // No speech needed for scroll to appear instant
        } else {
            android.util.Log.e("CommandProcessor", "Accessibility Service NOT FOUND!");
            speak("Please enable Ramu Accessibility Service in Settings");
            // Open accessibility settings
            android.content.Intent intent = new android.content.Intent(
                    android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private void performSearch(String query) {
        android.util.Log.d("CommandProcessor", "performSearch called with query: " + query);
        RamuAccessibilityService service = RamuAccessibilityService.getInstance();
        if (service != null) {
            android.util.Log.d("CommandProcessor", "Accessibility Service found, performing search");
            speakMultiLang("Searching for " + query,
                    query + " dhund raha hoon",
                    query + " shodhto aahe");
            service.performSearch(query);
        } else {
            android.util.Log.e("CommandProcessor", "Accessibility Service NOT FOUND!");
            speakMultiLang("Please enable Ramu Accessibility Service in Settings",
                    "Settings mein Ramu Accessibility Service enable karein",
                    "Settings madhe Ramu Accessibility Service enable kara");
            // Open accessibility settings
            android.content.Intent intent = new android.content.Intent(
                    android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private void answerCall() {
        RamuAccessibilityService service = RamuAccessibilityService.getInstance();
        if (service != null) {
            speakMultiLang("Answering call", "Call utha raha hoon", "Call gheto aahe");
            service.answerIncomingCall();
        } else {
            speakMultiLang("Please enable Accessibility Service",
                    "Accessibility Service enable karein",
                    "Accessibility Service enable kara");
        }
    }

    private void endCall() {
        RamuAccessibilityService service = RamuAccessibilityService.getInstance();
        if (service != null) {
            speakMultiLang("Ending call", "Call kaat raha hoon", "Call band karto aahe");
            service.endActiveCall();
        } else {
            speakMultiLang("Please enable Accessibility Service",
                    "Accessibility Service enable karein",
                    "Accessibility Service enable kara");
        }
    }

    private void performClick(String text) {
        RamuAccessibilityService service = RamuAccessibilityService.getInstance();
        if (service != null) {
            if (service.performClick(text)) {
                // Clicked successfully
            } else {
                speak("I couldn't find " + text + " to click");
            }
        } else {
            speak("Please enable Accessibility Service");
        }
    }

    private void openSpecificChat(String contactName) {
        RamuAccessibilityService service = RamuAccessibilityService.getInstance();
        if (service != null) {
            speakMultiLang("Opening chat with " + contactName,
                    contactName + " ki chat khol raha hoon",
                    contactName + " chi chat ughad raha aahe");
            // Universal chat opening - works in ANY app (WhatsApp, Snapchat, Instagram,
            // Telegram, etc.)
            service.openChatInCurrentApp(contactName);
        } else {
            speakMultiLang("Please enable Accessibility Service",
                    "Accessibility Service enable karein",
                    "Accessibility Service enable kara");
        }
    }

    private void typeMessageInChat(String message) {
        RamuAccessibilityService service = RamuAccessibilityService.getInstance();
        if (service != null) {
            android.util.Log.d("CommandProcessor", "Typing message: " + message);
            speakMultiLang("Typing message",
                    "Message likh raha hoon",
                    "Message lihat aahe");
            service.typeMessageInCurrentChat(message);
        } else {
            speakMultiLang("Please enable Accessibility Service",
                    "Accessibility Service enable karein",
                    "Accessibility Service enable kara");
        }
    }

    private void sendMessageInChat() {
        RamuAccessibilityService service = RamuAccessibilityService.getInstance();
        if (service != null) {
            android.util.Log.d("CommandProcessor", "Sending message");
            speakMultiLang("Sending message",
                    "Message bhej raha hoon",
                    "Message pathavto aahe");
            service.sendMessageInCurrentChat();
        } else {
            speakMultiLang("Please enable Accessibility Service",
                    "Accessibility Service enable karein",
                    "Accessibility Service enable kara");
        }
    }

    private void openInCurrentApp(String target) {
        RamuAccessibilityService service = RamuAccessibilityService.getInstance();
        if (service != null) {
            android.util.Log.d("CommandProcessor", "Opening in current app: " + target);
            speakMultiLang("Opening " + target,
                    target + " khol raha hoon",
                    target + " ughad raha aahe");
            service.openInCurrentApp(target);
        } else {
            speakMultiLang("Please enable Accessibility Service",
                    "Accessibility Service enable karein",
                    "Accessibility Service enable kara");
        }
    }

    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    // Multi-language speak - responds in same language as command
    private void speakMultiLang(String english, String hindi, String marathi) {
        String text;
        if (lastCommandLanguage.equals("hi")) {
            text = hindi;
        } else if (lastCommandLanguage.equals("mr")) {
            text = marathi;
        } else {
            text = english;
        }
        speak(text);
    }

    // Close all running apps - Exit everything
    private void closeAllApps() {
        android.util.Log.d("CommandProcessor", "closeAllApps called - Closing all running apps");

        speakMultiLang("Closing all apps", "Sab apps band kar raha hoon", "Sarva apps band karto");

        RamuAccessibilityService service = RamuAccessibilityService.getInstance();
        if (service != null) {
            // Method 1: Go home first
            service.performHome();

            // Method 2: Open recent apps and clear all
            new android.os.Handler().postDelayed(() -> {
                // Open recent apps (Recents button)
                service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS);

                // Wait for recents to open, then try to clear all
                new android.os.Handler().postDelayed(() -> {
                    AccessibilityNodeInfo root = service.getRootInActiveWindow();
                    if (root != null) {
                        // Try to find "Clear all" button in different languages
                        boolean cleared = service.performClick("Clear all") ||
                                service.performClick("Close all") ||
                                service.performClick("सभी बंद करें") ||
                                service.performClick("सब बंद करें") ||
                                service.performClick("Clear") ||
                                service.performClick("सभी साफ़ करें");

                        if (cleared) {
                            android.util.Log.d("CommandProcessor", "Cleared all apps from recents");
                            speakMultiLang("All apps closed", "Sab apps band ho gaye", "Sarva apps band jhale");
                        } else {
                            android.util.Log.w("CommandProcessor", "Could not find clear all button");
                            // Go back home anyway
                            service.performHome();
                            speakMultiLang("Went to home screen", "Home screen par aa gaye", "Home screen var aalo");
                        }
                        root.recycle();
                    }
                }, 1000); // Wait 1 second for recents to open
            }, 500); // Wait 0.5 seconds after going home

        } else {
            android.util.Log.e("CommandProcessor", "Accessibility Service not available");
            // Fallback: Just go to home screen
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(homeIntent);
            speakMultiLang("Going to home screen", "Home screen par ja raha hoon", "Home screen var jat aahe");
        }
    }

    // New method to open app with specific action
    private void openAppWithAction(String appName, String action, String target, String message) {
        try {
            PackageManager pm = context.getPackageManager();
            Intent intent = null;
            String packageName = "";

            android.util.Log.d("CommandProcessor", "openAppWithAction: " + appName + ", action: " + action);

            // Map app names to package names - Direct mapping
            final String finalAppName = appName.toLowerCase().trim();
            if (finalAppName.contains("instagram") || finalAppName.contains("insta")) {
                packageName = "com.instagram.android";
            } else if (finalAppName.contains("youtube")) {
                packageName = "com.google.android.youtube";
            } else if (finalAppName.contains("gmail") || finalAppName.contains("mail")) {
                packageName = "com.google.android.gm";
            } else if (finalAppName.contains("chrome") || finalAppName.contains("browser")) {
                packageName = "com.android.chrome";
            } else if (finalAppName.contains("facebook") || finalAppName.contains("fb")) {
                packageName = "com.facebook.katana";
            } else if (finalAppName.contains("twitter") || finalAppName.equals("x")) {
                packageName = "com.twitter.android";
            } else if (finalAppName.contains("snapchat") || finalAppName.contains("snap")) {
                packageName = "com.snapchat.android";
            } else if (finalAppName.contains("telegram")) {
                packageName = "org.telegram.messenger";
            } else {
                // Try to find package using cache
                AppCacheManager cacheManager = AppCacheManager.getInstance(context);
                packageName = cacheManager.findPackageName(finalAppName);
                android.util.Log.d("CommandProcessor", "Cache lookup for " + finalAppName + ": " + packageName);
            }

            if (packageName != null && !packageName.isEmpty()) {
                intent = pm.getLaunchIntentForPackage(packageName);
                android.util.Log.d("CommandProcessor", "Package: " + packageName + ", Intent: " + (intent != null));
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                // Speak what we're doing
                String actionText = action != null ? action : "opening";
                speak("Opening " + finalAppName);
                android.util.Log.d("CommandProcessor", "Opened " + finalAppName + " successfully");

                // Wait for app to open, then trigger automation
                final String finalAction = action;
                final String finalTarget = target;
                final String finalMessage = message;
                final String finalPackage = packageName;

                new android.os.Handler().postDelayed(() -> {
                    RamuAccessibilityService service = RamuAccessibilityService.getInstance();
                    if (service != null) {
                        android.util.Log.d("CommandProcessor", "Triggering automation for " + finalAppName);
                        // Trigger appropriate automation based on app
                        if (finalAppName.contains("instagram")) {
                            service.automateInstagram(finalAction, finalTarget != null ? finalTarget : finalMessage);
                        } else if (finalAppName.contains("youtube")) {
                            service.automateYouTube(finalAction, finalTarget);
                        } else if (finalAppName.contains("gmail")) {
                            service.automateGmail(finalAction, finalTarget, finalMessage);
                        } else if (finalAppName.contains("chrome") || finalAppName.contains("browser")) {
                            service.automateBrowser(finalAction, finalTarget);
                        } else if (finalAppName.contains("facebook")) {
                            service.automateInstagram(finalAction, finalMessage); // Similar to Instagram
                        } else if (finalAppName.contains("twitter")) {
                            service.automateInstagram(finalAction, finalMessage); // Similar pattern
                        } else {
                            // Generic automation
                            service.automateGenericApp(finalAction, finalTarget != null ? finalTarget : finalMessage);
                        }
                    } else {
                        android.util.Log.w("CommandProcessor", "Accessibility service not available");
                        speak("Please enable Accessibility Service for automation");
                    }
                }, 3000); // Wait 3 seconds for app to fully load
            } else {
                speak("I couldn't find " + finalAppName);
                android.util.Log.e("CommandProcessor", "App not found: " + finalAppName);
            }
        } catch (Exception e) {
            speak("Error opening " + appName);
            android.util.Log.e("CommandProcessor", "Error in openAppWithAction", e);
        }
    }
}
