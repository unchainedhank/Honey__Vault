package com.example.honeyvault.chinese.paper23_list_version;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriter;
import com.example.honeyvault.data_access.EncodeLine;
import com.example.honeyvault.tool.CalPath;
import com.xiaoleilu.hutool.util.CharsetUtil;
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
public class EncoderDecoderListCN {

//    double alpha;

    @Resource
    private EncoderTableListCN encoderTableListCN;


    public void init(double lambdaOp, double lambdaTimes, double listLambda) {
//        CsvWriter writer = CsvUtil.getWriter("/app/HvExpData/tables/table23L.csv", CharsetUtil.CHARSET_UTF_8);
        encoderTableListCN.buildEncodeTables(lambdaOp, lambdaTimes, listLambda);
//        writer.writeLine("encodeHdOpProbTable" + ":" + encoderTableListCN.encodeHdOpProbTable);
//        writer.writeLine("encodeHiOpProbTable" + ":" + encoderTableListCN.encodeHiOpProbTable);
//        writer.writeLine("encodeTdOpProbTable" + ":" + encoderTableListCN.encodeTdOpProbTable);
//        writer.writeLine("encodeTiOpProbTable" + ":" + encoderTableListCN.encodeTiOpProbTable);
//        writer.writeLine("encodeIfHdProbTable" + ":" + encoderTableListCN.encodeIfHdProbTable);
//        writer.writeLine("encodeIfTiProbTable" + ":" + encoderTableListCN.encodeIfTiProbTable);
//        writer.writeLine("encodeIfTdProbTable" + ":" + encoderTableListCN.encodeIfTdProbTable);
//        writer.writeLine("encodeIfHdProbTable" + ":" + encoderTableListCN.encodeIfHdProbTable);
//        writer.writeLine("encodeIfHiProbTable" + ":" + encoderTableListCN.encodeIfHiProbTable);
//        writer.writeLine("encodeHdTimesProbTable" + ":" + encoderTableListCN.encodeHdTimesProbTable);
//        writer.writeLine("encodeHiTimesProbTable" + ":" + encoderTableListCN.encodeHiTimesProbTable);
//        writer.writeLine("encodeTiTimesProbTable" + ":" + encoderTableListCN.encodeTiTimesProbTable);
//        writer.writeLine("encodeTdTimesProbTable" + ":" + encoderTableListCN.encodeTdTimesProbTable);
//        writer.writeLine("pswdFreqEncodeTable" + ":" + encoderTableListCN.pswdFreqEncodeTable);
//        writer.close();
    }

