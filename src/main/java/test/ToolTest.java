package test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.math.MathUtil;
import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import com.example.honeyvault.chinese.paper23_markov_version.EncoderDecoderMarkovCN;
import com.xiaoleilu.hutool.util.RandomUtil;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.*;


public class ToolTest {


    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println(genRandomStr());

        }
    }
    static List<String> candidateList = new ArrayList<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "a", "b", "c",
            "d", "e",
            "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+",
            ",", "-", ".", "/", ";", ":", "<", "=", ">", "?",
            "@", "[", "\\", "]", "^", "_", "`", "{", "|", "}", "~", " "));
    static List<String> greek = Arrays.asList("Α", "τ", "Β", "Γ", "Δ", "σ", "Ε", "Ζ", "Η", "ρ",
            "Θ", "Ι", "Κ", "Λ", "Μ", "Ν", "Ξ", "Ο", "Π", "Ρ",
            "Φ", "Χ", "Ψ",
            "Ω", "ω", "ψ",
            "χ", "φ", "υ");
    private static String genRandomStr() {
        candidateList.addAll(greek);
        boolean foundInvalid = true;
        StringBuilder s = new StringBuilder();
        while (foundInvalid) {
            s = new StringBuilder();
            int size = RandomUtil.randomInt(1, 16);
            for (int i = 1; i <= size; i++) {
                int i1 = RandomUtil.randomInt(0, 123);
                s.append(candidateList.get(i1));
                if (i > 5) {
                    foundInvalid = !isValid(s.toString());
                    if (foundInvalid) {
                        break;
                    }
                }
            }
            foundInvalid = !isValid(s.toString());
        }
        return s.toString();
    }

    private static boolean isValid(String decodeString) {
        int finalLength = 0;
        for (char c : decodeString.toCharArray()) {
            String s = String.valueOf(c);
            if (greek.contains(s)) {
                finalLength += 5;
            } else finalLength++;
            if (finalLength > 16) return false;
        }
        return finalLength >= 5;
    }
}
