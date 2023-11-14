package com.example.honeyvault.tool;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.math.MathUtil;
import com.example.honeyvault.data_access.EncodeLine;
import com.example.honeyvault.data_access.PasswdPath;
import com.example.honeyvault.data_access.PathRepo;
import com.xiaoleilu.hutool.util.RandomUtil;
import dev.mccue.guava.concurrent.AtomicDouble;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.honeyvault.tool.CalPath.countOccurrencesOfOp;

@Component
public class EncoderDecoderWithoutPII {

    private static List<PasswdPath> UserVaultSet;
    private static double alpha;

    @Resource
    private PathRepo repo;
    @Resource
    private EncoderTableWithoutPII encoderTable;


    @PostConstruct
    public void initVault() {
        UserVaultSet = repo.findAll();
    }

    @PostConstruct
    public void initAlpha() {
        alpha = calAlpha();
    }

    private double Pr_DR(int i) {
        return (i * alpha) / (i * alpha + 1 - alpha);
    }

    public List<Pair<String, String>> encodeWithoutPII(List<String> initVault, int fixedLength) {
        encoderTable.buildEncodeTablesWithoutPII();
        List<String> vault = initVault(initVault);
        Map<Pair<Integer, Integer>, Double> pathProbMap = new HashMap<>();
        List<Pair<String, String>> pswd2EncodeString = new LinkedList<>();
        String firstPswd = vault.get(1);
        String encodeFirstPswd = algo4(0, firstPswd, firstPswd);
        encodeFirstPswd = fillWithRandom(encodeFirstPswd, fixedLength);
        pswd2EncodeString.add(new Pair<>(firstPswd, encodeFirstPswd));

        double pr1 = 0;
        // j is i，i is i+1
        for (int i = 2; i < vault.size(); i++) {
            for (int j = 1; j < i; j++) {
                String pwi1 = vault.get(i);
                String pwi = vault.get(j);

                double Pr_DR_true = Pr_DR(i);
                double A = (1 - f_fit(i)) / (i - 1);
                double B = f_fit(i);

                double pr2 = B * getMarkovProb(vault.get(i));
                pathProbMap.put(new Pair<>(i, i), pr2);
                double pr1j = 0;
                if (!pwi.equals(pwi1)) {
                    List<List<String>> paths = CalPath.breadthFirstSearch(pwi, pwi1);
                    double pr_ssm = calPr_ssm(paths,Pr_DR_true);
                    pr1j = ((1 - (i - 1) * alpha) / ((i - 1) * alpha + 1 - alpha)) * A * pr_ssm;
                } else {
                    pr1j = ((i - 1) * alpha) / ((i - 1) * alpha + 1 - alpha) * A;
                }
                pr1 += pr1j;
                pathProbMap.put(new Pair<>(i, j), pr1j);
            }
        }
//          根据概率选择一个base口令
//          pw_i+1->pwi
//          修改：Key-Value顺序
//      选择一个唯一的路径
        Map<Integer, Integer> pwi1ToPwi = selectUniquei2(pathProbMap);
//        第一个密码
//      直接编码 map->table

        pwi1ToPwi.forEach((pwi1Index, pwiIndex) -> {
//            System.out.println("pw i+1" + ":" + vault.get(pwi1Index) + ":" + pwi1Index);
//            System.out.println("pw i" + ":" + vault.get(pwiIndex) + ":" + pwiIndex);
//              确定g
            int g;
            if (pwi1Index.equals(pwiIndex)) g = 0;
//              g=i'
            else g = pwiIndex;
//              index=i'

//            System.out.println("g" + ":" + g);
//            System.out.println("index" + ":" + pwiIndex);
            Pair<Double, Double> gBound = gEncoder(g, pwi1Index);
            BigDecimal gDecimal = getRandomValue(gBound.getKey(), gBound.getValue());
            String encodedG = toBinaryString(gDecimal, encoderTable.secParam_L);
            String encodeString = algo4(g, vault.get(pwiIndex), vault.get(pwi1Index));
            System.out.println("encoded string" + ":" + encodeString);

            encodeString = encodedG + encodeString;

            encodeString = fillWithRandom(encodeString, fixedLength);
            pswd2EncodeString.add(new Pair<>(vault.get(pwi1Index), encodeString));
            System.out.println(pswd2EncodeString);
        });
        return pswd2EncodeString;
    }

