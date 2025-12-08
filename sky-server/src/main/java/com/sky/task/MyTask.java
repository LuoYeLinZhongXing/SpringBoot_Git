package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class MyTask {

    //@Scheduled(cron = "0/5 * * * * ?")//注解表示每5秒执行一次
    //public void runTask1() {
    //    log.info("定时任务1开始执行: {}",new Date());
    //}
}
