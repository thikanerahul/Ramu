package com.example.ramu.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NLUProcessor {

    public CommandIntent parseCommand(String command) {
        command = command.toLowerCase().trim();

        CommandIntent intent = new CommandIntent();

        // Back (English + Hindi + Marathi) - Natural variations
        // Also handle "exit", "bahar nikalo", "close", etc.
        if (matchesPattern(command, ".*(go back|back|piche|maage|return|wapas|vapas|पीछे|वापस|माघे).*") ||
                matchesPattern(command, ".*(previous|last|पिछला).*") ||
                matchesPattern(command, ".*(ek page piche|one page back).*") ||
                matchesPattern(command, ".*(exit|bahar|बाहर|nikalo|निकालो|निकलो|close|band karo|बंद करो).*") ||
                matchesPattern(command, ".*(se bahar|से बाहर|se nikalo|से निकालो).*")) {
            intent.action = CommandAction.GO_BACK;
            return intent;
        }

        // Home (English + Hindi + Marathi) - Natural variations
        if (matchesPattern(command, ".*(go home|home|ghar|mukhya|घर|मुख्य).*") ||
                matchesPattern(command, ".*(main page|homepage|home screen|मुख्य पेज|होम स्क्रीन).*") ||
                matchesPattern(command, ".*(take me home|mujhe ghar le chalo).*")) {
            intent.action = CommandAction.GO_HOME;
            return intent;
        }

        // Scroll (ALL Languages + Local Words) - Comprehensive support
        // English, Hindi, Hinglish, Marathi, and local variations
        if (matchesPattern(command, ".*(scroll|swipe|move|slide|chalao|चलाओ|हलवा|हिलाओ|घुमाओ).*") ||
                matchesPattern(command, ".*(niche|upar|left|right|baye|daye|नीचे|ऊपर|बाएं|दाएं).*") ||
                matchesPattern(command, ".*(aage|piche|side|आगे|पीछे|साइड).*") ||
                matchesPattern(command, ".*(story|reel|स्टोरी|रील|next|previous|अगला|पिछला|अगली|पिछली).*") ||
                // Local/casual words
                matchesPattern(command, ".*(thoda|zara|थोड़ा|जरा).*") ||
                // Just direction words alone
                matchesPattern(command, "^(niche|upar|left|right|down|up|नीचे|ऊपर|बाएं|दाएं|aage|piche|आगे|पीछे)$")) {

            intent.action = CommandAction.SCROLL;

            // Check direction with comprehensive patterns
            String lowerCmd = command.toLowerCase();

            // LEFT patterns
            if (lowerCmd.contains("left") || lowerCmd.contains("baye") || lowerCmd.contains("bayen") ||
                    lowerCmd.contains("बाएं") || lowerCmd.contains("बायें") || lowerCmd.contains("बाये") ||
                    lowerCmd.contains("डावे") || lowerCmd.contains("side") || lowerCmd.contains("साइड") ||
                    lowerCmd.contains("piche") || lowerCmd.contains("पीछे") || lowerCmd.contains("पिछे")) {
                intent.messageText = "left";

                // RIGHT patterns
            } else if (lowerCmd.contains("right") || lowerCmd.contains("daye") || lowerCmd.contains("dayen") ||
                    lowerCmd.contains("दाएं") || lowerCmd.contains("दायें") || lowerCmd.contains("दाये") ||
                    lowerCmd.contains("उजवे") || lowerCmd.contains("दाहिने") ||
                    lowerCmd.contains("story") || lowerCmd.contains("reel") ||
                    lowerCmd.contains("स्टोरी") || lowerCmd.contains("रील") ||
                    lowerCmd.contains("next") || lowerCmd.contains("अगला") || lowerCmd.contains("अगली") ||
                    lowerCmd.contains("aage") || lowerCmd.contains("आगे")) {
                intent.messageText = "right";

                // UP patterns
            } else if (lowerCmd.contains("up") || lowerCmd.contains("upar") || lowerCmd.contains("opar") ||
                    lowerCmd.contains("ऊपर") || lowerCmd.contains("uppar") || lowerCmd.contains("var") ||
                    lowerCmd.contains("top") || lowerCmd.contains("above") ||
                    lowerCmd.contains("previous") || lowerCmd.contains("पिछला") || lowerCmd.contains("पिछली")) {
                intent.messageText = "up";

                // DOWN patterns (default)
            } else {
                intent.messageText = "down"; // niche, down, नीचे, etc
            }

            android.util.Log.d("NLUProcessor", "Scroll command detected - Direction: " + intent.messageText);
            return intent;
        }

        // Search Commands (English + Hindi + Marathi) - Natural variations
        if (matchesPattern(command, ".*(search|find|dhundo|ढूंढो|खोज|शोध|khojo|look for|dekho|देखो).*") ||
                matchesPattern(command, ".*(kaha hai|where is|kidhar hai|कहां है|किधर है).*") ||
                matchesPattern(command, ".*(dikhao|show me|दिखाओ|बताओ).*")) {
            intent.action = CommandAction.SEARCH;
            intent.searchQuery = extractSearchQuery(command);
            return intent;
        }

        // Call Answer (English + Hindi + Marathi)
        if (matchesPattern(command, ".*(answer|receive|pick|उठाओ|pick up).*(call|कॉल).*") ||
                matchesPattern(command, ".*(call|कॉल).*(answer|receive|pick|उठाओ|pick up).*")) {
            intent.action = CommandAction.CALL_ANSWER;
            return intent;
        }

        // CALL [NAME]
        if (matchesPattern(command, "^(call|phone|aklo|phon karo|कॉल|फोन)\\s+.*")) {
            intent.action = CommandAction.MAKE_CALL;
            intent.contactName = extractContactName(command);
            return intent;
        }

        // Call End (English + Hindi + Marathi)
        if (matchesPattern(command, ".*(end|cut|disconnect|काटो|बंद).*(call|कॉल).*") ||
                matchesPattern(command, ".*(call|कॉल).*(end|cut|disconnect|काटो|बंद).*") ||
                matchesPattern(command, ".*(hang up|रख दो|रखो).*")) {
            intent.action = CommandAction.CALL_END;
            return intent;
        }

        // MESSAGE [NAME] - Should open chat box
        if (matchesPattern(command, "^(message|msg|chat|message box|chat box)\\s+.*")) {
            intent.action = CommandAction.OPEN_CHAT;
            intent.contactName = extractContactName(command);
            return intent;
        }

        // TYPE MESSAGE - Just type
        // Handle: "type [text]", "likho [text]"
        if (matchesPattern(command, "^(type|likho|टाइप|लिखो)\\s+.*")) {
            intent.action = CommandAction.TYPE_MESSAGE;
            String text = command.replaceAll("(?i)^(type|likho|टाइप|लिखो)\\s+", "").trim();
            intent.messageText = text;
            return intent;
        }

        // ERASE WORD
        if (matchesPattern(command, "^(erase|mitao|backspace|delete last|pichla hatao|मिटाओ|हटाओ)$")) {
            intent.action = CommandAction.ERASE_WORD;
            return intent;
        }

        // SEND MESSAGE - Just press send button
        if (matchesPattern(command, "^(send|bhejo|भेजो|पाठव|karo|send it)$")) {
            intent.action = CommandAction.SEND_MESSAGE;
            return intent;
        }

        // Status and Story
        if (matchesPattern(command, ".*(status|story|स्टोरी).*")) {
            String contact = extractContactName(command);
            if (matchesPattern(command, ".*(status).*")) {
                intent.action = CommandAction.SHOW_STATUS;
            } else {
                intent.action = CommandAction.SHOW_STORY;
            }
            intent.contactName = contact;
            return intent;
        }

        // Open app patterns (English + Hindi + Marathi)
        if (matchesPattern(command, "^(open|launch|start|run|kholo|खोल|खोलो|उघड)\\s+.*") ||
                matchesPattern(command,
                        "^(whatsapp|instagram|youtube|facebook|chrome|camera|gmail|maps|insta|yt|fb|wa)$")) {

            String appName = extractAppName(command);

            // Check if it's a known app name
            if (isKnownAppName(appName)) {
                intent.action = CommandAction.OPEN_APP;
                intent.appName = appName;

                android.util.Log.d("NLUProcessor", "OPEN_APP detected: " + appName);

                // Check for app-specific actions in the same command
                String lowerAppName = appName.toLowerCase();

                // Instagram actions
                if (lowerAppName.contains("instagram") || lowerAppName.contains("insta")) {
                    if (matchesPattern(command, ".*(search|dhundo|खोज|शोध).*")) {
                        intent.action = CommandAction.INSTAGRAM_SEARCH;
                        intent.searchQuery = extractSearchQuery(command);
                    } else if (matchesPattern(command, ".*(like|पसंद).*")) {
                        intent.action = CommandAction.INSTAGRAM_LIKE;
                    } else if (matchesPattern(command, ".*(comment|टिप्पणी).*")) {
                        intent.action = CommandAction.INSTAGRAM_COMMENT;
                        intent.messageText = extractMessage(command);
                    }
                }
                // YouTube actions
                else if (lowerAppName.contains("youtube")) {
                    if (matchesPattern(command, ".*(search|dhundo|खोज).*")) {
                        intent.action = CommandAction.YOUTUBE_SEARCH;
                        intent.searchQuery = extractSearchQuery(command);
                    } else if (matchesPattern(command, ".*(play|chalao|चलाओ).*")) {
                        intent.action = CommandAction.YOUTUBE_PLAY;
                    } else if (matchesPattern(command, ".*(like|पसंद).*")) {
                        intent.action = CommandAction.YOUTUBE_LIKE;
                    }
                }
                // Gmail actions
                else if (lowerAppName.contains("gmail") || lowerAppName.contains("mail")) {
                    if (matchesPattern(command, ".*(compose|लिखो|नया).*")) {
                        intent.action = CommandAction.GMAIL_COMPOSE;
                    } else if (matchesPattern(command, ".*(send|bhejo|भेजो).*")) {
                        intent.action = CommandAction.GMAIL_SEND;
                        intent.contactName = extractContactName(command);
                        intent.messageText = extractMessage(command);
                    }
                }
                // Browser actions
                else if (lowerAppName.contains("chrome") || lowerAppName.contains("browser")) {
                    if (matchesPattern(command, ".*(search|dhundo|खोज).*")) {
                        intent.action = CommandAction.BROWSER_SEARCH;
                        intent.searchQuery = extractSearchQuery(command);
                    }
                }
                // Facebook actions
                else if (lowerAppName.contains("facebook")) {
                    if (matchesPattern(command, ".*(post|लिखो).*")) {
                        intent.action = CommandAction.FACEBOOK_POST;
                        intent.messageText = extractMessage(command);
                    }
                }
                // Twitter actions
                else if (lowerAppName.contains("twitter") || lowerAppName.contains("x")) {
                    if (matchesPattern(command, ".*(tweet|post|लिखो).*")) {
                        intent.action = CommandAction.TWITTER_TWEET;
                        intent.messageText = extractMessage(command);
                    }
                }
                // Generic app with action
                else if (matchesPattern(command, ".*(search|click|type|dhundo|खोज).*")) {
                    intent.action = CommandAction.GENERIC_APP_ACTION;
                    intent.targetAction = extractAction(command);
                    intent.searchQuery = extractSearchQuery(command);
                }

                return intent;
            }
        }

        // IN-APP OPEN - Open something within current app (NOT a new app)
        // Handle: "open chat", "open location", "open profile", "open settings", etc.
        // This should be checked AFTER OPEN_APP to avoid opening new apps
        if (matchesPattern(command, "^(open|khol|खोल|उघड)\\s+(.+)")) {
            String target = command.replaceAll("(?i)^(open|khol|खोल|उघड)\\s+", "").trim();

            // Check if target is NOT an app name
            if (!isKnownAppName(target)) {
                intent.action = CommandAction.IN_APP_OPEN;
                intent.messageText = target;
                android.util.Log.d("NLUProcessor", "IN_APP_OPEN detected: " + target);
                return intent;
            }
        }

        // Open Chat (Specific) - Check AFTER OPEN_APP and IN_APP_OPEN
        // Handle: "open chat [name]", "open any chat", "koi bhi chat", "chat kholo"
        if (matchesPattern(command, ".*(chat|बात|चैट).*(open|khol|खोल|उघड).*") ||
                matchesPattern(command, ".*(any chat|koi bhi chat|कोई भी चैट|कोई चैट).*")) {
            intent.action = CommandAction.OPEN_CHAT;
            intent.contactName = extractContactName(command);
            // If no specific contact found, use "any" to open first available chat
            if (intent.contactName.equals("Unknown") || intent.contactName.equals("unknown")) {
                intent.contactName = "any";
            }
            android.util.Log.d("NLUProcessor", "OPEN_CHAT detected for: " + intent.contactName);
            return intent;
        }

        // General questions/conversation
        if (matchesPattern(command,
                ".*(what|how|why|when|where|who|can you|tell me|क्या|कैसे|क्यों|कब|कहाँ|कौन|बताओ|बता).*")) {
            intent.action = CommandAction.GENERAL_QUERY;
            intent.messageText = command;
            return intent;
        }

        // Greetings
        if (matchesPattern(command, ".*(hello|hi|hey|namaste|नमस्ते|हेलो|हाय).*")) {
            intent.action = CommandAction.GREETING;
            return intent;
        }

        // Click / Tap (English + Hindi + Marathi) - Check AFTER OPEN_APP
        // Remove "open" from pattern to avoid conflict
        if (matchesPattern(command, ".*(click|tap|touch|press|dabao|daba).*")) {
            String target = extractClickTarget(command);
            if (!target.isEmpty() && !target.equals("unknown")) {
                intent.action = CommandAction.CLICK;
                intent.messageText = target;
                return intent;
            }
        }

        // Stop Service (English + Hindi + Marathi)
        if (matchesPattern(command, ".*(stop|sleep|shutdown|band|chup|off|shant|zhopp).*") &&
                (command.contains("ramu") || command.contains("service") || command.contains("ram")
                        || command.contains("app"))) {
            intent.action = CommandAction.STOP_SERVICE;
            return intent;
        } else if (matchesPattern(command,
                ".*(go to sleep|shut down|band ho jao|band ho|aram karo|aaram kar|zhop).*")) {
            intent.action = CommandAction.STOP_SERVICE;
            return intent;
        }

        // Default: treat as general query instead of unknown
        intent.action = CommandAction.GENERAL_QUERY;
        intent.messageText = command;
        return intent;
    }

    private boolean matchesPattern(String text, String pattern) {
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text).find();
    }

    private String extractContactName(String command) {
        // Try to find name after "to", "message", "send", "chat", etc.
        Pattern pattern = Pattern.compile(
                "(?:to|ko|la|message|msg|send|chat|search|dhundo|khoz|call|phone|कॉल|फोन|भेज|भेजो|बात)\\s+(\\w+)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            String name = matcher.group(1);
            if (!isCommonWord(name)) {
                return name;
            }
        }

        // Try to extract name before "ko", "को", "ला"
        pattern = Pattern.compile("(\\w+)\\s+(?:ko|को|ला|la)", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String name = matcher.group(1);
            if (!isCommonWord(name)) {
                return name;
            }
        }

        // Try to extract name after "open", "खोल", "उघड"
        pattern = Pattern.compile("(?:open|खोल|खोलो|उघड)\\s+(\\w+)\\s+(?:chat|बात|चैट)", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String name = matcher.group(1);
            if (!isCommonWord(name)) {
                return name;
            }
        }

        // Try to extract capitalized words (likely names)
        String[] words = command.split("\\s+");
        for (String word : words) {
            if (word.length() > 2 && !isCommonWord(word) &&
                    Character.isUpperCase(word.charAt(0))) {
                return word;
            }
        }

        // Try to extract any word that's not a common word
        for (String word : words) {
            if (word.length() > 2 && !isCommonWord(word)) {
                return word;
            }
        }

        return "Unknown";
    }

    private String extractMessage(String command) {
        // Try to extract message after "saying", "that says", "बोलो", "कहो"
        Pattern pattern = Pattern.compile("(?:saying|says|message|text|बोलो|कहो|भेजो)\\s+['\"]?(.+?)['\"]?$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // Try to extract message in quotes
        pattern = Pattern.compile("['\"]([^'\"]+)['\"]");
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // Try to extract after dash or colon
        if (command.contains(" - ")) {
            String[] parts = command.split(" - ");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        if (command.contains(": ")) {
            String[] parts = command.split(": ");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }

        return "Hello";
    }

    private String extractAppName(String command) {
        // Remove common action words and greetings (English + Hindi + Marathi)
        String cleaned = command.replaceAll(
                "(?i)(open|launch|start|run|the|app|application|खोल|खोलो|चालू|करो|karo|hello|hi|hey|namaste|नमस्ते|हेलो|हाय|उघड|सुरू|chalao|चलाओ|mujhe|take me to|le chalo|ले चलो|show me|dikhao|दिखाओ|please|कृपया)",
                "").trim();

        // Smart app name detection with aliases
        String lowerCleaned = cleaned.toLowerCase();

        // WhatsApp aliases
        if (lowerCleaned.contains("whatsapp") || lowerCleaned.contains("whats app") ||
                lowerCleaned.contains("व्हाट्सएप") || lowerCleaned.contains("व्हॉट्सअॅप") ||
                lowerCleaned.contains("wa") || lowerCleaned.contains("wp")) {
            return "whatsapp";
        }

        // Instagram aliases
        if (lowerCleaned.contains("instagram") || lowerCleaned.contains("insta") ||
                lowerCleaned.contains("ig") || lowerCleaned.contains("इंस्टा") ||
                lowerCleaned.contains("इंस्टाग्राम")) {
            return "instagram";
        }

        // YouTube aliases
        if (lowerCleaned.contains("youtube") || lowerCleaned.contains("you tube") ||
                lowerCleaned.contains("yt") || lowerCleaned.contains("यूट्यूब")) {
            return "youtube";
        }

        // Snapchat aliases
        if (lowerCleaned.contains("snapchat") || lowerCleaned.contains("snap") ||
                lowerCleaned.contains("sc") || lowerCleaned.contains("स्नैपचैट")) {
            return "snapchat";
        }

        // Facebook aliases
        if (lowerCleaned.contains("facebook") || lowerCleaned.contains("fb") ||
                lowerCleaned.contains("फेसबुक")) {
            return "facebook";
        }

        // Twitter/X aliases
        if (lowerCleaned.contains("twitter") || lowerCleaned.contains("x") ||
                lowerCleaned.contains("ट्विटर")) {
            return "twitter";
        }

        // Telegram aliases
        if (lowerCleaned.contains("telegram") || lowerCleaned.contains("tg") ||
                lowerCleaned.contains("टेलीग्राम")) {
            return "telegram";
        }

        // Gmail aliases
        if (lowerCleaned.contains("gmail") || lowerCleaned.contains("mail") ||
                lowerCleaned.contains("email") || lowerCleaned.contains("जीमेल")) {
            return "gmail";
        }

        // Chrome/Browser aliases
        if (lowerCleaned.contains("chrome") || lowerCleaned.contains("browser") ||
                lowerCleaned.contains("क्रोम") || lowerCleaned.contains("ब्राउज़र")) {
            return "chrome";
        }

        // Camera aliases
        if (lowerCleaned.contains("camera") || lowerCleaned.contains("cam") ||
                lowerCleaned.contains("कैमरा") || lowerCleaned.contains("कॅमेरा")) {
            return "camera";
        }

        // Maps aliases
        if (lowerCleaned.contains("maps") || lowerCleaned.contains("map") ||
                lowerCleaned.contains("मैप्स")) {
            return "maps";
        }

        // Return valid cleaned string
        if (!cleaned.isEmpty() && cleaned.length() > 1) {
            return cleaned;
        }

        return "unknown";
    }

    private String extractClickTarget(String command) {
        // Remove action words
        String cleaned = command.replaceAll("(?i)(click|tap|touch|press|dabao|daba|open)", "").trim();
        // Return remaining text (e.g., "Search" from "Click Search")
        return cleaned;
    }

    private String extractSearchQuery(String command) {
        // Try to extract query after "search", "dhundo", "find", etc.
        Pattern pattern = Pattern.compile(
                "(?:search|dhundo|find|खोज|ढूंढो|शोध|khojo|look for|dekho|देखो|dikhao|दिखाओ|kaha hai|where is|kidhar hai|कहां है)\\s+(?:for\\s+)?(.+?)(?:\\s+in|\\s+mein|\\s+pe|\\s+on|$)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            String query = matcher.group(1).trim();
            // Remove common words
            query = query.replaceAll("(?i)(karo|करो|do|please|chahiye|चाहिए|hai|है|me|mein|में)", "").trim();
            if (!query.isEmpty()) {
                return query;
            }
        }

        // Try "show me X" pattern
        pattern = Pattern.compile("(?:show me|dikhao|दिखाओ|bataao|बताओ)\\s+(.+?)(?:\\s+in|\\s+mein|\\s+pe|$)",
                Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(command);
        if (matcher.find()) {
            String query = matcher.group(1).trim();
            if (!query.isEmpty()) {
                return query;
            }
        }

        // Fallback: return words after search keyword
        String[] words = command.split("\\s+");
        StringBuilder query = new StringBuilder();
        boolean foundSearchWord = false;
        for (String word : words) {
            if (foundSearchWord && !isCommonWord(word)) {
                query.append(word).append(" ");
            }
            if (word.matches("(?i)(search|dhundo|find|खोज|ढूंढो|शोध|khojo|dekho|dikhao|show)")) {
                foundSearchWord = true;
            }
        }

        String result = query.toString().trim();
        return result.isEmpty() ? "search" : result;
    }

    private String extractAction(String command) {
        if (command.contains("search") || command.contains("dhundo") || command.contains("खोज")) {
            return "search";
        } else if (command.contains("click") || command.contains("dabao")) {
            return "click";
        } else if (command.contains("type") || command.contains("likho")) {
            return "type";
        } else if (command.contains("like") || command.contains("पसंद")) {
            return "like";
        } else if (command.contains("play") || command.contains("chalao")) {
            return "play";
        }
        return "search"; // default
    }

    private boolean isCommonWord(String word) {
        String[] commonWords = {
                // English
                "whatsapp", "message", "call", "phone", "open", "send", "to", "a", "the",
                "turn", "on", "off", "switch", "enable", "disable", "launch", "start",
                "bluetooth", "wifi", "camera", "app", "application", "saying", "says",
                "text", "msg", "sms", "that", "please", "can", "you", "i", "want",
                "go", "back", "home", "scroll", "down", "up", "click", "tap",
                "search", "find", "look", "show", "get", "give", "make", "do",
                "chat", "talk", "speak", "tell", "ask", "help", "need",
                "instagram", "insta", "youtube", "snapchat", "snap", "facebook",
                "twitter", "telegram", "gmail", "chrome", "browser", "maps",
                // Hindi
                "व्हाट्सएप", "मैसेज", "कॉल", "फोन", "खोल", "भेज", "को", "का", "की",
                "चालू", "बंद", "करो", "karo", "ko", "pe", "पे", "bhejo", "भेजो",
                "karo", "करो", "kya", "क्या", "hai", "है", "dhundo", "खोज", "ढूंढो",
                "baat", "बात", "बोल", "बोलो", "chahiye", "चाहिए",
                // Marathi
                "व्हॉट्सअॅप", "मेसेज", "संदेश", "पाठव", "ला", "उघड", "सुरू", "बंद",
                "कॅमेरा", "फोटो", "काढ", "ब्लुटूथ", "वायफाय", "वाईफाय", "शोध"
        };

        String lowerWord = word.toLowerCase();
        for (String common : commonWords) {
            if (lowerWord.equals(common)) {
                return true;
            }
        }
        return false;
    }

    // Check if text is a known app name
    private boolean isKnownAppName(String text) {
        String lower = text.toLowerCase().trim();
        String[] appNames = {
                "whatsapp", "whats app", "wa", "wp",
                "instagram", "insta", "ig",
                "snapchat", "snap", "sc",
                "youtube", "you tube", "yt",
                "facebook", "fb",
                "twitter", "x",
                "telegram", "tg",
                "gmail", "mail", "email",
                "chrome", "browser",
                "camera", "cam",
                "maps", "map",
                "spotify",
                "netflix",
                "amazon",
                "flipkart",
                "paytm",
                "gpay", "google pay",
                "phonepe", "phone pe",
                "settings",
                "calculator",
                "clock",
                "calendar",
                "contacts",
                "messages", "sms",
                "photos", "gallery",
                "play store", "playstore"
        };

        for (String appName : appNames) {
            if (lower.equals(appName) || lower.contains(appName)) {
                return true;
            }
        }
        return false;
    }
}

enum CommandAction {
    OPEN_APP,
    WHATSAPP_MESSAGE,
    WHATSAPP_CALL,
    MAKE_CALL,
    SEND_SMS,
    BLUETOOTH_ON,
    BLUETOOTH_OFF,
    WIFI_ON,
    WIFI_OFF,
    OPEN_CAMERA,
    GENERAL_QUERY,
    GREETING,
    GO_BACK,
    GO_HOME,
    SCROLL,
    CLICK,
    OPEN_CHAT,
    STOP_SERVICE,
    // App-specific actions
    INSTAGRAM_SEARCH,
    INSTAGRAM_LIKE,
    INSTAGRAM_COMMENT,
    YOUTUBE_SEARCH,
    YOUTUBE_PLAY,
    YOUTUBE_LIKE,
    GMAIL_COMPOSE,
    GMAIL_SEND,
    BROWSER_SEARCH,
    FACEBOOK_POST,
    TWITTER_TWEET,
    GENERIC_APP_ACTION,
    // New actions
    SEARCH,
    CALL_ANSWER,
    CALL_END,
    SCROLL_LEFT,
    SCROLL_RIGHT,
    // WhatsApp chat flow actions
    TYPE_MESSAGE,
    SEND_MESSAGE,
    // WhatsApp & Instagram specific
    SHOW_STATUS,
    SHOW_STORY,
    ERASE_WORD,
    // In-app navigation
    IN_APP_OPEN,
    UNKNOWN
}

class CommandIntent {
    CommandAction action;
    String appName;
    String contactName;
    String messageText;
    String searchQuery;
    String targetAction;
}
