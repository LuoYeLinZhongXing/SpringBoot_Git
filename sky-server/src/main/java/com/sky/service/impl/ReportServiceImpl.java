package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        //1、查询指定时间区间内的营业额数据
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            //计算需要累计营业额的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String join = StringUtils.join(dateList, ",");

        ArrayList<Double> TurnoverList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime localDateTimeMin = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime localDateTimeMax = LocalDateTime.of(localDate, LocalTime.MAX);
            HashMap HashMap = new HashMap<>();
            HashMap.put("begin", localDateTimeMin);
            HashMap.put("end", localDateTimeMax);
            HashMap.put("status", Orders.COMPLETED);
            Double sum = orderMapper.sumByMap(HashMap);
            sum = sum == null ? 0.0 : sum;
            TurnoverList.add(sum);
        }

        String join1 = StringUtils.join(TurnoverList, ",");
        TurnoverReportVO build = TurnoverReportVO
                .builder()
                .dateList(join)
                .turnoverList(join1)
                .build();
        return build;
    }
}
