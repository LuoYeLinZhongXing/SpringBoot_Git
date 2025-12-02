package com.sky.mapper;

import com.sky.entity.Setmeal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 条件查询
     * @param setmeal
     */
    void list(Setmeal setmeal);

    /**
     * 新增套餐
     * @param setmeal
     */
    void insert(Setmeal setmeal);
}
