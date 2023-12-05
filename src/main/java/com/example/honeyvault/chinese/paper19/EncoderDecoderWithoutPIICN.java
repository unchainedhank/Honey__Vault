package com.example.honeyvault.chinese.paper19;

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
public class EncoderDecoderWithoutPIICN {

//    double alpha;

    @Resource
    private EncoderTableWithoutPIICN encoderTableWithourPII;

    private Map<Pair<Integer, Boolean>, EncodeLine<Pair<Integer, Boolean>>> prDrEncodeLineMap = new HashMap<>();

    public void init(int mkv, double lambdaMkv, double lambdaMkv_1, double lambdaOp, double lambdaTimes) {

        CsvWriter writer = CsvUtil.getWriter("/writeData/tableChin19.csv", CharsetUtil.CHARSET_UTF_8);

//        CsvWriter writer = CsvUtil.getWriter("/app/HvExpData/tables/table19.csv", CharsetUtil.CHARSET_UTF_8);

        encoderTableWithourPII.buildEncodeTablesWithoutPII(mkv, lambdaMkv, lambdaMkv_1, lambdaOp, lambdaTimes);

        writer.writeLine("encodeFirstMkvTable"+encoderTableWithourPII.encodeFirstMkvTable);
        writer.writeLine(" ");
      //  writer.writeLine("encodeEveryMkv_1Table"+encoderTableWithourPII.encodeEveryMkv_1Table);
        writer.writeLine(" ");
        writer.writeLine("absentMkv_1Table"+encoderTableWithourPII.absentMkv_1Table);
        writer.writeLine(" ");
        writer.writeLine("encodePasswdLengthTable"+encoderTableWithourPII.encodePasswdLengthTable);
        writer.writeLine(" ");
        writer.writeLine("prMTable"+encoderTableWithourPII.prMTable);
        writer.writeLine(" ");
        writer.writeLine("encodeIfHdProbTable"+encoderTableWithourPII.encodeIfHdProbTable);
        writer.writeLine(" ");
        writer.writeLine("encodeIfTiProbTable"+encoderTableWithourPII.encodeIfTiProbTable);
        writer.writeLine(" ");
        writer.writeLine("encodeIfTdProbTable"+encoderTableWithourPII.encodeIfTdProbTable);
        writer.writeLine(" ");
        writer.writeLine("encodeIfHiProbTable"+encoderTableWithourPII.encodeIfHiProbTable);
        writer.writeLine(" ");
        writer.writeLine("encodeHdTimesProbTable"+encoderTableWithourPII.encodeHdTimesProbTable);
        writer.writeLine(" ");
        writer.writeLine("encodeHiTimesProbTable"+encoderTableWithourPII.encodeHiTimesProbTable);
        writer.writeLine(" ");
        writer.writeLine("encodeTdTimesProbTable"+encoderTableWithourPII.encodeTdTimesProbTable);
        writer.writeLine(" ");
        writer.writeLine("encodeTiTimesProbTable"+encoderTableWithourPII.encodeTiTimesProbTable);
        writer.writeLine(" ");
        writer.writeLine("encodeHiOpProbTable"+encoderTableWithourPII.encodeHiOpProbTable);
        writer.writeLine(" ");
        writer.writeLine("encodeHdOpProbTable"+encoderTableWithourPII.encodeHdOpProbTable);
        writer.writeLine(" ");
        writer.writeLine("encodeTiOpProbTable"+encoderTableWithourPII.encodeTiOpProbTable);
        writer.writeLine(" ");
        writer.writeLine("encodeTdOpProbTable"+encoderTableWithourPII.encodeTdOpProbTable);
        writer.writeLine(" ");
        writer.writeLine("prHOpTable"+encoderTableWithourPII.prHOpTable);
        writer.writeLine(" ");
        writer.writeLine("prTOpTable"+encoderTableWithourPII.prTOpTable);
        writer.writeLine(" ");
        writer.close();

//        writer.writeLine("encodeHdOpProbTable" + ":" + encoderTableWithourPII.encodeHdOpProbTable);
//        writer.writeLine("encodeHiOpProbTable" + ":" + encoderTableWithourPII.encodeHiOpProbTable);
//        writer.writeLine("encodeTdOpProbTable" + ":" + encoderTableWithourPII.encodeTdOpProbTable);
//        writer.writeLine("encodeTiOpProbTable" + ":" + encoderTableWithourPII.encodeTiOpProbTable);
//        writer.writeLine("encodeIfHdProbTable" + ":" + encoderTableWithourPII.encodeIfHdProbTable);
//        writer.writeLine("encodeIfTiProbTable" + ":" + encoderTableWithourPII.encodeIfTiProbTable);
//        writer.writeLine("encodeIfTdProbTable" + ":" + encoderTableWithourPII.encodeIfTdProbTable);
//        writer.writeLine("encodeIfHdProbTable" + ":" + encoderTableWithourPII.encodeIfHdProbTable);
//        writer.writeLine("encodeIfHiProbTable" + ":" + encoderTableWithourPII.encodeIfHiProbTable);
//        writer.writeLine("encodeHdTimesProbTable" + ":" + encoderTableWithourPII.encodeHdTimesProbTable);
//        writer.writeLine("encodeHiTimesProbTable" + ":" + encoderTableWithourPII.encodeHiTimesProbTable);
//        writer.writeLine("encodeTiTimesProbTable" + ":" + encoderTableWithourPII.encodeTiTimesProbTable);
//        writer.writeLine("encodeTdTimesProbTable" + ":" + encoderTableWithourPII.encodeTdTimesProbTable);
//        writer.writeLine("absentMkv_1Table" + ":" + encoderTableWithourPII.absentMkv_1Table);
//        writer.writeLine("encodeFirstMkvTable" + ":" + encoderTableWithourPII.encodeFirstMkvTable);
////        writer.writeLine("encodeEveryMkv_1Table" + ":" + encoderTableWithourPII.encodeEveryMkv_1Table);
//        writer.writeLine("encodePasswdLengthTable" + ":" + encoderTableWithourPII.encodePasswdLengthTable);
//        writer.close();

    }

//    @PostConstruct
//    public void initAlpha() {
//        alpha = calAlpha();
//    }

    private void initPr_DR() {
        BigDecimal pow = BigDecimal.valueOf(Math.pow(2, encoderTableWithourPII.secParam_L));
        for (int j = 0; j < 23; j++) {
            double alpha = 0.5690178377522002;
            double prob = (j * alpha) / (j * alpha + 1 - alpha);
            Pair<Integer, Boolean> i_true = new Pair<>(j, Boolean.TRUE);
            Pair<Integer, Boolean> i_false = new Pair<>(j, Boolean.FALSE);

            BigInteger upperBound = pow.multiply(BigDecimal.valueOf(prob)).toBigInteger();
            EncodeLine<Pair<Integer, Boolean>> trueLine =
                    EncodeLine.<Pair<Integer, Boolean>>builder().prob(prob).originValue(i_true).lowerBound(BigInteger.valueOf(0)).upperBound(
                            upperBound).build();
            EncodeLine<Pair<Integer, Boolean>> falseLine =
                    EncodeLine.<Pair<Integer, Boolean>>builder().prob(prob).originValue(i_false).lowerBound(upperBound).upperBound(pow.toBigInteger()).build();
            prDrEncodeLineMap.put(new Pair<>(j, true), trueLine);
            prDrEncodeLineMap.put(new Pair<>(j, false), falseLine);
        }

//        System.out.println(prDrEncodeLineMap);

    }

