package gd.water.reporter;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class Result {

    @ExcelProperty(value = "市")
    String city1;
    @ExcelProperty(value = "市(县)")
    String city2;
    @ExcelProperty(value = "站点")
    String station;
    @ExcelProperty(value = "日期")
    String date;

    /**
     * 水位
     */
    @ExcelProperty(value = "水位")
    String waterLine;

    /**
     * 境界水巍
     */
    @ExcelProperty(value = "警戒水位")
    String warningLine;

    /**
     * 水势
     */
    @ExcelProperty(value = "水势")
    String potential;

    @ExcelProperty(value = "时间段")
    String period;

}
