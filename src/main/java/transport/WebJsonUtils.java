package transport;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight JSON parser and formatter without external dependencies.
 */
public final class WebJsonUtils {
    private WebJsonUtils() {}

    public static String getField(String json, String key, String defaultVal) {
        if (json == null) return defaultVal;
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        if (m.find()) return m.group(1);

        Pattern numPattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?[0-9.]+)");
        Matcher numM = numPattern.matcher(json);
        if (numM.find()) return numM.group(1);

        return defaultVal;
    }

    public static List<String> getStringList(String json, String key) {
        List<String> result = new ArrayList<>();
        if (json == null) return result;
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\[([^\\]]*)\\]");
        Matcher m = p.matcher(json);
        if (m.find()) {
            String inner = m.group(1);
            Pattern elemPattern = Pattern.compile("\"([^\"]*)\"");
            Matcher em = elemPattern.matcher(inner);
            while (em.find()) {
                result.add(em.group(1).trim());
            }
        }
        return result;
    }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
