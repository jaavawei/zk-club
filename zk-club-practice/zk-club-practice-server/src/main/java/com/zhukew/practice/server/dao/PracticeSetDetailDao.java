package com.zhukew.practice.server.dao;

import com.zhukew.practice.server.entity.po.PracticeSetDetailPO;

import java.util.List;

public interface PracticeSetDetailDao {

    /**
     * 新增套题
     */
    int add(PracticeSetDetailPO po);

    List<PracticeSetDetailPO> selectBySetId(Long setId);


}