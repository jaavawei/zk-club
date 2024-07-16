package com.zhukew.subject.infra.basic.es;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
* Es配置属性
*
* @auther: Wei
*/
@Component
@ConfigurationProperties(prefix = "es.cluster")
public class EsConfigProperties {

    /**
    * 拿到配置文件中es集群的所有配置信息：name-nodes
    */
    private List<EsClusterConfig> esConfigs = new ArrayList<>();

    public List<EsClusterConfig> getEsConfigs() {
        return esConfigs;
    }

    public void setEsConfigs(List<EsClusterConfig> esConfigs) {
        this.esConfigs = esConfigs;
    }
}
