package com.sky.controller.user;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Api(tags = "C端菜品接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list( Long categoryId){
        log.info("根据分类id查询菜品：{}",categoryId);

        //查询Redis中是否有缓存的菜品数据
        String key = "dish_" + categoryId;
        List<DishVO> List  = (List<DishVO>)redisTemplate.opsForValue().get(key);
        if(List != null && List.size() > 0){
            //缓存中有数据，直接返回
            return Result.success(List);
        }


        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);

        List<DishVO> dishVOList= dishService.getListByCategoryIdWithFlavor(dish);
        //缓存中没有数据，查询数据库,并放在Redis中
        redisTemplate.opsForValue().set(key,dishVOList);

        return Result.success(dishVOList);
    }
}
