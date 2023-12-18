package com.example.honeyvault.english.paper23_list_version;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.math.MathUtil;
import com.example.honeyvault.data_access.EncodeLine;
import com.example.honeyvault.data_access.markov.MarkovStatistic;
import com.example.honeyvault.data_access.path.PathInfo;
import com.example.honeyvault.data_access.path.PathStatistic;
import lombok.ToString;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.honeyvault.tool.CalPath.countOccurrencesOfOp;

@Component
@ToString
public class EncoderTableListEng {

    Map<Integer, Double> ifInsertProbMap;
    Map<Integer, Double> ifDeleteProbMap;
    Map<String, Double> hdOpProbMap;
    Map<String, Double> hiOpProbMap;
    Map<String, Double> tdOpProbMap;
    Map<String, Double> tiOpProbMap;


    Map<Integer, EncodeLine<Integer>> encodeIfDeleteProbTable;
    Map<Integer, EncodeLine<Integer>> encodeIfInsertProbTable;
    Map<Integer, Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>>> encodeInsertTimesProbTable =
            new HashMap<>();
    //    k, (hdTime,tdTimes)->encodeLine
    Map<Integer, Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>>> encodeDeleteTimesProbTable =
            new HashMap<>();
    Map<String, EncodeLine<String>> encodeHdOpProbTable;
    Map<String, EncodeLine<String>> encodeHiOpProbTable;
    Map<String, EncodeLine<String>> encodeTdOpProbTable;
    Map<String, EncodeLine<String>> encodeTiOpProbTable;

    Map<String, Double> pswdFreqMap;
    Map<String, EncodeLine<String>> pswdFreqEncodeTable = new ConcurrentHashMap<>();
    Map<Integer, EncodeLine<Integer>> specialDeleteEncodeLine = new HashMap<>();

    Integer originPswdFreqSize = 0;
    BigInteger kNPlus1 = new BigInteger("0");


    int secParam_L=128;
    List<String> candidateList;

    @Resource
    private PathStatistic pathStatistic;
    @Resource
    private MarkovStatistic markovStatistic;

    @PostConstruct
    void buildCandidateList() {
        candidateList = new ArrayList<>();
        candidateList.addAll(Arrays.asList("Α", "τ", "Β", "Γ", "Δ", "σ", "Ε", "Ζ", "Η", "ρ",
                "Θ", "Ι", "Κ", "Λ", "Μ", "Ν", "Ξ", "Ο", "Π", "Ρ",
                "Φ", "Χ", "Ψ",
                "Ω", "ω", "ψ"
        ));
        List<String> strings = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e",
                "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                "U", "V", "W", "X", "Y", "Z");
        candidateList.addAll(strings);
        List<String> strings1 = Arrays.asList("!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+",
                ",", "-", ".", "/", ";", ":", "<", "=", ">", "?",
                "@", "[", "\\", "]", "^", "_", "`", "{", "|", "}", "~", " ");
        candidateList.addAll(strings1);
    }