    public List<Pair<String, String>> encode(List<String> initVault, int fixedLength, double listLambda) {
//        encoderTableList.buildEncodeTables(lambdaOp, lambdaTimes, listLambda);
        List<String> vault = initVault(initVault);
        Map<Pair<Integer, Integer>, Double> pathProbMap = new HashMap<>();
        List<Pair<String, String>> pswd2EncodeString = new LinkedList<>();
        String firstPswd = vault.get(1);
        String encodeFirstPswd = algo4(0, firstPswd, firstPswd, listLambda);
        encodeFirstPswd = fillWithRandom(encodeFirstPswd, fixedLength);
        pswd2EncodeString.add(new Pair<>(firstPswd, encodeFirstPswd));

        // j is i，i is i+1
        for (int i = 2; i < vault.size(); i++) {
            for (int j = 1; j < i; j++) {
                String pwi1 = vault.get(i);
                String pwi = vault.get(j);

                double A = f_fit(j) / j;
                double B = 1 - f_fit(j);
                double pr2 = B;
                EncodeLine<String> pswdLine = encoderTableListCN.pswdFreqEncodeTable.get(vault.get(i));
                if (pswdLine != null) {
                    pr2 *= pswdLine.getProb();
                } else {
                    pr2 *= getP1(listLambda).doubleValue();
                }

                pathProbMap.put(new Pair<>(i, i), pr2);

                double pr1j;

                if (((double) CalPath.LongestComSubstr(pwi1, pwi).length() / Math.max(pwi1.length(), pwi.length()) < 0.125)) {
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
//            System.out.println("pw i+1" + ":" + vault.get(pwi1Index) + ":" + pwi1Index);
//            System.out.println("pw i" + ":" + vault.get(pwiIndex) + ":" + pwiIndex);
            int g;
            if (pwi1Index.equals(pwiIndex)) g = 0;
            else g = pwiIndex;
//            System.out.println("g" + ":" + g);
//            System.out.println("index" + ":" + pwiIndex);
            Pair<Double, Double> gBound = gEncoder(g, pwi1Index);
            BigInteger gDecimal = getRandomValue(BigDecimal.valueOf(gBound.getKey()).toBigInteger(),
                    BigDecimal.valueOf(gBound.getValue()).toBigInteger());
            String encodedG = toBinaryString(gDecimal, encoderTableListCN.secParam_L);
            String encodeString = algo4(g, vault.get(pwiIndex), vault.get(pwi1Index), listLambda);
//            System.out.println("encoded string" + ":" + encodeString);

            encodeString = encodedG + encodeString;

            encodeString = fillWithRandom(encodeString, fixedLength);
            pswd2EncodeString.add(new Pair<>(vault.get(pwi1Index), encodeString));
//            System.out.println(pswd2EncodeString);
        });
        return pswd2EncodeString;
    }

    public static double f_fit(int i) {
        return 1 / (1 + Math.exp(-1.293 * i + 0.575));
    }

//    private double calAlpha() {
//        Set<PathAndAlphaUser> pathAndAlphaUsers = pathStatistic.parsePswds();
//        List<Double> alphaList = new ArrayList<>();
//        pathAndAlphaUsers.forEach(p -> {
//            double alpha;
//            List<String> passwds = p.getPswdList();
//            Map<String, Integer> freqMap = new HashMap<>();
//            for (String pswd : passwds) {
//                freqMap.put(pswd, freqMap.getOrDefault(pswd, 0) + 1);
//            }
//            long deno = MathUtil.combinationCount(passwds.size(), 2);
//            double totalCombinations = 0;
//            for (int count : freqMap.values()) {
//                if (count >= 2) {
//                    totalCombinations += MathUtil.combinationCount(count, 2);
//                }
//            }
//            if (totalCombinations == 0) {
//                alpha = 0;
//            } else {
//                alpha = totalCombinations / deno;
//            }
//            alphaList.add(alpha);
//        });
//        return alphaList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
//    }

    private Pair<Double, Double> gEncoder(int g, int i) {
        double L = encoderTableListCN.secParam_L;
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


    private String algo4(int g, String basePasswd, String targetString, double lambda) {
        StringBuilder encodeString = new StringBuilder();
        double fixedLength = encoderTableListCN.secParam_L;
        if (g == 0) {
            BigInteger encodeValue;
            if (encoderTableListCN.pswdFreqEncodeTable.get(targetString) == null) {
                BigDecimal p1 = getP1(lambda);
//                System.out.println("p1:" + p1);
                BigInteger twoL = BigInteger.valueOf(2).pow(encoderTableListCN.secParam_L);
                BigInteger kUp =
                        twoL.subtract(encoderTableListCN.kNPlus1).divide(p1.multiply(new BigDecimal(twoL)).toBigInteger());
                BigInteger k = getRandomValue(BigInteger.ZERO, kUp);
//                System.out.println("k:" + k);
                BigInteger newLower =
                        new BigDecimal(encoderTableListCN.kNPlus1).add(new BigDecimal(k).multiply(p1).multiply(new BigDecimal(twoL))).toBigInteger();

                while (findOldLower(newLower)) {
                    k = getRandomValue(BigInteger.ZERO, kUp);
                    newLower =
                            new BigDecimal(encoderTableListCN.kNPlus1).add(new BigDecimal(k).multiply(p1).multiply(new BigDecimal(twoL))).toBigInteger();
                }
                BigInteger kAdd1 = k.add(BigInteger.valueOf(1));
                BigInteger newUpper =
                        new BigDecimal(encoderTableListCN.kNPlus1).add(new BigDecimal(kAdd1).multiply(p1).multiply(new BigDecimal(twoL))).toBigInteger();
//                System.out.println("newUpper:" + newUpper);
//                System.out.println("newLower:" + newLower);
                EncodeLine<String> newLine =
                        EncodeLine.<String>builder().originValue(targetString).prob(p1.doubleValue()).lowerBound(newLower).upperBound(newUpper).build();
                encoderTableListCN.pswdFreqEncodeTable.put(targetString, newLine);
            }
            EncodeLine<String> line = encoderTableListCN.pswdFreqEncodeTable.get(targetString);
            encodeValue = getRandomValue(line.getLowerBound(), line.getUpperBound());
            encodeString.append(toBinaryString(encodeValue, fixedLength));
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
                EncodeLine<Integer> encodeLine = encoderTableListCN.specialDeleteEncodeLine.get(0);
                encodeValue = getRandomValue(encodeLine.getLowerBound(),
                        encodeLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));
            } else {
                EncodeLine<Integer> ifDeleteLine =
                        encoderTableListCN.encodeIfDeleteProbTable.get((selectedPath.contains("hd") || selectedPath.contains("td")) ?
                                1 : 0);
                encodeValue = getRandomValue(ifDeleteLine.getLowerBound(),
                        ifDeleteLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));
            }


            EncodeLine<Integer> ifInsertLine =
                    encoderTableListCN.encodeIfInsertProbTable.get((selectedPath.contains("hi") || selectedPath.contains("ti")) ?
                            1 : 0);
            encodeValue = getRandomValue(ifInsertLine.getLowerBound(),
                    ifInsertLine.getUpperBound());
            encodeString.append(toBinaryString(encodeValue, fixedLength));

//          2.编码opTimes

            if (hdTimes + tdTimes > 0) {
                EncodeLine<Pair<Integer, Integer>> encodeLine =
                        encoderTableListCN.encodeDeleteTimesProbTable.get(basePasswd.length()).get(new Pair<>(hdTimes,
                                tdTimes));
                encodeValue = getRandomValue(encodeLine.getLowerBound(),
                        encodeLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));
            }

