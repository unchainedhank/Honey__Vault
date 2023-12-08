package com.example.honeyvault;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class HoneyVaultApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public static void main(String[] args) {
        Map<Integer, Double> opTimesMap = new HashMap<>();
        double lambdaTimes = 0.001;
        opTimesMap.put(1, 0.1);
        opTimesMap.put(2, 0.2);
        opTimesMap.put(3, 0.3);
    }

    Map<Integer, Double> smoothTimesMap(Map<Integer, Double> opTimesMap, double lambdaTimes) {
        double originSize = opTimesMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double factor = originSize + lambdaTimes * 8;
        for (int i = 1; i < 9; i++) {
            if (opTimesMap.containsKey(i)) {
                opTimesMap.put(i, (opTimesMap.get(i) + lambdaTimes) / factor);
            } else {
                opTimesMap.put(i, lambdaTimes / factor);
            }
        }
        return opTimesMap;
    }

}
