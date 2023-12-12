package com.example.honeyvault.tool;

import cn.hutool.core.lang.Pair;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class CalPath {

    public static void main(String[] args) {
//        String source = "!@#1bqweag";
//        JSONArray testStr = JSONUtil.parseArray("[\"!@#1bqweag\", \"#1bqweag\",\"!@#1bqwe\", \"#1bqwe\"," +
//                "\"!@#1bqweag\",\"2,!@#1bqweag\",\"!@#1bqweag2,\",\"12!@#1bqweag2,\",\"12!@#1bqwe\",\"#1bqweag2,\"," +
//                "\"##1bqweag\",\"##1bqweagg\",\"##1bqwea\",\"##1bqweaa\",\"!@#1bqwe##\",\"#1bqwe##\",
//                \"#@#1bqwe##\"," +
//                "\"1#1bqwe##\"]");
////        List<String> testStr = Arrays.asList("2,!@#1bqweag");
//        for (Object t : testStr) {
//            String target = t.toString();
////            String target = "a";
//            List<List<String>> lists = breadthFirstSearch(source, target);
//            Set<List<String>> opSet = new HashSet<>(lists);
//
//            // 输出所有编辑路径
//            System.out.println("所有编辑路径：");
//            for (List<String> path : opSet) {
//                System.out.println(source+"->"+target);
//                System.out.println(path.toString());
//            }
//        }

        String source = "2,!@#1bqweag";
        String target = "!@#1bqwe##";
        List<List<String>> lists = breadthFirstSearch(source, target);
        lists.forEach(System.out::println);

    }


    public static List<List<String>> dfs(List<String> list, String source, String target, List<String> opList,
                                         int flag) {
        List<List<String>> paths = new LinkedList<>();

        if (source.equals(target)) {
            paths.add(new LinkedList<>(opList)); // 返回当前路径列表的副本
            return paths;
        }
        List<String> l;
        if (flag == 1) {
            l = List.of("hd", "td");
        } else {
            l = List.of("hi", "ti");
        }
        for (String op : l) {
            List<String> newOpList = new LinkedList<>(opList);
            Pair<String, String> opRes = new Pair<>("", "");
            // 执行操作
            switch (op) {
                case "hd":
                    opRes = hd(list, source, target);
                    break;
                case "td":
                    opRes = td(list, source, target);
                    break;
                case "hi":
                    opRes = hi(list, source, target);
                    break;
                case "ti":
                    opRes = ti(list, source, target);
                    break;
            }

            String param = opRes.getValue();
            if (!opRes.getKey().equals(source) && param != null) {
                newOpList.add(op + "(" + param + ")"); // 添加操作到路径列表
                List<List<String>> subPaths = dfs(list, opRes.getKey(), target, newOpList, flag);
                paths.addAll(subPaths);
            }
        }
        return paths;
    }


    public static double ManhattanDistance(String str1, String str2) {
        Set<Character> s3 = new LinkedHashSet<>();
        char[] chars1 = str1.toCharArray();
        char[] chars2 = str2.toCharArray();
        for (char c : chars1) {
            s3.add(c);
        }
        for (char c : chars2) {
            s3.add(c);
        }
        Function<String, Map<Character, Long>> calculateFrequency = str -> str.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<Character, Long> freq1 = calculateFrequency.apply(str1);
        long t1 = str1.length();
        Map<Character, Long> freq2 = calculateFrequency.apply(str2);
        long t2 = str2.length();

        Map<Character, Double> freqMap1 = freq1.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (double) entry.getValue() / t1
                ));
        Map<Character, Double> freqMap2 = freq2.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (double) entry.getValue() / t2
                ));
        List<Double> vec1 = new LinkedList<>();
        List<Double> vec2 = new LinkedList<>();
        s3.forEach(c -> {
            vec1.add(freqMap1.getOrDefault(c, 0.0));
            vec2.add(freqMap2.getOrDefault(c, 0.0));
        });
        return calculateEuclideanDistance(vec1, vec2);

    }

    private static int lsDist(String str1, String str2) {
        int m = str1.length();
        int n = str2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }

        return dp[m][n];
    }

    public static double calculateEuclideanDistance(List<Double> vec1, List<Double> vec2) {
        if (vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("Vectors must have the same dimensions");
        }

        double sumOfSquares = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            double diff = vec1.get(i) - vec2.get(i);
            sumOfSquares += diff * diff;
        }

        return Math.sqrt(sumOfSquares);
    }

    private static String ifOpValid(String source, String target, String newStr) {

        int d3 = lsDist(newStr, target);
        int d4 = lsDist(source, target);
        if (source.length() > newStr.length()) {
            double d1 = ManhattanDistance(newStr, target);
            double d2 = ManhattanDistance(source, target);
            if (((d1 < d2) && (d3 <= d4)) || ((d3 < d4))) {
                return newStr;
            }
        } else {
            if ((d3 < d4) && LongestComSubstr(newStr, target).length() > LongestComSubstr(source, target).length()) {
                return newStr;
            }
        }

        return null;
    }


    public static Pair<String, String> hi(List<String> listCandidates, String source, String target) {
        for (String candidate : listCandidates) {
            String newStr = candidate + source;
            newStr = ifOpValid(source, target, newStr);
            if (newStr != null) return new Pair<>(newStr, candidate);
        }
        return new Pair<>(source, null);
    }


    public static Pair<String, String> ti(List<String> listCandidates, String source, String target) {
        for (String ch : listCandidates) {
            String newStr = source + ch;
            newStr = ifOpValid(source, target, newStr);
            if (newStr != null) return new Pair<>(newStr, ch);
        }
        return new Pair<>(source, null);
    }

    public static Pair<String, String> hd(List<String> listCandidates, String source, String target) {
        if (!source.isEmpty()) {
            String newStr = source;
            for (String candidate : listCandidates) {
                if (source.startsWith(candidate)) {
                    assert newStr != null;
                    newStr = newStr.substring(candidate.length());
                    newStr = ifOpValid(source, target, newStr);
                    if (newStr != null) return new Pair<>(newStr, candidate);
                }
            }
        }
        return new Pair<>(source, null);
    }

    public static Pair<String, String> td(List<String> listCandidates, String source, String target) {
        if (!source.isEmpty()) {
            String newStr = source;
            for (String candidate : listCandidates) {
                if (source.endsWith(candidate)) {
                    assert newStr != null;
                    newStr = newStr.substring(0, source.length() - candidate.length());
                    newStr = ifOpValid(source, target, newStr);
                    if (newStr != null) return new Pair<>(newStr, candidate);
                }
            }
        }
        return new Pair<>(source, null);

    }

    public static String LongestComSubstr(String str1, String str2) {
        int m = str1.length();
        int n = str2.length();

        int[][] dp = new int[m + 1][n + 1];
        int maxLength = 0;
        int endIndex = 0;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    if (dp[i][j] > maxLength) {
                        maxLength = dp[i][j];
                        endIndex = i - 1;
                    }
                } else {
                    dp[i][j] = 0;
                }
            }
        }

        if (maxLength > 0) {
            return str1.substring(endIndex - maxLength + 1, endIndex + 1);
        } else {
            return "";
        }
    }

    public static List<String> initCandidate(String source, String target) {
        List<String> set = Arrays.asList("Α", "τ", "Β", "Γ", "Δ", "σ", "Ε", "Ζ", "Η", "ρ",
                "Θ", "Ι", "Κ", "Λ", "Μ", "Ν", "Ξ", "Ο", "Π", "Ρ",
                "Φ", "Χ", "Ψ",
                "Ω", "ω", "ψ",
                "χ", "φ", "υ", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e",
                "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                "U", "V", "W", "X", "Y", "Z", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+",
                ",", "-", ".", "/", ";", ":", "<", "=", ">", "?",
                "@", "[", "\\", "]", "^", "_", "`", "{", "|", "}", "~", " ");
        List<String> candidateList = new ArrayList<>();
        for (String s : set) {
            if (source.contains(s) || target.contains(s)) {
                candidateList.add(s);
            }
        }
        return candidateList;
    }

    public static int countOccurrencesOfOp(String p, String op) {
        int count = 0;
        int index = p.indexOf(op);

        while (index != -1) {
            count++;
            index = p.indexOf(op, index + 2); // 从上一个匹配的位置的下一个字符开始查找
        }

        return count;
    }


    public static List<List<String>> breadthFirstSearch(String source, String target) {
        List<String> candidateList = initCandidate(source, target);

        String comStr = LongestComSubstr(source, target);
        List<List<String>> opList1 = dfs(candidateList, source, comStr, new LinkedList<>(), 1);
        List<List<String>> opList2 = dfs(candidateList, comStr, target, new LinkedList<>(), 0);
        List<List<String>> finalList = new LinkedList<>();
        for (List<String> strings : opList1) {
            for (List<String> list : opList2) {
                List<String> temp = new LinkedList<>(strings);
                temp.addAll(list);
                finalList.add(temp);
            }

        }
        List<List<String>> opList = finalList;
        Map<String, Integer> priorityMap = new HashMap<>();
        priorityMap.put("hd", 1);
        priorityMap.put("td", 2);
        priorityMap.put("hi", 3);
        priorityMap.put("ti", 4);

        // 按照优先级排序
        opList.parallelStream().forEach(operations -> operations.sort(Comparator.comparingInt(s -> {
            String op = s.substring(0, 2); // 获取操作类型
            return priorityMap.getOrDefault(op, Integer.MAX_VALUE); // 根据优先级排序
        })));
        Set<List<String>> set = new HashSet<>(opList);
        opList.clear();
        opList.addAll(set);
        return opList;
    }


}
