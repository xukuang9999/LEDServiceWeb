package com.zhglxt.web.config;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.realm.NullRealm;
import org.apache.catalina.startup.Tomcat;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tomcat 配置类
 * 解决 UserDatabase 组件缺失问题和 APR 警告
 * 
 * @author zhglxt
 */
@Configuration
public class TomcatConfig {

    @Bean
    public TomcatServletWebServerFactory tomcatServletWebServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory() {
            @Override
            protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
                // 移除 APR 生命周期监听器以避免警告
                LifecycleListener[] listeners = tomcat.getServer().findLifecycleListeners();
                for (LifecycleListener listener : listeners) {
                    if (listener instanceof AprLifecycleListener) {
                        tomcat.getServer().removeLifecycleListener(listener);
                    }
                }
                return super.getTomcatWebServer(tomcat);
            }
        };
        
        // 添加上下文定制器来解决 UserDatabase 问题
        factory.addContextCustomizers(new TomcatContextCustomizer() {
            @Override
            public void customize(Context context) {
                // 使用 NullRealm 替代默认的 UserDatabaseRealm
                context.setRealm(new NullRealm());
                // 禁用会话持久化以避免相关警告
                context.setManager(null);
            }
        });
        
        // 添加连接器定制器
        factory.addConnectorCustomizers(connector -> {
            // 设置连接器属性以减少警告
            connector.setProperty("server", "Apache-Tomcat");
        });
        
        return factory;
    }
}