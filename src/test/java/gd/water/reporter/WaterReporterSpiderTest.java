package gd.water.reporter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

@Ignore
public class WaterReporterSpiderTest {


    @Test
    public void  test() throws IOException {

        WaterReporterSpider spider = new WaterReporterSpider();

        File file = new File("src\\test\\java\\resources\\template.html");
        Document document = Jsoup.parse(file, "UTF-8");

        Element form1 = document.getElementById("form1");

        Elements divList = form1.getElementsByTag("div");
        String val = divList.get(0).getElementsByTag("input").get(0).val();
        System.out.println(val);
        spider.parse(document);
    }

}