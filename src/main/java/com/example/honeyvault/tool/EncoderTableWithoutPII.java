package com.example.honeyvault.tool;

import com.example.honeyvault.data_access.EncodeLine;
import com.example.honeyvault.data_access.PasswdPath;
import com.example.honeyvault.data_access.PathRepo;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;

import static com.example.honeyvault.tool.CalPath.countOccurrencesOfOp;

@Component
public class EncoderTableWithoutPII {
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
    static List<PasswdPath> UserVaultSet;
    @Resource
    PathRepo repo;

    Map<Integer, Double> passwdLengthProbMap;
    Map<String, Double> first5ProbMap;
    HashMap<String, HashMap<String, Double>> every6ProbMap;
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


    Map<Integer, EncodeLine<Integer>> encodePasswdLengthTable;
    Map<String, EncodeLine<String>> encodeFirst5Table;
    Map<String, Map<String, EncodeLine<String>>> encodeEvery6Table = new HashMap<>();
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

    Map<String, EncodeLine<String>> absentMkv6Table;
    Map<String, Double> absentMkv6ProbMap;

    Double Pr_M_tail;
    Double Pr_M_head;

    Double Pr_T_insert;
    Double Pr_T_delete;
    Double Pr_T_deleteAndInsert;

    Double Pr_H_insert;
    Double Pr_H_delete;
    Double Pr_H_deleteAndInsert;

