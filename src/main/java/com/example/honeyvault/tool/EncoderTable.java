package com.example.honeyvault.tool;

import com.example.honeyvault.data_access.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.example.honeyvault.tool.CalPath.countOccurrencesOfOp;

@Component
public class EncoderTable {
    /*
    1. g(dynamic)
    2. length
    3. first3
    4. every4
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
    @Resource
    PreProcess preProcess;

    Map<Integer, Double> passwdLengthProbMap;
    Map<String, Double> first3ProbMap;
    HashMap<String, HashMap<String, Double>> every4ProbMap;
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
    Map<String, EncodeLine<String>> encodeFirst3Table;
    Map<String, Map<String, EncodeLine<String>>> encodeEvery4Table = new HashMap<>();
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

    Map<String, EncodeLine<String>> absentMkv4Table;
    Map<String, Double> absentMkv4ProbMap;

    int secParam_L;
    private List<List<String>> pswdsWithPII;
    List<String> candidateList;

    @PostConstruct
    void buildCandidateList() {
        candidateList = new ArrayList<>();
        candidateList.addAll(Arrays.asList("N1", "N2", "N3", "N4", "N5", "N6", "N7", "N8", "N9", "N10",
                "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "B10",
//                "A1", "A2", "A3",
                "E1", "E2", "E3",
                "P1", "P2", "P3",
                "I1", "I2", "I3"));
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

    void buildAbsentMkv4Table() {
        absentMkv4ProbMap = new HashMap<>();
        double defaultValue = 0.008064516129;
        candidateList.forEach(s -> absentMkv4ProbMap.put(s, defaultValue));
        absentMkv4Table = probMap2EncodeTable(absentMkv4ProbMap);
    }


    public void buildEncodeTables() {
//      从数据库取数据
        UserVaultSet = repo.findAll();
        pswdsWithPII = preProcess.modifyPswd2PII();

        secParam_L = 128;
        passwdLengthProbMap = initPasswdLengthProbMap();

        first3ProbMap = initMkvMap(3);
//        every4ProbMap = initMkvMap(4);
        buildAbsentMkv4Table();
        List<String> passwds = new ArrayList<>();
        UserVaultSet.forEach(p ->
        {
            passwds.add(p.getPasswd_CN());
            passwds.add(p.getPassword12306());
        });

        every4ProbMap = getMkv4(passwds);
        every4ProbMap.forEach((prefix, map) -> {
            Map<String, EncodeLine<String>> stringEncodeTableLineMap = probMap2EncodeTable(map);
            encodeEvery4Table.putIfAbsent(prefix, stringEncodeTableLineMap);
//            stringEncodeTableLineMap.forEach((suffix, line) -> {
//                System.out.println(prefix + "|" + suffix + "---" + "lower:" + line.getLowerBound() + ":" + "upper"
//                + line.getUpperBound());
//            });
        });

        ifHdProbMap = initIfHdProbMap();
        ifHiProbMap = initIfHiProbMap();
        ifTdProbMap = initIfTdProbMap();
        ifTiProbMap = initIfTiProbMap();

        hdTimesProbMap = smoothTimesMap(getOpCountProbMap("hd"));
        hiTimesProbMap = smoothTimesMap(getOpCountProbMap("hi"));
        tdTimesProbMap = smoothTimesMap(getOpCountProbMap("td"));
        tiTimesProbMap = smoothTimesMap(getOpCountProbMap("ti"));
        List<Map<String, Double>> maps = initProbMap();
        hdOpProbMap = maps.get(0);
        hiOpProbMap = maps.get(1);
        tdOpProbMap = maps.get(2);
        tiOpProbMap = maps.get(3);

//        secParam_L = calL();

        encodePasswdLengthTable = probMap2EncodeTable(passwdLengthProbMap);
        encodeFirst3Table = probMap2EncodeTable(first3ProbMap);

//        encodeEvery4Table = probMap2EncodeTable(every4ProbMap);
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
        List<String> pswdsStrings = new ArrayList<>();
        pswdsWithPII.forEach(pswdsStrings::addAll);
        pswdsStrings.forEach(s -> {
            passwdLengthCount.merge(s.length(), 1.0, Double::sum);

        });
//        UserVaultSet.forEach(p -> {
//            String passwd_cn = p.getPasswd_CN();
//            String password12306 = p.getPassword12306();
//            passwdLengthCount.merge(passwd_cn.length(), 1.0, Double::sum);
//            passwdLengthCount.merge(password12306.length(), 1.0, Double::sum);
//            String password163 = p.getPassword163();
//            if (password163!=null) {
//                passwdLengthCount.merge(password163.length(), 1.0, Double::sum);
//            }
//        });
        return calculateFrequency(passwdLengthCount);
    }

    private Map<String, Double> initMkvMap(int mkv) {
        List<String> pswdsStrings = new ArrayList<>();
        pswdsWithPII.forEach(pswdsStrings::addAll);
        return getMkv3(pswdsStrings);
//        原始的->拆95^3个Map
//        生成
//        遍历生成
//        如果原来有->value=有的频率+lambda
//        如果没有->value=lambda
//        转概率->value=value/写死xxxx
//
//        1234:1
//        1235:2
//              =>1234:1
//              =>1235:2
//        1245:1
    }

    private Map<Integer, Double> initIfHdProbMap() {
        double prHd = 0.111862836;
        Map<Integer, Double> ifHdProb = new LinkedHashMap<>();
        ifHdProb.put(1, prHd);
        ifHdProb.put(0, 1 - prHd);
        return ifHdProb;
    }

    private Map<Integer, Double> initIfHiProbMap() {
        double prHi = 0.04365152919;
        Map<Integer, Double> ifHiProb = new LinkedHashMap<>();
        ifHiProb.put(1, prHi);
        ifHiProb.put(0, 1 - prHi);
        return ifHiProb;
    }

    private Map<Integer, Double> initIfTdProbMap() {
        double prTd = 0.1423540315;
        Map<Integer, Double> ifTdProb = new LinkedHashMap<>();
        ifTdProb.put(1, prTd);
        ifTdProb.put(0, 1 - prTd);
        return ifTdProb;
    }

    private Map<Integer, Double> initIfTiProbMap() {
        double prTi = 0.03558850788;
        Map<Integer, Double> ifTiProb = new LinkedHashMap<>();
        ifTiProb.put(1, prTi);
        ifTiProb.put(0, 1 - prTi);
        return ifTiProb;
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
                    if (s.equals("hd()")||s.equals("hi()")||s.equals("ti()")||s.equals("td()")){
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
        double factor = originSize + lambda * 124;

        double factor2 = lambda/factor;

        opProbMap.replaceAll((op,occurTimes)->(occurTimes+lambda)/factor);

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
        double lambda=1;
        double factor = originSize+lambda*11;
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
        double L_min_first3ProbMap = Math.abs(Math.log(Collections.min(first3ProbMap.values())) / log2);
//        double L_min_every4ProbMap = Math.abs(Math.log(Collections.min(every4ProbMap.values())) / log2);
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
        mins.add(L_min_first3ProbMap);
//        mins.add(L_min_every4ProbMap);
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

    Map<String, Double> getMkv3(List<String> passwords) {
        Map<String, Double> result = new LinkedHashMap<>();
        for (String password : passwords) {
            if (password != null) {
                for (int i = 0; i < password.length() - 2; i++) {
                    String first3 = password.substring(i, i + 3);
                    result.put(first3, result.getOrDefault(first3, 0.0) + 1.0);
                }
            }
        }
        double originSize = result.values().stream().mapToDouble(Double::doubleValue).sum();
        double lambda = 0.001;
        double pow = Math.pow(124, 3);
        double factor = originSize + lambda * pow;
        double factor2 = lambda / factor;

        result.replaceAll((first3, times) ->  (times + lambda) / factor);
//        vault.forEach(pswd -> {
//            if (!result.containsKey(pswd)) {
//                result.put(pswd, factor2);
//            }
//        });
        generateStrings(3).forEach(s -> result.putIfAbsent(s, factor2));
//        System.out.println(result);
        return result;
    }

    HashMap<String, HashMap<String, Double>> getMkv4(List<String> passwords) {
//        Map<String, Double> map2 = new LinkedHashMap<>();
        HashMap<String, HashMap<String, Double>> result = new LinkedHashMap<>();
        for (String password : passwords) {
            if (password != null) {
                for (int i = 0; i < password.length() - 3; i++) {
                    String substring = password.substring(i, i + 4);
                    String prefix = substring.substring(0, 3);
                    String target = substring.substring(3, 4);

                    HashMap<String, Double> mkv4 = result.computeIfAbsent(prefix, k -> new LinkedHashMap<>());
                    mkv4.put(target, mkv4.getOrDefault(target, 0.0) + 1.0);
                    result.put(prefix, mkv4);
                }
            }
        }
        // Apply Laplace smoothing
        double lambda = 0.01;

        result.forEach((prefix, map) -> {
            double originSize = map.values().stream().mapToDouble(Double::doubleValue).sum();
            double smoothFactor = lambda / (originSize + lambda * 124);
            map.replaceAll((suffix, times) -> ((times+lambda) / (originSize + lambda * 124)));
            candidateList.forEach(s -> map.putIfAbsent(s, smoothFactor));
        });

//        first3ProbMap.keySet().forEach(prefix -> result.putIfAbsent(prefix, null));

//            double factor = originSize + lambda * pow;
//            map.replaceAll((charAt4, times) -> (times + lambda) / factor);

//            map.replaceAll((charAt4, times)->(charAt4,(times + lambda) / factor)));
//            map.forEach((charAt4, prob) -> map.put(charAt4, (prob + lambda) / factor));
//            for (char aChar : chars) {
//                String key = String.valueOf(aChar);
//                if (!map.containsKey(key)) {
//                    map.put(key, factor2);
//                }
//            }
//        });
//        vault.forEach(pswd->{
//            if (!result.containsKey(pswd)) {
//
//            }
//        });


//        HashMap<String, Double> tmp = new LinkedHashMap<>();
//        for (char aChar : chars) tmp.put(String.valueOf(aChar), factor2);
//        generateStrings(3).forEach(s -> result.putIfAbsent(s, new HashMap<>(tmp)));

//        for (Map.Entry<String, HashMap<String, Double>> entry : result.entrySet()) {
//            String first3 = entry.getKey();
//            HashMap<String, Double> prob4Map = entry.getValue();
//
//            for (Map.Entry<String, Double> prob4 : prob4Map.entrySet()) {
//                String combinedKey = first3 + prob4.getKey();
//                Double prob = prob4.getValue();
//                map2.put(combinedKey, prob);
//            }
//        }
        return result;
//        return map2;
    }

//    public String saveMkv4(List<String> passwords) {
//        Map<String, Double> map2 = new LinkedHashMap<>();
//        HashMap<String, HashMap<String, Double>> result = new LinkedHashMap<>();
//        for (String password : passwords) {
//            if (password != null) {
//                for (int i = 0; i < password.length() - 3; i++) {
//                    String substring = password.substring(i, i + 4);
//                    String prefix = substring.substring(0, 3);
//                    String target = substring.substring(3, 4);
//
//                    HashMap<String, Double> mkv4 = result.computeIfAbsent(prefix, k -> new LinkedHashMap<>());
//                    mkv4.put(target, mkv4.getOrDefault(target, 0.0) + 1.0);
//                    result.put(prefix, mkv4);
//                }
//            }
//        }
//        // Apply Laplace smoothing
//        double lambda = 0.01;
//        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
//        String signs = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~ ";
//        characters += signs;
//        final char[] chars = characters.toCharArray();
//        double pow = Math.pow(95, 3);
//        double factor2 = 1 / pow;
//        result.forEach((prefix, map) -> {
//            double originSize = map.values().stream().mapToDouble(Double::doubleValue).sum();
//            double factor = originSize + lambda * pow;
//            map.replaceAll((charAt4, times) -> (times + lambda) / factor);
//
//            for (char aChar : chars) {
//                String key = String.valueOf(aChar);
//                map.putIfAbsent(key, factor2);
//            }
//
//        });
//
//
//        for (Map.Entry<String, HashMap<String, Double>> entry : result.entrySet()) {
//            String first3 = entry.getKey();
//            HashMap<String, Double> prob4Map = entry.getValue();
//
//            for (Map.Entry<String, Double> prob4 : prob4Map.entrySet()) {
//                String combinedKey = first3 + prob4.getKey();
//                Double prob = prob4.getValue();
//                map2.put(combinedKey, prob);
//            }
//        }
//
//        Map<String, MkvTableLine> encodeTable = mkvProbMap2EncodeTable(map2);
//        encodeTable.forEach((string, line) -> tableLineRepo.save(line));
//        return "s";
//    }

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

//    public EncodeTableLine<String> laplaceMkv4(String pswd) {
//        String closestString = encodeEvery4Table.keySet().stream()
//                .min(Comparator.comparingInt(key -> calculateAsciiDistance(key, pswd)))
//                .orElse("aaaa");
//        double preUpperBound = encodeEvery4Table.get(closestString).getUpperBound();
//        int dist = calculateAsciiDistance(closestString, pswd);
//        double pow = Math.pow(95, 3);
//        double secureParam = Math.pow(2, secParam_L);
//        double upperBound = (preUpperBound + dist * (1 / pow) * secureParam);
//        double lowerBound = (upperBound - dist * (1 / pow) * secureParam);
//        EncodeTableLine<String> lapLaceLine =
//                EncodeTableLine.<String>builder().prob(0.01).originValue(pswd).upperBound(upperBound).lowerBound
//                (lowerBound).build();
//        encodeEvery4Table.put(pswd, lapLaceLine);
//        return lapLaceLine;
//
//    }

    int calculateAsciiDistance(String str1, String str2) {
        int distance = 0;
        for (int i = 0; i < str1.length(); i++) {
            int ascii1 = str1.charAt(i);
            int ascii2 = str2.charAt(i);
            distance += Math.abs(ascii1 - ascii2);
        }
        return distance;
    }

//    Map<String,Double> mkv4TestSetAdd(Map<String,Double> mkv4Map,List<PasswdPath> passwdPaths) {
//        // 只有mkv4的平滑
//        final double pow = 1/Math.pow(95, 3);
//
//        List<String> pswds = new ArrayList<>();
//        passwdPaths.forEach(passwdPath -> {
//            pswds.add(passwdPath.getPasswd_CN());
//            pswds.add(passwdPath.getPassword12306());
//        });
//        pswds.forEach(pswd->{
//            for (int i = 0; i < pswd.length()-3; i++) {
//                String mkv4String = pswd.substring(i, i + 3);
//                mkv4Map.putIfAbsent(mkv4String, pow);
//            }
//        });
//        return mkv4Map;
//    }

}
