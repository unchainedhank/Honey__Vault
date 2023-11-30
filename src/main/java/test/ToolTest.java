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



    static int fixedLength = 74 * 128;
    static List<String> candidateSet = Arrays.asList("Α", "τ", "Β", "Γ", "Δ", "σ", "Ε", "Ζ", "Η", "ρ",
            "Θ", "Ι", "Κ", "Λ", "Μ", "Ν", "Ξ", "Ο", "Π", "Ρ",
            "Φ", "Χ", "Ψ",
            "Ω", "ω", "ψ",
            "χ", "φ", "υ");

    static List<String> fullList = Arrays.asList("Α", "τ", "Β", "Γ", "Δ", "σ", "Ε", "Ζ", "Η", "ρ",
            "Θ", "Ι", "Κ", "Λ", "Μ", "Ν", "Ξ", "Ο", "Π", "Ρ",
            "Φ", "Χ", "Ψ",
            "Ω", "ω", "ψ",
            "χ", "φ", "υ", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e",
            "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+",
            ",", "-", ".", "/", ";", ":", "<", "=", ">", "?",
            "@", "[", "\\", "]", "^", "_", "`", "{", "|", "}", "~", " ");

    static List<String> genTestDecode(List<String> t) {
        int length = RandomUtil.randomInt(1, 17);
        List<String> decode = new ArrayList<>();

        for (int j = 0; j < t.size(); j++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                String randomS = fullList.get(RandomUtil.randomInt(0, fullList.size() - 1));
                sb.append(randomS);
            }
            decode.add(sb.toString());
        }

        return decode;

    }


    static String genRandomStr() {
        StringBuilder filledString = new StringBuilder();
        while (filledString.length() < fixedLength) {
            filledString.append(RandomUtil.randomInt(0, 2));
        }
        return filledString.toString();
    }

    static private boolean isFoundInvalid(List<String> decode) {
        boolean foundInvalid = false;
        for (int i1 = 0; (i1 < decode.size()) && !foundInvalid; i1++) {
            String s = decode.get(i1);
            for (String s1 : candidateSet) {
                if (s.length() < 5 && !s.contains(s1)) {
                    foundInvalid = true;
                    break;
                }
            }
        }
        return foundInvalid;
    }


}