    public List<Pair<String, String>> encode(List<String> initVault, int fixedLength, int mkv, double lambdaOp,
                                             double lambdaTimes, double lambdaMkv, double lambdaMkv_1) {
//        encoderTableWithourPII.buildEncodeTablesWithoutPII(mkv);
        encoderTableWithourPII.buildEncodeTablesWithoutPII(mkv, lambdaOp, lambdaTimes, lambdaMkv, lambdaMkv_1);
        List<String> vault = initVault(initVault);
        Map<Pair<Integer, Integer>, Double> pathProbMap = new HashMap<>();
        List<Pair<String, String>> pswd2EncodeString = new LinkedList<>();
        String firstPswd = vault.get(1);
        int index_first = 0;
        String encodeFirstPswd = algo4(0, firstPswd, firstPswd, mkv, lambdaMkv, true, index_first);
        encodeFirstPswd = fillWithRandom(encodeFirstPswd, fixedLength);
        pswd2EncodeString.add(new Pair<>(firstPswd, encodeFirstPswd));
        initPr_DR();
        // j is i，i is i+1
        for (int i = 2; i < vault.size(); i++) {
            for (int j = 1; j < i; j++) {
                String pwi1 = vault.get(i);
                String pwi = vault.get(j);

                double A = f_fit(j) / j;
                double B = 1 - f_fit(j);

                double pr2 = B * getMarkovProb(vault.get(i), mkv, lambdaMkv);
                pathProbMap.put(new Pair<>(i, i), pr2);
                double pr1j;
                if (!pwi.equals(pwi1)) {
                    List<List<String>> paths = CalPath.breadthFirstSearch(pwi, pwi1);
//                    System.out.println(pwi + "->" + pwi1);
                    double pr_ssm = calPr_ssm(paths);
                    pr1j = A * pr_ssm;
                } else {
                    EncodeLine<Pair<Integer, Boolean>> line = prDrEncodeLineMap.get(new Pair<>(i, true));
                    double Pr_DR_true = line.getProb();
                    pr1j = Pr_DR_true * A;
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
            String encodedG = toBinaryString(gDecimal, encoderTableWithourPII.secParam_L);
            boolean pathEquals = vault.get(pwiIndex).equals(vault.get(pwi1Index));
            String encodeString = algo4(g, vault.get(pwiIndex), vault.get(pwi1Index), mkv, lambdaMkv, pathEquals,
                    pwi1Index);
//            System.out.println("encoded string" + ":" + encodeString);

            encodeString = encodedG + encodeString;

            encodeString = fillWithRandom(encodeString, fixedLength);
            pswd2EncodeString.add(new Pair<>(vault.get(pwi1Index), encodeString));
//            System.out.println(pswd2EncodeString);
        });
        return pswd2EncodeString;
    }

    public static double f_fit(int i) {
        return 1 / (1 + Math.exp(-1.493 * i + 2.486));
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
        double L = encoderTableWithourPII.secParam_L;
        double pow = Math.pow(2, L);
        double col2;
        double lower = 0, upper;
        if (g == 0) {
            upper = Math.floor(pow *(1 - f_fit(i-1)));
        } else {
            col2 = f_fit(i-1) / (i-1);
            lower = Math.floor(((1-f_fit(i-1) + col2 * (g - 1)) * pow));
            upper = Math.floor(((1-f_fit(i-1) + col2 * g) * pow));
        }
        return new Pair<>(lower, upper);
    }


    private String algo4(int g, String basePasswd, String targetString, int mkv, double lambdaMkv, boolean ifPswdEquals,
                         int index) {
        StringBuilder encodeString = new StringBuilder();
        double fixedLength = encoderTableWithourPII.secParam_L;
        if (g == 0) {
//          编码长度
            EncodeLine<Integer> lengthEncodeLine =
                    encoderTableWithourPII.encodePasswdLengthTable.get(targetString.length());
            BigInteger encodeValue = getRandomValue(lengthEncodeLine.getLowerBound(),
                    lengthEncodeLine.getUpperBound());
            encodeString.append(toBinaryString(encodeValue, fixedLength));
//          编码头mkv个字符
            String firstMkvString = targetString.substring(0, mkv);


            double originSize =
                    encoderTableWithourPII.firstMkvProbMap.values().stream().mapToDouble(Double::doubleValue).sum();
            double pow = Math.pow(95, mkv);
            double factor = originSize + lambdaMkv * pow;
            double factor2 = lambdaMkv / factor;

            EncodeLine<String> firstMkvEncodeLine;
//                    encoderTable.encodefirstMkvTable.getOrDefault(firstMkvString, factor2);
            if (encoderTableWithourPII.encodeFirstMkvTable.get(firstMkvString) == null) {
                Optional<EncodeLine<String>> max =
                        encoderTableWithourPII.encodeFirstMkvTable.values().stream().max(Comparator.comparing(EncodeLine::getUpperBound));
                EncodeLine<String> line = max.get();
                BigDecimal preUpperBound = new BigDecimal(line.getUpperBound());
                BigDecimal f2Decimal = new BigDecimal(factor2);
                BigDecimal pow1 = BigDecimal.valueOf(2).pow(encoderTableWithourPII.secParam_L);
                BigDecimal res = preUpperBound.add(pow1.multiply(f2Decimal));
                firstMkvEncodeLine =
                        EncodeLine.<String>builder().originValue(firstMkvString).prob(factor2).lowerBound(preUpperBound.toBigInteger()).upperBound(res.toBigInteger()).build();
                encoderTableWithourPII.encodeFirstMkvTable.put(firstMkvString, firstMkvEncodeLine);
            } else {
                firstMkvEncodeLine = encoderTableWithourPII.encodeFirstMkvTable.get(firstMkvString);
            }
//            System.out.println(firstMkvEncodeLine.toString());
            encodeValue = getRandomValue(firstMkvEncodeLine.getLowerBound(),
                    firstMkvEncodeLine.getUpperBound());
            encodeString.append(toBinaryString(encodeValue, fixedLength));


//          编码每第mkv_1个字符
            Map<String, EncodeLine<String>> encodeMap;
            int mkv_1 = mkv + 1;
            for (int j = 0; j + mkv < targetString.length(); j++) {
                String window = targetString.substring(j, j + mkv_1);
                if (j == 0) {
                    encodeMap = encoderTableWithourPII.encodeEveryMkv_1Table.get(firstMkvString);
                } else {
                    String prefix = window.substring(0, mkv);
                    encodeMap = encoderTableWithourPII.encodeEveryMkv_1Table.get(prefix);
                }
                String suffix = window.substring(window.length() - 1);

                EncodeLine<String> encodeLine;
                if (encodeMap == null) {
                    encodeLine = encoderTableWithourPII.absentMkv_1Table.get(suffix);
                } else {
                    encodeLine = encodeMap.get(suffix);
                }
                encodeValue = getRandomValue(encodeLine.getLowerBound(),
                        encodeLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));
            }
        } else {
            BigInteger encodeValue;
            if (ifPswdEquals) {
                EncodeLine<Pair<Integer, Boolean>> prDrLine = prDrEncodeLineMap.get(new Pair<>(index, true));
                encodeValue = getRandomValue(prDrLine.getLowerBound(),
                        prDrLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));
            } else {
//                找到所有可能的路径
                List<List<String>> paths = CalPath.breadthFirstSearch(basePasswd, targetString);
                Map<List<String>, Double> pathProbMap = new ConcurrentHashMap<>();
//          路径->概率
                paths.forEach(path -> {
//                    System.out.println(path);
                    double pathProb = getEncodePathProb(path, index);
                    pathProbMap.put(path, pathProb);
                });
//          根据概率选择路径
                List<String> selectedOpList = selectPathByProbability(pathProbMap);
                String selectedPath = selectedOpList.toString();
//          编码路径
                selectedPath = selectedPath.trim().replace("[", "");
                selectedPath = selectedPath.trim().replace("]", "");
//          1.编码Pr_DR

                EncodeLine<Pair<Integer, Boolean>> pairEncodeLine = prDrEncodeLineMap.get(new Pair<>(index, false));
                encodeValue = getRandomValue(pairEncodeLine.getLowerBound(),
                        pairEncodeLine.getUpperBound());
                encodeString.append(toBinaryString(encodeValue, fixedLength));
//          2.tail or head or both
                boolean isHeadModified = selectedPath.contains("hd") || selectedPath.contains("hi");
                boolean isTailModified = selectedPath.contains("td") || selectedPath.contains("ti");
                if (isHeadModified && !isTailModified) {
                    EncodeLine<String> pr_m_head = encoderTableWithourPII.prMTable.get("pr_M_head");
                    encodeValue = getRandomValue(pr_m_head.getLowerBound(),
                            pr_m_head.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
                if (!isHeadModified && isTailModified) {
                    EncodeLine<String> pr_m_tail = encoderTableWithourPII.prMTable.get("pr_M_tail");
                    encodeValue = getRandomValue(pr_m_tail.getLowerBound(),
                            pr_m_tail.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
                if (isHeadModified && isTailModified) {
                    EncodeLine<String> pr_m_headAndTail = encoderTableWithourPII.prMTable.get("pr_M_headAndTail");
                    encodeValue = getRandomValue(pr_m_headAndTail.getLowerBound(),
                            pr_m_headAndTail.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
//          3.编码pr_x_op
                //head
                if (selectedPath.contains("hi") && !selectedPath.contains("hd")) {
                    EncodeLine<String> pr_h_insert = encoderTableWithourPII.prHOpTable.get("pr_H_insert");
                    encodeValue = getRandomValue(pr_h_insert.getLowerBound(),
                            pr_h_insert.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
                if (!selectedPath.contains("hi") && selectedPath.contains("hd")) {
                    EncodeLine<String> pr_h_delete = encoderTableWithourPII.prHOpTable.get("pr_H_delete");
                    encodeValue = getRandomValue(pr_h_delete.getLowerBound(),
                            pr_h_delete.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
                if (selectedPath.contains("hi") && selectedPath.contains("hd")) {
                    EncodeLine<String> pr_h_deleteAndInsert = encoderTableWithourPII.prHOpTable.get(
                            "pr_H_deleteAndInsert");
                    encodeValue = getRandomValue(pr_h_deleteAndInsert.getLowerBound(),
                            pr_h_deleteAndInsert.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
//              tail
                if (selectedPath.contains("ti") && !selectedPath.contains("td")) {
                    EncodeLine<String> pr_t_insert = encoderTableWithourPII.prTOpTable.get("pr_T_insert");
                    encodeValue = getRandomValue(pr_t_insert.getLowerBound(),
                            pr_t_insert.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
                if (!selectedPath.contains("ti") && selectedPath.contains("td")) {
                    EncodeLine<String> pr_T_delete = encoderTableWithourPII.prTOpTable.get("pr_T_delete");
                    encodeValue = getRandomValue(pr_T_delete.getLowerBound(),
                            pr_T_delete.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }
                if (selectedPath.contains("ti") && selectedPath.contains("td")) {
                    EncodeLine<String> pr_T_deleteAndInsert = encoderTableWithourPII.prTOpTable.get(
                            "pr_T_deleteAndInsert");
                    encodeValue = getRandomValue(pr_T_deleteAndInsert.getLowerBound(),
                            pr_T_deleteAndInsert.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }

//          4.编码opTimes
                int hdTimes = countOccurrencesOfOp(selectedPath, "hd");
                if (hdTimes != 0) {
                    EncodeLine<Integer> hdTimesEncodeLine = encoderTableWithourPII.encodeHdTimesProbTable.get(hdTimes);
                    encodeValue = getRandomValue(hdTimesEncodeLine.getLowerBound(),
                            hdTimesEncodeLine.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }

                int hiTimes = countOccurrencesOfOp(selectedPath, "hi");
                if (hiTimes != 0) {
                    EncodeLine<Integer> hiTimesEncodeLine = encoderTableWithourPII.encodeHiTimesProbTable.get(hiTimes);
                    encodeValue = getRandomValue(hiTimesEncodeLine.getLowerBound(),
                            hiTimesEncodeLine.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }

                int tdTimes = countOccurrencesOfOp(selectedPath, "td");
                if (tdTimes != 0) {
                    EncodeLine<Integer> tdTimesEncodeLine = encoderTableWithourPII.encodeTdTimesProbTable.get(tdTimes);
                    encodeValue = getRandomValue(tdTimesEncodeLine.getLowerBound(),
                            tdTimesEncodeLine.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }

                int tiTimes = countOccurrencesOfOp(selectedPath, "ti");
                if (tiTimes != 0) {
                    EncodeLine<Integer> tiTimesEncodeLine = encoderTableWithourPII.encodeTiTimesProbTable.get(tiTimes);
                    encodeValue = getRandomValue(tiTimesEncodeLine.getLowerBound(),
                            tiTimesEncodeLine.getUpperBound());
                    encodeString.append(toBinaryString(encodeValue, fixedLength));
                }

//          5.编码op

                for (String op : selectedOpList) {
                    op = op.trim();
                    if (op.contains("hd")) {
                        EncodeLine<String> hdOpEncodeLine = encoderTableWithourPII.encodeHdOpProbTable.get(op);
                        encodeValue = getRandomValue(hdOpEncodeLine.getLowerBound(),
                                hdOpEncodeLine.getUpperBound());
                        encodeString.append(toBinaryString(encodeValue, fixedLength));
                    }
                    if (op.contains("hi")) {
                        EncodeLine<String> hiOpEncodeLine = encoderTableWithourPII.encodeHiOpProbTable.get(op);
                        encodeValue = getRandomValue(hiOpEncodeLine.getLowerBound(),
                                hiOpEncodeLine.getUpperBound());
                        encodeString.append(toBinaryString(encodeValue, fixedLength));
                    }
                    if (op.contains("ti")) {
                        EncodeLine<String> tiOpEncodeLine = encoderTableWithourPII.encodeTiOpProbTable.get(op);
                        encodeValue = getRandomValue(tiOpEncodeLine.getLowerBound(),
                                tiOpEncodeLine.getUpperBound());
                        encodeString.append(toBinaryString(encodeValue, fixedLength));
                    }
                    if (op.contains("td")) {
                        EncodeLine<String> tdOpEncodeLine = encoderTableWithourPII.encodeTdOpProbTable.get(op);
                        encodeValue = getRandomValue(tdOpEncodeLine.getLowerBound(),
                                tdOpEncodeLine.getUpperBound());
//                        System.out.println(encodeValue);
                        encodeString.append(toBinaryString(encodeValue, fixedLength));
                    }
                }
            }
        }
//
        return encodeString.toString();
    }

    //  cal method
    private double getMarkovProb(String passwd, int mkv, double lambdaMkv) {
        // g=0
        Double lengthProb = encoderTableWithourPII.passwdLengthProbMap.get(passwd.length());
        double originSize =
                encoderTableWithourPII.firstMkvProbMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double pow = Math.pow(95, mkv);
        double factor = originSize + lambdaMkv * pow;
        double factor2 = lambdaMkv / factor;
        Double firstMkvProb = encoderTableWithourPII.firstMkvProbMap.getOrDefault(passwd.substring(0, mkv), factor2);

        double finalProb = lengthProb * firstMkvProb;
        int mkv_1 = mkv + 1;
        for (int i = 0; i + mkv < passwd.length(); i++) {
            String window = passwd.substring(i, i + mkv_1);
            String prefix = window.substring(0, mkv);
            String suffix = window.substring(window.length() - 1);
            HashMap<String, Double> suffixProb = encoderTableWithourPII.everyMkv_1ProbMap.get(prefix);
            Double mkv_1Prob;
            if (suffixProb == null) {
                mkv_1Prob = encoderTableWithourPII.absentMkv_1ProbMap.getOrDefault(suffix, 0.01052631579);
            } else {
                mkv_1Prob = suffixProb.getOrDefault(suffix, 0.01052631579);
            }
            finalProb *= mkv_1Prob;
        }
        return finalProb;
    }

    private double getPathProb(List<String> path) {
        if (path != null && path.size() > 0) {
            double prob = 1;
            for (String op : path) {
                op = op.trim();
                if (op.contains("hd")) prob *= encoderTableWithourPII.hdOpProbMap.get(op);
                else if (op.contains("hi")) prob *= encoderTableWithourPII.hiOpProbMap.get(op);
                else if (op.contains("td")) prob *= encoderTableWithourPII.tdOpProbMap.get(op);
                else if (op.contains("ti")) prob *= encoderTableWithourPII.tiOpProbMap.get(op);
            }
            return prob;
        } else return 1;
    }

    private double getEncodePathProb(List<String> path, int i) {
        double result = 1;
//        System.out.println(prDrEncodeLineMap);
//        System.out.println(i);
        result *= prDrEncodeLineMap.get(new Pair<>(i, false)).getProb();
        String pathString = path.toString();
        if (pathString.contains("hd") || pathString.contains("hi")) {
            result *= encoderTableWithourPII.Pr_M_head;
            if (pathString.contains("hd") && !pathString.contains("hi")) {
                result *= encoderTableWithourPII.Pr_H_delete;
                int hd = countOccurrencesOfOp(pathString, "hd");
                result *= encoderTableWithourPII.encodeHdTimesProbTable.get(hd).getProb();
            }
            if (!pathString.contains("hd") && pathString.contains("hi")) {
                result *= encoderTableWithourPII.Pr_H_insert;
                int hi = countOccurrencesOfOp(pathString, "hi");
                result *= encoderTableWithourPII.encodeHiTimesProbTable.get(hi).getProb();
            }
            if (pathString.contains("hd") && pathString.contains("hi")) {
                result *= encoderTableWithourPII.Pr_H_deleteAndInsert;
                int hd = countOccurrencesOfOp(pathString, "hd");
                result *= encoderTableWithourPII.encodeHdTimesProbTable.get(hd).getProb();
                int hi = countOccurrencesOfOp(pathString, "hi");
                result *= encoderTableWithourPII.encodeHiTimesProbTable.get(hi).getProb();
            }
        }
        if (pathString.contains("ti") | pathString.contains("td")) {
            result *= encoderTableWithourPII.Pr_M_tail;
            if (pathString.contains("td") && !pathString.contains("ti")) {
                result *= encoderTableWithourPII.Pr_T_delete;
                int td = countOccurrencesOfOp(pathString, "td");
                result *= encoderTableWithourPII.encodeTdTimesProbTable.get(td).getProb();
            }
            if (!pathString.contains("td") && pathString.contains("ti")) {
                result *= encoderTableWithourPII.Pr_T_insert;
                int ti = countOccurrencesOfOp(pathString, "ti");
                result *= encoderTableWithourPII.encodeTiTimesProbTable.get(ti).getProb();
            }
            if (pathString.contains("td") && pathString.contains("ti")) {
                result *= encoderTableWithourPII.Pr_T_deleteAndInsert;
                int td = countOccurrencesOfOp(pathString, "td");
                result *= encoderTableWithourPII.encodeTdTimesProbTable.get(td).getProb();
                int ti = countOccurrencesOfOp(pathString, "ti");
                result *= encoderTableWithourPII.encodeTiTimesProbTable.get(ti).getProb();
            }

        }

        result *= getPathProb(path);
        return result;

    }

    //  tools
    private double calPr_ssm(List<List<String>> paths) {
        AtomicDouble pr_ssm = new AtomicDouble(0);
        paths.forEach(path -> {
            double pathProb = getPathProb(path);
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


    public List<String> decode(List<String> encodedList, int mkv, double lambdaMkv) {
//        encoderTableWithourPII.buildEncodeTablesWithoutPII(mkv);
//        System.out.println(encoderTableWithourPII.encodeHiOpProbTable.get("hi(1)"));
        initPr_DR();
        encodedList = initVault(encodedList);
        int fixedLength = encoderTableWithourPII.secParam_L;

        List<String> originPswd = new ArrayList<>();
        CsvWriter writer = CsvUtil.getWriter("/writeData/tableChin19.csv", CharsetUtil.CHARSET_UTF_8);
        for (int index = 1; index < encodedList.size(); index++) {
            StringBuilder decodedPswd = new StringBuilder();
            String encodedString = encodedList.get(index);
            if (encodedString == null) continue;
            List<String> encodeElementList = splitString(encodedString, fixedLength);
            if (index == 1) {
                BigInteger encodedPwLength = new BigInteger(encodeElementList.get(0), 2);
                Integer pwLength = findOriginValue(encodedPwLength, encoderTableWithourPII.encodePasswdLengthTable);
                BigInteger encodedfirstMkv = new BigInteger(encodeElementList.get(1), 2);
                String firstMkv = findOriginValue(encodedfirstMkv, encoderTableWithourPII.encodeFirstMkvTable);
                if (firstMkv == null) {
                    BigInteger kNPlus1 = encoderTableWithourPII.kNPlus1;
                    BigDecimal pow = BigDecimal.valueOf(2).pow(encoderTableWithourPII.secParam_L);
                    BigDecimal p1;
                    BigDecimal topP1 = new BigDecimal(lambdaMkv);
                    BigDecimal bottomP1 =
                            new BigDecimal(encoderTableWithourPII.originFirstMkvSize).add(BigDecimal.valueOf(95).pow(5).multiply(BigDecimal.valueOf(lambdaMkv)));
                    p1 = topP1.divide(bottomP1, 20, RoundingMode.DOWN);

                    BigDecimal bottom = p1.multiply(pow);
                    BigDecimal top = new BigDecimal(encodedfirstMkv).subtract(new BigDecimal(kNPlus1));
                    BigInteger lowerBound =
                            new BigDecimal(kNPlus1).add(top.divide(bottom, 40, RoundingMode.FLOOR)
                                    .multiply(bottom)).toBigInteger();
                    BigInteger upperBound =
                            new BigDecimal(kNPlus1).add(top.divide(bottom, 40, RoundingMode.FLOOR).add(BigDecimal.valueOf(1))
                                    .multiply(bottom)).toBigInteger();
                    String randomStr = genRandomStr();
                    while (encoderTableWithourPII.encodeFirstMkvTable.containsKey(randomStr)) {
                        randomStr = genRandomStr();
                    }
                    EncodeLine<String> newRandomLine =
                            EncodeLine.<String>builder().lowerBound(lowerBound).upperBound(upperBound).originValue(randomStr).build();
                    encoderTableWithourPII.encodeFirstMkvTable.put(randomStr, newRandomLine);
                    writer.writeLine(String.valueOf(encoderTableWithourPII.encodeFirstMkvTable));
                    decodedPswd.append(randomStr);
                } else {
                    decodedPswd.append(firstMkv);
                }
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
                            encoderTableWithourPII.encodeEveryMkv_1Table.get(prefix);
                    if (encodeSuffixTable == null) {
                        suffix = findOriginValue(encodedSuffix, encoderTableWithourPII.absentMkv_1Table);
                    } else {
                        suffix = findOriginValue(encodedSuffix, encodeSuffixTable);
                    }
                    decodedPswd.append(suffix);
                }
                originPswd.add(decodedPswd.toString());
//                System.out.println(originPswd);
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
//                System.out.println("index:" + index);
//                System.out.println("g" + ":" + g);
//                System.out.println("-----------------------------");
                if (g == 0) {
                    BigInteger encodedPwLength = new BigInteger(encodeElementList.get(1), 2);
                    Integer pwLength = findOriginValue(encodedPwLength, encoderTableWithourPII.encodePasswdLengthTable);
                    BigInteger encodedfirstMkv = new BigInteger(encodeElementList.get(2), 2);
                    String firstMkv = findOriginValue(encodedfirstMkv, encoderTableWithourPII.encodeFirstMkvTable);
                    if (firstMkv == null) {
                        BigInteger kNPlus1 = encoderTableWithourPII.kNPlus1;
                        BigDecimal pow = BigDecimal.valueOf(2).pow(encoderTableWithourPII.secParam_L);
                        BigDecimal p1 = getP1(lambdaMkv);
                        BigDecimal bottom = p1.multiply(pow);
                        BigDecimal top = new BigDecimal(encodedfirstMkv).subtract(new BigDecimal(kNPlus1));
                        BigInteger lowerBound =
                                new BigDecimal(kNPlus1).add(top.divide(bottom, 0, RoundingMode.FLOOR)
                                        .multiply(bottom)).toBigInteger();
                        BigInteger upperBound =
                                new BigDecimal(kNPlus1).add(top.divide(bottom, 0, RoundingMode.FLOOR).add(BigDecimal.valueOf(1))
                                        .multiply(bottom)).toBigInteger();
                        String randomStr = genRandomStr();
                        while (encoderTableWithourPII.encodeFirstMkvTable.containsKey(randomStr)) {
                            randomStr = genRandomStr();
                        }
                        EncodeLine<String> newRandomLine =
                                EncodeLine.<String>builder().lowerBound(lowerBound).upperBound(upperBound).originValue(randomStr).build();
                        encoderTableWithourPII.encodeFirstMkvTable.put(randomStr, newRandomLine);
                        writer.writeLine(String.valueOf(encoderTableWithourPII.encodeFirstMkvTable));
                        decodedPswd.append(randomStr);
                    } else {
                        decodedPswd.append(firstMkv);
                    }

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
                                encoderTableWithourPII.encodeEveryMkv_1Table.get(prefix);
                        if (encodeSuffixTable == null) {
                            suffix = findOriginValue(encodedSuffix, encoderTableWithourPII.absentMkv_1Table);
                        } else {
                            suffix = findOriginValue(encodedSuffix, encodeSuffixTable);
                        }
                        decodedPswd.append(suffix);
                    }
                    originPswd.add(decodedPswd.toString());
//                    System.out.println(originPswd);

                } else {
//                  1.Pr_Dr
                    BigInteger encodedPr_DR = new BigInteger(encodeElementList.get(1), 2);
                    Map<Pair<Integer, Boolean>, EncodeLine<Pair<Integer, Boolean>>> tmpPrDr = new HashMap<>();
                    tmpPrDr.put(new Pair<>(g, true), prDrEncodeLineMap.get(new Pair<>(g, true)));
                    tmpPrDr.put(new Pair<>(g, false), prDrEncodeLineMap.get(new Pair<>(g, false)));
                    Pair<Integer, Boolean> pr_dr = findOriginValue(encodedPr_DR, tmpPrDr);
                    if (pr_dr.getValue()) {
                        originPswd.add(originPswd.get(g - 1));
                    } else {
                        String baseString = originPswd.get(g - 1);
//                  2. pr_M
                        BigInteger encodedPr_M = new BigInteger(encodeElementList.get(2), 2);
                        String prM = findOriginValue(encodedPr_M, encoderTableWithourPII.prMTable);
                        switch (prM) {
                            case "pr_M_head": {
                                BigInteger encodedHeadOpType = new BigInteger(encodeElementList.get(3), 2);
                                String headOpType = findOriginValue(encodedHeadOpType,
                                        encoderTableWithourPII.prHOpTable);
                                switch (headOpType) {
                                    case "pr_H_insert": {
                                        BigInteger encodedHiTimes = new BigInteger(encodeElementList.get(4), 2);
                                        Integer hiTimes = findOriginValue(encodedHiTimes,
                                                encoderTableWithourPII.encodeHiTimesProbTable);
                                        for (int i = 1; i <= hiTimes; i++) {
                                            BigInteger encodedHiOp = new BigInteger(encodeElementList.get(4 + i), 2);
                                            String hiOp = findOriginValue(encodedHiOp,
                                                    encoderTableWithourPII.encodeHiOpProbTable);
                                            String parameter = hiOp.substring(3, 4);
                                            baseString = hi(baseString, parameter);
                                        }

                                        break;
                                    }
                                    case "pr_H_delete": {
                                        BigInteger encodedHdTimes = new BigInteger(encodeElementList.get(4), 2);
                                        Integer hdTimes = findOriginValue(encodedHdTimes,
                                                encoderTableWithourPII.encodeHdTimesProbTable);
                                        for (int i = 1; i <= hdTimes; i++) {
                                            BigInteger encodedHdOp = new BigInteger(encodeElementList.get(4 + i), 2);
                                            String hdOp = findOriginValue(encodedHdOp,
                                                    encoderTableWithourPII.encodeHdOpProbTable);
                                            String parameter = hdOp.substring(3, 4);
                                            baseString = hd(baseString, parameter);
                                        }
                                        break;
                                    }
                                    case "pr_H_deleteAndInsert": {
                                        BigInteger encodedHdTimes = new BigInteger(encodeElementList.get(4), 2);
                                        Integer hdTimes = findOriginValue(encodedHdTimes,
                                                encoderTableWithourPII.encodeHdTimesProbTable);
                                        BigInteger encodedHiTimes = new BigInteger(encodeElementList.get(5), 2);
                                        Integer hiTimes = findOriginValue(encodedHiTimes,
                                                encoderTableWithourPII.encodeHiTimesProbTable);
                                        for (int i = 1; i <= hdTimes; i++) {
                                            BigInteger encodedHdOp = new BigInteger(encodeElementList.get(5 + i), 2);
                                            String hdOp = findOriginValue(encodedHdOp,
                                                    encoderTableWithourPII.encodeHdOpProbTable);
                                            String parameter = hdOp.substring(3, 4);
                                            baseString = hd(baseString, parameter);
                                        }
                                        for (int i = 1; i <= hiTimes; i++) {
                                            BigInteger encodedHiOp =
                                                    new BigInteger(encodeElementList.get(5 + i + hdTimes), 2);
                                            String hiOp = findOriginValue(encodedHiOp,
                                                    encoderTableWithourPII.encodeHiOpProbTable);
                                            String parameter = hiOp.substring(3, 4);
                                            baseString = hi(baseString, parameter);
                                        }
                                        break;
                                    }
                                }
                                break;
                            }
                            case "pr_M_tail": {
                                BigInteger encodedTailOpType = new BigInteger(encodeElementList.get(3), 2);
                                String tailOpType = findOriginValue(encodedTailOpType,
                                        encoderTableWithourPII.prTOpTable);
                                switch (tailOpType) {
                                    case "pr_T_insert": {
                                        BigInteger encodedTiTimes = new BigInteger(encodeElementList.get(4), 2);
                                        Integer tiTimes = findOriginValue(encodedTiTimes,
                                                encoderTableWithourPII.encodeTiTimesProbTable);
                                        for (int i = 1; i <= tiTimes; i++) {
                                            BigInteger encodedTiOp = new BigInteger(encodeElementList.get(4 + i), 2);
                                            String tiOp = findOriginValue(encodedTiOp,
                                                    encoderTableWithourPII.encodeTiOpProbTable);
                                            String parameter = tiOp.substring(3, 4);
                                            baseString = ti(baseString, parameter);
                                        }

                                        break;
                                    }
                                    case "pr_T_delete": {
                                        BigInteger encodedTdTimes = new BigInteger(encodeElementList.get(4), 2);
                                        Integer tdTimes = findOriginValue(encodedTdTimes,
                                                encoderTableWithourPII.encodeTdTimesProbTable);
                                        for (int i = 1; i <= tdTimes; i++) {
                                            BigInteger encodedTdOp = new BigInteger(encodeElementList.get(4 + i), 2);
                                            String tdOp = findOriginValue(encodedTdOp,
                                                    encoderTableWithourPII.encodeTdOpProbTable);
                                            String parameter = tdOp.substring(3, 4);
                                            baseString = td(baseString, parameter);
                                        }
                                        break;
                                    }
                                    case "pr_T_deleteAndInsert": {
                                        BigInteger encodedTdTimes = new BigInteger(encodeElementList.get(4), 2);
                                        Integer tdTimes = findOriginValue(encodedTdTimes,
                                                encoderTableWithourPII.encodeTdTimesProbTable);
                                        BigInteger encodedTiTimes = new BigInteger(encodeElementList.get(5), 2);
                                        Integer tiTimes = findOriginValue(encodedTiTimes,
                                                encoderTableWithourPII.encodeTiTimesProbTable);
                                        for (int i = 1; i <= tdTimes; i++) {
                                            BigInteger encodedTdOp = new BigInteger(encodeElementList.get(5 + i), 2);
                                            String tdOp = findOriginValue(encodedTdOp,
                                                    encoderTableWithourPII.encodeTdOpProbTable);
                                            String parameter = tdOp.substring(3, 4);
                                            baseString = td(baseString, parameter);
                                        }
                                        for (int i = 1; i <= tiTimes; i++) {
                                            BigInteger encodedTiOp =
                                                    new BigInteger(encodeElementList.get(5 + i + tdTimes), 2);
                                            String tiOp = findOriginValue(encodedTiOp,
                                                    encoderTableWithourPII.encodeTiOpProbTable);
                                            String parameter = tiOp.substring(3, 4);
                                            baseString = ti(baseString, parameter);
                                        }
                                        break;
                                    }
                                }

                                break;
                            }
                            case "pr_M_headAndTail": {
                                BigInteger encodedHeadOpType = new BigInteger(encodeElementList.get(3), 2);
                                String headOpType = findOriginValue(encodedHeadOpType,
                                        encoderTableWithourPII.prHOpTable);
                                BigInteger encodedTailOpType = new BigInteger(encodeElementList.get(4), 2);
                                String tailOpType = findOriginValue(encodedTailOpType,
                                        encoderTableWithourPII.prTOpTable);


                                boolean hiOnly = headOpType.equals("pr_H_insert");
                                boolean hdOnly = headOpType.equals("pr_H_delete");
                                boolean hBoth = headOpType.equals("pr_H_deleteAndInsert");
                                boolean tiOnly = tailOpType.equals("pr_T_insert");
                                boolean tdOnly = tailOpType.equals("pr_T_delete");
                                boolean tBoth = tailOpType.equals("pr_T_deleteAndInsert");
                                if (hiOnly && tiOnly) {
                                    BigInteger encodedHiTimes = new BigInteger(encodeElementList.get(5), 2);
                                    BigInteger encodedTiTimes = new BigInteger(encodeElementList.get(6), 2);
                                    Integer hiTimes = findOriginValue(encodedHiTimes,
                                            encoderTableWithourPII.encodeHiTimesProbTable);
                                    Integer tiTimes = findOriginValue(encodedTiTimes,
                                            encoderTableWithourPII.encodeTiTimesProbTable);
                                    for (int i = 1; i <= hiTimes; i++) {
                                        BigInteger encodedHiOp = new BigInteger(encodeElementList.get(6 + i), 2);
                                        String hiOp = findOriginValue(encodedHiOp,
                                                encoderTableWithourPII.encodeHiOpProbTable);
                                        String parameter = hiOp.substring(3, 4);
                                        baseString = hi(baseString, parameter);
                                    }
                                    for (int i = 1; i <= tiTimes; i++) {
                                        BigInteger encodedTiOp =
                                                new BigInteger(encodeElementList.get(6 + i + hiTimes), 2);
                                        String tiOp = findOriginValue(encodedTiOp,
                                                encoderTableWithourPII.encodeTiOpProbTable);
                                        String parameter = tiOp.substring(3, 4);
                                        baseString = ti(baseString, parameter);
                                    }
                                }
                                if (hiOnly && tdOnly) {
                                    BigInteger encodedHiTimes = new BigInteger(encodeElementList.get(5), 2);
                                    BigInteger encodedTdTimes = new BigInteger(encodeElementList.get(6), 2);
                                    Integer hiTimes = findOriginValue(encodedHiTimes,
                                            encoderTableWithourPII.encodeHiTimesProbTable);
                                    Integer tdTimes = findOriginValue(encodedTdTimes,
                                            encoderTableWithourPII.encodeTdTimesProbTable);
                                    for (int i = 1; i <= tdTimes; i++) {
                                        BigInteger encodedTdOp = new BigInteger(encodeElementList.get(6 + i), 2);
                                        String tdOp = findOriginValue(encodedTdOp,
                                                encoderTableWithourPII.encodeTdOpProbTable);
                                        String parameter = tdOp.substring(3, 4);
                                        baseString = td(baseString, parameter);
                                    }
                                    for (int i = 1; i <= hiTimes; i++) {
                                        BigInteger encodedHiOp =
                                                new BigInteger(encodeElementList.get(6 + i + tdTimes), 2);
                                        String hiOp = findOriginValue(encodedHiOp,
                                                encoderTableWithourPII.encodeHiOpProbTable);
                                        String parameter = hiOp.substring(3, 4);
                                        baseString = hi(baseString, parameter);
                                    }

                                }
                                if (hiOnly && tBoth) {
                                    BigInteger encodedHiTimes = new BigInteger(encodeElementList.get(5), 2);
                                    Integer hiTimes = findOriginValue(encodedHiTimes,
                                            encoderTableWithourPII.encodeHiTimesProbTable);
                                    BigInteger encodedTdTimes = new BigInteger(encodeElementList.get(6), 2);
                                    Integer tdTimes = findOriginValue(encodedTdTimes,
                                            encoderTableWithourPII.encodeTdTimesProbTable);
                                    BigInteger encodedTiTimes = new BigInteger(encodeElementList.get(7), 2);
                                    Integer tiTimes = findOriginValue(encodedTiTimes,
                                            encoderTableWithourPII.encodeTiTimesProbTable);
                                    for (int i = 1; i <= hiTimes; i++) {
                                        BigInteger encodedHiOp = new BigInteger(encodeElementList.get(7 + i), 2);
                                        String hiOp = findOriginValue(encodedHiOp,
                                                encoderTableWithourPII.encodeHiOpProbTable);
                                        String parameter = hiOp.substring(3, 4);
                                        baseString = hi(baseString, parameter);
                                    }
                                    for (int i = 1; i <= tdTimes; i++) {
                                        BigInteger encodedTdOp =
                                                new BigInteger(encodeElementList.get(7 + i + hiTimes), 2);
                                        String tdOp = findOriginValue(encodedTdOp,
                                                encoderTableWithourPII.encodeTdOpProbTable);
                                        String parameter = tdOp.substring(3, 4);
                                        baseString = td(baseString, parameter);
                                    }
                                    for (int i = 1; i <= tiTimes; i++) {
                                        BigInteger encodedTiOp =
                                                new BigInteger(encodeElementList.get(7 + i + hiTimes + tdTimes), 2);
                                        String tiOp = findOriginValue(encodedTiOp,
                                                encoderTableWithourPII.encodeTiOpProbTable);
                                        String parameter = tiOp.substring(3, 4);
                                        baseString = ti(baseString, parameter);
                                    }
                                }
                                if (hdOnly && tiOnly) {
                                    BigInteger encodedHdTimes = new BigInteger(encodeElementList.get(5), 2);
                                    BigInteger encodedTiTimes = new BigInteger(encodeElementList.get(6), 2);
                                    Integer hdTimes = findOriginValue(encodedHdTimes,
                                            encoderTableWithourPII.encodeHdTimesProbTable);
                                    Integer tiTimes = findOriginValue(encodedTiTimes,
                                            encoderTableWithourPII.encodeTiTimesProbTable);
                                    for (int i = 1; i <= hdTimes; i++) {
                                        BigInteger encodedHdOp = new BigInteger(encodeElementList.get(6 + i), 2);
                                        String hddOp = findOriginValue(encodedHdOp,
                                                encoderTableWithourPII.encodeHdOpProbTable);
                                        String parameter = hddOp.substring(3, 4);
                                        baseString = hd(baseString, parameter);
                                    }
                                    for (int i = 1; i <= tiTimes; i++) {
                                        BigInteger encodedTiOp =
                                                new BigInteger(encodeElementList.get(6 + i + hdTimes), 2);
                                        String tiOp = findOriginValue(encodedTiOp,
                                                encoderTableWithourPII.encodeTiOpProbTable);
                                        String parameter = tiOp.substring(3, 4);
                                        baseString = ti(baseString, parameter);
                                    }
                                }
                                if (hdOnly && tdOnly) {
                                    BigInteger encodedHdTimes = new BigInteger(encodeElementList.get(5), 2);
                                    Integer hdTimes = findOriginValue(encodedHdTimes,
                                            encoderTableWithourPII.encodeHdTimesProbTable);
                                    BigInteger encodedTdTimes = new BigInteger(encodeElementList.get(6), 2);
                                    Integer tdTimes = findOriginValue(encodedTdTimes,
                                            encoderTableWithourPII.encodeTdTimesProbTable);

                                    for (int i = 1; i <= hdTimes; i++) {
                                        BigInteger encodedHdOp = new BigInteger(encodeElementList.get(6 + i), 2);
                                        String hddOp = findOriginValue(encodedHdOp,
                                                encoderTableWithourPII.encodeHdOpProbTable);
                                        String parameter = hddOp.substring(3, 4);
                                        baseString = hd(baseString, parameter);
                                    }
                                    for (int i = 1; i <= tdTimes; i++) {
                                        BigInteger encodedTdOp =
                                                new BigInteger(encodeElementList.get(6 + i + hdTimes), 2);
                                        String tdOp = findOriginValue(encodedTdOp,
                                                encoderTableWithourPII.encodeTdOpProbTable);
                                        String parameter = tdOp.substring(3, 4);
                                        baseString = td(baseString, parameter);
                                    }
                                }
                                if (hdOnly && tBoth) {
                                    BigInteger encodedHdTimes = new BigInteger(encodeElementList.get(5), 2);
                                    Integer hdTimes = findOriginValue(encodedHdTimes,
                                            encoderTableWithourPII.encodeHdTimesProbTable);
                                    BigInteger encodedTdTimes = new BigInteger(encodeElementList.get(6), 2);
                                    Integer tdTimes = findOriginValue(encodedTdTimes,
                                            encoderTableWithourPII.encodeTdTimesProbTable);
                                    BigInteger encodedTiTimes = new BigInteger(encodeElementList.get(7), 2);
                                    Integer tiTimes = findOriginValue(encodedTiTimes,
                                            encoderTableWithourPII.encodeTiTimesProbTable);
                                    for (int i = 1; i <= hdTimes; i++) {
                                        BigInteger encodedHdOp = new BigInteger(encodeElementList.get(7 + i), 2);
                                        String hddOp = findOriginValue(encodedHdOp,
                                                encoderTableWithourPII.encodeHdOpProbTable);
                                        String parameter = hddOp.substring(3, 4);
                                        baseString = hd(baseString, parameter);
                                    }
                                    for (int i = 1; i <= tdTimes; i++) {
                                        BigInteger encodedTdOp =
                                                new BigInteger(encodeElementList.get(7 + i + hdTimes), 2);
                                        String tdOp = findOriginValue(encodedTdOp,
                                                encoderTableWithourPII.encodeTdOpProbTable);
                                        String parameter = tdOp.substring(3, 4);
                                        baseString = td(baseString, parameter);
                                    }
                                    for (int i = 1; i <= tiTimes; i++) {
                                        BigInteger encodedTiOp =
                                                new BigInteger(encodeElementList.get(7 + i + hdTimes + tdTimes), 2);
                                        String tiOp = findOriginValue(encodedTiOp,
                                                encoderTableWithourPII.encodeTiOpProbTable);
                                        String parameter = tiOp.substring(3, 4);
                                        baseString = ti(baseString, parameter);
                                    }
                                }
                                if (hBoth && tiOnly) {
                                    BigInteger encodedHdTimes = new BigInteger(encodeElementList.get(5), 2);
                                    Integer hdTimes = findOriginValue(encodedHdTimes,
                                            encoderTableWithourPII.encodeHdTimesProbTable);
                                    BigInteger encodedHiTimes = new BigInteger(encodeElementList.get(6), 2);
                                    Integer hiTimes = findOriginValue(encodedHiTimes,
                                            encoderTableWithourPII.encodeHiTimesProbTable);
                                    BigInteger encodedTiTimes = new BigInteger(encodeElementList.get(7), 2);
                                    Integer tiTimes = findOriginValue(encodedTiTimes,
                                            encoderTableWithourPII.encodeTiTimesProbTable);
                                    for (int i = 1; i <= hdTimes; i++) {
                                        BigInteger encodedHdOp = new BigInteger(encodeElementList.get(7 + i), 2);
                                        String hdOp = findOriginValue(encodedHdOp,
                                                encoderTableWithourPII.encodeHdOpProbTable);
                                        String parameter = hdOp.substring(3, 4);
                                        baseString = hd(baseString, parameter);
                                    }
                                    for (int i = 1; i <= hiTimes; i++) {
                                        BigInteger encodedHiOp =
                                                new BigInteger(encodeElementList.get(7 + i + hdTimes), 2);
                                        String hiOp = findOriginValue(encodedHiOp,
                                                encoderTableWithourPII.encodeHiOpProbTable);
                                        String parameter = hiOp.substring(3, 4);
                                        baseString = hi(baseString, parameter);
                                    }
                                    for (int i = 1; i <= tiTimes; i++) {
                                        BigInteger encodedTiOp =
                                                new BigInteger(encodeElementList.get(7 + i + hdTimes + hiTimes), 2);
                                        String tiOp = findOriginValue(encodedTiOp,
                                                encoderTableWithourPII.encodeTiOpProbTable);
                                        String parameter = tiOp.substring(3, 4);
                                        baseString = ti(baseString, parameter);
                                    }
                                }
                                if (hBoth && tdOnly) {
                                    BigInteger encodedHdTimes = new BigInteger(encodeElementList.get(5), 2);
                                    Integer hdTimes = findOriginValue(encodedHdTimes,
                                            encoderTableWithourPII.encodeHdTimesProbTable);
                                    BigInteger encodedHiTimes = new BigInteger(encodeElementList.get(6), 2);
                                    Integer hiTimes = findOriginValue(encodedHiTimes,
                                            encoderTableWithourPII.encodeHiTimesProbTable);
                                    BigInteger encodedTdTimes = new BigInteger(encodeElementList.get(7), 2);
                                    Integer tdTimes = findOriginValue(encodedTdTimes,
                                            encoderTableWithourPII.encodeTdTimesProbTable);
                                    for (int i = 1; i <= hdTimes; i++) {
                                        BigInteger encodedHdOp = new BigInteger(encodeElementList.get(7 + i), 2);
                                        String hdOp = findOriginValue(encodedHdOp,
                                                encoderTableWithourPII.encodeHdOpProbTable);
                                        String parameter = hdOp.substring(3, 4);
                                        baseString = hd(baseString, parameter);
                                    }
                                    for (int i = 1; i <= hiTimes; i++) {
                                        BigInteger encodedHiOp =
                                                new BigInteger(encodeElementList.get(7 + i + hdTimes), 2);
                                        String hiOp = findOriginValue(encodedHiOp,
                                                encoderTableWithourPII.encodeHiOpProbTable);
                                        String parameter = hiOp.substring(3, 4);
                                        baseString = hi(baseString, parameter);
                                    }
                                    for (int i = 1; i <= tdTimes; i++) {
                                        BigInteger encodedTdOp =
                                                new BigInteger(encodeElementList.get(7 + i + hdTimes + hiTimes), 2);
                                        String tdOp = findOriginValue(encodedTdOp,
                                                encoderTableWithourPII.encodeTdOpProbTable);
                                        String parameter = tdOp.substring(3, 4);
                                        baseString = td(baseString, parameter);
                                    }
                                }
                                if (hBoth && tBoth) {
                                    BigInteger encodedHdTimes = new BigInteger(encodeElementList.get(5), 2);
                                    Integer hdTimes = findOriginValue(encodedHdTimes,
                                            encoderTableWithourPII.encodeHdTimesProbTable);
                                    BigInteger encodedHiTimes = new BigInteger(encodeElementList.get(6), 2);
                                    Integer hiTimes = findOriginValue(encodedHiTimes,
                                            encoderTableWithourPII.encodeHiTimesProbTable);
                                    BigInteger encodedTdTimes = new BigInteger(encodeElementList.get(7), 2);
                                    Integer tdTimes = findOriginValue(encodedTdTimes,
                                            encoderTableWithourPII.encodeTdTimesProbTable);
                                    BigInteger encodedTiTimes = new BigInteger(encodeElementList.get(8), 2);
                                    Integer tiTimes = findOriginValue(encodedTiTimes,
                                            encoderTableWithourPII.encodeTiTimesProbTable);
                                    for (int i = 1; i <= hdTimes; i++) {
                                        BigInteger encodedHdOp = new BigInteger(encodeElementList.get(8 + i), 2);
                                        String hdOp = findOriginValue(encodedHdOp,
                                                encoderTableWithourPII.encodeHdOpProbTable);
                                        String parameter = hdOp.substring(3, 4);
                                        baseString = hd(baseString, parameter);
                                    }
                                    for (int i = 1; i <= hiTimes; i++) {
                                        BigInteger encodedHiOp =
                                                new BigInteger(encodeElementList.get(8 + i + hdTimes), 2);
                                        String hiOp = findOriginValue(encodedHiOp,
                                                encoderTableWithourPII.encodeHiOpProbTable);
                                        String parameter = hiOp.substring(3, 4);
                                        baseString = hi(baseString, parameter);
                                    }
                                    for (int i = 1; i <= tdTimes; i++) {
                                        BigInteger encodedTdOp =
                                                new BigInteger(encodeElementList.get(8 + i + hdTimes + hiTimes), 2);
                                        String tdOp = findOriginValue(encodedTdOp,
                                                encoderTableWithourPII.encodeTdOpProbTable);
                                        String parameter = tdOp.substring(3, 4);
                                        baseString = td(baseString, parameter);
                                    }
                                    for (int i = 1; i <= tiTimes; i++) {
                                        BigInteger encodedTiOp =
                                                new BigInteger(encodeElementList.get(8 + i + hdTimes + hiTimes + tdTimes)
                                                        , 2);
                                        String tiOp = findOriginValue(encodedTiOp,
                                                encoderTableWithourPII.encodeTiOpProbTable);
                                        String parameter = tiOp.substring(3, 4);
                                        baseString = ti(baseString, parameter);
                                    }
                                }
                                break;
                            }
                        }

//              加入到已解码列表中
                        decodedPswd.append(baseString);
                        originPswd.add(decodedPswd.toString());
//                        System.out.println(originPswd);
                    }
                }


            }


        }
        return originPswd;

    }

    private String genRandomStr() {
        List<String> candidateList = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c",
                "d", "e",
                "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                "U", "V", "W", "X", "Y", "Z", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+",
                ",", "-", ".", "/", ";", ":", "<", "=", ">", "?",
                "@", "[", "\\", "]", "^", "_", "`", "{", "|", "}", "~", " ");
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            int i1 = RandomUtil.randomInt(0, 94);
            s.append(candidateList.get(i1));
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


    static BigInteger getRandomValue(BigInteger lower, BigInteger upper) {
        double seed = Math.random();
        BigDecimal lowerBound = new BigDecimal(lower);
        BigDecimal upperBound = new BigDecimal(upper);
        BigDecimal randomValue = upperBound.subtract(lowerBound).multiply(BigDecimal.valueOf(seed)).add(lowerBound);
        return randomValue.toBigInteger();
    }

    private BigDecimal getP1(double lambda) {
        BigDecimal p1;
        BigDecimal topP1 = new BigDecimal(lambda);
        BigDecimal listNumber = BigDecimal.valueOf(95).pow(5);
        BigDecimal bottomP1 =
                new BigDecimal(encoderTableWithourPII.originFirstMkvSize).add(listNumber.multiply(BigDecimal.valueOf(lambda)));
        p1 = topP1.divide(bottomP1, 40, RoundingMode.FLOOR);
        return p1;
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
