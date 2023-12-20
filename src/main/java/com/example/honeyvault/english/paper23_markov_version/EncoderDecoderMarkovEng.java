package com.example.honeyvault.english.paper23_markov_version;

import cn.hutool.core.lang.Pair;
import com.example.honeyvault.data_access.EncodeLine;
import com.example.honeyvault.tool.CalPath;
import com.xiaoleilu.hutool.util.RandomUtil;
import dev.mccue.guava.concurrent.AtomicDouble;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.honeyvault.tool.CalPath.countOccurrencesOfOp;

@Component
public class EncoderDecoderMarkovEng {

//    double alpha;

    @Resource
    private EncoderTableMarkovEng encoderTableMarkovEng;

    List<String> greekList = Arrays.asList("Α", "τ", "Β", "Γ", "Δ", "σ", "Ε", "Ζ", "Η", "ρ",
            "Θ", "Ι", "Κ", "Λ", "Μ", "Ν", "Ξ", "Ο", "Π", "Ρ",
            "Φ", "Χ", "Ψ",
            "Ω", "ω", "ψ");
    List<String> normalList = new ArrayList<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "a", "b", "c",
            "d", "e",
            "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+",
            ",", "-", ".", "/", ";", ":", "<", "=", ">", "?",
            "@", "[", "\\", "]", "^", "_", "`", "{", "|", "}", "~", " "));

    public void init(int mkv, double lambdaOp, double lambdaTimes, double lambdaMkv, double lambdaMkv_1,
                     double listLambda) {
//        CsvWriter writer = CsvUtil.getWriter("/app/HvExpData/tables/table23M.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/static/table23M
//        .csv", CharsetUtil.CHARSET_UTF_8);
        encoderTableMarkovEng.buildEncodeTables(mkv, lambdaOp, lambdaTimes, lambdaMkv, lambdaMkv_1, listLambda);
//        writer.writeLine(encoderTableMarkovEng.toString());
    }

