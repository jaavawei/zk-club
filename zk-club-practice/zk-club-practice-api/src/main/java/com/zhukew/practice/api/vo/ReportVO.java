package com.zhukew.practice.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 评估报告VO
 *
 * @author: Wei
 */
@Data
public class ReportVO implements Serializable {

    /**
     * 试卷题目
     */
    private String title;

    /**
     * 正确题目数
     */
    private String correctSubject;

    /**
     * 技能图谱
     */
    private List<ReportSkillVO> skill;

}