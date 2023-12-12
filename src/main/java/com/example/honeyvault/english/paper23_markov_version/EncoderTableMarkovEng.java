package com.example.honeyvault.english.paper23_markov_version;

import cn.hutool.core.lang.Pair;
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
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.honeyvault.tool.CalPath.countOccurrencesOfOp;

@Component
@ToString
public class EncoderTableMarkovEng {

    Map<Integer, Double> passwdLengthProbMap;
    Map<String, Double> firstMkvProbMap;
    HashMap<String, HashMap<String, Double>> everyMkv_1ProbMap;
    Map<Integer, Double> ifInsertProbMap;
    Map<Integer, Double> ifDeleteProbMap;

    Map<String, Double> hdOpProbMap;
    Map<String, Double> hiOpProbMap;
    Map<String, Double> tdOpProbMap;
    Map<String, Double> tiOpProbMap;


    Map<Integer, EncodeLine<Integer>> encodePasswdLengthTable;
    Map<String, EncodeLine<String>> encodefirstMkvTable;
    Map<String, Map<String, EncodeLine<String>>> encodeEveryMkv_1Table = new HashMap<>();
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

    Map<String, EncodeLine<String>> absentMkv_1Table;
    Map<String, Double> absentMkv_1ProbMap;

    Map<Integer, EncodeLine<Integer>> specialDeleteEncodeLine = new HashMap<>();


    int secParam_L;
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

    void buildAbsentMkv_1Table() {
        absentMkv_1ProbMap = new HashMap<>();
        double defaultValue = 0.00826446281;
        candidateList.forEach(s -> absentMkv_1ProbMap.put(s, defaultValue));
        absentMkv_1Table = probMap2EncodeTable(absentMkv_1ProbMap);
    }


    public void buildEncodeTables(int mkv, double lambdaOp, double lambdaTimes, double lambdaMkv, double lambdaMkv_1) {
        List<String> passwds = markovStatistic.parseEngMerge();
        List<PathInfo> pathInfoTrainSet = pathStatistic.getEngPathTrainSet();
        secParam_L = 128;
        passwdLengthProbMap = initPasswdLengthProbMap(passwds);
        firstMkvProbMap = getMkv(passwds, mkv, lambdaMkv);
        List<String> pathTrainSet = new ArrayList<>();
        pathInfoTrainSet.forEach(info -> {
            pathTrainSet.add(info.getPath());
        });
        List<Map<String, Double>> maps = initProbMap(pathTrainSet, lambdaOp);
        hdOpProbMap = maps.get(0);
        hiOpProbMap = maps.get(1);
        tdOpProbMap = maps.get(2);
        tiOpProbMap = maps.get(3);
        buildAbsentMkv_1Table();
        int mkv_1 = mkv + 1;
        everyMkv_1ProbMap = getMkv_1(passwds, mkv_1, lambdaMkv_1);
        everyMkv_1ProbMap.forEach((prefix, map) -> {
            Map<String, EncodeLine<String>> stringEncodeTableLineMap = probMap2EncodeTable(map);
            encodeEveryMkv_1Table.putIfAbsent(prefix, stringEncodeTableLineMap);
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



        encodePasswdLengthTable = probMap2EncodeTable(passwdLengthProbMap);
        encodefirstMkvTable = probMap2EncodeTable(firstMkvProbMap);

        encodeIfInsertProbTable = probMap2EncodeTable(ifInsertProbMap);
        encodeIfDeleteProbTable = probMap2EncodeTable(ifDeleteProbMap);

        encodeHdOpProbTable = probMap2EncodeTable(hdOpProbMap);
        encodeHiOpProbTable = probMap2EncodeTable(hiOpProbMap);
        encodeTdOpProbTable = probMap2EncodeTable(tdOpProbMap);
        encodeTiOpProbTable = probMap2EncodeTable(tiOpProbMap);
    }


    private Map<Integer, Double> initPasswdLengthProbMap(List<String> passwds) {
        Map<Integer, Double> passwdLengthCount = new LinkedHashMap<>();
        passwds.forEach(s -> passwdLengthCount.merge(s.length(), 1.0, Double::sum));
        return calculateFrequency(passwdLengthCount);
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
        int up = Math.min(7 * k, 16 - k);
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
        double k = Math.floor(0.875 * k1);
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

    Map<String, Double> getMkv(List<String> passwords, int mkv, double lambdaMkv) {
        Map<String, Double> result = new LinkedHashMap<>();
        int t = mkv - 1;
        for (String password : passwords) {
            if (password != null) {
                for (int i = 0; i < password.length() - t; i++) {
                    String firstMkv = password.substring(i, i + mkv);
                    result.put(firstMkv, result.getOrDefault(firstMkv, 0.0) + 1.0);
                }
            }
        }
        double originSize = result.values().stream().mapToDouble(Double::doubleValue).sum();
        double pow = Math.pow(121, mkv);
        double factor = originSize + lambdaMkv * pow;
        double factor2 = lambdaMkv / factor;

        result.replaceAll((firstMkv, times) -> (times + lambdaMkv) / factor);
        generateStrings(mkv).forEach(s -> result.putIfAbsent(s, factor2));
        return result;
    }

    HashMap<String, HashMap<String, Double>> getMkv_1(List<String> passwords, int mkv, double lambdaMkv_1) {
//        Map<String, Double> map2 = new LinkedHashMap<>();
        HashMap<String, HashMap<String, Double>> result = new LinkedHashMap<>();
        int t = mkv - 1;
        for (String password : passwords) {
            if (password != null) {
                for (int i = 0; i < password.length() - t; i++) {
                    String substring = password.substring(i, i + mkv);
                    String prefix = substring.substring(0, t);
                    String target = substring.substring(t, mkv);

                    HashMap<String, Double> mkv_1 = result.computeIfAbsent(prefix, k -> new LinkedHashMap<>());
                    mkv_1.put(target, mkv_1.getOrDefault(target, 0.0) + 1.0);
                    result.put(prefix, mkv_1);
                }
            }
        }
        // Apply Laplace smoothing

        result.forEach((prefix, map) -> {
            double originSize = map.values().stream().mapToDouble(Double::doubleValue).sum();
            double smoothFactor = lambdaMkv_1 / (originSize + lambdaMkv_1 * 121);
            map.replaceAll((suffix, times) -> ((times + lambdaMkv_1) / (originSize + lambdaMkv_1 * 121)));
            candidateList.forEach(s -> map.putIfAbsent(s, smoothFactor));
        });

        return result;
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
        double pow = Math.pow(2, secParam_L);
        double lowerBound = 0.0;
        double upperBound;
        Map<T, EncodeLine<T>> encodeTable = new LinkedHashMap<>();
        for (Map.Entry<T, Double> entry : map.entrySet()) {
            T key = entry.getKey();
            double value = entry.getValue();
            upperBound = lowerBound + value;
            encodeTable.put(key,
                    EncodeLine.<T>builder()
                            .originValue(key)
                            .prob(value)
                            .lowerBound(BigDecimal.valueOf(pow).multiply(BigDecimal.valueOf(lowerBound)).toBigInteger())
                            .upperBound(BigDecimal.valueOf(pow).multiply(BigDecimal.valueOf(upperBound)).toBigInteger())
                            .build());
            lowerBound = upperBound;
        }
        return encodeTable;
    }


}