//    @PostConstruct
//    public void initAlpha() {
//        alpha = calAlpha();
//    }

    public List<Pair<String, String>> encode(List<String> initVault, int fixedLength, int mkv, double lambdaOp,
                                             double lambdaTimes, double lambdaMkv, double lambdaMkv_1,
                                             double listLambda) {
        List<String> vault = initVault(initVault);
        Map<Pair<Integer, Integer>, Double> pathProbMap = new HashMap<>();
        List<Pair<String, String>> pswd2EncodeString = new LinkedList<>();
        String firstPswd = vault.get(1);
        String encodeFirstPswd = algo4(0, firstPswd, firstPswd, mkv, listLambda);
        encodeFirstPswd = fillWithRandom(encodeFirstPswd, fixedLength);
        pswd2EncodeString.add(new Pair<>(firstPswd, encodeFirstPswd));

        // j is i，i is i+1
        for (int i = 2; i < vault.size(); i++) {
            for (int j = 1; j < i; j++) {
                String pwi1 = vault.get(i);
                String pwi = vault.get(j);

                double A = f_fit(j) / j;
                double pr2 = 1 - f_fit(j);
                if (pwi1.length() <= 4) {
                    EncodeLine<String> pswdLine = encoderTableMarkovEng.pswdFreqEncodeTable.get(vault.get(i));
                    if (pswdLine != null) {
                        pr2 *= pswdLine.getProb();
                    } else {
                        pr2 *= getP1(listLambda).doubleValue();
                    }
                } else {
                    pr2 *= getMarkovProb(vault.get(i), mkv);
                }
                pathProbMap.put(new Pair<>(i, i), pr2);
                double pr1j;
//                int commonLength = getRealLength(CalPath.LongestComSubstr(pwi, pwi1));
//                int pwi1Length = getRealLength(pwi1);
//                int pwiLength = getRealLength(pwi);

                int comLength = CalPath.LongestComSubstr(pwi1, pwi).length();
                if (((double) comLength / Math.max(pwi1.length(), pwi.length()) < 0.2)
                        || (pwi.length() < 5 && comLength < pwi.length())) {
                    pr1j = 0;
                } else {
                    List<List<String>> paths = CalPath.breadthFirstSearch(pwi, pwi1);
                    double pr_ssm = calPr_ssm(paths, pwi);
                    pr1j = A * pr_ssm;
                }
                pathProbMap.put(new Pair<>(i, j), pr1j);


            }
        }
        Map<Integer, Integer> pwi1ToPwi = selectUniquei2(pathProbMap);
        pwi1ToPwi.forEach((pwi1Index, pwiIndex) -> {
            int g;
            if (pwi1Index.equals(pwiIndex)) g = 0;
            else g = pwiIndex;
            Pair<Double, Double> gBound = gEncoder(g, pwi1Index);
            BigInteger gDecimal = getRandomValue(BigDecimal.valueOf(gBound.getKey()).toBigInteger(),
                    BigDecimal.valueOf(gBound.getValue()).toBigInteger());
            String encodedG = toBinaryString(gDecimal, encoderTableMarkovEng.secParam_L);
            String encodeString = algo4(g, vault.get(pwiIndex), vault.get(pwi1Index), mkv, listLambda);

            encodeString = encodedG + encodeString;

            encodeString = fillWithRandom(encodeString, fixedLength);
            pswd2EncodeString.add(new Pair<>(vault.get(pwi1Index), encodeString));
        });
        return pswd2EncodeString;
    }

    public static double f_fit(int i) {
        return 1 / (1 + Math.exp(-0.324 * i - 0.933));

    }

    private Pair<Double, Double> gEncoder(int g, int i) {
        double L = encoderTableMarkovEng.secParam_L;
        double pow = Math.pow(2, L);
        double col2;
        double lower = 0, upper;
        if (g == 0) {
            upper = Math.floor(pow * (1 - f_fit(i - 1)));
        } else {
            col2 = f_fit(i - 1) / (i - 1);
            lower = Math.floor(((1 - f_fit(i - 1) + col2 * (g - 1)) * pow));
            upper = Math.floor(((1 - f_fit(i - 1) + col2 * g) * pow));
        }
        return new Pair<>(lower, upper);
    }


    private String algo4(int g, String basePasswd, String targetString, int mkv, double listLambda) {
        StringBuilder encodeString = new StringBuilder();
        double fixedLength = encoderTableMarkovEng.secParam_L;
        if (g == 0) {
//          编码长度
            EncodeLine<Integer> lengthEncodeLine =
                    encoderTableMarkovEng.encodePasswdLengthTable.get(targetString.length());
            BigInteger encodeValue = getRandomValue(lengthEncodeLine.getLowerBound(),
                    lengthEncodeLine.getUpperBound());
            encodeString.append(toBinaryString(encodeValue, fixedLength));
            //          用list编码
            if (targetString.length() <= 4) {
                if (encoderTableMarkovEng.pswdFreqEncodeTable.get(targetString) == null) {
                    BigDecimal p1 = getP1(listLambda);
                    BigInteger twoL = BigInteger.valueOf(2).pow(encoderTableMarkovEng.secParam_L);
                    BigInteger kUp =
                            twoL.subtract(encoderTableMarkovEng.kNPlus1).divide(p1.multiply(new BigDecimal(twoL)).toBigInteger());
                    BigInteger k = getRandomValue(BigInteger.ZERO, kUp);
                    BigInteger newLower =
                            new BigDecimal(encoderTableMarkovEng.kNPlus1).add(new BigDecimal(k).multiply(p1).multiply(new BigDecimal(twoL))).toBigInteger();

                    while (findOldLower(newLower)) {
                        k = getRandomValue(BigInteger.ZERO, kUp);
                        newLower =
                                new BigDecimal(encoderTableMarkovEng.kNPlus1).add(new BigDecimal(k).multiply(p1).multiply(new BigDecimal(twoL))).toBigInteger();
                    }
                    BigInteger kAdd1 = k.add(BigInteger.valueOf(1));
                    BigInteger newUpper =
                            new BigDecimal(encoderTableMarkovEng.kNPlus1).add(new BigDecimal(kAdd1).multiply(p1).multiply(new BigDecimal(twoL))).toBigInteger();
                    EncodeLine<String> newLine =
                            EncodeLine.<String>builder().originValue(targetString).prob(p1.doubleValue()).lowerBound(newLower).upperBound(newUpper).build();
                    encoderTableMarkovEng.pswdFreqEncodeTable.put(targetString, newLine);
                }
                EncodeLine<String> pswdEncodeLine = encoderTableMarkovEng.pswdFreqEncodeTable.get(targetString);
                encodeValue = getRandomValue(pswdEncodeLine.getLowerBound(),
                        pswdEncodeLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));
            } else {
                int countGreek = 0;
                int lastIndexOfGreek = -1;
                for (int i = 0; i < targetString.length(); i++) {
                    if (greekList.contains(String.valueOf(targetString.charAt(i)))) {
                        countGreek += 1;
                        lastIndexOfGreek = i;
                    }
                }
                //          编码头mkv个字符
                String firstMkvString = targetString.substring(0, mkv);
                EncodeLine<String> firstMkvEncodeLine =
                        encoderTableMarkovEng.encodeFirstMkvTable.get(firstMkvString);
                encodeValue = getRandomValue(firstMkvEncodeLine.getLowerBound(),
                        firstMkvEncodeLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));

                if (countGreek == 4) {
                    //                  编码每第mkv_1个字符
                    Map<String, EncodeLine<String>> encodeMap;
                    int mkv_1 = mkv + 1;
                    for (int j = 0; j + mkv < lastIndexOfGreek + 1; j++) {
                        String window = targetString.substring(j, j + mkv_1);
                        if (j == 0) {
                            encodeMap = encoderTableMarkovEng.encodeEveryMkv_1Table.get(firstMkvString);
                        } else {
                            String prefix = window.substring(0, mkv);
                            encodeMap = encoderTableMarkovEng.encodeEveryMkv_1Table.get(prefix);
                        }
                        String suffix = window.substring(window.length() - 1);

                        EncodeLine<String> encodeLine;
                        if (encodeMap == null) {
                            encodeLine = encoderTableMarkovEng.absentMkv_1Table.get(suffix);
                        } else {
                            encodeLine = encodeMap.get(suffix);
                        }
                        encodeValue = getRandomValue(encodeLine.getLowerBound(),
                                encodeLine.getUpperBound());
                        encodeString.append(toBinaryString(encodeValue, fixedLength));
                    }
                    for (int j = lastIndexOfGreek; j + mkv < targetString.length(); j++) {
                        String window = targetString.substring(j, j + mkv_1);
                        if (j == lastIndexOfGreek) {
                            encodeMap =
                                    encoderTableMarkovEng.greekTo95Table.get(targetString.substring(lastIndexOfGreek,
                                            lastIndexOfGreek + 1));
                        } else {
                            String prefix = window.substring(0, mkv);
                            encodeMap = encoderTableMarkovEng.encodeEveryMkv_1Table95.get(prefix);
                        }
                        String suffix = window.substring(window.length() - 1);

                        EncodeLine<String> encodeLine;
                        if (encodeMap == null) {
                            encodeLine = encoderTableMarkovEng.absentMkv_1Table95.get(suffix);
                        } else {
                            encodeLine = encodeMap.get(suffix);
                        }
                        encodeValue = getRandomValue(encodeLine.getLowerBound(),
                                encodeLine.getUpperBound());
                        encodeString.append(toBinaryString(encodeValue, fixedLength));
                    }
                } else {
                    Map<String, EncodeLine<String>> encodeMap;
                    int mkv_1 = mkv + 1;
                    for (int j = 0; j + mkv < targetString.length(); j++) {
                        String window = targetString.substring(j, j + mkv_1);
                        if (j == 0) {
                            encodeMap = encoderTableMarkovEng.encodeEveryMkv_1Table.get(firstMkvString);
                        } else {
                            String prefix = window.substring(0, mkv);
                            encodeMap = encoderTableMarkovEng.encodeEveryMkv_1Table.get(prefix);
                        }
                        String suffix = window.substring(window.length() - 1);

                        EncodeLine<String> encodeLine;
                        if (encodeMap == null) {
                            encodeLine = encoderTableMarkovEng.absentMkv_1Table.get(suffix);
                        } else {
                            encodeLine = encodeMap.get(suffix);
                        }
                        encodeValue = getRandomValue(encodeLine.getLowerBound(),
                                encodeLine.getUpperBound());
                        encodeString.append(toBinaryString(encodeValue, fixedLength));
                    }
                }
            }


        } else {
            BigInteger encodeValue;
//          找到所有可能的路径
            List<List<String>> paths = CalPath.breadthFirstSearch(basePasswd, targetString);
            Map<List<String>, Double> pathProbMap = new ConcurrentHashMap<>();
//          路径->概率
            paths.forEach(path -> {
                double pathProb = getPathProb(path, basePasswd);
                pathProbMap.put(path, pathProb);
            });
//          根据概率选择路径
            List<String> selectedOpList = selectPathByProbability(pathProbMap);
            String selectedPath = selectedOpList.toString();
//          编码路径
            int hdTimes = countOccurrencesOfOp(selectedPath, "hd");
            int tdTimes = countOccurrencesOfOp(selectedPath, "td");
            int hiTimes = countOccurrencesOfOp(selectedPath, "hi");
            int tiTimes = countOccurrencesOfOp(selectedPath, "ti");
//          1.编码ifOp
            if (basePasswd.length() == 1) {
                EncodeLine<Integer> encodeLine = encoderTableMarkovEng.specialDeleteEncodeLine.get(0);
                encodeValue = getRandomValue(encodeLine.getLowerBound(),
                        encodeLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));
            } else {
                EncodeLine<Integer> ifDeleteLine =
                        encoderTableMarkovEng.encodeIfDeleteProbTable.get((selectedPath.contains("hd") || selectedPath.contains("td")) ?
                                1 : 0);
                encodeValue = getRandomValue(ifDeleteLine.getLowerBound(),
                        ifDeleteLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));

            }


            EncodeLine<Integer> ifInsertLine =
                    encoderTableMarkovEng.encodeIfInsertProbTable.get((selectedPath.contains("hi") || selectedPath.contains("ti")) ?
                            1 : 0);
            encodeValue = getRandomValue(ifInsertLine.getLowerBound(),
                    ifInsertLine.getUpperBound());
            encodeString.append(toBinaryString(encodeValue, fixedLength));