    public static double f_fit(int i) {
        i = i - 1;
        double i2 = Math.pow(i, 2);
        double i3 = Math.pow(i, 3);
        return 1 / (0.02455 * i3 - 0.2945 * i2 + 3.409 * i + 0.0852);
    }

    private double calAlpha() {
        List<Double> alphaList = new ArrayList<>();
        UserVaultSet = repo.findAll();
        UserVaultSet.forEach(p -> {
            double alpha;
            String password12306 = p.getPassword12306();
            String passwdCn = p.getPasswd_CN();
            List<String> passwds = new ArrayList<>();
            passwds.add(password12306);
            passwds.add(passwdCn);
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
        });
        return alphaList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private Pair<Double, Double> gEncoder(int g, int i) {
        double L = encoderTable.secParam_L;
        double pow = Math.pow(2, L);
        double col2;
        double lower = 0, upper;
        if (g == 0) {
            upper = Math.floor(pow * f_fit(i));
        } else {
            col2 = (1 - f_fit(i)) / (i - 1);
            lower = Math.floor(((f_fit(i) + col2 * (g - 1)) * pow));
            upper = Math.floor(((f_fit(i) + col2 * g) * pow));
        }
        return new Pair<>(lower, upper);

    }


    private String algo4(int g, String basePasswd, String targetString) {
        StringBuilder encodeString = new StringBuilder();
        double fixedLength = encoderTable.secParam_L;
        if (g == 0) {
//          编码长度
            EncodeLine<Integer> lengthEncodeLine =
                    encoderTable.encodePasswdLengthTable.get(targetString.length());
            BigDecimal encodeValue = getRandomValue(lengthEncodeLine.getLowerBound(),
                    lengthEncodeLine.getUpperBound());
            encodeString.append(toBinaryString(encodeValue, fixedLength));
//          编码头3个字符
            String first3String = targetString.substring(0, 3);

            EncodeLine<String> first3EncodeLine = encoderTable.encodeEvery6Table.get(first3String);
            encodeValue = getRandomValue(first3EncodeLine.getLowerBound(),
                    first3EncodeLine.getUpperBound());
            encodeString.append(toBinaryString(encodeValue, fixedLength));

//          编码每第4个字符
            Map<String, EncodeLine<String>> encodeMap;
            for (int j = 0; j + 3 < targetString.length(); j++) {
                String window = targetString.substring(j, j + 4);
                if (j == 0) {
                    encodeMap = encoderTable.encodeEvery4Table.get(first3String);
                } else {
                    String prefix = window.substring(0, 3);
                    encodeMap = encoderTable.encodeEvery4Table.get(prefix);
                }
                String suffix = window.substring(window.length() - 1);

                EncodeLine<String> encodeLine;
                if (encodeMap == null) {
                    encodeLine = encoderTable.absentMkv4Table.get(suffix);
                } else {
                    encodeLine = encodeMap.get(suffix);
                }


                encodeValue = getRandomValue(encodeLine.getLowerBound(),
                        encodeLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));
            }
        } else {
            BigDecimal encodeValue;
//          找到所有可能的路径
            List<List<String>> paths = CalPath.breadthFirstSearch(basePasswd, targetString);
            Map<String, Double> pathProbMap = new ConcurrentHashMap<>();
//          路径->概率
            paths.forEach(path -> {
                double pathProb = getPathProb(path.toString());
                pathProbMap.put(path.toString(), pathProb);
            });
//          根据概率选择路径
            String selectedPath = selectPathByProbability(pathProbMap);
//          编码路径
            selectedPath = selectedPath.trim().replace("[", "");
            selectedPath = selectedPath.trim().replace("]", "");
            String[] ops = selectedPath.split(",");
//          1.编码ifOp
            EncodeLine<Integer> ifHdLine = encoderTable.encodeIfHdProbTable.get(selectedPath.contains("hd") ? 1
                    : 0);
            encodeValue = getRandomValue(ifHdLine.getLowerBound(),
                    ifHdLine.getUpperBound());
            encodeString.append(toBinaryString(encodeValue, fixedLength));

            EncodeLine<Integer> ifHiLine = encoderTable.encodeIfHiProbTable.get(selectedPath.contains("hi") ? 1
                    : 0);
            encodeValue = getRandomValue(ifHiLine.getLowerBound(),
                    ifHiLine.getUpperBound());
            encodeString.append(toBinaryString(encodeValue, fixedLength));

            EncodeLine<Integer> ifTdLine = encoderTable.encodeIfTdProbTable.get(selectedPath.contains("td") ? 1
                    : 0);
            encodeValue = getRandomValue(ifTdLine.getLowerBound(),
                    ifTdLine.getUpperBound());
            encodeString.append(toBinaryString(encodeValue, fixedLength));

            EncodeLine<Integer> ifTiLine = encoderTable.encodeIfTiProbTable.get(selectedPath.contains("ti") ? 1
                    : 0);
            encodeValue = getRandomValue(ifTiLine.getLowerBound(),
                    ifTiLine.getUpperBound());
            encodeString.append(toBinaryString(encodeValue, fixedLength));
//          2.编码opTimes
            int hdTimes = countOccurrencesOfOp(selectedPath, "hd");
            if (hdTimes != 0) {
                EncodeLine<Integer> hdTimesEncodeLine = encoderTable.encodeHdTimesProbTable.get(hdTimes);
                encodeValue = getRandomValue(hdTimesEncodeLine.getLowerBound(),
                        hdTimesEncodeLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));
            }


            int hiTimes = countOccurrencesOfOp(selectedPath, "hi");
            if (hiTimes != 0) {
                EncodeLine<Integer> hiTimesEncodeLine = encoderTable.encodeHiTimesProbTable.get(hiTimes);
                encodeValue = getRandomValue(hiTimesEncodeLine.getLowerBound(),
                        hiTimesEncodeLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));
            }


