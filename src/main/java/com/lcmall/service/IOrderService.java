package com.lcmall.service;

public interface IOrderService {

    /**
     * hour个小时以内未付款的订单，进行关闭
     */
    void closeOrder(int hour);


}
