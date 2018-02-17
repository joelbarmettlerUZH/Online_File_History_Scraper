import java.util.ArrayList;
import java.util.Arrays;

public class SimpleSimilarity implements Similarity {

    public boolean similarity(String s1, String s2, double treshold){
        String bigger = s1, smaller = s2;
        if(s2.length() > s1.length()){ bigger = s2; smaller = s1; }
        if(Math.abs(smaller.length() / bigger.length()) < treshold ) { return false; }

        boolean similar = true;
        for(int i = 0; i < 10; i++){
            int shorter = Math.min(s1.length(), s2.length());
            int length = (int) (Math.random() * Math.min(shorter/10, 2048));
            int start = (int) (Math.random() * Math.min(s1.length()-length, s2.length()-length));
            String sub1 = s1.substring(start, start+length);
            String sub2 = s2.substring(start, start+length);
            similar = similar && new LevenshteinSimilairty().similarity(sub1, sub2, treshold);
        }
        return similar;
    }

}
