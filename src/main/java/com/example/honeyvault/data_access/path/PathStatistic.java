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
    public Set<PathAndAlphaUser> parsePswds() {
        CsvReader reader = CsvUtil.getReader();

        CsvData data = reader.read(FileUtil.file("/readData/t_12306_163_replace.csv"));

        List<CsvRow> rows = data.getRows();
        Set<PathAndAlphaUser> pathTrainSet = new HashSet<>();

        for (int i = 1; i < 91226; i++) {

            CsvRow csvRow = rows.get(i);
            List<String> rawList = csvRow.getRawList();
            String p1 = rawList.get(4);
            String p2 = rawList.get(5);
            if (CalPath.LongestComSubstr(p1, p2).length() >=
                    0.5 * Math.max(p1.length(), p2.length())) {
                String replace_12306 = rawList.get(6);
                String replace_163 = rawList.get(7);

                PathAndAlphaUser user =
                        PathAndAlphaUser.builder().replace_163(replace_163).replace_12306(replace_12306).build();
                pathTrainSet.add(user);
            }

        }
        return pathTrainSet;
    }

    public Set<PathAndAlphaUser> parsePswdsWithoutPII() {
        CsvReader reader = CsvUtil.getReader();

        CsvData data = reader.read(FileUtil.file("/readData/t_12306_163_replace.csv"));

        List<CsvRow> rows = data.getRows();
        Set<PathAndAlphaUser> pathTrainSet = new HashSet<>();
        for (int i = 1; i < 91577; i++) {
//        for (int i = 1; i < 9122; i++) {
            CsvRow csvRow = rows.get(i);
            List<String> rawList = csvRow.getRawList();
            String p1 = rawList.get(4);
            String p2 = rawList.get(5);
            if (CalPath.LongestComSubstr(p1, p2).length() >=
                    0.5 * Math.max(p1.length(), p2.length())) {
                String pswd12306 = rawList.get(4);
                String pswd163 = rawList.get(5);

                PathAndAlphaUser user =
                        PathAndAlphaUser.builder().replace_163(pswd163).replace_12306(pswd12306).build();
                pathTrainSet.add(user);
            }
        }
        return pathTrainSet;
    }


    public List<String> getPathTrainSet() {
        Set<PathAndAlphaUser> userSet = parsePswds();
        List<String> pathTrainList = new ArrayList<>();
        for (PathAndAlphaUser user : userSet) {
            String pw_163 = user.getReplace_163();
            String pw_12306 = user.getReplace_12306();
            if (pw_163 == null || pw_12306 == null) {
                continue;
            }
            List<List<String>> paths = CalPath.breadthFirstSearch(pw_12306, pw_163);
            paths.forEach(path -> pathTrainList.add(path.toString()));

        }
        return pathTrainList;
    }

    public List<String> getPathTrainSetWithoutPII() {
        Set<PathAndAlphaUser> userSet = parsePswdsWithoutPII();
        List<String> pathTrainList = new ArrayList<>();
        for (PathAndAlphaUser user : userSet) {
            String pw_163 = user.getReplace_163();
            String pw_12306 = user.getReplace_12306();
            if (pw_163 == null || pw_12306 == null) {
                continue;
            }
            List<List<String>> paths = CalPath.breadthFirstSearch(pw_12306, pw_163);
            paths.forEach(path -> pathTrainList.add(path.toString()));

        }
        return pathTrainList;
    }


    public Set<PathAndAlphaUser> parsePswdsEngl() {
        CsvReader reader = CsvUtil.getReader();
        CsvData data = reader.read(FileUtil.file("/readData/Clix_BC_all.csv"));
        List<CsvRow> rows = data.getRows();
        Set<PathAndAlphaUser> pathTrainSet = new HashSet<>();
        for (int i = 1; i < 214005; i++) {
            CsvRow csvRow = rows.get(i);
            List<String> rawList = csvRow.getRawList();
            String p1 = rawList.get(5);
            String p2 = rawList.get(6);
            if (CalPath.LongestComSubstr(p1, p2).length() >=
                    0.5 * Math.max(p1.length(), p2.length())) {
                String replace_Clix = rawList.get(7);
                String replace_BC = rawList.get(8);

                PathAndAlphaUser user =
                        PathAndAlphaUser.builder().replace_BC(replace_BC).replace_Clix(replace_Clix).build();
                pathTrainSet.add(user);
            }

        }
        return pathTrainSet;
    }

    public Set<PathAndAlphaUser> parsePswdsWithoutPIIEngl() {
        CsvReader reader = CsvUtil.getReader();
        CsvData data = reader.read(FileUtil.file("/readData/Clix_BC_all.csv"));
        List<CsvRow> rows = data.getRows();
        Set<PathAndAlphaUser> pathTrainSet = new HashSet<>();
        for (int i = 1; i < 214005; i++) {
//        for (int i = 1; i < 9122; i++) {
            CsvRow csvRow = rows.get(i);
            List<String> rawList = csvRow.getRawList();
            String p1 = rawList.get(5);
            String p2 = rawList.get(6);
            if (CalPath.LongestComSubstr(p1, p2).length() >=
                    0.5 * Math.max(p1.length(), p2.length())) {
                String pswdClix = rawList.get(5);
                String pswdBC = rawList.get(6);

                PathAndAlphaUser user =
                        PathAndAlphaUser.builder().replace_BC(pswdBC).replace_Clix(pswdClix).build();
                pathTrainSet.add(user);
            }
//            好的，现在加入另外两个编辑函数hi()和ti()，当bfSource!=target且hd()和td()不再能使bfSrouce发生变化时，尝试广度搜索hi和ti，搜索的逻辑同上，将路径
        }
        return pathTrainSet;
    }

    public List<String> getPathTrainSetWithoutPIIEngl() {
        Set<PathAndAlphaUser> userSet = parsePswdsWithoutPIIEngl();
        List<String> pathTrainList = new ArrayList<>();
        for (PathAndAlphaUser user : userSet) {
            String pw_BC = user.getReplace_BC();
            String pw_Clix = user.getReplace_Clix();
            if (pw_BC == null || pw_Clix == null) {
                continue;
            }
            List<List<String>> paths = CalPath.breadthFirstSearch(pw_Clix, pw_BC);
            paths.forEach(path -> pathTrainList.add(path.toString()));

        }
        return pathTrainList;
    }


    public List<String> getPathTrainSetEngl() {
        Set<PathAndAlphaUser> userSet = parsePswdsEngl();
        List<String> pathTrainList = new ArrayList<>();
        for (PathAndAlphaUser user : userSet) {
            String pw_BC = user.getReplace_BC();
            String pw_Clix = user.getReplace_Clix();
            if (pw_BC == null || pw_Clix == null) {
                continue;
            }
            List<List<String>> paths = CalPath.breadthFirstSearch(pw_Clix, pw_BC);
            paths.forEach(path -> pathTrainList.add(path.toString()));

        }
        return pathTrainList;
    }

}
