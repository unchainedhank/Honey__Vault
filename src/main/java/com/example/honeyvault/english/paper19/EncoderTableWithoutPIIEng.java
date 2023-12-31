package com.example.honeyvault.english.paper19;

import cn.hutool.core.lang.Pair;
import com.example.honeyvault.data_access.EncodeLine;
import com.example.honeyvault.data_access.markov.MarkovStatistic;
import com.example.honeyvault.data_access.path.PathAndAlphaUser;
import com.example.honeyvault.data_access.path.PathInfo;
import com.example.honeyvault.data_access.path.PathStatistic;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.honeyvault.tool.CalPath.countOccurrencesOfOp;

@Component
public class EncoderTableWithoutPIIEng {
    /*
    1. g(dynamic)
    2. length
    3. first5
    4. every6
    5. ifHd
    6. ifHi
    7. ifTd
    8. ifTi
    9. hdTimes
    10. hiTimes
    11. tdTimes
    12. tiTimes
    13. hdOp
    14. hiOp
    15. tdOp
    16. tiOp
     */
//   Count->ProbMap->Lower,Upper

    Map<Integer, Double> passwdLengthProbMap;
    Map<String, Double> firstMkvProbMap;
    HashMap<String, HashMap<String, Double>> everyMkv_1ProbMap;
    Map<Integer, Double> ifHdProbMap;
    Map<Integer, Double> ifHiProbMap;
    Map<Integer, Double> ifTdProbMap;
    Map<Integer, Double> ifTiProbMap;

    Map<Integer, Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>>> encodeInsertTimesProbTable =
            new HashMap<>();

    Map<Integer, Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>>> hiOnlyTable = new HashMap<>();
    Map<Integer, Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>>> tiOnlyTable = new HashMap<>();
    Map<Integer, Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>>> bothInsertTable = new HashMap<>();
    //    k, (hdTime,tdTimes)->encodeLine
    Map<Integer, Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>>> encodeDeleteTimesProbTable =
            new HashMap<>();
    Map<Integer, Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>>> hdOnlyTable = new HashMap<>();
    Map<Integer, Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>>> tdOnlyTable = new HashMap<>();
    Map<Integer, Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>>> bothDeleteTable = new HashMap<>();
    Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>> k6SpecDeleteTable = new HashMap<>();

    Map<String, Double> hdOpProbMap;
    Map<String, Double> hiOpProbMap;
    Map<String, Double> tdOpProbMap;
    Map<String, Double> tiOpProbMap;


    Map<Integer, EncodeLine<Integer>> encodePasswdLengthTable;
    Map<String, EncodeLine<String>> encodeFirstMkvTable;

    Integer originFirstMkvSize = 0;
    BigInteger kNPlus1 = new BigInteger("0");

    Map<String, Map<String, EncodeLine<String>>> encodeEveryMkv_1Table = new HashMap<>();

    Map<Integer, EncodeLine<Integer>> encodeIfHdProbTable;
    Map<Integer, EncodeLine<Integer>> encodeIfHiProbTable;
    Map<Integer, EncodeLine<Integer>> encodeIfTdProbTable;
    Map<Integer, EncodeLine<Integer>> encodeIfTiProbTable;

    Map<String, EncodeLine<String>> encodeHdOpProbTable;
    Map<String, EncodeLine<String>> encodeHiOpProbTable;
    Map<String, EncodeLine<String>> encodeTdOpProbTable;
    Map<String, EncodeLine<String>> encodeTiOpProbTable;

    Map<String, EncodeLine<String>> absentMkv_1Table;
    Map<String, Double> absentMkv_1ProbMap;

    public Double Pr_M_tail;
    public Double Pr_M_head;

    public double Pr_M_headAndTail;

    public Double Pr_T_insert;
    public Double Pr_T_delete;
    public Double Pr_T_deleteAndInsert;

    public Double Pr_H_insert;
    public Double Pr_H_delete;
    public Double Pr_H_deleteAndInsert;