    int secParam_L;
    List<String> candidateList;

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
        absentMkv6ProbMap = new HashMap<>();
        double defaultValue = 0.01052631579;
        candidateList.forEach(s -> absentMkv6ProbMap.put(s, defaultValue));
        absentMkv6Table = probMap2EncodeTable(absentMkv6ProbMap);
    }


    public void buildEncodeTablesWithoutPII() {
//      从数据库取数据
        UserVaultSet = repo.findAll();

        secParam_L = 128;
        passwdLengthProbMap = initPasswdLengthProbMap();

        first5ProbMap = initMkvMap();
        buildAbsentMkv6Table();
        List<String> passwds = new ArrayList<>();
        UserVaultSet.forEach(p ->
        {
            passwds.add(p.getPasswd_CN());
            passwds.add(p.getPassword12306());
        });

        every6ProbMap = getMkv6(passwds);
        every6ProbMap.forEach((prefix, map) -> {
            Map<String, EncodeLine<String>> stringEncodeTableLineMap = probMap2EncodeTable(map);
            encodeEvery6Table.putIfAbsent(prefix, stringEncodeTableLineMap);
        });

        ifHdProbMap = initIfOpProbMap("hd");
        ifHiProbMap = initIfOpProbMap("hi");
        ifTdProbMap = initIfOpProbMap("td");
        ifTiProbMap = initIfOpProbMap("ti");

        hdTimesProbMap = smoothTimesMap(getOpCountProbMap("hd"));
        hiTimesProbMap = smoothTimesMap(getOpCountProbMap("hi"));
        tdTimesProbMap = smoothTimesMap(getOpCountProbMap("td"));
        tiTimesProbMap = smoothTimesMap(getOpCountProbMap("ti"));
        List<Map<String, Double>> maps = initProbMap();
        hdOpProbMap = maps.get(0);
        hiOpProbMap = maps.get(1);
        tdOpProbMap = maps.get(2);
        tiOpProbMap = maps.get(3);


        Pr_M_tail = initProbM("tail");
        Pr_M_head = initProbM("head");

        List<Double> Pr_head = initPr_part_op("head");
        List<Double> Pr_tail = initPr_part_op("tail");
        Pr_T_insert = Pr_tail.get(0);
        Pr_T_delete = Pr_tail.get(1);
        Pr_T_deleteAndInsert = Pr_tail.get(2);

        Pr_H_insert = Pr_head.get(0);
        Pr_H_delete = Pr_head.get(1);
        Pr_H_deleteAndInsert = Pr_head.get(2);


        encodePasswdLengthTable = probMap2EncodeTable(passwdLengthProbMap);
        encodeFirst5Table = probMap2EncodeTable(first5ProbMap);

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


    private Map<Integer, Double> initPasswdLengthProbMap() {
        Map<Integer, Double> passwdLengthCount = new LinkedHashMap<>();
        UserVaultSet.forEach(p -> {
            String passwd_cn = p.getPasswd_CN();
            String password12306 = p.getPassword12306();
            passwdLengthCount.merge(passwd_cn.length(), 1.0, Double::sum);
            passwdLengthCount.merge(password12306.length(), 1.0, Double::sum);
            String password163 = p.getPassword163();
            if (password163 != null) {
                passwdLengthCount.merge(password163.length(), 1.0, Double::sum);
            }
        });
        return calculateFrequency(passwdLengthCount);
    }

    private Map<String, Double> initMkvMap() {
        List<String> passwds = new ArrayList<>();
        UserVaultSet.forEach(p ->
        {
            passwds.add(p.getPasswd_CN());
            passwds.add(p.getPassword12306());
            String password163 = p.getPassword163();
            if (password163 != null) passwds.add(password163);
        });
        return getMkv5(passwds);
//        原始的->拆95^3个Map
//        生成
//        遍历生成
//        如果原来有->value=有的频率+lambda
//        如果没有->value=lambda
//        转概率->value=value/写死xxxx
//
    }

    private Map<Integer, Double> initIfOpProbMap(String op) {
        LongAdder opCount = new LongAdder();
        LongAdder pathCount = new LongAdder();
        UserVaultSet.forEach(user -> {
            String path = user.getPath();
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

    private Double initProbM(String part) {
        LongAdder opCount = new LongAdder();
        LongAdder pathCount = new LongAdder();
        if (part.equals("head")) {
            UserVaultSet.forEach(user -> {
                String path = user.getPath();
                int hd = countOccurrences(path, "hd");
                int hi = countOccurrences(path, "hi");
                opCount.add(hd);
                opCount.add(hi);
                int totalOpTimes = 0;
                if (!path.equals("[]")) {
                    totalOpTimes = path.split(",").length;
                }
                pathCount.add(totalOpTimes);
            });
        } else {
//          计算尾部改变
            UserVaultSet.forEach(user -> {
                String path = user.getPath();
                int td = countOccurrences(path, "td");
                int ti = countOccurrences(path, "ti");
                opCount.add(td);
                opCount.add(ti);
                int totalOpTimes = 0;
                if (!path.equals("[]")) {
                    totalOpTimes = path.split(",").length;
                }
                pathCount.add(totalOpTimes);
            });
        }
        return opCount.doubleValue() / pathCount.doubleValue();
    }

    /**
     * @param part head or tail
     * @return insert and delete
     */
    private List<Double> initPr_part_op(String part) {
        LongAdder insert = new LongAdder();
        LongAdder delete = new LongAdder();
        LongAdder insert_delete = new LongAdder();
        LongAdder pathCount = new LongAdder();
        if (part.equals("head")) {
            UserVaultSet.forEach(user -> {
                String path = user.getPath();
                if (!path.equals("[]")) {
                    int hd = countOccurrences(path, "hd");
                    int hi = countOccurrences(path, "hi");
                    if (hd > 0 && hi == 0) {
                        delete.add(1);
                    } else if (hd == 0 && hi > 0) {
                        insert.add(1);
                    } else if (hd > 0 && hi > 0) {
                        insert_delete.add(1);
                    }
                    pathCount.add(1);
                }
            });
        } else {
//          计算尾部改变
            UserVaultSet.forEach(user -> {
                String path = user.getPath();
                if (!path.equals("[]")) {
                    int td = countOccurrences(path, "td");
                    int ti = countOccurrences(path, "ti");
                    if (td > 0 && ti == 0) {
                        delete.add(1);
                    } else if (td == 0 && ti > 0) {
                        insert.add(1);
                    } else if (td > 0 && ti > 0) {
                        insert_delete.add(1);
                    }
                    pathCount.add(1);
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

    private List<Map<String, Double>> initProbMap() {

        Map<String, Double> hdOpProbMap = new LinkedHashMap<>();
        Map<String, Double> hiOpProbMap = new LinkedHashMap<>();
        Map<String, Double> tdOpProbMap = new LinkedHashMap<>();
        Map<String, Double> tiOpProbMap = new LinkedHashMap<>();
        UserVaultSet.forEach(p -> {
            String path = p.getPath();
            if (path != null && !path.equals("[]")) {
                path = path.replace("[", "");
                path = path.replace("]", "");
                String[] split = path.split(",");
                for (String s : split) {
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
        mList.add(smoothOpProbMap(hdOpProbMap, "hd"));
        mList.add(smoothOpProbMap(hiOpProbMap, "hi"));
        mList.add(smoothOpProbMap(tdOpProbMap, "td"));
        mList.add(smoothOpProbMap(tiOpProbMap, "ti"));
        return mList;
    }

    Map<String, Double> smoothOpProbMap(Map<String, Double> opProbMap, String opName) {


        double lambda = 0.01;
        double originSize = opProbMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double factor = originSize + lambda * 95;

        double factor2 = lambda / factor;

        opProbMap.replaceAll((op, occurTimes) -> (occurTimes + lambda) / factor);

        candidateList.forEach(s -> {
            String s1 = opName + "(" + s + ")";
            opProbMap.putIfAbsent(s1, factor2);
        });

        return opProbMap;
    }

    Map<Integer, Double> getOpCountProbMap(String op) {
        Map<Integer, Double> countOp = new LinkedHashMap<>();
        for (PasswdPath p : UserVaultSet) {
            String path = p.getPath();
            if (path != null) {
                int opTimes = countOccurrencesOfOp(path, op);
                if (opTimes >= 1 && opTimes <= 12) {
                    countOp.merge(opTimes, 1.0, Double::sum);
                }
            }
        }
        return calculateFrequency(countOp);
    }

    Map<Integer, Double> smoothTimesMap(Map<Integer, Double> opTimesMap) {
        double originSize = opTimesMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double lambda = 1;
        double factor = originSize + lambda * 11;
        for (int i = 1; i < 12; i++) {
            if (opTimesMap.containsKey(i)) {
                opTimesMap.put(i, (opTimesMap.get(i) + lambda) / factor);
            } else {
                opTimesMap.put(i, lambda / factor);
            }
        }
        return opTimesMap;
    }

    int calL() {
        double log2 = Math.log(2);
        double L_min_first5ProbMap = Math.abs(Math.log(Collections.min(first5ProbMap.values())) / log2);
        double L_min_ifHdProbMap = Math.abs(Math.log(Collections.min(ifHdProbMap.values())) / log2);
        double L_min_ifHiProbMap = Math.abs(Math.log(Collections.min(ifHiProbMap.values())) / log2);
        double L_min_ifTdProbMap = Math.abs(Math.log(Collections.min(ifTdProbMap.values())) / log2);
        double L_min_ifTiProbMap = Math.abs(Math.log(Collections.min(ifTiProbMap.values())) / log2);
        double L_min_hdTimesProbMap = Math.abs(Math.log(Collections.min(hdTimesProbMap.values())) / log2);
        double L_min_hiTimesProbMap = Math.abs(Math.log(Collections.min(hiTimesProbMap.values())) / log2);
        double L_min_tdTimesProbMap = Math.abs(Math.log(Collections.min(tdTimesProbMap.values())) / log2);
        double L_min_tiTimesProbMap = Math.abs(Math.log(Collections.min(tiTimesProbMap.values())) / log2);
        double L_min_hdOpProbMap = Math.abs(Math.log(Collections.min(hdOpProbMap.values())) / log2);
        double L_min_hiOpProbMap = Math.abs(Math.log(Collections.min(hiOpProbMap.values())) / log2);
        double L_min_tdOpProbMap = Math.abs(Math.log(Collections.min(tdOpProbMap.values())) / log2);
        double L_min_tiOpProbMap = Math.abs(Math.log(Collections.min(tiOpProbMap.values())) / log2);
        Set<Double> mins = new HashSet<>();
        mins.add(L_min_first5ProbMap);
        mins.add(L_min_ifHdProbMap);
        mins.add(L_min_ifHiProbMap);
        mins.add(L_min_ifTdProbMap);
        mins.add(L_min_ifTiProbMap);
        mins.add(L_min_hdTimesProbMap);
        mins.add(L_min_hiTimesProbMap);
        mins.add(L_min_tdTimesProbMap);
        mins.add(L_min_tiTimesProbMap);
        mins.add(L_min_hdOpProbMap);
        mins.add(L_min_hiOpProbMap);
        mins.add(L_min_tdOpProbMap);
        mins.add(L_min_tiOpProbMap);
        int max = Collections.max(mins).intValue();
        // 实验证明
        int L_g = 12;
        int calL = Math.max(L_g, max);
        if (calL < 16) calL = 16;
        return calL;
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

    Map<String, Double> getMkv5(List<String> passwords) {
        Map<String, Double> result = new LinkedHashMap<>();
        for (String password : passwords) {
            if (password != null) {
                for (int i = 0; i < password.length() - 4; i++) {
                    String first5 = password.substring(i, i + 5);
                    result.put(first5, result.getOrDefault(first5, 0.0) + 1.0);
                }
            }
        }
        double originSize = result.values().stream().mapToDouble(Double::doubleValue).sum();
        double lambda = 0.001;
        double pow = Math.pow(95, 3);
        double factor = originSize + lambda * pow;
        double factor2 = lambda / factor;

        result.replaceAll((first5, times) -> (times + lambda) / factor);
//        vault.forEach(pswd -> {
//            if (!result.containsKey(pswd)) {
//                result.put(pswd, factor2);
//            }
//        });
        generateStrings().forEach(s -> result.putIfAbsent(s, factor2));
//        System.out.println(result);
        return result;
    }

    HashMap<String, HashMap<String, Double>> getMkv6(List<String> passwords) {
        HashMap<String, HashMap<String, Double>> result = new LinkedHashMap<>();
        for (String password : passwords) {
            if (password != null) {
                for (int i = 0; i < password.length() - 5; i++) {
                    String substring = password.substring(i, i + 6);
                    String prefix = substring.substring(0, 5);
                    String target = substring.substring(5, 6);

                    HashMap<String, Double> mkv6 = result.computeIfAbsent(prefix, k -> new LinkedHashMap<>());
                    mkv6.put(target, mkv6.getOrDefault(target, 0.0) + 1.0);
                    result.put(prefix, mkv6);
                }
            }
        }
        // Apply Laplace smoothing
        double lambda = 0.001;

        result.forEach((prefix, map) -> {
            double originSize = map.values().stream().mapToDouble(Double::doubleValue).sum();
            double smoothFactor = lambda / (originSize + lambda * 95);
            map.replaceAll((suffix, times) -> ((times + lambda) / (originSize + lambda * 95)));
            candidateList.forEach(s -> map.putIfAbsent(s, smoothFactor));
        });

        return result;
    }

    Set<String> generateStrings() {
        Set<String> res = new HashSet<>();

        for (String s : candidateList) {
            for (String value1 : candidateList) {
                for (String value2 : candidateList) {
                    for (String value3 : candidateList) {
                        for (String value4 : candidateList) {
                            String generatedString =
                                    "" + s + value1 + value2 + value3 + value4;
                            res.add(generatedString);
                        }
                    }
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
                    EncodeLine.<T>builder().originValue(key).prob(value).lowerBound(Math.floor((lowerBound * pow))).upperBound(Math.floor((upperBound * pow))).build());
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

}