    public void buildEncodeTables(double lambdaOp, double lambdaTimes,double listLambda) {
        List<String> passwds = markovStatistic.parseEngMerge();
        final double totalSize = passwds.size();
        pswdFreqMap = passwds.stream()
                .collect(Collectors.groupingBy(String::toString, Collectors.counting()))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() / totalSize));
        double originSize = pswdFreqMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double pow = Math.pow(121, 4) - Math.pow(95, 4);
        for (int i = 5; i < 17; i++) {
            pow += Math.pow(121, i);
        }
        for (int i = 5; i < 17; i++) {
            pow -= MathUtil.combinationCount(16, i) * Math.pow(29, i) * Math.pow(95, (16 - i));
        }
        double factor = originSize + listLambda * pow;
        pswdFreqMap.replaceAll((pswd, freq) -> (freq + listLambda) / factor);

        pswdFreqEncodeTable = probMap2EncodeTable(pswdFreqMap);
        originPswdFreqSize = pswdFreqEncodeTable.size();

        EncodeLine<String> maxEncodeLine = null;
        BigInteger maxUpperBound = BigInteger.valueOf(Long.MIN_VALUE);

        for (EncodeLine<String> encodeLine : pswdFreqEncodeTable.values()) {
            if (encodeLine.getUpperBound().compareTo(maxUpperBound) > 0) {
                maxUpperBound = encodeLine.getUpperBound();
                maxEncodeLine = encodeLine;
            }
        }
        kNPlus1 = maxEncodeLine.getUpperBound();

        List<PathInfo> pathInfoTrainSet = pathStatistic.getEngPathTrainSet();
        List<String> pathTrainSet = new ArrayList<>();
        pathInfoTrainSet.forEach(info ->{
            pathTrainSet.add(info.getPath());
        });

        ifInsertProbMap = initIfOpProbMap(pathTrainSet, "i");
        ifDeleteProbMap = initIfOpProbMap(pathTrainSet, "d");

        pathInfoTrainSet.forEach(pathInfo -> {
            int hdCount = countOccurrencesOfOp(pathInfo.getPath(), "hd");
            int tdCount = countOccurrencesOfOp(pathInfo.getPath(), "td");
            pathInfo.setLengthMinusDelete(pathInfo.getLength() - hdCount - tdCount);
        });

        Map<Integer, List<String>> insertGroupedByLength =
                pathInfoTrainSet.stream().collect(Collectors.groupingBy(PathInfo::getLengthMinusDelete,
                        Collectors.mapping(PathInfo::getPath, Collectors.toList())));

        insertGroupedByLength.forEach((k, path) -> {
            Map<Pair<Integer, Integer>, Integer> iTimesMap = getITimes(path, k);
//          (hdTime,tdTimes)->prob
            Map<Pair<Integer, Integer>, Double> iTimeProbMap = smoothITimesMap(iTimesMap, lambdaTimes, k);
            Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>> iTimeEncodeLine =
                    probMap2EncodeTable(iTimeProbMap);
            encodeInsertTimesProbTable.put(k, iTimeEncodeLine);
        });

//      length:k->path
        Map<Integer, List<String>> deleteGroupedByLength =
                pathInfoTrainSet.stream().collect(Collectors.groupingBy(PathInfo::getLength,
                        Collectors.mapping(PathInfo::getPath, Collectors.toList())));

        deleteGroupedByLength.forEach((k, path) -> {
            Map<Pair<Integer, Integer>, Integer> dTimesMap = getDTimes(path, k);
//          (hdTime,tdTimes)->prob
            Map<Pair<Integer, Integer>, Double> dTimeProbMap = smoothDTimesMap(dTimesMap, lambdaTimes, k);
            Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>> dTimeEncodeLine =
                    probMap2EncodeTable(dTimeProbMap);
            encodeDeleteTimesProbTable.put(k, dTimeEncodeLine);
        });

        specialDeleteEncodeLine.put(0,
                EncodeLine.<Integer>builder().prob(1.0).lowerBound(BigInteger.ZERO).upperBound(BigInteger.valueOf(2).pow(secParam_L)).originValue(0).build());


        List<Map<String, Double>> maps = initProbMap(pathTrainSet, lambdaOp);
        hdOpProbMap = maps.get(0);
        hiOpProbMap = maps.get(1);
        tdOpProbMap = maps.get(2);
        tiOpProbMap = maps.get(3);

