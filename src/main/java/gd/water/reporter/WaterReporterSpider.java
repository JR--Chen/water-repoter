package gd.water.reporter;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class WaterReporterSpider {

    private final RestTemplate client;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private final PostParam postParam;

    public WaterReporterSpider() {
        this.client = new RestTemplate();
        this.postParam = getPostParam();
    }


    public List<Result> getDateSet(Date start, Date end) {

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add("txt_time1", sdf.format(start));
        map.add("txt_time2", sdf.format(end));
        map.add("btn_query", "查询");
        map.add("__EVENTVALIDATION", postParam.__EVENTVALIDATION);
        map.add("__VIEWSTATE", postParam.__VIEWSTATE);

        Document document = getDocument(map);

        Elements elements = parse(document);
        String period = sdf.format(start) + " " + sdf.format(end);
        return getResult(elements, period);
    }


    private Document getDocument(MultiValueMap<String, String> map) {
        // 这个地方网络抖动比较多导致任务失败  加了默认3次的重试
        RetryTemplate template = new RetryTemplate();
        try {
            return template.execute((RetryCallback<Document, Exception>)
                    context -> Jsoup.parse(postFormData(map)));
        } catch (Exception e) {
            log.error(" get date error param {}", map, e);
            throw new RuntimeException(e);
        }
    }

    Elements parse(Document document) {

        Element form1 = document.getElementById("form1");
        Element table = form1.getElementsByTag("table").get(0);
        Element tbody = table.getElementsByTag("tbody").get(0);

        return tbody.getElementsByTag("tr").get(2).getElementsByTag("td").get(0)
                .getElementById("report")
                .getElementById("LeftTree")
                .getElementsByTag("tbody");
    }

    private PostParam getPostParam() {
        Document document = getDocument(new LinkedMultiValueMap<>(0));
        Element form1 = document.getElementById("form1");

        Elements divList = form1.getElementsByTag("input");
        String __VIEWSTATE = divList.get(0).val();
        String __EVENTVALIDATION = divList.get(1).val();

        PostParam param = new PostParam();
        param.__EVENTVALIDATION = __EVENTVALIDATION;
        param.__VIEWSTATE = __VIEWSTATE;
        return param;
    }

    private List<Result> getResult(Elements resultTable, String period) {
        List<Result> list = resultTable.stream().flatMap(x -> x.children().stream())
                .map(child -> {
                    Elements children = child.children();
                    Result result = new Result();
                    result.city1 = children.get(0).text();
                    result.city2 = children.get(1).text();
                    result.station = children.get(2).text();
                    result.date = children.get(3).text();
                    result.waterLine = children.get(4).text();
                    result.warningLine = children.get(5).text();
                    result.potential = children.get(6).text();
                    result.period = period;
                    return result;
                })
                .collect(Collectors.toList());

        if (!list.isEmpty()) {
            list.remove(0);
        }
        return list;
    }


    private String postFormData(MultiValueMap<String, String> map) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return client.postForEntity("http://113.108.186.79:9001/Report/WaterReport.aspx", request, String.class).getBody();
    }


    private static class PostParam {
        String __EVENTVALIDATION;
        String __VIEWSTATE;
    }

}
