package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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

    @Autowired
    private WorkspaceService workspaceService;
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

    /**
     * 导出营业数据
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //1、查询数据库，获取营业数据
        LocalDate localDate = LocalDate.now().minusDays(30);

        LocalDate localDate1 = LocalDate.now().minusDays(1);

        //1.1 查询概览数据
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(localDate, LocalTime.MIN), LocalDateTime.of(localDate1, LocalTime.MAX));

        //2.通过POI 将数据写入到Excel中
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模板创建一个Excel表格对象
            XSSFWorkbook sheets = new XSSFWorkbook(resourceAsStream);

            //填充数据
            //获取第一个表单
            XSSFSheet sheetAt = sheets.getSheetAt(0);

            //填充时间段
            sheetAt.getRow(1).getCell(1).setCellValue("时间:"+localDate+ "~" + localDate1);

            //获取第四行
            XSSFRow row = sheetAt.getRow(3);
            //填充营业额
            row.getCell(2).setCellValue(businessData.getTurnover());
            //填充订单完成率
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            //填充新增用户数
            row.getCell(6).setCellValue(businessData.getNewUsers());

            //获取第五行
            row = sheetAt.getRow(4);
            //填充有效订单数
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            //填充平均客单价
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = localDate.plusDays(1);
                BusinessDataVO businessData1 = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                //获取对应行
                XSSFRow row1 = sheetAt.getRow(7 + i);
                //填充日期
                row1.getCell(1).setCellValue(date.toString());
                //填充营业额
                row1.getCell(2).setCellValue(businessData1.getTurnover());
                //填充有效订单数
                row1.getCell(3).setCellValue(businessData1.getValidOrderCount());
                //填充订单完成率
                row1.getCell(4).setCellValue(businessData1.getOrderCompletionRate());
                //填充平均客单价
                row1.getCell(5).setCellValue(businessData1.getUnitPrice());
                //填充新增用户数
                row1.getCell(6).setCellValue(businessData1.getNewUsers());



            }

            //3.将Excel文件下载到客户机中
            ServletOutputStream outputStream = response.getOutputStream();
            sheets.write(outputStream);

            //关闭流
            outputStream.close();
            sheets.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }


}
