package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //判断购物车数据是否已存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        //存在，数量+1
        if(list!=null && list.size()>0){
            ShoppingCart shoppingCart1 = list.get(0);
            shoppingCart1.setNumber(shoppingCart1.getNumber()+1);
            shoppingCartMapper.updateNumberById(shoppingCart1);
        }else{
            //不存在，添加到购物车，数量默认为1
            if(shoppingCartDTO.getDishId()!=null){
                //本次添加的就是菜品
                Dish byId = dishMapper.getById(shoppingCartDTO.getDishId());
                shoppingCart.setName(byId.getName());
                shoppingCart.setImage(byId.getImage());
                shoppingCart.setAmount(byId.getPrice());
            }else{
                //本次添加的是套餐
                Setmeal byId = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                shoppingCart.setName(byId.getName());
                shoppingCart.setImage(byId.getImage());
                shoppingCart.setAmount(byId.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }


    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        Long currentId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart =ShoppingCart.builder()
                        .userId(currentId)
                                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    /**
     * 清空购物车
     */
    @Override
    public void deleteAll() {
        ShoppingCart build = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .build();
        shoppingCartMapper.deleteByUserId(build);
    }
}
