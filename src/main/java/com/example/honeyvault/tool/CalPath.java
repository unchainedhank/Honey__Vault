package com.example.honeyvault.tool;

import cn.hutool.core.lang.Pair;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Component
public class CalPath {

    public static void main(String[] args) {
//        List<String> strings = breadthFirstSearch3("2abec", "1abab", 100);
//        System.out.println(strings.toString());


    }

    public static int editDistance(String str1, String str2) {
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

    public static String hi(List<String> listCandidates, String source, String target) {
        for (String ch : listCandidates) {
            String newStr = ch + source;
            if (editDistance(newStr, target) < editDistance(source, target)) {
                return newStr;
            }
        }
        return source;
    }

    public static String ti(List<String> listCandidates, String source, String target) {
        for (String ch : listCandidates) {
            String newStr = source + ch;
            if (editDistance(newStr, target) < editDistance(source, target)) {
                return newStr;
            }
        }
        return source;
    }

    public static String hd(List<String> listCandidates, String source, String target) {
        if (!source.isEmpty()) {
            String newStr = source;
            for (String candidate : listCandidates) {
                if (source.startsWith(candidate)) {
                    String tmp = newStr;
                    newStr = newStr.substring(candidate.length());
                    if (editDistance(newStr, target) < editDistance(source, target)) {
                        return newStr;
                    } else newStr = tmp;
                }
            }

        }
        return source;
    }

    public static String td(List<String> listCandidates, String source, String target) {
        if (!source.isEmpty()) {
            String newStr = source;
            for (String candidate : listCandidates) {
                if (source.endsWith(candidate)) {
                    String tmp = newStr;
                    newStr = newStr.substring(0, source.length() - candidate.length());
                    if (editDistance(newStr, target) < editDistance(source, target)) {
                        return newStr;
                    } else {
                        newStr = tmp;
                    }
                }
            }
        }
        return source;
    }

//    public static List<String> breadthFirstSearch2(String source, String target, int limit) {
//        List<String> listCandidates = initCandidate(source, target);
//
//        List<String> queue = new ArrayList<>();
//        String newStr = source;
//        while (!newStr.equals(target) && limit > 0) {
//            String temp = newStr;
//            newStr = hi(listCandidates, newStr, target);
//            if (!newStr.equals(temp)) {
//                queue.add("hi(" + diff(temp, newStr) + ")");
//            } else {
//                temp = newStr;
//                newStr = ti(listCandidates, newStr, target);
//                if (!newStr.equals(temp)) {
//                    queue.add("ti(" + diff(temp, newStr) + ")");
//                } else {
//                    temp = newStr;
//                    newStr = hd(listCandidates, newStr, target);
//                    if (!newStr.equals(temp)) {
//                        String diff = diff(temp, newStr);
//                        queue.add("hd(" + diff + ")");
//                    } else {
//                        temp = newStr;
//                        newStr = td(listCandidates, newStr, target);
//                        if (!newStr.equals(temp)) {
//                            queue.add("td(" + diff(temp, newStr) + ")");
//                        } else {
//                            return queue;
//                        }
//                    }
//                }
//            }
//            limit--;
//        }
//        return queue;
//    }

    public static String diff(String temp, String newStr) {
        String longestCommonSubstring = findLongestCommonSubstring(temp, newStr);
        temp = temp.replace(longestCommonSubstring, "");
        newStr = newStr.replace(longestCommonSubstring, "");
        if (temp.length() == 0) return newStr;
        else return temp;
    }

    public static String findLongestCommonSubstring(String str1, String str2) {
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
        Set<String> set = new HashSet<>(Arrays.asList("N1", "N2", "N3", "N4", "N5", "N6", "N7",
                "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "B10",
                "A1", "A2", "A3",
                "E1", "E2", "E3",
                "P1", "P2", "P3",
                "I1", "I2", "I3"));
        List<String> candidateList = new ArrayList<>();
        for (String s :
                set) {
            if (source.contains(s) || target.contains(s)) {
                candidateList.add(s);
            }
        }
        candidateList.addAll(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e",
                "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                "U", "V", "W", "X", "Y", "Z"));
        candidateList.addAll(Arrays.asList("!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+",
                ",", "-", ".", "/", ";", ":", "<", "=", ">", "?",
                "@", "[", "\\", "]", "^", "_", "`", "{", "|", "}", "~", " "));
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
//        source = source.trim();
//        target = target.trim();
        List<String> listCandidates = initCandidate(source, target);

        Queue<Pair<String, List<String>>> queue = new LinkedList<>();
        List<List<String>> methods = new ArrayList<>();
        queue.add(new Pair<>(source, new ArrayList<>()));

        while (!queue.isEmpty()) {
            Pair<String, List<String>> pair = queue.poll();
            String currStr = pair.getKey();
            List<String> path = pair.getValue();

            if (currStr.equals(target)) {
                methods.add(new ArrayList<>(path));
                if (methods.size() > 5) {
                    List<List<String>> result = new ArrayList<>();
                    methods.forEach(l -> result.add(l.stream()
                            .sorted(new CustomComparator())
                            .collect(Collectors.toList()))
                    );
                    return result.stream().distinct().collect(Collectors.toList());
                }

            } else {
                String newStr = hi(listCandidates, currStr, target);
                if (!newStr.equals(currStr)) {
                    List<String> newPath = new ArrayList<>(path);
                    newPath.add("hi(" + diff(currStr, newStr) + ")");
                    queue.add(new Pair<>(newStr, newPath));
                }
                newStr = ti(listCandidates, currStr, target);
                if (!newStr.equals(currStr)) {
                    List<String> newPath = new ArrayList<>(path);
                    newPath.add("ti(" + diff(currStr, newStr) + ")");
                    queue.add(new Pair<>(newStr, newPath));
                }
                newStr = hd(listCandidates, currStr, target);
                if (!newStr.equals(currStr)) {
                    List<String> newPath = new ArrayList<>(path);
                    newPath.add("hd(" + diff(currStr, newStr) + ")");
                    queue.add(new Pair<>(newStr, newPath));
                }
                newStr = td(listCandidates, currStr, target);
                if (!newStr.equals(currStr)) {
                    List<String> newPath = new ArrayList<>(path);
                    newPath.add("td(" + diff(currStr, newStr) + ")");
                    queue.add(new Pair<>(newStr, newPath));
                }
            }
        }


        List<List<String>> result = new ArrayList<>();
        methods.forEach(l -> result.add(l.stream()
                .sorted(new CustomComparator())
                .collect(Collectors.toList()))
        );

        return result.stream().distinct().collect(Collectors.toList());
    }


    static class CustomComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            return getPriority(s1) - getPriority(s2);
        }

        private int getPriority(String s) {
            if (s.startsWith("hd")) {
                return 1;
            } else if (s.startsWith("td")) {
                return 2;
            } else if (s.startsWith("hi")) {
                return 3;
            } else if (s.startsWith("ti")) {
                return 4;
            } else {
                return 0; // 如果出现了不认识的运算符，可以根据需要进行处理
            }
        }
    }
}
