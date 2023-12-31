package com.example.honeyvault.data_access.markov;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MarkovStatistic {
    public List<String> parseT12306() {
        CsvReader reader = CsvUtil.getReader();
//        CsvData data = reader.read(FileUtil.file("/app/classes/static/t_12306_replace.csv"));
        CsvData data = reader.read(FileUtil.file("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/static/t_12306_replace.csv"));
        List<CsvRow> rows = data.getRows();
        List<String> markovTrainSet = new ArrayList<>();
//        for (int i=1;i<101756;i++) {
        for (int i=1;i<101756;i++) {
            CsvRow csvRow = rows.get(i);
            List<String> rawList = csvRow.getRawList();
            String pswdPII = rawList.get(5);
            markovTrainSet.add(pswdPII);
        }
        return markovTrainSet;
    }

    public List<String> parseEngMerge() {
        CsvReader reader = CsvUtil.getReader();
//        CsvData data = reader.read(FileUtil.file("/app/classes/static/t_12306_replace.csv"));
        CsvData data = reader.read(FileUtil.file("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/static/english_merge_replace.csv"));
        List<CsvRow> rows = data.getRows();
        List<String> markovTrainSet = new ArrayList<>();
        for (int i=1;i<5000;i++) {
//        for (int i=1;i<222413;i++) {
            CsvRow csvRow = rows.get(i);
            List<String> rawList = csvRow.getRawList();
            String pswdPII = rawList.get(5);
            markovTrainSet.add(pswdPII);
        }
        return markovTrainSet;
    }

    public List<String> parseT12306WithoutPII() {
        CsvReader reader = CsvUtil.getReader();
        CsvData data = reader.read(FileUtil.file("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/static/t_12306_replace.csv"));
//        CsvData data = reader.read(FileUtil.file("/app/classes/static/t_12306.csv"));
        List<CsvRow> rows = data.getRows();
        List<String> markovTrainSet = new ArrayList<>();
        for (int i=1;i<101757;i++) {
//        for (int i=1;i<101757;i++) {
            CsvRow csvRow = rows.get(i);
            List<String> rawList = csvRow.getRawList();
            String pswd = rawList.get(4);
            markovTrainSet.add(pswd);
        }
        return markovTrainSet;
    }

    public List<String> parseEngMergeWithoutPII() {
        CsvReader reader = CsvUtil.getReader();
        CsvData data = reader.read(FileUtil.file("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/static/english_merge.csv"));
//        CsvData data = reader.read(FileUtil.file("/app/classes/static/t_12306.csv"));
        List<CsvRow> rows = data.getRows();
        List<String> markovTrainSet = new ArrayList<>();
        for (int i=1;i<5000;i++) {
//        for (int i=1;i<222413;i++) {
            CsvRow csvRow = rows.get(i);
            List<String> rawList = csvRow.getRawList();
            String pswd = rawList.get(5);
            markovTrainSet.add(pswd);
        }
        return markovTrainSet;
    }





}
