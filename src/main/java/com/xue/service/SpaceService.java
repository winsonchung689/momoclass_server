package com.xue.service;

import com.xue.entity.model.*;

import java.util.List;

public interface SpaceService {

    public List getSpaceTeacher(String openid);

    public List getBookUser(String openid);

    public List getSpaceCases(String openid);


}
