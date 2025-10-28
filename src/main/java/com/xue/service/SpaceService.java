package com.xue.service;

import com.xue.entity.model.*;

import java.util.List;

public interface SpaceService {

    public List getSpaceTeacher(String openid);

    public List getBookUser(String openid);

    public List getSpaceCases(String openid);

    public List getSpaceLesson(String openid);

    public List getSpaceOrder(String openid);

    public List getSpaceGoodsList(String openid);

    public List getSpaceGoodsListById(String goods_id);

    public List getWorkingDetail(String openid,String date_time);

    public List getSpaceGoodsOrderByGoodsId(String goods_id);

    public List getSpaceGoodsOrderByGoodsIdOpenid(String goods_id,String openid);

    public List getSpaceGoodsOrderByOpenid(String openid);

    public List getLibraryByPublic(String openid);

}
