package test;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.math.MathUtil;
import com.example.honeyvault.data_access.EncodeLine;
import com.xiaoleilu.hutool.util.RandomUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class ToolTest {

    static List<String> candidateList;

    public static void main(String[] args) throws ParseException {
        List<String> pswdsStrings = new ArrayList<>();
        List<List<String>> pswdsWithPII = new ArrayList<>();
        pswdsWithPII.add(List.of("123","abc"));
        pswdsWithPII.add(List.of("234","cvb"));
        pswdsWithPII.forEach(pswdsStrings::addAll);
        System.out.println(pswdsStrings);

    }

    private static Date parseBirthdayFromIdCard(String idCard) throws ParseException {
        // 身份证号码中的生日部分通常是从第7位到第14位
        String birthdayString = idCard.substring(6, 14);

        // 使用SimpleDateFormat进行解析
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.parse(birthdayString);
    }


    public static double f_fit(int i) {
        double i2 = Math.pow(i, 2);
        double i3 = Math.pow(i, 3);
        return 1 / (0.02455 * i3 - 0.2945 * i2 + 3.409 * i + 0.0852);
    }

    private static Pair<Double, Double> gEncoder(int g, int i) {
        double L = 128;
        double pow = Math.pow(2, L);
        double col2;
        double lower = 0, upper;
        if (g == 0) {
            upper = Math.floor(pow * f_fit(i));
        } else {
            col2 = (1 - f_fit(i)) / i;
            lower = Math.floor(((f_fit(i) + col2 * (g - 1)) * pow));
            upper = Math.floor(((f_fit(i) + col2 * g) * pow));
        }

        return new Pair<>(lower, upper);

    }

    static List<String> splitString(String input, int chunkSize) {
        List<String> chunks = new ArrayList<>();

        for (int i = 0; i < input.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, input.length());
            chunks.add(input.substring(i, end));
        }

        return chunks;
    }


    static <T> T findOriginValue(BigInteger encodedValue, Map<T, EncodeLine<T>> encodeTable) {
        T result = null;
        for (Map.Entry<T, EncodeLine<T>> line : encodeTable.entrySet()) {
            BigDecimal bigDecimal = BigDecimal.valueOf(line.getValue().getLowerBound());
            BigInteger bigInteger = bigDecimal.toBigInteger();
            BigDecimal bigDecimal1 = BigDecimal.valueOf(line.getValue().getUpperBound());
            BigInteger bigInteger1 = bigDecimal1.toBigInteger();
            if (bigInteger.compareTo(encodedValue) <= 0 &&
                    bigInteger1.compareTo(encodedValue) > 0) {
                result = (line.getValue().getOriginValue());
            }
        }
        return result;
//                .filter(line -> encodedValue.compareTo(BigDecimal.valueOf(line.getLowerBound()).toBigInteger()) > 0 &&
//                        encodedValue.compareTo(BigDecimal.valueOf(line.getUpperBound()).toBigInteger()) < 0)
//                .findFirst();

//        if (result.isPresent()) {
//            EncodeLine<T> encodeLine = result.get();
//            // 在这里对符合条件的 EncodeLine 进行处理
//            return encodeLine.getOriginValue();
//        } else {
//            // 没有找到符合条件的 EncodeLine
//            return null;
//        }
    }

    public static String selectPathByProbability(Map<String, Double> pathProbMap) {
        double totalProb = pathProbMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double randomProb = new Random().nextDouble() * totalProb;

        double cumulativeProb = 0.0;
        for (Map.Entry<String, Double> entry : pathProbMap.entrySet()) {
            cumulativeProb += entry.getValue();
            if (randomProb < cumulativeProb) {
                return entry.getKey();
            }
        }

        return pathProbMap.entrySet().iterator().next().getKey();
    }


    public static BigDecimal getRandom32BitBigDecimal(double lowerBound, double upperBound) {
        SecureRandom secureRandom = new SecureRandom();

        // 将输入的 double 类型转换为 BigDecimal
        BigDecimal lower = BigDecimal.valueOf(lowerBound);
        BigDecimal upper = BigDecimal.valueOf(upperBound);

        // 生成一个 0 到 1 之间的随机 BigInteger
        BigInteger randomBigInteger = new BigInteger(128, secureRandom);

        // 将随机数映射到指定的区间
        BigDecimal randomBigDecimal = lower.add(
                upper.subtract(lower).multiply(new BigDecimal(randomBigInteger))
                        .divide(new BigDecimal(BigInteger.ONE.shiftLeft(128)))
        );

        // 如果随机数等于上界，递归调用直到随机数小于上界
        if (randomBigDecimal.equals(upper)) {
            return getRandom32BitBigDecimal(lowerBound, upperBound);
        }

        return randomBigDecimal;
    }

    private static double calAlpha() {
        List<Double> alphaList = new ArrayList<>();
//        UserVaultSet = repo.findAll();
//        UserVaultSet.forEach(p -> {
        double alpha;
//        String password12306 = p.getPassword12306();
//        String passwdCn = p.getPasswd_CN();
        List<String> passwds = new ArrayList<>();
//        passwds.add(password12306);
//        passwds.add(passwdCn);

        passwds.add("abc");
        passwds.add("abc");
        passwds.add("abc1");
        passwds.add("abc2");

        Map<String, Integer> freqMap = new HashMap<>();
        for (String pswd : passwds) {
            freqMap.put(pswd, freqMap.getOrDefault(pswd, 0) + 1);
        }
        long deno = MathUtil.combinationCount(passwds.size(), 2);
        double totalCombinations = 0;
        for (int count : freqMap.values()) {
            if (count >= 2) {
                totalCombinations += MathUtil.combinationCount(count, 2);
            }
        }
        if (totalCombinations == 0) {
            alpha = 0;
        } else {
            alpha = totalCombinations / deno;
        }
        alphaList.add(alpha);
//        });
        return alphaList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    static Set<String> generateStrings(int length) {
        Set<String> res = new HashSet<>();
        if (length == 3) {
            for (String s : candidateList) {
                for (String value : candidateList) {
                    for (String item : candidateList) {
                        String generatedString =
                                "" + s + value + item;
                        res.add(generatedString);
                    }
                }
            }
        }
        return res;
    }

//    public static void main(String[] args) {
//        // 示例数据
//        String input = "0110ab";
//        int ifHd = Integer.parseInt(input.substring(0, 1));
//        int ifTd = Integer.parseInt(input.substring(1, 2));
//        int ifHi = Integer.parseInt(input.substring(2, 3));
//        int ifTi = Integer.parseInt(input.substring(3, 4));
//
//        int length = ifHd + ifTd + ifHi + ifTi;
//        Queue<String> timesQueue = new LinkedList<>(Arrays.as);
//        char[] chars = input.substring(4, 4 + length).toCharArray();
//        for (Character c : chars) {
//            timesQueue.add(String.valueOf(c));
//        }
//        int hdTimes = (ifHd == 1) ? Integer.parseInt(timesQueue.poll()) : 0;
//        int tdTimes = (ifTd == 1) ? Integer.parseInt(timesQueue.poll()) : 0;
//        int hiTimes = (ifHi == 1) ? Integer.parseInt(timesQueue.poll()) : 0;
//        int tiTimes = (ifTi == 1) ? Integer.parseInt(timesQueue.poll()) : 0;
//
//
//        String hd = (ifHd == 1) ? input.substring(getIndex(ifHd, ifTd, ifHi, ifTi, 0), getIndex(ifHd, ifTd, ifHi,
//        ifTi, 1)) : "";
//        String td = (ifTd == 1) ? input.substring(getIndex(ifHd, ifTd, ifHi, ifTi, 2), getIndex(ifHd, ifTd, ifHi,
//        ifTi, 3)) : "";
//        String hi = (ifHi == 1) ? input.substring(getIndex(ifHd, ifTd, ifHi, ifTi, 4), getIndex(ifHd, ifTd, ifHi,
//        ifTi, 5)) : "";
//        String ti = (ifTi == 1) ? input.substring(getIndex(ifHd, ifTd, ifHi, ifTi, 6)) : "";
//
//        System.out.println("hd: " + hd);
//        System.out.println("td: " + td);
//        System.out.println("hi: " + hi);
//        System.out.println("ti: " + ti);
//    }

    private static int getIndex(int ifHd, int ifTd, int ifHi, int ifTi, int index) {
        int[] order = {ifHd, ifTd, ifHi, ifTi};
        int count = 0;
        for (int value : order) {
            if (value == 1) {
                if (count == index) {
                    return index + 4;
                }
                count++;
            }
        }
        return -1; // 如果找不到对应的位置，可以根据需要进行处理
    }

    private static String generatePossibleKey(String target, int offset) {
        StringBuilder possibleKey = new StringBuilder();
        for (int i = 0; i < target.length(); i++) {
            char c = target.charAt(i);
            int newAscii = (int) c + offset;
            possibleKey.append((char) newAscii);
        }
        return possibleKey.toString();
    }

    public static int calculateAsciiDistance(String str1, String str2) {
        int distance = 0;
        for (int i = 0; i < str1.length(); i++) {
            int ascii1 = str1.charAt(i);
            int ascii2 = str2.charAt(i);
            distance += Math.abs(ascii1 - ascii2);
        }
        return distance;
    }

    public static Map<Integer, Integer> selectUniquei2(Map<Pair<Integer, Integer>, Double> pr1Map) {
        Map<Integer, Integer> i1ToUniquei2 = new HashMap<>();
        Random random = new Random();

        for (Integer i1 : getDistincti1(pr1Map)) {
            double totalProbability = getTotalProbabilityFori1(i1, pr1Map);
            double randomValue = random.nextDouble() * totalProbability;

            double cumulativeProbability = 0;
            for (Map.Entry<Pair<Integer, Integer>, Double> entry : pr1Map.entrySet()) {
                if (entry.getKey().getKey().equals(i1)) {
                    cumulativeProbability += entry.getValue();
                    if (cumulativeProbability >= randomValue) {
                        i1ToUniquei2.put(i1, entry.getKey().getValue());
                        break;
                    }
                }
            }
        }

        return i1ToUniquei2;
    }

    public static double getTotalProbabilityFori1(int i1, Map<Pair<Integer, Integer>, Double> pr1Map) {
        return pr1Map.entrySet().stream()
                .filter(entry -> entry.getKey().getKey().equals(i1))
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }

    public static Iterable<Integer> getDistincti1(Map<Pair<Integer, Integer>, Double> pr1Map) {
        return pr1Map.keySet().stream()
                .map(Pair::getKey)
                .distinct()::iterator;
    }
}
