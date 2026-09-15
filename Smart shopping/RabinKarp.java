import java.util.ArrayList;
import java.util.List;

public class RabinKarp {

    private static final long MOD = 1_000_000_007L;
    private static final long BASE = 256L;

    public static boolean containsSubstring(String text, String pattern) {
        int n = text.length();
        int m = pattern.length();
        if (m == 0) return true;
        if (m > n) return false;

        long patternHash = 0;
        long windowHash = 0;
        long highOrder = 1;

        for (int i = 0; i < m - 1; i++) {
            highOrder = (highOrder * BASE) % MOD;
        }

        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * BASE + pattern.charAt(i)) % MOD;
            windowHash = (windowHash * BASE + text.charAt(i)) % MOD;
        }

        for (int i = 0; ; i++) {
            if (windowHash == patternHash && text.regionMatches(i, pattern, 0, m)) {
                return true;
            }
            if (i == n - m) break;

            windowHash = (windowHash - text.charAt(i) * highOrder % MOD + MOD) % MOD;
            windowHash = (windowHash * BASE + text.charAt(i + m)) % MOD;
        }
        return false;
    }

    public static List<String> searchDictionary(List<String> words, String pattern) {
        List<String> matches = new ArrayList<>();
        for (String word : words) {
            if (containsSubstring(word, pattern)) {
                matches.add(word);
            }
        }
        return matches;
    }
}
