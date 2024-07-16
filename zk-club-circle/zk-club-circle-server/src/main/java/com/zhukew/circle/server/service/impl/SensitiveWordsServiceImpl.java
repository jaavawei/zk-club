package com.zhukew.circle.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhukew.circle.server.dao.SensitiveWordsMapper;
import com.zhukew.circle.server.entity.po.SensitiveWords;
import com.zhukew.circle.server.service.SensitiveWordsService;
import org.springframework.stereotype.Service;

/**
 * 敏感词表 服务实现类
 *
 * @author Wei
 * @since 2024/05/17
 */
@Service
public class SensitiveWordsServiceImpl extends ServiceImpl<SensitiveWordsMapper, SensitiveWords> implements SensitiveWordsService {

}