//        secParam_L = calL();


        encodeIfInsertProbTable = probMap2EncodeTable(ifInsertProbMap);
        encodeIfDeleteProbTable = probMap2EncodeTable(ifDeleteProbMap);
        encodeHdOpProbTable = probMap2EncodeTable(hdOpProbMap);
        encodeHiOpProbTable = probMap2EncodeTable(hiOpProbMap);
        encodeTdOpProbTable = probMap2EncodeTable(tdOpProbMap);
        encodeTiOpProbTable = probMap2EncodeTable(tiOpProbMap);
    }


    private Map<Integer, Double> initIfOpProbMap(List<String> pathTrainSet, String op) {
        Map<Integer, Double> ifOpProb = new HashMap<>();
        LongAdder ifOpCount = new LongAdder();
        if (op.equals("d")) {
            pathTrainSet.forEach(path -> {
                if (path.contains("hd") || path.contains("td")) {
                    ifOpCount.add(1);
                }
            });
        } else {
            pathTrainSet.forEach(path -> {
                if (path.contains("hi") || path.contains("ti")) {
                    ifOpCount.add(1);
                }
            });
        }

        double prOp = ifOpCount.doubleValue() / pathTrainSet.size();
        ifOpProb.put(1, prOp);
        ifOpProb.put(0, 1 - prOp);
        return ifOpProb;
    }

    private List<Map<String, Double>> initProbMap(List<String> pathTrainSet, double lambdaOp) {

        Map<String, Double> hdOpProbMap = new LinkedHashMap<>();
        Map<String, Double> hiOpProbMap = new LinkedHashMap<>();
        Map<String, Double> tdOpProbMap = new LinkedHashMap<>();
        Map<String, Double> tiOpProbMap = new LinkedHashMap<>();
        pathTrainSet.forEach(path -> {
            if (path != null && !path.equals("[]")) {
                Pattern pattern = Pattern.compile("\\w+\\([^)]*\\)|\\w+\\(\\)");
                Matcher matcher = pattern.matcher(path);

                // 提取操作元素
                List<String> operations = new ArrayList<>();
                while (matcher.find()) {
                    operations.add(matcher.group());
                }
                for (String s : operations) {
                    if (s.equals("hd()") || s.equals("hi()") || s.equals("ti()") || s.equals("td()")) {
                        continue;
                    }
                    if (s.contains("hd")) hdOpProbMap.merge(s.trim(), 1.0, Double::sum);
                    else if (s.contains("hi")) hiOpProbMap.merge(s.trim(), 1.0, Double::sum);
                    else if (s.contains("td")) tdOpProbMap.merge(s.trim(), 1.0, Double::sum);
                    else if (s.contains("ti")) tiOpProbMap.merge(s.trim(), 1.0, Double::sum);
                }
            }
        });

        List<Map<String, Double>> mList = new ArrayList<>();
        mList.add(smoothOpProbMap(hdOpProbMap, "hd", lambdaOp));
        mList.add(smoothOpProbMap(hiOpProbMap, "hi", lambdaOp));
        mList.add(smoothOpProbMap(tdOpProbMap, "td", lambdaOp));
        mList.add(smoothOpProbMap(tiOpProbMap, "ti", lambdaOp));
        return mList;
    }

    Map<String, Double> smoothOpProbMap(Map<String, Double> opProbMap, String opName, double lambdaOp) {


        double originSize = opProbMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double factor = originSize + lambdaOp * 121;

        double factor2 = lambdaOp / factor;

        opProbMap.replaceAll((op, occurTimes) -> (occurTimes + lambdaOp) / factor);

        candidateList.forEach(s -> {
            String s1 = opName + "(" + s + ")";
            opProbMap.putIfAbsent(s1, factor2);
        });

        return opProbMap;
    }

    Map<Pair<Integer, Integer>, Integer> getITimes(List<String> pathList, int k) {
        Map<Pair<Integer, Integer>, Integer> countOp = new LinkedHashMap<>();
        for (String path : pathList) {
            if (path != null) {
                int tiTimes = countOccurrencesOfOp(path, "ti");
                int hiTimes = countOccurrencesOfOp(path, "hi");
                int opTimes = tiTimes + hiTimes;
                if (opTimes >= 1 && opTimes <= k) {
                    countOp.merge(new Pair<>(hiTimes, tiTimes), 1, Integer::sum);
                }
            }
        }
        return countOp;
    }

    Map<Pair<Integer, Integer>, Integer> getDTimes(List<String> pathList, int k) {
        Map<Pair<Integer, Integer>, Integer> countOp = new LinkedHashMap<>();
        for (String path : pathList) {
            if (path != null) {
                int tdTimes = countOccurrencesOfOp(path, "td");
                int hdTimes = countOccurrencesOfOp(path, "hd");
                int opTimes = tdTimes + hdTimes;
                if (opTimes >= 1 && opTimes <= k) {
                    countOp.merge(new Pair<>(hdTimes, tdTimes), 1, Integer::sum);
                }
            }
        }
        return countOp;
    }

    Map<Pair<Integer, Integer>, Double> smoothITimesMap(Map<Pair<Integer, Integer>, Integer> opTimesMap,
                                                        double lambdaTimes, int k) {
        Map<Pair<Integer, Integer>, Double> opTimesProbMap = new HashMap<>();
        double originSize = opTimesMap.values().stream().mapToDouble(Integer::doubleValue).sum();
        int up = Math.min(4 * k, 16 - k);
//        int k = 16 - k1;
        for (int i = 0; i <= up; i++) {
            for (int j = 0; j <= up - i; j++) {
                if ((i + j) > 0) {
                    double factor = ((double) (up + 2) * (up + 1) / 2 - 1) * lambdaTimes + originSize;
//                    double factor = originSize + ((up + 2) * (up + 1) * lambdaTimes) / 2;
                    if (opTimesMap.containsKey(new Pair<>(i, j))) {
                        double t = (opTimesMap.get(new Pair<>(i, j)) + lambdaTimes) / factor;
                        opTimesProbMap.put(new Pair<>(i, j), t);
                    } else {
                        double t = lambdaTimes / factor;
                        opTimesProbMap.putIfAbsent(new Pair<>(i, j), t);
                    }
                }
            }
        }
        return opTimesProbMap;
    }

    Map<Pair<Integer, Integer>, Double> smoothDTimesMap(Map<Pair<Integer, Integer>, Integer> opTimesMap,
                                                        double lambdaTimes, int k1) {
        Map<Pair<Integer, Integer>, Double> opTimesProbMap = new HashMap<>();
        double originSize = opTimesMap.values().stream().mapToDouble(Integer::doubleValue).sum();
        double k = Math.min(k1 - 3, Math.floor(0.875 * k1));
        for (int i = 0; i <= k; i++) {
            for (int j = 0; j <= k - i; j++) {
                if ((i + j) > 0) {
                    double factor = ((k + 2) * (k + 1) / 2 - 1) * lambdaTimes + originSize;
                    if (opTimesMap.containsKey(new Pair<>(i, j))) {
                        opTimesProbMap.put(new Pair<>(i, j),
                                (opTimesMap.get(new Pair<>(i, j)) + lambdaTimes) / factor);
                    } else {
                        opTimesProbMap.put(new Pair<>(i, j),
                                lambdaTimes / factor);
                    }
                }
            }
        }
        return opTimesProbMap;
    }

