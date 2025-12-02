package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;

import java.util.List;

public interface SetmealService {
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 新增套餐
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);
}