            int tdTimes = countOccurrencesOfOp(selectedPath, "td");
            if (tdTimes != 0) {
                EncodeLine<Integer> tdTimesEncodeLine = encoderTable.encodeTdTimesProbTable.get(tdTimes);
                encodeValue = getRandomValue(tdTimesEncodeLine.getLowerBound(),
                        tdTimesEncodeLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));
            }


            int tiTimes = countOccurrencesOfOp(selectedPath, "ti");
            if (tiTimes != 0) {
                EncodeLine<Integer> tiTimesEncodeLine = encoderTable.encodeTiTimesProbTable.get(tiTimes);
                encodeValue = getRandomValue(tiTimesEncodeLine.getLowerBound(),
                        tiTimesEncodeLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));
            }

//          3.编码op
            for (String op : ops) {
                op = op.trim();
                if (op.contains("hd")) {
                    EncodeLine<String> hdOpEncodeLine = encoderTable.encodeHdOpProbTable.get(op);
                    encodeValue = getRandomValue(hdOpEncodeLine.getLowerBound(),
                            hdOpEncodeLine.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
                if (op.contains("hi")) {
                    EncodeLine<String> hiOpEncodeLine = encoderTable.encodeHiOpProbTable.get(op);
                    encodeValue = getRandomValue(hiOpEncodeLine.getLowerBound(),
                            hiOpEncodeLine.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
                if (op.contains("ti")) {
                    EncodeLine<String> tiOpEncodeLine = encoderTable.encodeTiOpProbTable.get(op);
                    encodeValue = getRandomValue(tiOpEncodeLine.getLowerBound(),
                            tiOpEncodeLine.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
                if (op.contains("td")) {
                    EncodeLine<String> tdOpEncodeLine = encoderTable.encodeTdOpProbTable.get(op);
                    encodeValue = getRandomValue(tdOpEncodeLine.getLowerBound(),
                            tdOpEncodeLine.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
            }
        }
        return encodeString.toString();
    }

    //  cal method
    private double getMarkovProb(String passwd) {
        // g=0
        Double lengthProb = encoderTable.passwdLengthProbMap.get(passwd.length());
        Double first3Prob = encoderTable.first3ProbMap.get(passwd.substring(0, 3));
        double finalProb = lengthProb * first3Prob;
        for (int i = 0; i + 3 < passwd.length(); i++) {
            String window = passwd.substring(i, i + 4);
            String prefix = window.substring(0, 3);
            String suffix = window.substring(window.length() - 1);
            HashMap<String, Double> suffixProb = encoderTable.every4ProbMap.get(prefix);
            Double mkv4Prob;
            //                mkv4Prob = encoderTable.absentMkv4Table.get(suffix);
            //                1/124
            if (suffixProb == null) {
                mkv4Prob = encoderTable.absentMkv4ProbMap.getOrDefault(suffix, 0.008064516129);
            } else {
                mkv4Prob = suffixProb.getOrDefault(suffix, 0.008064516129);
            }
            finalProb *= mkv4Prob;
        }
        return finalProb;
    }
    private int countOccurrences(String input, String substring) {
        int count = 0;
        int index = 0;

        while ((index = input.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }

        return count;
    }

    private double getPathProb(String path,double Pr_DR_true) {
        double prob = 1;

        if (path != null && !path.equals("[]")) {
            double Pr_DR_false = 1 - Pr_DR_true;

            int ti = countOccurrences(path, "ti");
            int td = countOccurrences(path, "td");
            int hd = countOccurrences(path, "hd");
            int hi = countOccurrences(path, "hi");

            int tailM = ti + td;
            int headM = hd + hi;

            if (tailM>0) prob *= encoderTable.ifHdProbMap.get(1)+encoderTable.ifHdProbMap.get(1);

            path = path.replace("[", "");
            path = path.replace("]", "");
            String[] split = path.split(",");
            for (String op : split) {
                op = op.trim();
                if (op.contains("hd")) prob *= encoderTable.hdOpProbMap.get(op);
                else if (op.contains("hi")) prob *= encoderTable.hiOpProbMap.get(op);
                else if (op.contains("td")) prob *= encoderTable.tdOpProbMap.get(op);
                else if (op.contains("ti")) prob *= encoderTable.tiOpProbMap.get(op);
            }
            return prob;
        } else return 1;
    }

    //  tools
    private double calPr_ssm(List<List<String>> paths,double Pr_DR_true) {
        AtomicDouble pr_ssm = new AtomicDouble(0);
        paths.forEach(path -> {
            double pathProb = getPathProb(path.toString(),Pr_DR_true);
            pr_ssm.getAndAdd(pathProb);
        });
        return pr_ssm.get();
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

//    public static double LevenshteinSim(String x, String y) {
//
//        double maxLength = Double.max(x.length(), y.length());
//        if (maxLength > 0) {
//            // 如果需要，可以选择忽略大小写
//            return (maxLength - StringUtils.getLevenshteinDistance(x, y)) / maxLength;
//        }
//        return 1.0;
//    }

//    public static double JaccardSimilarity(String str1, String str2) {
//        // 将字符串划分为字符集合
//        Set<Character> set1 = strToCharSet(str1);
//        Set<Character> set2 = strToCharSet(str2);
//
//        // 计算交集和并集的大小
//        Set<Character> intersection = new HashSet<>(set1);
//        intersection.retainAll(set2);
//
//        Set<Character> union = new HashSet<>(set1);
//        union.addAll(set2);
//
//
//        // 计算Jaccard相似度
//        return (double) intersection.size() / union.size();
//    }

    String toBinaryString(BigDecimal number, double fixedLength) {
        String binary = number.toBigInteger().toString(2);

        double zeroLength = fixedLength - binary.length();

        String zeroString = "0".repeat((int) zeroLength);
        zeroString += binary;
        return zeroString;
    }


    public List<String> decode(List<String> encodedList) {
        encoderTable.buildEncodeTables();
        encodedList = initVault(encodedList);
        int fixedLength = encoderTable.secParam_L;
        List<String> originPswd = new ArrayList<>();
        for (int index = 1; index < encodedList.size(); index++) {
            StringBuilder decodedPswd = new StringBuilder();
            String encodedString = encodedList.get(index);
            if (encodedString == null) continue;
            List<String> encodeElementList = splitString(encodedString, fixedLength);
            if (index == 1) {
                BigInteger encodedPwLength = new BigInteger(encodeElementList.get(0), 2);
                Integer pwLength = findOriginValue(encodedPwLength, encoderTable.encodePasswdLengthTable);
                BigInteger encodedFirst3 = new BigInteger(encodeElementList.get(1), 2);
                String first3 = findOriginValue(encodedFirst3, encoderTable.encodeFirst3Table);
                decodedPswd.append(first3);
                List<String> encodedEvery4List = encodeElementList.subList(2, pwLength - 1);
                for (int i = 0; i < encodedEvery4List.size(); i++) {
                    String prefix;
                    String suffix;
                    BigInteger encodedSuffix;
                    if (i == 0) {
                        prefix = first3;
                        encodedSuffix = new BigInteger(encodedEvery4List.get(0), 2);
                    } else {
                        prefix = decodedPswd.substring(i, i + 3);
                        encodedSuffix = new BigInteger(encodedEvery4List.get(i), 2);
                    }
                    Map<String, EncodeLine<String>> encodeSuffixTable =
                            encoderTable.encodeEvery4Table.get(prefix);
                    if (encodeSuffixTable == null) {
                        suffix = findOriginValue(encodedSuffix, encoderTable.absentMkv4Table);
                    } else {
                        suffix = findOriginValue(encodedSuffix, encodeSuffixTable);
                    }
                    decodedPswd.append(suffix);
                }
                originPswd.add(decodedPswd.toString());
                System.out.println(originPswd);
            } else {
                BigInteger encodedG = new BigInteger(encodeElementList.get(0), 2);
                System.out.println("encodedG" + ":" + encodedG);
                int g = 0;
                boolean found = false;
                for (int t = 0; t < index && !found; t++) {
                    Pair<Double, Double> gBound = gEncoder(t, index);
                    BigInteger lowerBound = BigDecimal.valueOf(gBound.getKey()).toBigInteger();
                    BigInteger upperBound = BigDecimal.valueOf(gBound.getValue()).toBigInteger();

                    if (encodedG.compareTo(lowerBound) > 0 && encodedG.compareTo(upperBound) < 0) {
                        g = t;
                        found = true;
                        System.out.println("lowerBound" + ":" + lowerBound);
                        System.out.println("upperBound" + ":" + upperBound);
                    }
                }
                System.out.println("index" + ":" + index);
                System.out.println("g" + ":" + g);
                System.out.println("-----------------------------");
                if (g == 0) {
                    BigInteger encodedPwLength = new BigInteger(encodeElementList.get(1), 2);
                    Integer pwLength = findOriginValue(encodedPwLength, encoderTable.encodePasswdLengthTable);

                    BigInteger encodedFirst3 = new BigInteger(encodeElementList.get(2), 2);
                    String first3 = findOriginValue(encodedFirst3, encoderTable.encodeFirst3Table);
                    decodedPswd.append(first3);

                    List<String> encodedEvery4List = encodeElementList.subList(3, pwLength);
                    for (int i = 0; i < encodedEvery4List.size(); i++) {
                        String prefix;
                        String suffix;
                        BigInteger encodedSuffix;
                        if (i == 0) {
                            prefix = first3;
                            encodedSuffix = new BigInteger(encodedEvery4List.get(0), 2);
                        } else {
                            prefix = decodedPswd.substring(i, i + 3);
                            encodedSuffix = new BigInteger(encodedEvery4List.get(i), 2);
                        }
                        Map<String, EncodeLine<String>> encodeSuffixTable =
                                encoderTable.encodeEvery4Table.get(prefix);
                        if (encodeSuffixTable == null) {
                            suffix = findOriginValue(encodedSuffix, encoderTable.absentMkv4Table);
                        } else {
                            suffix = findOriginValue(encodedSuffix, encodeSuffixTable);
                        }
                        decodedPswd.append(suffix);
                    }
                    originPswd.add(decodedPswd.toString());
                    System.out.println(originPswd);

                } else {
                    BigInteger encodedIfHd = new BigInteger(encodeElementList.get(1), 2);
                    BigInteger encodedIfHi = new BigInteger(encodeElementList.get(2), 2);
                    BigInteger encodedIfTd = new BigInteger(encodeElementList.get(3), 2);
                    BigInteger encodedIfTi = new BigInteger(encodeElementList.get(4), 2);

//              编码时if=true:=1 false:=0
                    int ifHd = findOriginValue(encodedIfHd, encoderTable.encodeIfHdProbTable);
                    int ifHi = findOriginValue(encodedIfHi, encoderTable.encodeIfHiProbTable);
                    int ifTd = findOriginValue(encodedIfTd, encoderTable.encodeIfTdProbTable);
                    int ifTi = findOriginValue(encodedIfTi, encoderTable.encodeIfTiProbTable);

                    int timesLength = ifHd + ifHi + ifTd + ifTi;

                    Queue<String> opTimesList = new LinkedList<>();


                    for (int i = 0; i < timesLength; i++) {
                        String s = encodeElementList.get(5 + i);
                        opTimesList.add(s);
                    }
//              按照优先级排列
                    BigInteger encodedHdTimes = ((ifHd == 1) && opTimesList.size() > 0) ?
                            new BigInteger(opTimesList.poll(), 2) : BigInteger.valueOf(0);
                    BigInteger encodedTdTimes = ((ifTd == 1) && opTimesList.size() > 0) ?
                            new BigInteger(opTimesList.poll(), 2) : BigInteger.valueOf(0);
                    BigInteger encodedHiTimes = ((ifHi == 1) && opTimesList.size() > 0) ?
                            new BigInteger(opTimesList.poll(), 2) : BigInteger.valueOf(0);
                    BigInteger encodedTiTimes = ((ifTi == 1) && opTimesList.size() > 0) ?
                            new BigInteger(opTimesList.poll(), 2) : BigInteger.valueOf(0);


//              查表找原始值
                    int hdTimes = 0, tdTimes = 0, hiTimes = 0, tiTimes = 0;
                    if (!encodedHdTimes.equals(BigInteger.valueOf(0))) {
                        hdTimes = findOriginValue(encodedHdTimes, encoderTable.encodeHdTimesProbTable);
                    }
                    if (!encodedTdTimes.equals(BigInteger.valueOf(0))) {
                        tdTimes = findOriginValue(encodedTdTimes, encoderTable.encodeTdTimesProbTable);

                    }
                    if (!encodedHiTimes.equals(BigInteger.valueOf(0))) {
                        hiTimes = findOriginValue(encodedHiTimes, encoderTable.encodeHiTimesProbTable);

                    }
                    if (!encodedTiTimes.equals(BigInteger.valueOf(0))) {
                        tiTimes = findOriginValue(encodedTiTimes, encoderTable.encodeTiTimesProbTable);

                    }
//              总共操作次数
                    int opsLength = hdTimes + tdTimes + hiTimes + tiTimes;
//              具体操作list
                    Queue<String> finalOpList = new LinkedList<>();
                    if (timesLength > 0) {
                        Queue<String> opQueue = new LinkedList<>(encodeElementList.subList(5 + timesLength,
                                5 + timesLength + opsLength));
                        while (hdTimes != 0 && opQueue.size() > 0) {
                            String finalOp = findOriginValue(new BigInteger(opQueue.poll(), 2),
                                    encoderTable.encodeHdOpProbTable);
                            finalOpList.add(finalOp);
                            hdTimes--;
                        }
                        while (tdTimes != 0 && opQueue.size() > 0) {
                            String finalOp = findOriginValue(new BigInteger(opQueue.poll(), 2),
                                    encoderTable.encodeTdOpProbTable);
                            finalOpList.add(finalOp);
                            tdTimes--;
                        }
                        while (hiTimes != 0 && opQueue.size() > 0) {
                            String finalOp = findOriginValue(new BigInteger(opQueue.poll(), 2),
                                    encoderTable.encodeHiOpProbTable);
                            finalOpList.add(finalOp);
                            hiTimes--;
                        }
                        while (tiTimes != 0 && opQueue.size() > 0) {
                            String finalOp = findOriginValue(new BigInteger(opQueue.poll(), 2),
                                    encoderTable.encodeTiOpProbTable);
                            finalOpList.add(finalOp);
                            tiTimes--;
                        }
                    }
//              找到baseString
                    String baseString = originPswd.get(g - 1);
//              对baseString执行操作
                    for (String op : finalOpList) {
                        int openBracketIndex = op.indexOf("(");
                        int closeBracketIndex = op.indexOf(")");
                        String parameter = "";
                        if (openBracketIndex != -1 && closeBracketIndex != -1 && closeBracketIndex > openBracketIndex) {
                            parameter = op.substring(openBracketIndex + 1, closeBracketIndex);
                        }
                        if (op.contains("hd")) baseString = hd(baseString, parameter);
                        if (op.contains("td")) baseString = td(baseString, parameter);
                        if (op.contains("hi")) baseString = hi(baseString, parameter);
                        if (op.contains("ti")) baseString = ti(baseString, parameter);
                    }
//              加入到已解码列表中
                    decodedPswd.append(baseString);
                    originPswd.add(decodedPswd.toString());
                    System.out.println(originPswd);
                }


            }


        }
        return originPswd;

//        for (int i = 0; i < encodedList.size(); i++) {
//            String encoded = encodedList.get(i);
//            String decoded = decodedPswds.get(i);
//            codeMap.put(encoded, decoded);
//        }
//        return codeMap;
    }


    //    originValue lower upper
    <T> T findOriginValue(BigInteger encodedValue, Map<T, EncodeLine<T>> encodeTable) {
        Optional<EncodeLine<T>> result = encodeTable.values().stream()
                .filter(line -> encodedValue.compareTo(BigDecimal.valueOf(line.getLowerBound()).toBigInteger()) > 0 &&
                        encodedValue.compareTo(BigDecimal.valueOf(line.getUpperBound()).toBigInteger()) < 0)
                .findFirst();

        if (result.isPresent()) {
            EncodeLine<T> encodeLine = result.get();
            // 在这里对符合条件的 EncodeLine 进行处理
            return encodeLine.getOriginValue();
        } else {
            // 没有找到符合条件的 EncodeLine
            return null;
        }
//        T result = null;
//        for (Map.Entry<T, EncodeLine<T>> line : encodeTable.entrySet()) {
//            BigDecimal bigDecimal = BigDecimal.valueOf(line.getValue().getLowerBound());
//            BigInteger bigInteger = bigDecimal.toBigInteger();
//            BigDecimal bigDecimal1 = BigDecimal.valueOf(line.getValue().getUpperBound());
//            BigInteger bigInteger1 = bigDecimal1.toBigInteger();
//            if (bigInteger.compareTo(encodedValue) <= 0 &&
//                    bigInteger1.compareTo(encodedValue) > 0) {
//                result = (line.getValue().getOriginValue());
//            }
//        }
//        return result;
    }


    BigDecimal getRandomValue(double lowerBound, double upperBound) {
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

    BigDecimal getRandom32BitBigDecimal(double lowerBound, double upperBound) {
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

    List<String> splitString(String input, int chunkSize) {
        List<String> chunks = new ArrayList<>();

        for (int i = 0; i < input.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, input.length());
            chunks.add(input.substring(i, end));
        }

        return chunks;
    }

    String hd(String baseString, String content) {
        if (baseString.startsWith(content)) {
            baseString = baseString.substring(content.length());
        }
        return baseString;
    }

    String td(String baseString, String content) {
        if (baseString.endsWith(content)) {
            baseString = baseString.substring(0, baseString.length() - content.length());
        }
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