    public Map<String, EncodeLine<String>> prMTable = new HashMap<>();
    public Map<String, EncodeLine<String>> prTOpTable = new HashMap<>();
    public Map<String, EncodeLine<String>> prHOpTable = new HashMap<>();

    int secParam_L = 128;
    List<String> candidateList;

    @Resource
    private PathStatistic pathStatistic;
    @Resource
    private MarkovStatistic markovStatistic;

    @PostConstruct
    void buildCandidateList() {
        candidateList = new ArrayList<>();
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

    void buildAbsentMkv6Table() {
        absentMkv_1ProbMap = new HashMap<>();
        double defaultValue = 0.01052631579;
        candidateList.forEach(s -> absentMkv_1ProbMap.put(s, defaultValue));
        absentMkv_1Table = probMap2EncodeTable(absentMkv_1ProbMap);
    }

    private void initPrMTable(double pr_M_head, double pr_M_tail, double pr_M_headAndTail) {
        BigDecimal pow = BigDecimal.valueOf(2).pow(secParam_L);
        BigInteger value1 = pow.multiply(BigDecimal.valueOf(pr_M_head)).toBigInteger();
        EncodeLine<String> pr_m_head =
                EncodeLine.<String>builder()
                        .prob(pr_M_head)
                        .originValue("pr_M_head")
                        .lowerBound(BigInteger.valueOf(0))
                        .upperBound(value1).build();
        BigDecimal value2 = pow.multiply(BigDecimal.valueOf(pr_M_head + pr_M_tail));
        EncodeLine<String> pr_m_tail =
                EncodeLine.<String>builder()
                        .prob(pr_M_tail)
                        .originValue("pr_M_tail")
                        .lowerBound(value1)
                        .upperBound(value2.toBigInteger()).build();
        EncodeLine<String> pr_m_headAndTail =
                EncodeLine.<String>builder()
                        .prob(pr_M_headAndTail)
                        .originValue("pr_M_headAndTail")
                        .lowerBound(value2.toBigInteger())
                        .upperBound(pow.toBigInteger()).build();
        prMTable.put("pr_M_head", pr_m_head);
        prMTable.put("pr_M_tail", pr_m_tail);
        prMTable.put("pr_M_headAndTail", pr_m_headAndTail);
    }

    private void initPrHOpTable(double pr_H_insert, double pr_H_delete, double pr_H_deleteAndInsert) {
        BigDecimal pow = BigDecimal.valueOf(Math.pow(2, secParam_L));
        EncodeLine<String> pr_h_insert =
                EncodeLine.<String>builder().prob(pr_H_insert).originValue("pr_H_insert").lowerBound(BigInteger.valueOf(0)).upperBound(pow.multiply(BigDecimal.valueOf(pr_H_insert)).toBigInteger()).build();
        BigInteger mid = pow.multiply(BigDecimal.valueOf(pr_H_insert + pr_H_delete)).toBigInteger();
        EncodeLine<String> pr_h_delete =
                EncodeLine.<String>builder().prob(pr_H_delete).originValue("pr_H_delete").lowerBound(pow.multiply(new BigDecimal(pr_H_insert)).toBigInteger()).upperBound(mid).build();
        EncodeLine<String> pr_h_deleteAndInsert =
//                EncodeLine.<String>builder().prob(pr_H_deleteAndInsert).originValue("pr_H_deleteAndInsert")
//                .lowerBound(mid).upperBound(pow.toBigInteger()).build();
                EncodeLine.<String>builder().prob(pr_H_deleteAndInsert).originValue("pr_H_deleteAndInsert").lowerBound(mid).upperBound(pow.multiply(BigDecimal.valueOf(pr_H_insert + pr_H_delete + pr_H_deleteAndInsert)).toBigInteger()).build();
        prHOpTable.put("pr_H_insert", pr_h_insert);
        prHOpTable.put("pr_H_delete", pr_h_delete);
        prHOpTable.put("pr_H_deleteAndInsert", pr_h_deleteAndInsert);
    }


    private void initPrTOpTable(double pr_T_insert, double pr_T_delete, double pr_T_deleteAndInsert) {
        BigDecimal pow = BigDecimal.valueOf(Math.pow(2, secParam_L));
        BigInteger upperBound = pow.multiply(BigDecimal.valueOf(pr_T_insert)).toBigInteger();
        EncodeLine<String> pr_t_insert =
                EncodeLine.<String>builder().prob(pr_T_insert).originValue("pr_T_insert").lowerBound(BigInteger.valueOf(0)).upperBound(upperBound).build();
        BigInteger floor = pow.multiply(BigDecimal.valueOf(pr_T_insert + pr_T_delete)).toBigInteger();
        EncodeLine<String> pr_t_delete =
                EncodeLine.<String>builder().prob(pr_T_delete).originValue("pr_T_delete").lowerBound(upperBound).upperBound(floor).build();
        EncodeLine<String> pr_t_deleteAndInsert =
//                EncodeLine.<String>builder().prob(pr_T_deleteAndInsert).originValue("pr_T_deleteAndInsert")
//                .lowerBound(floor).upperBound(pow.toBigInteger()).build();
                EncodeLine.<String>builder().prob(pr_T_deleteAndInsert).originValue("pr_T_deleteAndInsert").lowerBound(floor).upperBound(pow.multiply(BigDecimal.valueOf(pr_T_insert + pr_T_delete + pr_T_deleteAndInsert)).toBigInteger()).build();
        prTOpTable.put("pr_T_insert", pr_t_insert);
        prTOpTable.put("pr_T_delete", pr_t_delete);
        prTOpTable.put("pr_T_deleteAndInsert", pr_t_deleteAndInsert);
    }


    public void buildEncodeTablesWithoutPII(int mkv, double lambdaMkv, double lambdaMkv_1, double lambdaOp,
                                            double lambdaTimes) {
        Set<PathAndAlphaUser> userVaultSet = pathStatistic.parsePswdsEngWithoutPII();
        List<PathInfo> pathInfoTrainSet = pathStatistic.getEngPathTrainSetWithoutPII();
        List<String> pathTrainSet = new ArrayList<>();
        pathInfoTrainSet.forEach(info -> pathTrainSet.add(info.getPath()));
        List<String> passwds = markovStatistic.parseEngMergeWithoutPII();

        List<Map<String, Double>> maps = initProbMap(pathTrainSet, lambdaOp);
        hdOpProbMap = maps.get(0);
        hiOpProbMap = maps.get(1);
        tdOpProbMap = maps.get(2);
        tiOpProbMap = maps.get(3);

        Pr_M_head = initProbM(pathTrainSet).get(0);
        Pr_M_tail = initProbM(pathTrainSet).get(1);
        Pr_M_headAndTail = initProbM(pathTrainSet).get(2);

        List<Double> Pr_head = initPr_part_op(pathTrainSet, "head");
        List<Double> Pr_tail = initPr_part_op(pathTrainSet, "tail");
        Pr_T_insert = Pr_tail.get(0);
        Pr_T_delete = Pr_tail.get(1);
        Pr_T_deleteAndInsert = Pr_tail.get(2);

        Pr_H_insert = Pr_head.get(0);
        Pr_H_delete = Pr_head.get(1);
        Pr_H_deleteAndInsert = Pr_head.get(2);

        initPrMTable(Pr_M_head, Pr_M_tail, Pr_M_headAndTail);
        initPrTOpTable(Pr_T_insert, Pr_T_delete, Pr_T_deleteAndInsert);
        initPrHOpTable(Pr_H_insert, Pr_H_delete, Pr_H_deleteAndInsert);

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

        Map<Pair<Integer, Integer>, Double> temp = new HashMap<>();
        double prob = 0.5;
        temp.put(new Pair<>(1, 0), prob);
        temp.put(new Pair<>(0, 1), prob);
        Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>> tempEncodeLine =
                probMap2EncodeTable(temp);
        encodeInsertTimesProbTable.put(15, tempEncodeLine);

        tiOnlyTable = encodeInsertTimesProbTable.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().stream()
                                .filter(innerEntry -> innerEntry.getKey().getKey() == 0) // 假设 hiTimes 位于整数对的左侧
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                ));
        hiOnlyTable = encodeInsertTimesProbTable.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().stream()
                                .filter(innerEntry -> innerEntry.getKey().getValue() == 0) // 假设 hiTimes 位于整数对的左侧
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                ));

        bothInsertTable = encodeInsertTimesProbTable.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().stream()
                                .filter(innerEntry -> innerEntry.getKey().getValue() != 0 && innerEntry.getKey().getKey() != 0) // 假设 hiTimes 位于整数对的左侧
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                ));
        tdOnlyTable = encodeDeleteTimesProbTable.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().stream()
                                .filter(innerEntry -> innerEntry.getKey().getKey() == 0) // 假设 hiTimes 位于整数对的左侧
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                ));
        hdOnlyTable = encodeDeleteTimesProbTable.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().stream()
                                .filter(innerEntry -> innerEntry.getKey().getValue() == 0) // 假设 hiTimes 位于整数对的左侧
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                ));

        bothDeleteTable = encodeDeleteTimesProbTable.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().stream()
                                .filter(innerEntry -> innerEntry.getKey().getValue() != 0 && innerEntry.getKey().getKey() != 0) // 假设 hiTimes 位于整数对的左侧
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                ));


        modifyProb(hiOnlyTable);
        modifyProb(tiOnlyTable);
        modifyProb(hdOnlyTable);
        modifyProb(tdOnlyTable);
        modifyProb(bothInsertTable);
        modifyProb(bothDeleteTable);