//          2.编码opTimes

            if (hdTimes + tdTimes > 0) {
                EncodeLine<Pair<Integer, Integer>> encodeLine =
                        encoderTableMarkovEng.encodeDeleteTimesProbTable.get(basePasswd.length()).get(new Pair<>(hdTimes,
                                tdTimes));
                encodeValue = getRandomValue(encodeLine.getLowerBound(),
                        encodeLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));

            }

            if (hiTimes + tiTimes > 0) {
                EncodeLine<Pair<Integer, Integer>> encodeLine =
                        encoderTableMarkovEng.encodeInsertTimesProbTable.get(basePasswd.length() - hdTimes - tdTimes).get(new Pair<>(hiTimes,
                                tiTimes));
                encodeValue = getRandomValue(encodeLine.getLowerBound(),
                        encodeLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));
                System.out.println("编码insertTimes" + encodeLine.getOriginValue() + "为" + encodeValue);

            }


//          3.编码op
            for (String op : selectedOpList) {
                op = op.trim();
                if (op.contains("hd")) {
                    EncodeLine<String> hdOpEncodeLine = encoderTableMarkovEng.encodeHdOpProbTable.get(op);
                    encodeValue = getRandomValue(hdOpEncodeLine.getLowerBound(),
                            hdOpEncodeLine.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
                if (op.contains("hi")) {
                    EncodeLine<String> hiOpEncodeLine = encoderTableMarkovEng.encodeHiOpProbTable.get(op);
                    encodeValue = getRandomValue(hiOpEncodeLine.getLowerBound(),
                            hiOpEncodeLine.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
                if (op.contains("ti")) {
                    EncodeLine<String> tiOpEncodeLine = encoderTableMarkovEng.encodeTiOpProbTable.get(op);
                    encodeValue = getRandomValue(tiOpEncodeLine.getLowerBound(),
                            tiOpEncodeLine.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
                if (op.contains("td")) {
                    EncodeLine<String> tdOpEncodeLine = encoderTableMarkovEng.encodeTdOpProbTable.get(op);
                    encodeValue = getRandomValue(tdOpEncodeLine.getLowerBound(),
                            tdOpEncodeLine.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
            }
        }
        return encodeString.toString();
    }

    private BigDecimal getP1(double lambda) {
        BigDecimal p1;
        BigDecimal topP1 = new BigDecimal(lambda);
        double listNumber =
                29 + Math.pow(124, 2) - Math.pow(95, 2) + Math.pow(124, 3) - Math.pow(95, 3) + Math.pow(124, 4) - Math.pow(95, 4);
        BigDecimal bottomP1 =
                BigDecimal.valueOf(encoderTableMarkovEng.pswdFreqMap.values().stream().mapToDouble(Double::doubleValue).sum()).add(BigDecimal.valueOf(listNumber).multiply(BigDecimal.valueOf(lambda)));
        p1 = topP1.divide(bottomP1, 40, RoundingMode.FLOOR);
        return p1;
    }

    private boolean findOldLower(BigInteger finalNewLower) {
        return encoderTableMarkovEng.pswdFreqEncodeTable.values().stream()
                .anyMatch(encodeLine -> encodeLine.getLowerBound().equals(finalNewLower));

    }

    //  cal method
    private double getMarkovProb(String passwd, int mkv) {
        // g=0
        Double lengthProb = encoderTableMarkovEng.passwdLengthProbMap.get(passwd.length());
        Double firstMkvProb = encoderTableMarkovEng.firstMkvProbMap.get(passwd.substring(0, mkv));
        double finalProb = lengthProb * firstMkvProb;
        int mkv_1 = mkv + 1;
        for (int i = 0; i + mkv < passwd.length(); i++) {
            String window = passwd.substring(i, i + mkv_1);
            String prefix = window.substring(0, mkv);
            String suffix = window.substring(window.length() - 1);
            HashMap<String, Double> suffixProb = encoderTableMarkovEng.everyMkv_1ProbMap.get(prefix);
            Double mkv_1Prob;
            if (suffixProb == null) {
                mkv_1Prob = encoderTableMarkovEng.absentMkv_1ProbMap.getOrDefault(suffix, 0.00826446281);
            } else {
                mkv_1Prob = suffixProb.getOrDefault(suffix, 0.00826446281);
            }
            finalProb *= mkv_1Prob;
        }
        return finalProb;
    }

    private double getPathProb(List<String> path, String pw) {
        if (path != null && path.size() > 0) {

            double prob = 1;
            if (path.contains("hd") || path.contains("td")) {
                prob *= encoderTableMarkovEng.ifDeleteProbMap.get(1);
                int hdTimes = countOccurrencesOfOp(String.valueOf(path), "hd");
                int tdTimes = countOccurrencesOfOp(String.valueOf(path), "td");
                Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>> kEncodeLine =
                        encoderTableMarkovEng.encodeDeleteTimesProbTable.get(pw.length());
                EncodeLine<Pair<Integer, Integer>> encodeLine = kEncodeLine.get(new Pair<>(hdTimes, tdTimes));
                prob *= encodeLine.getProb();
            } else prob *= encoderTableMarkovEng.ifDeleteProbMap.get(0);

            if (path.contains("hi") || path.contains("ti")) {
                prob *= encoderTableMarkovEng.ifInsertProbMap.get(1);
                int hiTimes = countOccurrencesOfOp(String.valueOf(path), "hi");
                int tiTimes = countOccurrencesOfOp(String.valueOf(path), "ti");
                int hdTimes = countOccurrencesOfOp(String.valueOf(path), "hd");
                int tdTimes = countOccurrencesOfOp(String.valueOf(path), "td");
                Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>> kEncodeLine =
                        encoderTableMarkovEng.encodeInsertTimesProbTable.get(pw.length() - hdTimes - tdTimes);
                EncodeLine<Pair<Integer, Integer>> encodeLine = kEncodeLine.get(new Pair<>(hiTimes, tiTimes));
                prob *= encodeLine.getProb();
            } else prob *= encoderTableMarkovEng.ifInsertProbMap.get(0);

            for (String op : path) {
                op = op.trim();
                if (op.contains("hd")) prob *= encoderTableMarkovEng.hdOpProbMap.get(op);
                else if (op.contains("hi")) prob *= encoderTableMarkovEng.hiOpProbMap.get(op);
                else if (op.contains("td")) prob *= encoderTableMarkovEng.tdOpProbMap.get(op);
                else if (op.contains("ti")) prob *= encoderTableMarkovEng.tiOpProbMap.get(op);
            }
            return prob;
        } else {
            return 1;
        }
    }


    //  tools
    private double calPr_ssm(List<List<String>> paths, String pw) {
        AtomicDouble pr_ssm = new AtomicDouble(0);
        paths.forEach(path -> {
            double pathProb = getPathProb(path, pw);
            pr_ssm.getAndAdd(pathProb);
        });
        return pr_ssm.get();
    }

    public static List<String> selectPathByProbability(Map<List<String>, Double> pathProbMap) {
        double totalProb = pathProbMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double randomProb = new Random().nextDouble() * totalProb;

        double cumulativeProb = 0.0;
        for (Map.Entry<List<String>, Double> entry : pathProbMap.entrySet()) {
            cumulativeProb += entry.getValue();
            if (randomProb < cumulativeProb) {
                return entry.getKey();
            }
        }

        return pathProbMap.entrySet().iterator().next().getKey();
    }


    public static Map<Integer, Integer> selectUniquei2(Map<Pair<Integer, Integer>, Double> pr1Map) {
//      根据路径概率 均匀的选择一个
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


    static List<String> initVault(List<String> vault) {
        List<String> temp = new LinkedList<>();
        temp.add(null);
        temp.addAll(vault);
        return temp;
    }


    String toBinaryString(BigInteger number, double fixedLength) {
        String binary = number.toString(2);

        double zeroLength = fixedLength - binary.length();

        String zeroString = "0".repeat((int) zeroLength);
        zeroString += binary;
        return zeroString;
    }

    private String genRandomStr() {
        StringBuilder s = new StringBuilder();
        int size = RandomUtil.randomInt(1, 5);
        int greekSize = RandomUtil.randomInt(1, size + 1);
        int normalSize = size - greekSize;

        while (greekSize * 2 + normalSize > 16 || (size <= 4 && greekSize == 0)) {
            size = RandomUtil.randomInt(1, 17);
            if (size <= 4) {
                greekSize = RandomUtil.randomInt(1, Math.min(5, size + 1));
            } else {
                greekSize = RandomUtil.randomInt(0, Math.min(5, size + 1));
            }
            normalSize = size - greekSize;
        }
        if (size == 1) {
            int ranS = RandomUtil.randomInt(0, greekList.size());
            s.append(greekList.get(ranS));
        } else {
            for (int i = 0; i < size; i++) {
                int randomValue = RandomUtil.randomInt(0, 2);

                if (randomValue == 0) {
                    if (greekSize - 1 != -1) {
                        greekSize -= 1;
                        int ranS = RandomUtil.randomInt(0, greekList.size());
                        s.append(greekList.get(ranS));
                    } else {
                        normalSize -= 1;
                        int ranS = RandomUtil.randomInt(0, greekList.size());
                        s.append(normalList.get(ranS));
                    }
                } else {
                    if (normalSize - 1 != -1) {
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


    public List<String> decode(List<String> encodedList, int mkv, double listLambda) {
        encodedList = initVault(encodedList);
        int fixedLength = encoderTableMarkovEng.secParam_L;
        List<String> originPswd = new ArrayList<>();
//        CsvWriter writer = CsvUtil.getWriter("/app/HvExpData/tables/table21M.csv", CharsetUtil.CHARSET_UTF_8);
        for (int index = 1; index < encodedList.size(); index++) {
            StringBuilder decodedPswd = new StringBuilder();
            String encodedString = encodedList.get(index);
            if (encodedString == null) continue;
            List<String> encodeElementList = splitString(encodedString, fixedLength);
            if (index == 1) {
                BigInteger encodedPwLength = new BigInteger(encodeElementList.get(0), 2);
                Integer pwLength = findOriginValue(encodedPwLength, encoderTableMarkovEng.encodePasswdLengthTable);

                if (pwLength <= 4) {
                    BigInteger encodedPswd = new BigInteger(encodeElementList.get(1), 2);
                    String pswd = findOriginValue(encodedPswd, encoderTableMarkovEng.pswdFreqEncodeTable);
                    if (pswd == null) {
                        BigInteger kNPlus1 = encoderTableMarkovEng.kNPlus1;
                        BigDecimal pow = BigDecimal.valueOf(2).pow(encoderTableMarkovEng.secParam_L);
                        BigDecimal p1 = getP1(listLambda);

                        BigDecimal bottom = p1.multiply(pow);
                        BigDecimal top = new BigDecimal(encodedPswd).subtract(new BigDecimal(kNPlus1));
                        BigInteger lowerBound =
                                new BigDecimal(kNPlus1).add(top.divide(bottom, 40, RoundingMode.FLOOR)
                                        .multiply(bottom)).toBigInteger();
                        BigInteger upperBound =
                                new BigDecimal(kNPlus1).add(top.divide(bottom, 40, RoundingMode.FLOOR).add(BigDecimal.valueOf(1))
                                        .multiply(bottom)).toBigInteger();
                        String randomStr = genRandomStr();
                        while (encoderTableMarkovEng.pswdFreqEncodeTable.containsKey(randomStr)) {
                            randomStr = genRandomStr();
                        }
                        EncodeLine<String> newRandomLine =
                                EncodeLine.<String>builder().lowerBound(lowerBound).upperBound(upperBound).originValue(randomStr).build();
                        encoderTableMarkovEng.pswdFreqEncodeTable.put(randomStr, newRandomLine);
                        decodedPswd.append(randomStr);
                    } else {
                        decodedPswd.append(pswd);
                    }
                    originPswd.add(decodedPswd.toString());
                } else {
                    int countGreek = 0;
                    BigInteger encodedFirstMkv = new BigInteger(encodeElementList.get(1), 2);
                    String firstMkv = findOriginValue(encodedFirstMkv, encoderTableMarkovEng.encodeFirstMkvTable);
                    decodedPswd.append(firstMkv);
                    if (greekList.contains(firstMkv)) countGreek++;
                    int preIndex = 0;
                    String crossGreek = "";
//                    EncodeLine(originValue=a, prob=0.04575472596562739,
//                    lowerBound=110818597201708566735832842465927000000,
//                    upperBound=126388123651111183353591822365115600000)
                    List<String> encodedEveryMkv_1List = encodeElementList.subList(2, pwLength + 2 - mkv);
                    for (int i = 0; i < encodedEveryMkv_1List.size(); i++) {
                        String prefix;
                        String suffix;
                        BigInteger encodedSuffix;
                        if (i == 0) {
                            prefix = firstMkv;
                            encodedSuffix = new BigInteger(encodedEveryMkv_1List.get(0), 2);
                        } else {
                            prefix = decodedPswd.substring(i, i + mkv);
                            encodedSuffix = new BigInteger(encodedEveryMkv_1List.get(i), 2);
                        }
                        Map<String, EncodeLine<String>> encodeSuffixTable =
                                encoderTableMarkovEng.encodeEveryMkv_1Table.get(prefix);
                        if (encodeSuffixTable == null) {
                            suffix = findOriginValue(encodedSuffix, encoderTableMarkovEng.absentMkv_1Table);
                        } else {
                            suffix = findOriginValue(encodedSuffix, encodeSuffixTable);
                        }
                        decodedPswd.append(suffix);
                        if (greekList.contains(suffix)) countGreek++;
                        if (countGreek == 4) {
                            preIndex = i;
                            crossGreek = suffix;
                            break;
                        }
                    }
                    if (countGreek == 4) {
                        for (int i = preIndex + 1; i < encodedEveryMkv_1List.size(); i++) {
                            String prefix;
                            String suffix;
                            BigInteger encodedSuffix;
                            prefix = decodedPswd.substring(i, i + mkv);
                            encodedSuffix = new BigInteger(encodedEveryMkv_1List.get(i), 2);
                            if (i == preIndex + 1) {
                                Map<String, EncodeLine<String>> greekTo95 =
                                        encoderTableMarkovEng.greekTo95Table.get(crossGreek);
                                suffix = findOriginValue(encodedSuffix, greekTo95);
                            } else {
                                Map<String, EncodeLine<String>> encodeSuffixTable =
                                        encoderTableMarkovEng.encodeEveryMkv_1Table95.get(prefix);
                                if (encodeSuffixTable == null) {
                                    suffix = findOriginValue(encodedSuffix, encoderTableMarkovEng.absentMkv_1Table95);
                                } else {
                                    suffix = findOriginValue(encodedSuffix, encodeSuffixTable);
                                }
                            }
                            decodedPswd.append(suffix);
                        }
                    }
                    originPswd.add(decodedPswd.toString());
                    System.out.println("已经解码：" + originPswd);


                }

            } else {
                BigInteger encodedG = new BigInteger(encodeElementList.get(0), 2);
                int g = 0;
                boolean found = false;
                for (int t = 0; t < index && !found; t++) {
                    Pair<Double, Double> gBound = gEncoder(t, index);
                    BigInteger lowerBound = BigDecimal.valueOf(gBound.getKey()).toBigInteger();
                    BigInteger upperBound = BigDecimal.valueOf(gBound.getValue()).toBigInteger();

                    if (encodedG.compareTo(lowerBound) > 0 && encodedG.compareTo(upperBound) < 0) {
                        g = t;
                        found = true;
                    }
                }
                if (g == 0) {
                    BigInteger encodedPwLength = new BigInteger(encodeElementList.get(1), 2);
                    Integer pwLength = findOriginValue(encodedPwLength, encoderTableMarkovEng.encodePasswdLengthTable);

                    if (pwLength <= 4) {
                        BigInteger encodedPswd = new BigInteger(encodeElementList.get(2), 2);
                        String pswd = findOriginValue(encodedPswd, encoderTableMarkovEng.pswdFreqEncodeTable);
                        if (pswd == null) {
                            BigInteger kNPlus1 = encoderTableMarkovEng.kNPlus1;
                            BigDecimal pow = BigDecimal.valueOf(2).pow(encoderTableMarkovEng.secParam_L);
                            BigDecimal p1 = getP1(listLambda);

                            BigDecimal bottom = p1.multiply(pow);
                            BigDecimal top = new BigDecimal(encodedPswd).subtract(new BigDecimal(kNPlus1));
                            BigInteger lowerBound =
                                    new BigDecimal(kNPlus1).add(top.divide(bottom, 40, RoundingMode.FLOOR)
                                            .multiply(bottom)).toBigInteger();
                            BigInteger upperBound =
                                    new BigDecimal(kNPlus1).add(top.divide(bottom, 40, RoundingMode.FLOOR).add(BigDecimal.valueOf(1))
                                            .multiply(bottom)).toBigInteger();
                            String randomStr = genRandomStr();
                            while (encoderTableMarkovEng.pswdFreqEncodeTable.containsKey(randomStr)) {
                                randomStr = genRandomStr();
                            }
                            EncodeLine<String> newRandomLine =
                                    EncodeLine.<String>builder().lowerBound(lowerBound).upperBound(upperBound).originValue(randomStr).build();
                            encoderTableMarkovEng.pswdFreqEncodeTable.put(randomStr, newRandomLine);
                            decodedPswd.append(randomStr);
                        } else {
                            decodedPswd.append(pswd);
                        }
                        originPswd.add(decodedPswd.toString());
                    } else {
                        int countGreek = 0;
                        BigInteger encodedFirstMkv = new BigInteger(encodeElementList.get(2), 2);
                        String firstMkv = findOriginValue(encodedFirstMkv, encoderTableMarkovEng.encodeFirstMkvTable);
                        decodedPswd.append(firstMkv);
                        if (greekList.contains(firstMkv)) countGreek++;
                        int preIndex = 0;
                        String crossGreek = "";
                        List<String> encodedEveryMkv_1List = encodeElementList.subList(3, pwLength + 3 - mkv);
                        for (int i = 0; i < encodedEveryMkv_1List.size(); i++) {
                            String prefix;
                            String suffix;
                            BigInteger encodedSuffix;
                            if (i == 0) {
                                prefix = firstMkv;
                                encodedSuffix = new BigInteger(encodedEveryMkv_1List.get(0), 2);
                            } else {
                                prefix = decodedPswd.substring(i, i + mkv);
                                encodedSuffix = new BigInteger(encodedEveryMkv_1List.get(i), 2);
                            }
                            Map<String, EncodeLine<String>> encodeSuffixTable =
                                    encoderTableMarkovEng.encodeEveryMkv_1Table.get(prefix);
                            if (encodeSuffixTable == null) {
                                suffix = findOriginValue(encodedSuffix, encoderTableMarkovEng.absentMkv_1Table);
                            } else {
                                suffix = findOriginValue(encodedSuffix, encodeSuffixTable);
                            }
                            decodedPswd.append(suffix);
                            if (greekList.contains(suffix)) countGreek++;
                            if (countGreek == 4) {
                                preIndex = i;
                                crossGreek = suffix;
                                break;
                            }
                        }
                        if (countGreek == 4) {
                            for (int i = preIndex + 1; i < encodedEveryMkv_1List.size(); i++) {
                                String prefix;
                                String suffix;
                                BigInteger encodedSuffix;
                                prefix = decodedPswd.substring(i, i + mkv);
                                encodedSuffix = new BigInteger(encodedEveryMkv_1List.get(i), 2);

                                if (i == preIndex + 1) {
                                    Map<String, EncodeLine<String>> greekTo95 =
                                            encoderTableMarkovEng.greekTo95Table.get(crossGreek);
                                    suffix = findOriginValue(encodedSuffix, greekTo95);
                                } else {
                                    Map<String, EncodeLine<String>> encodeSuffixTable =
                                            encoderTableMarkovEng.encodeEveryMkv_1Table95.get(prefix);
                                    if (encodeSuffixTable == null) {
                                        suffix = findOriginValue(encodedSuffix,
                                                encoderTableMarkovEng.absentMkv_1Table95);
                                    } else {
                                        suffix = findOriginValue(encodedSuffix, encodeSuffixTable);
                                    }
                                }
                                decodedPswd.append(suffix);
                            }
                        }
                        originPswd.add(decodedPswd.toString());
                        System.out.println("已经解码：" + originPswd);


                    }

                } else {
                    BigInteger encodedIfDelete = new BigInteger(encodeElementList.get(1), 2);
                    BigInteger encodedIfInsert = new BigInteger(encodeElementList.get(2), 2);


                    String baseString = originPswd.get(g - 1);
                    int ifInsert = findOriginValue(encodedIfInsert, encoderTableMarkovEng.encodeIfInsertProbTable);
                    int ifDelete;

                    if (baseString.length() <4) {
                        ifDelete = 0;
                    } else {
                        ifDelete = findOriginValue(encodedIfDelete, encoderTableMarkovEng.encodeIfDeleteProbTable);
                    }
                    if (baseString.length() >= 16) {
                        ifInsert = 0;
                    }
                    System.out.println("解码ifDelete" + encodedIfDelete + "为" + ifDelete);
                    System.out.println("更新ifInsert" + encodedIfInsert + "为" + ifInsert);


                    int timesLength = ifInsert + ifDelete;
                    System.out.println("timesLength:" + timesLength);
                    Queue<String> opTimesList = new LinkedList<>();
                    for (int i = 0; i < timesLength; i++) {
                        String s = encodeElementList.get(3 + i);
                        opTimesList.add(s);
                    }
//              按照优先级排列
                    BigInteger encodedDeleteTimes = ((ifDelete == 1) && opTimesList.size() > 0) ?
                            new BigInteger(opTimesList.poll(), 2) : BigInteger.valueOf(0);
                    BigInteger encodedInsertTimes = ((ifInsert == 1) && opTimesList.size() > 0) ?
                            new BigInteger(opTimesList.poll(), 2) : BigInteger.valueOf(0);

//              查表找原始值
                    int hdTimes = 0, tdTimes = 0, hiTimes = 0, tiTimes = 0;
                    if (!encodedDeleteTimes.equals(BigInteger.valueOf(0))) {
                        Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>> encodeDeleteTimesForKLine =
                                encoderTableMarkovEng.encodeDeleteTimesProbTable.get(baseString.length());
                        Pair<Integer, Integer> hdAndTdTimes = findOriginValue(encodedDeleteTimes,
                                encodeDeleteTimesForKLine);
                        hdTimes = hdAndTdTimes.getKey();
                        tdTimes = hdAndTdTimes.getValue();
                    }

                    if (!encodedInsertTimes.equals(BigInteger.valueOf(0))) {

                        Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>> encodeInsertTimesForKLine =
                                encoderTableMarkovEng.encodeInsertTimesProbTable.get(baseString.length() - hdTimes - tdTimes);

                        Pair<Integer, Integer> hiAndTiTimes = findOriginValue(encodedInsertTimes,
                                encodeInsertTimesForKLine);

                        hiTimes = hiAndTiTimes.getKey();
                        tiTimes = hiAndTiTimes.getValue();
                    }
//              总共操作次数
                    int opsLength = hdTimes + tdTimes + hiTimes + tiTimes;
//              具体操作list
                    Queue<String> finalOpList = new LinkedList<>();
                    if (timesLength > 0) {
                        Queue<String> opQueue = new LinkedList<>(encodeElementList.subList(3 + timesLength,
                                3 + timesLength + opsLength));
                        while (hdTimes != 0 && opQueue.size() > 0) {
                            String finalOp = findOriginValue(new BigInteger(opQueue.poll(), 2),
                                    encoderTableMarkovEng.encodeHdOpProbTable);
                            finalOpList.add(finalOp);
                            hdTimes--;
                        }
                        while (tdTimes != 0 && opQueue.size() > 0) {
                            String finalOp = findOriginValue(new BigInteger(opQueue.poll(), 2),
                                    encoderTableMarkovEng.encodeTdOpProbTable);
                            finalOpList.add(finalOp);
                            tdTimes--;
                        }
                        while (hiTimes != 0 && opQueue.size() > 0) {
                            String finalOp = findOriginValue(new BigInteger(opQueue.poll(), 2),
                                    encoderTableMarkovEng.encodeHiOpProbTable);
                            finalOpList.add(finalOp);
                            hiTimes--;
                        }
                        while (tiTimes != 0 && opQueue.size() > 0) {
                            String finalOp = findOriginValue(new BigInteger(opQueue.poll(), 2),
                                    encoderTableMarkovEng.encodeTiOpProbTable);
                            finalOpList.add(finalOp);
                            tiTimes--;
                        }
                    }

//              对baseString执行操作
                    for (String op : finalOpList) {
                        String parameter;
                        parameter = op.substring(3, 4);
                        if (op.contains("hd")) baseString = hd(baseString, parameter);
                        if (op.contains("td")) baseString = td(baseString, parameter);
                        if (op.contains("hi")) baseString = hi(baseString, parameter);
                        if (op.contains("ti")) baseString = ti(baseString, parameter);
                    }
//              加入到已解码列表中
                    decodedPswd.append(baseString);
                    originPswd.add(decodedPswd.toString());
                    System.out.println("已经解码：" + originPswd);
                }

            }


        }
        return originPswd;

    }


    <T> T findOriginValue(BigInteger encodedValue, Map<T, EncodeLine<T>> encodeTable) {
        Optional<EncodeLine<T>> result = encodeTable.values().stream()
                .filter(line -> encodedValue.compareTo(line.getLowerBound()) > 0 &&
                        encodedValue.compareTo(line.getUpperBound()) < 0)
                .findFirst();

        if (result.isPresent()) {
            EncodeLine<T> encodeLine = result.get();
            // 在这里对符合条件的 EncodeLine 进行处理
            return encodeLine.getOriginValue();
        } else {
            // 没有找到符合条件的 EncodeLine
            return null;
        }
    }


    static BigInteger getRandomValue(BigInteger lower, BigInteger upper) {
        double seed = Math.random();
        BigDecimal lowerBound = new BigDecimal(lower);
        BigDecimal upperBound = new BigDecimal(upper);
        BigDecimal randomValue = upperBound.subtract(lowerBound).multiply(BigDecimal.valueOf(seed)).add(lowerBound);
        return randomValue.toBigInteger();
    }


    List<String> splitString(String input, int chunkSize) {
        List<String> chunks = new ArrayList<>();

        for (int i = 0; i < input.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, input.length());
            chunks.add(input.substring(i, end));
        }

        return chunks;
    }

    String hd(String baseString, String content) {
//        if (baseString.startsWith(content)) {
        baseString = baseString.substring(content.length());
//        }
        return baseString;
    }

    String td(String baseString, String content) {
//        if (baseString.endsWith(content)) {
        baseString = baseString.substring(0, baseString.length() - content.length());
//        }
        return baseString;
    }

    String hi(String baseString, String content) {
        return content + baseString;
    }

    String ti(String baseString, String content) {
        return baseString + content;
    }

    String fillWithRandom(String original, int targetLength) {
        if (targetLength <= original.length()) {
            return original;
        }
        StringBuilder filledString = new StringBuilder(original);
        while (filledString.length() < targetLength) {
            filledString.append(RandomUtil.randomInt(0, 2));
        }

        return filledString.toString();
    }

}
