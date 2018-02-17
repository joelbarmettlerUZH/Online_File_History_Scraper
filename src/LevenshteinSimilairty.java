import org.apache.commons.text.similarity.LevenshteinDistance;

public class LevenshteinSimilairty implements Similarity {

    /*
    SIMILARITY OF STRINGS (Autor: Joel Barmettler, 17.02.2019)
    Computes similarity between two strings using Levenshtein Distance
    Returns percentage of similarity as double
 */
    public boolean similarity(String s1, String s2, double treshold){
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) {
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return true;
        }
        LevenshteinDistance dist = new LevenshteinDistance();
        return ((longerLength - dist.apply(longer, shorter)) / (double) longerLength) >= 1-treshold;
    }

}
