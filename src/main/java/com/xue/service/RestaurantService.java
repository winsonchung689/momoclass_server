package com.xue.service;

import com.xue.entity.model.RestaurantUser;

import java.util.List;

public interface RestaurantService {


    public List getRestaurantUser(String openid);

    public List getRestaurantUserAll(String openid);

    public List getRestaurantOrder(String openid,String type);

    public List getRestaurantCategory(String restaurant);

    public List getRestaurantMenu(String restaurant);

    public int insertRestaurantUser(RestaurantUser restaurantUser);



}
