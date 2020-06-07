package gd.water.reporter;

import com.alibaba.excel.EasyExcel;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class ExcelController {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    private static final Set<String> DEFAULT_STATION = Sets.newHashSet("博罗(二)", "石角", "高要");

    private static final Splitter splitter = Splitter.on(",").omitEmptyStrings();

    @RequestMapping("/")
    public String getData(@RequestParam(value = "date") String date,
                          @RequestParam(value = "station", required = false) String station,
                          HttpServletResponse response){

        Set<String> filterStationSet = StringUtils.isEmpty(station) ? DEFAULT_STATION : new HashSet<>(splitter.splitToList(station));;

        log.info("request date {}", date);
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + date + ".xlsx");


        Date parse;
        try {
            parse = sdf.parse(date);
        } catch (ParseException e) {
            return "日期解析错误";
        }
        Calendar instance = Calendar.getInstance();
        instance.setTime(parse);


        SpiderExecutor executor = new SpiderExecutor();
        try {
            List<Result> daySet = executor.getDaySet(instance.get(Calendar.YEAR), instance.get(Calendar.MONTH),
                    instance.get(Calendar.DAY_OF_MONTH));


            EasyExcel.write(response.getOutputStream(), Result.class).sheet("模板").doWrite(
                    daySet.stream()
                            .filter(x-> filterStationSet.contains(x.station))
                            .collect(Collectors.toList()));
        } catch (IOException e) {
            return "写文件错误";
        }

        return "ok";
    }


    @RequestMapping("/station")
    public String testStation(@RequestParam(value = "station", required = false) String station,
                          HttpServletResponse response){



        Set<String> set = new HashSet<>(splitter.splitToList(station));


        return set.toString();
    }
}