//        k6SpecDeleteTable
//        bothDeleteTable.put(6,)

//        hiOnlyTable = modifyTable(hiOnlyTable);
//        tiOnlyTable = modifyTable(tiOnlyTable);
//        hdOnlyTable = modifyTable(hdOnlyTable);
//        tdOnlyTable = modifyTable(tdOnlyTable);
//        bothInsertTable = modifyTable(bothInsertTable);
//        bothDeleteTable = modifyTable(bothDeleteTable);


        int mkv_1 = mkv + 1;

        passwdLengthProbMap = initPasswdLengthProbMap(userVaultSet);

        firstMkvProbMap = getMkv(passwds, mkv, lambdaMkv);
        buildAbsentMkv6Table();

        everyMkv_1ProbMap = getMkv_1(passwds, mkv_1, lambdaMkv_1);
        everyMkv_1ProbMap.forEach((prefix, map) -> {
            Map<String, EncodeLine<String>> stringEncodeTableLineMap = probMap2EncodeTable(map);
            encodeEveryMkv_1Table.put(prefix, stringEncodeTableLineMap);
        });

        ifHdProbMap = initIfOpProbMap(pathTrainSet, "hd");
        ifHiProbMap = initIfOpProbMap(pathTrainSet, "hi");
        ifTdProbMap = initIfOpProbMap(pathTrainSet, "td");
        ifTiProbMap = initIfOpProbMap(pathTrainSet, "ti");


        encodePasswdLengthTable = probMap2EncodeTable(passwdLengthProbMap);
        encodeFirstMkvTable = probMap2EncodeTable(firstMkvProbMap);
        originFirstMkvSize = encodeFirstMkvTable.size();

        Optional<EncodeLine<String>> max =
                encodeFirstMkvTable.values().stream().max(Comparator.comparing(EncodeLine::getUpperBound));
        AtomicReference<BigInteger> kNPlusOneReference = new AtomicReference<>(new BigInteger("0"));
        max.ifPresent(line -> kNPlusOneReference.set(line.getUpperBound()));
        kNPlus1 = kNPlusOneReference.get();

        encodeIfHdProbTable = probMap2EncodeTable(ifHdProbMap);
        encodeIfHiProbTable = probMap2EncodeTable(ifHiProbMap);
        encodeIfTdProbTable = probMap2EncodeTable(ifTdProbMap);
        encodeIfTiProbTable = probMap2EncodeTable(ifTiProbMap);
        encodeHdOpProbTable = probMap2EncodeTable(hdOpProbMap);
        encodeHiOpProbTable = probMap2EncodeTable(hiOpProbMap);
        encodeTdOpProbTable = probMap2EncodeTable(tdOpProbMap);
        encodeTiOpProbTable = probMap2EncodeTable(tiOpProbMap);
    }

    private void modifyProb(Map<Integer, Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>>> table) {
        table.forEach((key, innerMap) -> {
            double sum = innerMap.values().stream().mapToDouble(EncodeLine::getProb).sum();

            Map<Pair<Integer, Integer>, Double> temp = new HashMap<>();
            innerMap.forEach((pair, encodeLine) -> {
                temp.put(pair, encodeLine.getProb() / sum);
            });
            Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>> tempTable =
                    probMap2EncodeTable(temp);
            table.put(key, tempTable);
        });
    }


    private Map<Integer, Double> initPasswdLengthProbMap(Set<PathAndAlphaUser> userVaultSet) {
        Map<Integer, Double> passwdLengthCount = new LinkedHashMap<>();
        userVaultSet.forEach(p -> p.getPswdList().forEach(pswd -> passwdLengthCount.merge(pswd.length(), 1.0,
                Double::sum)));
        return calculateFrequency(passwdLengthCount);
    }


    private Map<Integer, Double> initIfOpProbMap(List<String> pathTrainSet, String op) {
        LongAdder opCount = new LongAdder();
        LongAdder pathCount = new LongAdder();
        pathTrainSet.forEach(path -> {
            pathCount.add(1);
            if (path.contains(op)) {
                opCount.add(1);
            }
        });
        double probOp = opCount.doubleValue() / pathCount.doubleValue();

        Map<Integer, Double> ifOpProb = new LinkedHashMap<>();
        ifOpProb.put(1, probOp);
        ifOpProb.put(0, 1 - probOp);
        return ifOpProb;
    }

    private List<Double> initProbM(List<String> pathTrainSet) {
        LongAdder headCount = new LongAdder();
        LongAdder tailCount = new LongAdder();
        LongAdder headAndTailCount = new LongAdder();
        LongAdder pathCount = new LongAdder();
        pathTrainSet.forEach(path -> {
            boolean isHeadModified = path.contains("hd") || path.contains("hi");
            boolean isTailModified = path.contains("td") || path.contains("ti");
            if (!"[]".equals(path)) {
                pathCount.add(1);
            }
            if (isHeadModified && isTailModified) {
                headAndTailCount.add(1);
            } else if (isHeadModified) {
                headCount.add(1);
            } else if (isTailModified) {
                tailCount.add(1);
            }
        });
        List<Double> result = new ArrayList<>();
        result.add(headCount.doubleValue() / pathCount.doubleValue());
        result.add(tailCount.doubleValue() / pathCount.doubleValue());
        result.add(headAndTailCount.doubleValue() / pathCount.doubleValue());
        return result;
    }

    /**
     * @param part head or tail
     * @return insert and delete
     */
    private List<Double> initPr_part_op(List<String> pathTrainSet, String part) {
        LongAdder insert = new LongAdder();
        LongAdder delete = new LongAdder();
        LongAdder insert_delete = new LongAdder();
        LongAdder pathCount = new LongAdder();
        if (part.equals("head")) {
            pathTrainSet.forEach(path -> {
                if (!path.equals("[]")) {
                    int hd = countOccurrences(path, "hd");
                    int hi = countOccurrences(path, "hi");
                    if (hd > 0 || hi > 0) {
                        if (hd > 0 && hi == 0) {
                            delete.add(1);
                        } else if (hd == 0) {
                            insert.add(1);
                        } else if (hd > 0 && hi > 0) {
                            insert_delete.add(1);
                        }
                        pathCount.add(1);
                    }

                }
            });
        } else {
//          计算尾部改变
            pathTrainSet.forEach(path -> {
                if (!path.equals("[]")) {
                    int td = countOccurrences(path, "td");
                    int ti = countOccurrences(path, "ti");
                    if (td > 0 || ti > 0) {
                        if (td > 0 && ti == 0) {
                            delete.add(1);
                        } else if (td == 0) {
                            insert.add(1);
                        } else if (td > 0 && ti > 0) {
                            insert_delete.add(1);
                        }
                        pathCount.add(1);
                    }
                }
            });
        }
        List<Double> res = new ArrayList<>(3);
        double v = pathCount.doubleValue();
        res.add(insert.doubleValue() / v);
        res.add(delete.doubleValue() / v);
        res.add(insert_delete.doubleValue() / v);
        return res;
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
                    if (s.contains("hd")) {
                        hdOpProbMap.merge(s.trim(), 1.0, Double::sum);
                    } else if (s.contains("hi")) hiOpProbMap.merge(s.trim(), 1.0, Double::sum);
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
        double factor = originSize + lambdaOp * 95;

        double factor2 = lambdaOp / factor;

        opProbMap.replaceAll((op, occurTimes) -> (occurTimes + lambdaOp) / factor);

        candidateList.forEach(s -> {
            String s1 = opName + "(" + s + ")";
            opProbMap.putIfAbsent(s1, factor2);
        });

        return opProbMap;
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
        double pow = Math.pow(95, mkv);
        double factor = originSize + lambdaMkv * pow;

        result.replaceAll((firstMkv, times) -> (times + lambdaMkv) / factor);
        return result;
    }


    HashMap<String, HashMap<String, Double>> getMkv_1(List<String> passwords, int mkv, double lambda) {
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
            double smoothFactor = lambda / (originSize + lambda * 95);
            map.replaceAll((suffix, times) -> ((times + lambda) / (originSize + lambda * 95)));
            candidateList.forEach(s -> map.putIfAbsent(s, smoothFactor));
        });

        return result;
    }

    private Map<Integer,
            Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>>> modifyTable(Map<Integer,
            Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>>> table) {
        Map<Integer, Map<Pair<Integer, Integer>, Double>> map1 = table.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        innerEntry -> innerEntry.getValue().getProb()
                                ))
                ));
        Map<Integer, Map<Pair<Integer, Integer>, EncodeLine<Pair<Integer, Integer>>>> temp = new HashMap<>();
        map1.forEach((k, probMap) -> temp.put(k, probMap2EncodeTable(probMap)));
        return temp;
    }


    private <T> Map<T, EncodeLine<T>> probMap2EncodeTable(Map<T, Double> map) {
        BigDecimal pow = BigDecimal.valueOf(Math.pow(2, secParam_L));
        double lowerBound = 0.0;
        double upperBound;
        Map<T, EncodeLine<T>> encodeTable = new LinkedHashMap<>();
        for (Map.Entry<T, Double> entry : map.entrySet()) {
            T key = entry.getKey();
            double value = entry.getValue();
            upperBound = lowerBound + value;
            encodeTable.put(key,
                    EncodeLine.<T>builder().originValue(key).prob(value).lowerBound(pow.multiply(BigDecimal.valueOf(lowerBound)).toBigInteger()).upperBound(pow.multiply(BigDecimal.valueOf(upperBound)).toBigInteger()).build());
            lowerBound = upperBound;
        }
        return encodeTable;
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
        int up = Math.min(2 * k, 16 - k);
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
        double k = Math.min(Math.floor(0.5 * k1), k1 - 5);
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


}
