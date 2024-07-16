package com.zhukew.subject.infra.basic.es;

import lombok.Data;

import java.io.Serializable;

/**
* Es请求索引信息类
*
* @auther: Wei
*/
@Data
public class EsIndexInfo implements Serializable {

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 索引名称
     */
    private String indexName;

}