//    int calL() {
//        double log2 = Math.log(2);
////        double L_min_everyMkv_1ProbMap = Math.abs(Math.log(Collections.min(everyMkv_1ProbMap.values())) / log2);
//        double L_min_ifHdProbMap = Math.abs(Math.log(Collections.min(ifHdProbMap.values())) / log2);
//        double L_min_ifHiProbMap = Math.abs(Math.log(Collections.min(ifHiProbMap.values())) / log2);
//        double L_min_ifTdProbMap = Math.abs(Math.log(Collections.min(ifTdProbMap.values())) / log2);
//        double L_min_ifTiProbMap = Math.abs(Math.log(Collections.min(ifTiProbMap.values())) / log2);
//        double L_min_hdTimesProbMap = Math.abs(Math.log(Collections.min(hdTimesProbMap.values())) / log2);
//        double L_min_hiTimesProbMap = Math.abs(Math.log(Collections.min(hiTimesProbMap.values())) / log2);
//        double L_min_tdTimesProbMap = Math.abs(Math.log(Collections.min(tdTimesProbMap.values())) / log2);
//        double L_min_tiTimesProbMap = Math.abs(Math.log(Collections.min(tiTimesProbMap.values())) / log2);
//        double L_min_hdOpProbMap = Math.abs(Math.log(Collections.min(hdOpProbMap.values())) / log2);
//        double L_min_hiOpProbMap = Math.abs(Math.log(Collections.min(hiOpProbMap.values())) / log2);
//        double L_min_tdOpProbMap = Math.abs(Math.log(Collections.min(tdOpProbMap.values())) / log2);
//        double L_min_tiOpProbMap = Math.abs(Math.log(Collections.min(tiOpProbMap.values())) / log2);
//        Set<Double> mins = new HashSet<>();
//        mins.add(L_min_ifHdProbMap);
//        mins.add(L_min_ifHiProbMap);
//        mins.add(L_min_ifTdProbMap);
//        mins.add(L_min_ifTiProbMap);
//        mins.add(L_min_hdTimesProbMap);
//        mins.add(L_min_hiTimesProbMap);
//        mins.add(L_min_tdTimesProbMap);
//        mins.add(L_min_tiTimesProbMap);
//        mins.add(L_min_hdOpProbMap);
//        mins.add(L_min_hiOpProbMap);
//        mins.add(L_min_tdOpProbMap);
//        mins.add(L_min_tiOpProbMap);
//        int max = Collections.max(mins).intValue();
//        // 实验证明
//        int L_g = 12;
//        int calL = Math.max(L_g, max);
//        if (calL < 16) calL = 16;
//        return calL;
//    }


    //    Tools
