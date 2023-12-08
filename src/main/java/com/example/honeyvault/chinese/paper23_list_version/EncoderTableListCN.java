package com.example.honeyvault.chinese.paper23_list_version;

import com.example.honeyvault.data_access.EncodeLine;
import com.example.honeyvault.data_access.markov.MarkovStatistic;
import com.example.honeyvault.data_access.path.PathStatistic;
import com.example.honeyvault.tool.PathInfo;
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
public class EncoderTableListCN {

    Map<Integer, Double> ifHdProbMap;
    Map<Integer, Double> ifHiProbMap;
    Map<Integer, Double> ifTdProbMap;
    Map<Integer, Double> ifTiProbMap;
    Map<Integer, Double> hdTimesProbMap;
    Map<Integer, Double> hiTimesProbMap;
    Map<Integer, Double> tdTimesProbMap;
    Map<Integer, Double> tiTimesProbMap;
    Map<String, Double> hdOpProbMap;
    Map<String, Double> hiOpProbMap;
    Map<String, Double> tdOpProbMap;
    Map<String, Double> tiOpProbMap;


    Map<Integer, EncodeLine<Integer>> encodeIfHdProbTable;
    Map<Integer, EncodeLine<Integer>> encodeIfHiProbTable;
    Map<Integer, EncodeLine<Integer>> encodeIfTdProbTable;
    Map<Integer, EncodeLine<Integer>> encodeIfTiProbTable;
    Map<Integer, EncodeLine<Integer>> encodeHdTimesProbTable;
    Map<Integer, EncodeLine<Integer>> encodeHiTimesProbTable;
    Map<Integer, EncodeLine<Integer>> encodeTdTimesProbTable;
    Map<Integer, EncodeLine<Integer>> encodeTiTimesProbTable;
    Map<String, EncodeLine<String>> encodeHdOpProbTable;
    Map<String, EncodeLine<String>> encodeHiOpProbTable;
    Map<String, EncodeLine<String>> encodeTdOpProbTable;
    Map<String, EncodeLine<String>> encodeTiOpProbTable;

    Map<String, Double> pswdFreqMap;
    Map<String, EncodeLine<String>> pswdFreqEncodeTable = new ConcurrentHashMap<>();

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
                "Ω", "ω", "ψ",
                "χ", "φ", "υ"
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
        List<String> passwds = markovStatistic.parseT12306();
        final double totalSize = passwds.size();
        pswdFreqMap = passwds.stream()
                .collect(Collectors.groupingBy(String::toString, Collectors.counting()))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() / totalSize));
        double originSize = pswdFreqMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double pow = 29;
        for (int i = 2; i < 17; i++) {
            pow += Math.pow(124, i);
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

        List<PathInfo> pathInfoTrainSet = pathStatistic.getPathTrainSet();
        List<String> pathTrainSet = new ArrayList<>();
        pathInfoTrainSet.forEach(info ->{
            pathTrainSet.add(info.getPath());
        });

        ifHdProbMap = initIfOpProbMap(pathTrainSet, "hd");
        ifHiProbMap = initIfOpProbMap(pathTrainSet, "hi");
        ifTdProbMap = initIfOpProbMap(pathTrainSet, "td");
        ifTiProbMap = initIfOpProbMap(pathTrainSet, "ti");

        hdTimesProbMap = smoothTimesMap(getOpCountProbMap(pathTrainSet, "hd"), lambdaTimes);
        hiTimesProbMap = smoothTimesMap(getOpCountProbMap(pathTrainSet, "hi"), lambdaTimes);
        tdTimesProbMap = smoothTimesMap(getOpCountProbMap(pathTrainSet, "td"), lambdaTimes);
        tiTimesProbMap = smoothTimesMap(getOpCountProbMap(pathTrainSet, "ti"), lambdaTimes);
        List<Map<String, Double>> maps = initProbMap(pathTrainSet, lambdaOp);
        hdOpProbMap = maps.get(0);
        hiOpProbMap = maps.get(1);
        tdOpProbMap = maps.get(2);
        tiOpProbMap = maps.get(3);

//        secParam_L = calL();


        encodeIfHdProbTable = probMap2EncodeTable(ifHdProbMap);
        encodeIfHiProbTable = probMap2EncodeTable(ifHiProbMap);
        encodeIfTdProbTable = probMap2EncodeTable(ifTdProbMap);
        encodeIfTiProbTable = probMap2EncodeTable(ifTiProbMap);
        encodeHdTimesProbTable = probMap2EncodeTable(hdTimesProbMap);
        encodeHiTimesProbTable = probMap2EncodeTable(hiTimesProbMap);
        encodeTdTimesProbTable = probMap2EncodeTable(tdTimesProbMap);
        encodeTiTimesProbTable = probMap2EncodeTable(tiTimesProbMap);
        encodeHdOpProbTable = probMap2EncodeTable(hdOpProbMap);
        encodeHiOpProbTable = probMap2EncodeTable(hiOpProbMap);
        encodeTdOpProbTable = probMap2EncodeTable(tdOpProbMap);
        encodeTiOpProbTable = probMap2EncodeTable(tiOpProbMap);
    }


    private Map<Integer, Double> initIfOpProbMap(List<String> pathTrainSet, String op) {
        Map<Integer, Double> ifOpProb = new HashMap<>();
        LongAdder ifOpCount = new LongAdder();
        pathTrainSet.forEach(path -> {
            if (path.contains(op)) {
                ifOpCount.add(1);
            }
        });
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
        double factor = originSize + lambdaOp * 124;

        double factor2 = lambdaOp / factor;

        opProbMap.replaceAll((op, occurTimes) -> (occurTimes + lambdaOp) / factor);

        candidateList.forEach(s -> {
            String s1 = opName + "(" + s + ")";
            opProbMap.putIfAbsent(s1, factor2);
        });

        return opProbMap;
    }

    Map<Integer, Double> getOpCountProbMap(List<String> pathTrainSet, String op) {
        Map<Integer, Double> countOp = new LinkedHashMap<>();
        for (String path : pathTrainSet) {
            if (path != null) {
                int opTimes = countOccurrencesOfOp(path, op);
                if (opTimes >= 1 && opTimes < 16) {
                    countOp.merge(opTimes, 1.0, Double::sum);
                }
            }
        }
        return calculateFrequency(countOp);
    }

    Map<Integer, Double> smoothTimesMap(Map<Integer, Double> opTimesMap, double lambdaTimes) {
        double originSize = opTimesMap.values().size();
        double factor = originSize + lambdaTimes * 15;
        for (int i = 1; i < 16; i++) {
            if (opTimesMap.containsKey(i)) {
                opTimesMap.put(i, (opTimesMap.get(i) + lambdaTimes) / factor);
            } else {
                opTimesMap.put(i, lambdaTimes / factor);
            }
        }
        return opTimesMap;
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
