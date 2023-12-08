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
        Map<Integer, Double> opTimesMap = new HashMap<>();
        double lambdaTimes = 0.001;
        opTimesMap.put(1, 0.1);
        opTimesMap.put(2, 0.2);
        opTimesMap.put(3, 0.3);
    }

    Map<Integer, Double> smoothTimesMap(Map<Integer, Double> opTimesMap, double lambdaTimes) {
        double originSize = opTimesMap.values().size();
        double factor = originSize + lambdaTimes * 8;
        for (int i = 1; i < 9; i++) {
            if (opTimesMap.containsKey(i)) {
                opTimesMap.put(i, (opTimesMap.get(i) + lambdaTimes) / factor);
            } else {
                opTimesMap.put(i, lambdaTimes / factor);
            }
        }
        return opTimesMap;
    }
}