            if (hiTimes + tiTimes > 0) {
                EncodeLine<Pair<Integer, Integer>> encodeLine =
                        encoderTableListCN.encodeInsertTimesProbTable.get(basePasswd.length() - hdTimes - tdTimes).get(new Pair<>(hiTimes,
                                tiTimes));
                encodeValue = getRandomValue(encodeLine.getLowerBound(),
                        encodeLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));
            }

//          3.编码op
            for (String op : selectedOpList) {
                op = op.trim();
                if (op.contains("hd")) {
                    EncodeLine<String> hdOpEncodeLine = encoderTableListCN.encodeHdOpProbTable.get(op);
                    encodeValue = getRandomValue(hdOpEncodeLine.getLowerBound(),
                            hdOpEncodeLine.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
                if (op.contains("hi")) {
                    EncodeLine<String> hiOpEncodeLine = encoderTableListCN.encodeHiOpProbTable.get(op);
                    encodeValue = getRandomValue(hiOpEncodeLine.getLowerBound(),
                            hiOpEncodeLine.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
                if (op.contains("ti")) {
                    EncodeLine<String> tiOpEncodeLine = encoderTableListCN.encodeTiOpProbTable.get(op);
                    encodeValue = getRandomValue(tiOpEncodeLine.getLowerBound(),
                            tiOpEncodeLine.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
                if (op.contains("td")) {
                    EncodeLine<String> tdOpEncodeLine = encoderTableListCN.encodeTdOpProbTable.get(op);
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
        double listNumber = 29;
        for (int i = 2; i < 17; i++) {
            listNumber += Math.pow(124, i);
        }
        BigDecimal bottomP1 =
                new BigDecimal(encoderTableListCN.originPswdFreqSize).add(BigDecimal.valueOf(listNumber).multiply(BigDecimal.valueOf(lambda)));
        p1 = topP1.divide(bottomP1, 40, RoundingMode.FLOOR);
        return p1;
    }

    private boolean findOldLower(BigInteger finalNewLower) {
        return encoderTableListCN.pswdFreqEncodeTable.values().stream()
                .anyMatch(encodeLine -> encodeLine.getLowerBound().equals(finalNewLower));

    }

    //  cal method


    private double getPathProb(List<String> path, String pw) {
        if (path != null && path.size() > 0) {

            double prob = 1;
            if (path.contains("hd") || path.contains("td")) {
                prob *= encoderTableListCN.ifDeleteProbMap.get(1);
                int hdTimes = countOccurrencesOfOp(String.valueOf(path), "hd");
                int tdTimes = countOccurrencesOfOp(String.valueOf(path), "td");
                Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>> kEncodeLine =
                        encoderTableListCN.encodeDeleteTimesProbTable.get(pw.length());
                EncodeLine<Pair<Integer, Integer>> encodeLine = kEncodeLine.get(new Pair<>(hdTimes, tdTimes));
                prob *= encodeLine.getProb();
            } else prob *= encoderTableListCN.ifDeleteProbMap.get(0);

            if (path.contains("hi") || path.contains("ti")) {
                prob *= encoderTableListCN.ifInsertProbMap.get(1);
                int hiTimes = countOccurrencesOfOp(String.valueOf(path), "hi");
                int tiTimes = countOccurrencesOfOp(String.valueOf(path), "ti");
                int hdTimes = countOccurrencesOfOp(String.valueOf(path), "hd");
                int tdTimes = countOccurrencesOfOp(String.valueOf(path), "td");
                Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>> kEncodeLine =
                        encoderTableListCN.encodeInsertTimesProbTable.get(pw.length() - hdTimes - tdTimes);
                EncodeLine<Pair<Integer, Integer>> encodeLine = kEncodeLine.get(new Pair<>(hiTimes, tiTimes));
                prob *= encodeLine.getProb();
            } else prob *= encoderTableListCN.ifInsertProbMap.get(0);

            for (String op : path) {
                op = op.trim();
                if (op.contains("hd")) prob *= encoderTableListCN.hdOpProbMap.get(op);
                else if (op.contains("hi")) prob *= encoderTableListCN.hiOpProbMap.get(op);
                else if (op.contains("td")) prob *= encoderTableListCN.tdOpProbMap.get(op);
                else if (op.contains("ti")) prob *= encoderTableListCN.tiOpProbMap.get(op);
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


    public List<String> decode(List<String> encodedList, double lambda) {
//        encoderTable.buildEncodeTables(mkv, lambdaOp, lambdaTimes, lambdaMkv, lambdaMkv_1);
        encodedList = initVault(encodedList);
        int fixedLength = encoderTableListCN.secParam_L;
        List<String> originPswd = new ArrayList<>();
//        CsvWriter writer = CsvUtil.getWriter("/app/HvExpData/tables/table21L.csv", CharsetUtil.CHARSET_UTF_8);
        for (int index = 1; index < encodedList.size(); index++) {
            StringBuilder decodedPswd = new StringBuilder();
            String encodedString = encodedList.get(index);
            if (encodedString == null) continue;
            List<String> encodeElementList = splitString(encodedString, fixedLength);
            if (index == 1) {
                BigInteger encodedPswd = new BigInteger(encodeElementList.get(0), 2);
                String pswd = findOriginValue(encodedPswd, encoderTableListCN.pswdFreqEncodeTable);
                if (pswd == null) {
                    BigInteger kNPlus1 = encoderTableListCN.kNPlus1;
                    BigDecimal pow = BigDecimal.valueOf(2).pow(encoderTableListCN.secParam_L);
                    BigDecimal p1 = getP1(lambda);

                    BigDecimal bottom = p1.multiply(pow);
                    BigDecimal top = new BigDecimal(encodedPswd).subtract(new BigDecimal(kNPlus1));
                    BigInteger lowerBound =
                            new BigDecimal(kNPlus1).add(top.divide(bottom, 40, RoundingMode.FLOOR)
                                    .multiply(bottom)).toBigInteger();
                    BigInteger upperBound =
                            new BigDecimal(kNPlus1).add(top.divide(bottom, 40, RoundingMode.FLOOR).add(BigDecimal.valueOf(1))
                                    .multiply(bottom)).toBigInteger();
                    String randomStr = genRandomStr();

                    while (encoderTableListCN.pswdFreqEncodeTable.containsKey(randomStr)) {
                        randomStr = genRandomStr();
                    }
                    EncodeLine<String> newRandomLine =
                            EncodeLine.<String>builder().lowerBound(lowerBound).upperBound(upperBound).originValue(randomStr).build();
                    encoderTableListCN.pswdFreqEncodeTable.putIfAbsent(randomStr, newRandomLine);
                    decodedPswd.append(randomStr);
                } else {
                    decodedPswd.append(pswd);
                }
                originPswd.add(decodedPswd.toString());
            } else {
                BigInteger encodedG = new BigInteger(encodeElementList.get(0), 2);
//                System.out.println("encodedG" + ":" + encodedG);
                int g = 0;
                boolean found = false;
                for (int t = 0; t < index && !found; t++) {
                    Pair<Double, Double> gBound = gEncoder(t, index);
                    BigInteger lowerBound = BigDecimal.valueOf(gBound.getKey()).toBigInteger();
                    BigInteger upperBound = BigDecimal.valueOf(gBound.getValue()).toBigInteger();

                    if (encodedG.compareTo(lowerBound) > 0 && encodedG.compareTo(upperBound) < 0) {
                        g = t;
                        found = true;
//                        System.out.println("lowerBound" + ":" + lowerBound);
//                        System.out.println("upperBound" + ":" + upperBound);
                    }
                }
//                System.out.println("index" + ":" + index);
//                System.out.println("g" + ":" + g);
//                System.out.println("-----------------------------");
                if (g == 0) {
                    BigInteger encodedPswd = new BigInteger(encodeElementList.get(1), 2);
                    String pswd = findOriginValue(encodedPswd, encoderTableListCN.pswdFreqEncodeTable);
                    if (pswd == null) {
                        BigInteger kNPlus1 = encoderTableListCN.kNPlus1;
                        BigDecimal pow = BigDecimal.valueOf(2).pow(encoderTableListCN.secParam_L);
                        BigDecimal p1 = getP1(lambda);

                        BigDecimal bottom = p1.multiply(pow);
                        BigDecimal top = new BigDecimal(encodedPswd).subtract(new BigDecimal(kNPlus1));
                        BigInteger lowerBound =
                                new BigDecimal(kNPlus1).add(top.divide(bottom, 40, RoundingMode.FLOOR)
                                        .multiply(bottom)).toBigInteger();
                        BigInteger upperBound =
                                new BigDecimal(kNPlus1).add(top.divide(bottom, 40, RoundingMode.FLOOR).add(BigDecimal.valueOf(1))
                                        .multiply(bottom)).toBigInteger();
                        String randomStr = genRandomStr();
                        while (encoderTableListCN.pswdFreqEncodeTable.containsKey(randomStr)) {
                            randomStr = genRandomStr();
                        }
                        EncodeLine<String> newRandomLine =
                                EncodeLine.<String>builder().lowerBound(lowerBound).upperBound(upperBound).originValue(randomStr).build();
                        encoderTableListCN.pswdFreqEncodeTable.put(randomStr, newRandomLine);
//                        writer.writeLine(String.valueOf(encoderTableListCN.pswdFreqEncodeTable));
                        decodedPswd.append(randomStr);
                    } else {
                        decodedPswd.append(pswd);

                    }
                    originPswd.add(decodedPswd.toString());
                } else {
                    BigInteger encodedIfDelete = new BigInteger(encodeElementList.get(1), 2);
                    BigInteger encodedIfInsert = new BigInteger(encodeElementList.get(2), 2);

                    String baseString = originPswd.get(g - 1);
                    int ifInsert = findOriginValue(encodedIfInsert, encoderTableListCN.encodeIfInsertProbTable);
                    int ifDelete;
                    if (baseString.length() == 1) {
                        ifDelete = 0;
                    } else {
                        ifDelete = findOriginValue(encodedIfDelete, encoderTableListCN.encodeIfDeleteProbTable);
                    }
                    if (baseString.length() >= 16) {
                        ifInsert = 0;
                    }


                    int timesLength = ifInsert + ifDelete;
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
                    System.out.println(baseString);
                    if (!encodedDeleteTimes.equals(BigInteger.valueOf(0))) {
                        Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>> encodeDeleteTimesForKLine =
                                encoderTableListCN.encodeDeleteTimesProbTable.get(baseString.length());
                        System.out.println("delete k=" + baseString.length());
                        System.out.println("查找结果：" + encodeDeleteTimesForKLine);
                        Pair<Integer, Integer> hdAndTdTimes = findOriginValue(encodedDeleteTimes,
                                encodeDeleteTimesForKLine);
                        System.out.println("编码后的deleteTimes:" + encodedDeleteTimes);
                        System.out.println("删除次数" + hdAndTdTimes);
                        hdTimes = hdAndTdTimes.getKey();
                        tdTimes = hdAndTdTimes.getValue();
                    }

                    if (!encodedInsertTimes.equals(BigInteger.valueOf(0))) {
                        int insertK = baseString.length() - hdTimes - tdTimes;
                        Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>> encodeInsertTimesForKLine =
                                encoderTableListCN.encodeInsertTimesProbTable.get(insertK);
                        System.out.println("insert k=" + insertK);
                        System.out.println("查找结果：" + encodeInsertTimesForKLine);
                        System.out.println("编码后的insertTimes:" + encodedInsertTimes);

                        Pair<Integer, Integer> hiAndTiTimes = findOriginValue(encodedInsertTimes,
                                encodeInsertTimesForKLine);
                        System.out.println("增加次数" + hiAndTiTimes);

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
                                    encoderTableListCN.encodeHdOpProbTable);
                            finalOpList.add(finalOp);
                            hdTimes--;
                        }
                        while (tdTimes != 0 && opQueue.size() > 0) {
                            String finalOp = findOriginValue(new BigInteger(opQueue.poll(), 2),
                                    encoderTableListCN.encodeTdOpProbTable);
                            finalOpList.add(finalOp);
                            tdTimes--;
                        }
                        while (hiTimes != 0 && opQueue.size() > 0) {
                            String finalOp = findOriginValue(new BigInteger(opQueue.poll(), 2),
                                    encoderTableListCN.encodeHiOpProbTable);
                            finalOpList.add(finalOp);
                            hiTimes--;
                        }
                        while (tiTimes != 0 && opQueue.size() > 0) {
                            String finalOp = findOriginValue(new BigInteger(opQueue.poll(), 2),
                                    encoderTableListCN.encodeTiOpProbTable);
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

    static List<String> candidateList = new ArrayList<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "a", "b", "c",
            "d", "e",
            "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+",
            ",", "-", ".", "/", ";", ":", "<", "=", ">", "?",
            "@", "[", "\\", "]", "^", "_", "`", "{", "|", "}", "~", " ", "Α", "τ", "Β", "Γ", "Δ", "σ", "Ε", "Ζ", "Η",
            "ρ",
            "Θ", "Ι", "Κ", "Λ", "Μ", "Ν", "Ξ", "Ο", "Π", "Ρ",
            "Φ", "Χ", "Ψ",
            "Ω", "ω", "ψ",
            "χ", "φ", "υ"));
    static Set<String> greekSet = new HashSet<>(Arrays.asList("Α", "τ", "Β", "Γ", "Δ", "σ", "Ε", "Ζ", "Η", "ρ",
            "Θ", "Ι", "Κ", "Λ", "Μ", "Ν", "Ξ", "Ο", "Π", "Ρ",
            "Φ", "Χ", "Ψ",
            "Ω", "ω", "ψ",
            "χ", "φ", "υ"));


    private String genRandomStr() {
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

    private boolean isValid(String decodeString) {
        int finalLength = 0;
        for (char c : decodeString.toCharArray()) {
            String s = String.valueOf(c);
            if (greekSet.contains(s)) {
                finalLength += 5;
            } else finalLength++;
            if (finalLength > 16) return false;
        }
        return finalLength >= 5;
    }

}
