package test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.math.MathUtil;
import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import com.example.honeyvault.chinese.paper23_markov_version.EncoderDecoderMarkovCN;
import com.example.honeyvault.data_access.EncodeLine;
import com.xiaoleilu.hutool.util.RandomUtil;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ToolTest {


    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            System.out.println(genRandomStr());
        }
    }


    static List<String> normalList = new ArrayList<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "a", "b", "c",
            "d", "e",
            "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+",
            ",", "-", ".", "/", ";", ":", "<", "=", ">", "?",
            "@", "[", "\\", "]", "^", "_", "`", "{", "|", "}", "~", " "));
    static List<String> greekList = Arrays.asList("Α", "τ", "Β", "Γ", "Δ", "σ", "Ε", "Ζ", "Η", "ρ",
            "Θ", "Ι", "Κ", "Λ", "Μ", "Ν", "Ξ", "Ο", "Π", "Ρ",
            "Φ", "Χ", "Ψ",
            "Ω", "ω", "ψ",
            "χ", "φ", "υ");


    private static String genRandomStr() {
        StringBuilder s = new StringBuilder();
        int size = RandomUtil.randomInt(1, 5);
        int greekSize = RandomUtil.randomInt(1, size+1);
        int normalSize = size - greekSize;


        if (size == 1) {
            int ranS = RandomUtil.randomInt(0, greekList.size());
            s.append(greekList.get(ranS));
        } else {
            for (int i = 0; i < size; i++) {
                int randomValue = RandomUtil.randomInt(0, 2);

                if (randomValue == 0) {
                    if (greekSize - 1 != 1) {
                        greekSize -= 1;
                        int ranS = RandomUtil.randomInt(0, greekList.size());
                        s.append(greekList.get(ranS));
                    } else {
                        normalSize -= 1;
                        int ranS = RandomUtil.randomInt(0, greekList.size());
                        s.append(normalList.get(ranS));
                    }
                } else {
                    if (normalSize - 1 != 1) {
                        normalSize -= 1;
                        int ranS = RandomUtil.randomInt(0, greekList.size());
                        s.append(normalList.get(ranS));
                    } else {
                        greekSize -= 1;
                        int ranS = RandomUtil.randomInt(0, greekList.size());
                        s.append(greekList.get(ranS));
                    }
                }

            }
        }

        return s.toString();
    }



}
