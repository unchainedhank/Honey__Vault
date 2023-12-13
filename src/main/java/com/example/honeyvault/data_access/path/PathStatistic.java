package com.example.honeyvault.data_access.path;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import com.example.honeyvault.tool.CalPath;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PathStatistic {
    public Set<PathAndAlphaUser> parsePswdsCN() {
        CsvReader reader = CsvUtil.getReader();
//        CsvData data = reader.read(FileUtil.file("/app/classes/static/t_12306_163_replace.csv"));
        CsvData data = reader.read(FileUtil.file("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/static" +
                "/t_12306_163_replace.csv"));
        List<CsvRow> rows = data.getRows();
        Set<PathAndAlphaUser> pathTrainSet = new HashSet<>();
        for (int i = 1; i < 91577; i++) {
//        for (int i = 1; i < 91577; i++) {
            CsvRow csvRow = rows.get(i);
            List<String> rawList = csvRow.getRawList();
//            String p1 = rawList.get(4);
//            String p2 = rawList.get(5);
            String replace_12306 = rawList.get(6);
            String replace_163 = rawList.get(7);
            int comLen = CalPath.LongestComSubstr(replace_12306, replace_163).length();
            if ((comLen >=
                    0.125 * Math.max(replace_12306.length(), replace_163.length()))
            && !((replace_12306.length()<4) && comLen<replace_12306.length())) {

                PathAndAlphaUser user =
                        PathAndAlphaUser.builder().pw2(replace_163).pw1(replace_12306).build();
                pathTrainSet.add(user);
            }

        }
        return pathTrainSet;
    }

    public Set<PathAndAlphaUser> parsePswdsWithoutPII() {
        CsvReader reader = CsvUtil.getReader();
        CsvData data = reader.read(FileUtil.file("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/static" +
                "/t_12306_163_replace.csv"));
//        CsvData data = reader.read(FileUtil.file("/app/classes/static/t_12306_163.csv"));
        List<CsvRow> rows = data.getRows();
        Set<PathAndAlphaUser> pathTrainSet = new HashSet<>();
        for (int i = 1; i < 91577; i++) {
//        for (int i = 1; i < 91577; i++) {
            CsvRow csvRow = rows.get(i);
            List<String> rawList = csvRow.getRawList();
            String p1 = rawList.get(4);
            String p2 = rawList.get(5);
            if (CalPath.LongestComSubstr(p1, p2).length() >=
                    0.5 * Math.max(p1.length(), p2.length())) {
                String pswd12306 = rawList.get(4);
                String pswd163 = rawList.get(5);

                PathAndAlphaUser user =
                        PathAndAlphaUser.builder().pw2(pswd163).pw1(pswd12306).build();
                pathTrainSet.add(user);
            }
        }
        return pathTrainSet;
    }

    public Set<PathAndAlphaUser> parsePswdsEng() {
        CsvReader reader = CsvUtil.getReader();
//        CsvData data = reader.read(FileUtil.file("/app/classes/static/t_12306_163_replace.csv"));
        CsvData data = reader.read(FileUtil.file("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/static" +
                "/Clix_BC_all.csv"));
        List<CsvRow> rows = data.getRows();
        Set<PathAndAlphaUser> pathTrainSet = new HashSet<>();
        for (int i = 1; i < 214005; i++) {
//        for (int i = 1; i < 214005; i++) {
            CsvRow csvRow = rows.get(i);
            List<String> rawList = csvRow.getRawList();
//            String p1 = rawList.get(4);
//            String p2 = rawList.get(5);
            String replace_clix = rawList.get(7);
            String replace_BC = rawList.get(8);
            int comLen = CalPath.LongestComSubstr(replace_clix, replace_BC).length();
            if (comLen >=
                    0.125 * Math.max(replace_clix.length(), replace_BC.length())
                    && !((replace_clix.length()<4) && comLen<replace_BC.length())) {

                PathAndAlphaUser user =
                        PathAndAlphaUser.builder().pw2(replace_BC).pw1(replace_clix).build();
                pathTrainSet.add(user);
            }

        }
        return pathTrainSet;
    }

    public Set<PathAndAlphaUser> parsePswdsEngWithoutPII() {
        CsvReader reader = CsvUtil.getReader();
        CsvData data = reader.read(FileUtil.file("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/static" +
                "/Clix_BC_all.csv"));
//        CsvData data = reader.read(FileUtil.file("/app/classes/static/t_12306_163.csv"));
        List<CsvRow> rows = data.getRows();
        Set<PathAndAlphaUser> pathTrainSet = new HashSet<>();
        for (int i = 1; i < 9157; i++) {
//        for (int i = 1; i < 214005; i++) {
            CsvRow csvRow = rows.get(i);
            List<String> rawList = csvRow.getRawList();
            String p1 = rawList.get(5);
            String p2 = rawList.get(6);
            if (CalPath.LongestComSubstr(p1, p2).length() >=
                    0.5 * Math.max(p1.length(), p2.length())) {
                PathAndAlphaUser user =
                        PathAndAlphaUser.builder().pw2(p1).pw1(p2).build();
                pathTrainSet.add(user);
            }
        }
        return pathTrainSet;
    }


    public List<PathInfo> getPathTrainSet() {
        Set<PathAndAlphaUser> userSet = parsePswdsCN();
        List<PathInfo> pathTrainList = new ArrayList<>();
        for (PathAndAlphaUser user : userSet) {
            String pw_163 = user.getPw2();
            String pw_12306 = user.getPw1();
            if (pw_163 == null || pw_12306 == null) {
                continue;
            }
            List<List<String>> paths = CalPath.breadthFirstSearch(pw_12306, pw_163);
            paths.forEach(path -> {
                PathInfo build = PathInfo.builder().path(path.toString()).length(pw_12306.length()).build();
//                int hdTimes = countOccurrencesOfOp(path.toString(), "hd");
//                int tdTimes = countOccurrencesOfOp(path.toString(), "td");
//                if (pw_12306.length() == 11 && (hdTimes + tdTimes) ==6) {
//                    System.out.println(pw_12306 + "->" + pw_163 + ":" + path);
//                }
                pathTrainList.add(build);
            });
        }
        return pathTrainList;
    }

    public List<PathInfo> getPathTrainSetWithoutPII() {
        Set<PathAndAlphaUser> userSet = parsePswdsWithoutPII();
        List<PathInfo> pathTrainList = new ArrayList<>();
        for (PathAndAlphaUser user : userSet) {
            String pw_163 = user.getPw2();
            String pw_12306 = user.getPw1();
            if (pw_163 == null || pw_12306 == null) {
                continue;
            }
            List<List<String>> paths = CalPath.breadthFirstSearch(pw_12306, pw_163);
            paths.forEach(path ->
                    {
                        int hd = countOccurrencesOfOp(path.toString(), "hd");
                        int td = countOccurrencesOfOp(path.toString(), "td");
                        PathInfo build =
                                PathInfo.builder().path(path.toString()).length(pw_12306.length()).lengthMinusDelete(pw_12306.length() - hd - td).build();
                        pathTrainList.add(build);

                    }
            );

        }
        return pathTrainList;
    }

    public List<PathInfo> getEngPathTrainSet() {
        Set<PathAndAlphaUser> userSet = parsePswdsEng();
        List<PathInfo> pathTrainList = new ArrayList<>();
        for (PathAndAlphaUser user : userSet) {
            String pw1 = user.getPw2();
            String pw2 = user.getPw1();
            if (pw1 == null || pw2 == null) {
                continue;
            }
            List<List<String>> paths = CalPath.breadthFirstSearch(pw2, pw1);
            paths.forEach(path -> {
                PathInfo build = PathInfo.builder().path(path.toString()).length(pw2.length()).build();
                pathTrainList.add(build);
            });
        }
        return pathTrainList;
    }

    public List<PathInfo> getEngPathTrainSetWithoutPII() {
        Set<PathAndAlphaUser> userSet = parsePswdsEngWithoutPII();
        List<PathInfo> pathTrainList = new ArrayList<>();
        for (PathAndAlphaUser user : userSet) {
            String pw1 = user.getPw2();
            String pw_2 = user.getPw1();
            if (pw1 == null || pw_2 == null) {
                continue;
            }
            List<List<String>> paths = CalPath.breadthFirstSearch(pw_2, pw1);
            paths.forEach(path ->
                    {
                        int hd = countOccurrencesOfOp(path.toString(), "hd");
                        int td = countOccurrencesOfOp(path.toString(), "td");
                        PathInfo build =
                                PathInfo.builder().path(path.toString()).length(pw_2.length()).lengthMinusDelete(pw_2.length() - hd - td).build();
                        pathTrainList.add(build);

                    }
            );

        }
        return pathTrainList;
    }

    private int countOccurrencesOfOp(String p, String op) {
        int count = 0;
        int index = p.indexOf(op);

        while (index != -1) {
            count++;
            index = p.indexOf(op, index + 2); // 从上一个匹配的位置的下一个字符开始查找
        }

        return count;
    }

}
