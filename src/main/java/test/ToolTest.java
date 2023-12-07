package test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.math.MathUtil;
import cn.hutool.core.text.csv.*;
import com.example.honeyvault.chinese.paper23_markov_version.EncoderDecoderMarkovCN;
import com.example.honeyvault.data_access.EncodeLine;
import com.xiaoleilu.hutool.util.CharsetUtil;
import com.xiaoleilu.hutool.util.RandomUtil;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ToolTest {


    public static void main(String[] args) {
        List<String> decode = new ArrayList<>();
        CsvWriter writer = CsvUtil.getWriter(new File("./test.csv"), CharsetUtil.CHARSET_UTF_8);
        decode.add("\"" + RandomUtil.randomString(6) + "\"");
        System.out.println(String.valueOf(decode));
        writer.writeLine(String.valueOf(decode));
        writer.write(decode);
    }

    private static Map<Pair<Integer, Boolean>, EncodeLine<Pair<Integer, Boolean>>> prDrEncodeLineMap = new LinkedHashMap<>();

    private static void initPr_DR() {
        BigDecimal pow = BigDecimal.valueOf(2).pow(128);
        for (int j = 1; j < 23; j++) {
            double alpha = 0.5690178377522002;
            double prob = (j * alpha) / (j * alpha + 1 - alpha);
            Pair<Integer, Boolean> i_true = new Pair<>(j, Boolean.TRUE);
            Pair<Integer, Boolean> i_false = new Pair<>(j, Boolean.FALSE);

            BigInteger upperBound = pow.multiply(BigDecimal.valueOf(prob)).toBigInteger();
            EncodeLine<Pair<Integer, Boolean>> trueLine =
                    EncodeLine.<Pair<Integer, Boolean>>builder().prob(prob).originValue(i_true).lowerBound(BigInteger.valueOf(0)).upperBound(
                            upperBound).build();
            EncodeLine<Pair<Integer, Boolean>> falseLine =
                    EncodeLine.<Pair<Integer, Boolean>>builder().prob(1 - prob).originValue(i_false).lowerBound(upperBound).upperBound(pow.toBigInteger()).build();
            prDrEncodeLineMap.put(new Pair<>(j, true), trueLine);
            prDrEncodeLineMap.put(new Pair<>(j, false), falseLine);
        }
    }
}
