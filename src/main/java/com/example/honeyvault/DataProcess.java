//package com.example.honeyvault;
//
//import net.sourceforge.pinyin4j.PinyinHelper;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.annotation.Resource;
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//
//@RestController
//public class DataProcess {
//    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//
//
////    @Resource
////    private PreProcess preProcess;
//
////    @Resource
////    private CalPath calPath;
//
////    @Resource
////    private PathRepo pathRepo;
////    @Resource
////    private DaoCN daoCN;
////    @Resource
////    private Jdbc jdbc;
//
////    private Set<String> collect;
//
////    @GetMapping(value = "/hello")
////    public String main() {
////        ExecutorService executorService = Executors.newFixedThreadPool(3);  // 设置线程池大小
////        try {
////            // 读取txt文件并插入数据
////            String filePath = "/Users/a3/Downloads/dataset/www.csdn.net.txt";
////            List<String> email = jdbc.get12306Email();
////            collect = new HashSet<>(email);
////            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
////                String line;
////                while ((line = reader.readLine()) != null) {
////                    String finalLine = line;
////                    executorService.submit(() -> processCNLine(finalLine));
////                }
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
////            return "success";
////        } catch (Exception e) {
////            e.printStackTrace();
////
////        }
////        return "success_CN";
////    }
//
//    public void processCNLine(String line) {
//        String[] data = line.split("#");
//        try {
//            if (data.length == 3 && collect.contains(data[2].trim())) {
//                daoCN.save(Csdn.builder().userName_CN(data[0].trim()).passwd_CN(data[1].trim()).email_CN(data[2].trim()).build());
//            }
//        } catch (Exception e) {
//            System.out.println(Arrays.toString(data));
//            e.printStackTrace();
//        }
//
//    }
//
//
////    @GetMapping(value = "/path")
////    public String StringPath() {
////        jdbc.setPathNull();
////        List<PasswdPath> all = pathRepo.findAll();
////        System.out.println(all.size());
////
////
//////        String pythonPath = "src/main/resources/calculate_path.py";
////        for (PasswdPath c : all) {
////            try {
//////            PasswdPath c = PasswdPath.builder().passwd_CN("lj2010350").password12306("2010350").email_CN(
//////                    "ads9980@126.com").user_name("ads9980").real_name("刘健").id12306("330328199003154612").phone(
//////                            "18626355218").build();
////
////                String passwd_cn = c.getPasswd_CN();
////                String password12306 = c.getPassword12306();
////
////                String birth = "";
////                if (c.getId12306().length() == 18) {
////                    birth = c.getId12306().substring(6, 14);
////                } else {
////                    continue;
////                }
////
////
////                LocalDate birthday = LocalDate.parse(birth, formatter);
////                String year = Integer.toString(birthday.getYear());
////                String day = String.format("%02d", birthday.getDayOfMonth());
////                String month = String.format("%02d", birthday.getMonthValue());
////
////                Map<String, String> nameMap = preProcess.preName(c.getReal_name());
////                Map<String, String> userMap = preProcess.preAccountName(c.getUser_name());
////                Map<String, String> emailMap = preProcess.preEmail(c.getEmail_CN());
////                Map<String, String> phoneMap = preProcess.prePhone(c.getPhone());
////                Map<String, String> birthMap = preProcess.preBirth(year, month, day);
////                Map<String, String> idCardMap = preProcess.preIdCard(c.getId12306());
////
////                List<Map<String, String>> maps = new ArrayList<>();
////                maps.add(nameMap);
////                maps.add(birthMap);
////                maps.add(userMap);
////                maps.add(emailMap);
////                maps.add(phoneMap);
////                maps.add(idCardMap);
////
////                for (Map<String, String> m : maps) {
////                    passwd_cn = replaceValues(passwd_cn, m);
////                    password12306 = replaceValues(password12306, m);
////                }
////
////
////                if (passwd_cn != null && password12306 != null) {
////                    double similarity1 = calculateJaccardSimilarity(passwd_cn, password12306);
////                    double similarity2 = getLevenshteinSim(passwd_cn, password12306);
////                    if (similarity1 > 0.3 || similarity2 > 0.3) {
//////                        String[] arguments = new String[]{"python3", pythonPath, password12306, passwd_cn};
//////                        String path = invokePython(arguments);
////                        List<String> path = calPath.breadthFirstSearch2(passwd_cn, password12306, 30);
////                        if (path.size() <= 255) {
////                            if (!path.toString().equals("[]")) {
////                                System.out.println(c);
////                                System.out.println(path);
////                            }
////                            c.setPath(path.toString());
////                        }
////
////                        pathRepo.save(c);
////                    }
////                }
////            } catch (Exception e) {
////                System.out.println(c);
////            }
////
////        }
////        return "done";
////    }
//
////    @GetMapping(value = "/sta")
////    public String statistic() {
////        Map<String, Integer> map = new HashMap<>();
////        List<PasswdPath> all = pathRepo.findAll();
////        int count = 0;
////        for (PasswdPath p :
////                all) {
////            String path = p.getPath();
////            if (path != null) {
////                count++;
////
//////                if (path.contains("ti")) map.merge("ti", 1, Integer::sum);
//////                if (path.contains("hi")) map.merge("hi", 1, Integer::sum);
//////                if (path.contains("td")) map.merge("td", 1, Integer::sum);
//////                if (path.contains("hd")) map.merge("hd", 1, Integer::sum);
////            }
////        }
////        return "";
////    }
//
//    public static int countOccurrencesOfOp(String p,String op) {
//        int count = 0;
//        int index = p.indexOf(op);
//
//        while (index != -1) {
//            count++;
//            index = p.indexOf(op, index + 2); // 从上一个匹配的位置的下一个字符开始查找
//        }
//
//        return count;
//    }
//
//    public static String replaceValues(String inputString, Map<String, String> replacementMap) {
//        for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
//            String key = entry.getKey();
//            String value = entry.getValue();
//
//            if (inputString.contains(value)) {
//                inputString = StringUtils.replace(inputString, value, key);
//            }
//        }
//        return inputString;
//    }
//
//
//    public static double getLevenshteinSim(String x, String y) {
//
//        double maxLength = Double.max(x.length(), y.length());
//        if (maxLength > 0) {
//            // 如果需要，可以选择忽略大小写
//            return (maxLength - StringUtils.getLevenshteinDistance(x, y)) / maxLength;
//        }
//        return 1.0;
//    }
//
//    private static double calculateJaccardSimilarity(String str1, String str2) {
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
//
//    private static Set<Character> strToCharSet(String str) {
//        try {
//            Set<Character> charSet = new HashSet<>();
//            for (char c : str.toCharArray()) {
//                charSet.add(c);
//            }
//            return charSet;
//        } catch (Exception e) {
//            System.out.println(str);
//            e.printStackTrace();
//        }
//        return new HashSet<>();
//
//    }
//
//    @GetMapping(value = "/test")
//    public void test(@RequestParam String test) {
//        List<PasswdPath> all = pathRepo.findAll();
//        for (PasswdPath path : all) {
//            System.out.println(path.getPath());
//        }
//
//    }
//
////    public Map<String, Integer> countUniqueOperations() {
////        String sql = "select path from CN12306 where path is not null";
////        List<String> paths = jdbc.queryForList(sql, String.class);
////        Map<String, Integer> operationsCount = new HashMap<>();
////        for (String path : paths) {
////            List<String> operations = getOperations(path);
////            for (String operation : operations) {
////                if (!operationsCount.containsKey(operation)) {
////                    operationsCount.put(operation, 0);
////                }
////                int count = operationsCount.get(operation) + 1;
////                operationsCount.put(operation, count);
////            }
////        }
////        return operationsCount;
////    }
//
//    private List<String> getOperations(String path) {
//        List<String> operations = new ArrayList<>();
//        Pattern p = Pattern.compile("\\('(.*?)',.*?\\)");
//        Matcher m = p.matcher(path);
//        while (m.find()) {
//            operations.add(m.group(1));
//        }
//        return operations;
//    }
//
////    @GetMapping(value = "/modify")
////    public Map<String, Double> countEdits() {
////        String sql = "SELECT path FROM cn12306 where path is not null";
////        List<String> paths = jdbc.queryForList(sql, String.class);
////        Map<String, Double> editCountMap = new HashMap<>();
////        for (String path : paths) {
////            List<String> edits = extractEdits(path);
////            for (String edit : edits) {
////                editCountMap.put(edit, editCountMap.getOrDefault(edit, 0.0) + 1);
////            }
////        }
////        return editCountMap;
////    }
//
//    public static List<String> extractEdits(String path) {
//        // 提取编辑方法的逻辑，你可以根据实际情况进行修改
//        // 此处的提取逻辑仅作示例
//
//        // 去除首尾的中括号
//        String trimmedPath = path.replaceAll("^\\[|\\]$", "");
//
//        // 分割字符串
//        String[] parts = trimmedPath.split(", ");
//
//        // 去除单引号和括号
//        for (int i = 0; i < parts.length; i++) {
//            parts[i] = parts[i].replaceAll("^['(]|[')]$", "");
//        }
//
//        return List.of(parts);
//    }
//
//    private Set<String> initCommonSet() {
//        Set<String> stringSet = new HashSet<>();
//
//        // 添加字符串到Set集合
//        stringSet.add("123456789");
//        stringSet.add("12345678");
//        stringSet.add("11111111");
//        stringSet.add("00000000");
//        stringSet.add("123123123");
//        stringSet.add("a123456789");
//        stringSet.add("88888888");
//        stringSet.add("11223344");
//        stringSet.add("qq123456");
//        stringSet.add("woaini1314");
//        stringSet.add("1q2w3e4r");
//        stringSet.add("aaaaaaaa");
//        stringSet.add("dearbook");
//        stringSet.add("1234567890");
//        stringSet.add("123456789a");
//        stringSet.add("1234qwer");
//        stringSet.add("123qweasd");
//        stringSet.add("asdasdasd");
//        stringSet.add("q1w2e3r4");
//        stringSet.add("aa123456");
//        stringSet.add("123456abc");
//        stringSet.add("a5201314");
//        stringSet.add("as123456");
//        stringSet.add("123321123");
//        stringSet.add("qqqqqqqq");
//        stringSet.add("woaini123");
//        stringSet.add("zhangjing");
//        stringSet.add("31415926");
//        stringSet.add("111111111");
//        stringSet.add("asdfasdf");
//        stringSet.add("abcd1234");
//        stringSet.add("123456");
//        stringSet.add("111111");
//        stringSet.add("a123456");
//        stringSet.add("000000");
//        stringSet.add("1qaz2wsx");
//        stringSet.add("123456a");
//        stringSet.add("5201314");
//        stringSet.add("5211314");
//        stringSet.add("123qwe");
//        stringSet.add("123123");
//        stringSet.add("w123456");
//        stringSet.add("123321");
//        stringSet.add("woaini");
//        stringSet.add("1314520");
//        stringSet.add("a123123");
//        stringSet.add("100200");
//        stringSet.add("7758521");
//        stringSet.add("qwe123");
//        stringSet.add("456852");
//        stringSet.add("7758258");
//        return stringSet;
//    }
//
//    public static String getInitials(String name) {
//        StringBuilder initialsBuilder = new StringBuilder();
//
//        for (char c : name.toCharArray()) {
//            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
//
//            if (pinyinArray != null && pinyinArray.length > 0) {
//                String pinyin = pinyinArray[0];
//                char firstChar = pinyin.charAt(0);
//                initialsBuilder.append(firstChar);
//            }
//        }
//
//        return initialsBuilder.toString();
//    }
//
//    public static boolean isCommonSubstringLongerThan4(String str1, String str2, int limit) {
//        int[][] dp = new int[str1.length() + 1][str2.length() + 1];
//        int maxLength = 0;
//
//        for (int i = 1; i <= str1.length(); i++) {
//            for (int j = 1; j <= str2.length(); j++) {
//                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
//                    dp[i][j] = dp[i - 1][j - 1] + 1;
//                    maxLength = Math.max(maxLength, dp[i][j]);
//                }
//            }
//        }
//
//        return maxLength > limit;
//    }
//
//}
