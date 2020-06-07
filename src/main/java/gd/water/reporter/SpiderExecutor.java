package gd.water.reporter;

import javafx.util.Pair;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class SpiderExecutor {

    private static final ExecutorService service = Executors.newFixedThreadPool(4);
    private static final int size = 24;


    @SneakyThrows
    public List<Result> getDaySet(int year, int month, int day) {

        WaterReporterSpider spider = new WaterReporterSpider();
        List<Pair<Date, List<Result>>> resultList = Collections.synchronizedList(new ArrayList<>());

        CountDownLatch latch = new CountDownLatch(size);
        for (int x = 0; x < size; x++) {
            int finalX = x;
            service.submit(() -> {
                try {
                    Date start = getDate(year, month, day, finalX);
                    Date end = getDate(year, month, day, finalX + 1);
                    log.info("get date start {} end {}", start, end);
                    List<Result> dateSet = spider.getDateSet(start, end);
                    resultList.add(new Pair<>(start, dateSet));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();


        return resultList.stream()
                .sorted(Comparator.comparing(Pair::getKey))
                .flatMap(x-> x.getValue().stream())
                .collect(Collectors.toList());
    }


    private Date getDate(int year, int month, int day, int hour) {
        Calendar instance = Calendar.getInstance();
        instance.set(year, month, day, hour, 0, 0);

        return instance.getTime();
    }


}
