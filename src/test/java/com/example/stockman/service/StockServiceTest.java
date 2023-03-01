package com.example.stockman.service;

import com.example.stockman.domain.Stock;
import com.example.stockman.repository.StockRepository;
import org.aspectj.lang.annotation.After;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockServiceTest {

    @Autowired
    private PessimisticLockStockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    void beforeEach() {
        Stock stock = new Stock(1L, 100L);
        stockRepository.save(stock);
    }

    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @Test
    void stock_decrease() {
        stockService.decrease(1L, 1L);

        Stock stock = stockRepository.findById(1L).orElseThrow();

        // 100 -1 == 99
        assertThat(stock.getQuantity()).isEqualTo(99);
    }

    @Test
    @DisplayName("동시에 100개의 요청이 들어온다")
    void 동시에_100개의_요청() throws InterruptedException {
        int threadCount = 100;
        // 비동기로 실행하는 작업을 단순화하여 사용할 수 있도록 함.
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);
                } finally {
                    // latch -1 씩
                    latch.countDown();
                }
            });
        }
        // latch가 0 이 될때까지 기다린다.
        latch.await();

        // 모든 요청이 완료되면
        Stock stock = stockRepository.findById(1L).orElseThrow();

        // 100 - ( 1 * 100 ) = 0
        assertThat(stock.getQuantity()).isEqualTo(0);
    }


}