package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;
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

    /**
     * 用户统计
     * @param begin
     * @param end
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //1、查询指定时间区间内的营业额数据
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            //计算需要累计营业额的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }



        ArrayList<Integer> newUserList = new ArrayList<>();
        ArrayList<Integer> totalUserList = new ArrayList<>();

        //遍历日期集合
        for (LocalDate localDate : dateList) {
            LocalDateTime localDateTimeMin = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime localDateTimeMax = LocalDateTime.of(localDate, LocalTime.MAX);
            HashMap Map= new HashMap<>();

            Map.put("end",localDateTimeMax);
            Integer i = userMapper.countByMap(Map);
            newUserList.add(i);

            Map.put("begin",localDateTimeMin);
            Integer j = userMapper.countByMap(Map);
            totalUserList.add(j);
        }
        //拼接日期
        String dateListStr = StringUtils.join(dateList, ",");
        String newUserListStr = StringUtils.join(newUserList, ",");
        String totalUserListStr = StringUtils.join(totalUserList, ",");

        return UserReportVO.builder()
                .dateList(dateListStr)
                .newUserList(newUserListStr)
                .totalUserList(totalUserListStr)
                .build();
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        //1、查询指定时间区间内的营业额数据
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            //计算需要累计营业额的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }


        ArrayList<Integer> vaildOrderCountList = new ArrayList<>();
        ArrayList<Integer> OrderCountList = new ArrayList<>();

        for (LocalDate localDate : dateList) {
            LocalDateTime localDateTimeMin = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime localDateTimeMax = LocalDateTime.of(localDate, LocalTime.MAX);
            HashMap Map= new HashMap<>();
            Map.put("begin",localDateTimeMin);
            Map.put("end",localDateTimeMax);
            Integer i = orderMapper.countByMap(Map);
            OrderCountList.add(i);

            Map.put("status",Orders.COMPLETED);
            Integer j = orderMapper.countByMap(Map);
            vaildOrderCountList.add(j);

        }

        Integer validOrderCount = vaildOrderCountList.stream().reduce(Integer::sum).get();
        Integer totalOrderCount = OrderCountList.stream().reduce(Integer::sum).get();

        Double orderCompletionRate =0.0;
        if(totalOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        OrderReportVO build = OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(OrderCountList, ","))
                .validOrderCountList(StringUtils.join(vaildOrderCountList, ","))
                .validOrderCount(validOrderCount)
                .totalOrderCount(totalOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();

        return build;
    }

    /**
     * 销售top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        LocalDateTime localDateTimeMin = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime localDateTimeMax = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(localDateTimeMin, localDateTimeMax);

        List<String> nameList = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameListStr = StringUtils.join(nameList, ",");
        List<Integer> numberList = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberListStr = StringUtils.join(numberList, ",");
        SalesTop10ReportVO build = SalesTop10ReportVO.builder()
                .nameList(nameListStr)
                .numberList(numberListStr)
                .build();
        return build;
    }


}
