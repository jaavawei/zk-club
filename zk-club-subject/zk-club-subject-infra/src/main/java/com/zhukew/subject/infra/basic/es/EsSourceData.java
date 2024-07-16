package com.zhukew.subject.infra.basic.es;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
* Es元数据
*
* @auther: Wei
*/
@Data
public class EsSourceData implements Serializable {

    private String docId;

    private Map<String, Object> data;

}
