package com.lcmall.dao;

import com.lcmall.po.Product;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    /**这里一定要用Integer，因为int无法为NULL，考虑到很多商品已经删除的情况。*/
    Integer selectStockByProductId(Integer id);
}