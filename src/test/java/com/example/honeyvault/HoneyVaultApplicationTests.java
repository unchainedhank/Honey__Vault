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


}
