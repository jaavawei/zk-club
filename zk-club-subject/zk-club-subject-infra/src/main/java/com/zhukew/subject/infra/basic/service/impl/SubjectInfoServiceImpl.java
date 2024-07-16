package com.zhukew.subject.infra.basic.service.impl;

import com.zhukew.subject.infra.basic.entity.SubjectInfo;
import com.zhukew.subject.infra.basic.mapper.SubjectInfoDao;
import com.zhukew.subject.infra.basic.service.SubjectInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 题目信息表(SubjectInfo)表服务实现类
 *
 * @author Wei
 * @since 2023-10-05 21:28:58
 */
@Service("subjectInfoService")
public class SubjectInfoServiceImpl implements SubjectInfoService {
    @Resource
    private SubjectInfoDao subjectInfoDao;

    /**
     * 通过ID查询单条数据
     */
    @Override
    public SubjectInfo queryById(Long id) {
        return this.subjectInfoDao.queryById(id);
    }

    /**
     * 新增数据
     */
    @Override
    public SubjectInfo insert(SubjectInfo subjectInfo) {
        this.subjectInfoDao.insert(subjectInfo);
        return subjectInfo;
    }

    /**
     * 修改数据
     */
    @Override
    public SubjectInfo update(SubjectInfo subjectInfo) {
        this.subjectInfoDao.update(subjectInfo);
        return this.queryById(subjectInfo.getId());
    }

    /**
     * 通过主键删除数据
     */
    @Override
    public boolean deleteById(Long id) {
        return this.subjectInfoDao.deleteById(id) > 0;
    }

    /**
     * 根据条件计数
     */
    @Override
    public int countByCondition(SubjectInfo subjectInfo, Long categoryId, Long labelId) {
        return this.subjectInfoDao.countByCondition(subjectInfo, categoryId, labelId);
    }

    /**
     * 分页查询
     */
    @Override
    public List<SubjectInfo> queryPage(SubjectInfo subjectInfo, Long categoryId, Long labelId, int start, Integer pageSize) {
        return this.subjectInfoDao.queryPage(subjectInfo, categoryId, labelId, start, pageSize);
    }

    /**
     * 查询贡献题目数量
     */
    @Override
    public List<SubjectInfo> getContributeCount() {
        return this.subjectInfoDao.getContributeCount();
    }

    /**
     * 查询题目游标
     * cursor标识上一题或下一题
     */
    @Override
    public Long querySubjectIdCursor(Long subjectId, Long categoryId, Long labelId, int cursor) {
        return this.subjectInfoDao.querySubjectIdCursor(subjectId, categoryId, labelId, cursor);
    }

}
