import java.util.List;

public class AutoCorrect {

    private final FuzzySearchEngine engine;

    public AutoCorrect(FuzzySearchEngine engine) {
        this.engine = engine;
    }

    public String correctWord(String query, int maxDistance) {
        List<Suggestion> results = engine.search(query, maxDistance);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0).word;
    }

    public String correctPhrase(String phrase, Trie dictionary) {
        String[] words = phrase.trim().toLowerCase().split("\\s+");
        StringBuilder corrected = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            String fixedWord = word;

            if (!word.isEmpty() && !dictionary.containsExact(word)) {
                int maxDistance = maxDistanceFor(word);
                String suggestion = correctWord(word, maxDistance);
                if (suggestion != null) {
                    fixedWord = suggestion;
                }
            }

            corrected.append(fixedWord);
            if (i < words.length - 1) corrected.append(" ");
        }
        return corrected.toString();
    }

    private int maxDistanceFor(String query) {
        if (query.length() <= 4) return 2;
        if (query.length() <= 8) return 3;
        return 4;
    }
}