//      概率 = 频率/ 总数
    <T> Map<T, Double> calculateFrequency(Map<T, Double> frequencyMap) {
        double total = frequencyMap.values().stream().mapToDouble(Double::doubleValue).sum();

        Map<T, Double> frequencyResult = new LinkedHashMap<>();

        for (Map.Entry<T, Double> entry : frequencyMap.entrySet()) {
            double frequency = entry.getValue() / total;
            frequencyResult.put(entry.getKey(), frequency);
        }
        return frequencyResult;
    }


    Set<String> generateStrings(int length) {
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
        } else if (length == 1) {
            for (String item : candidateList) {
                String generatedString =
                        "" + item;
                res.add(generatedString);
            }
        } else if (length == 2) {
            for (String value : candidateList) {
                for (String item : candidateList) {
                    String generatedString =
                            "" + value + item;
                    res.add(generatedString);
                }
            }
        }
        return res;
    }

    <T> Map<T, EncodeLine<T>> probMap2EncodeTable(Map<T, Double> map) {
        BigDecimal pow = BigDecimal.valueOf(2).pow(secParam_L);
        BigDecimal lowerBound = BigDecimal.ZERO;
        BigDecimal upperBound;
        Map<T, EncodeLine<T>> encodeTable = new ConcurrentHashMap<>();
        for (Map.Entry<T, Double> entry : map.entrySet()) {
            T key = entry.getKey();
            double value = entry.getValue();
            upperBound = lowerBound.add(BigDecimal.valueOf(value)) ;
            encodeTable.put(key,
                    EncodeLine.<T>builder()
                            .originValue(key)
                            .prob(value)
                            .lowerBound(pow.multiply(lowerBound).toBigInteger())
                            .upperBound(pow.multiply(upperBound).toBigInteger())
                            .build());
            lowerBound = upperBound;
        }
        return encodeTable;
    }


}
