package com.github.cunvoas.geoserviceisochrone.config.hardening;

import org.apache.catalina.Container;
import org.apache.catalina.core.StandardHost;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * @see https://www.springcloud.io/post/2022-07/spring-boot-hardening/#gsc.tab=0
 */
@Configuration(proxyBeanMethods = false)
@Slf4j
public class ErrorConfig {

    // https://docs.spring.io/spring-boot/docs/2.5.4/reference/htmlsingle/#howto-use-tomcat-legacycookieprocessor
    // https://github.com/spring-projects/spring-boot/issues/21257#issuecomment-745565376
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> errorReportValveCustomizer() {
        return (factory) -> {
            factory.addContextCustomizers(context -> {
                final Container parent = context.getParent();
                if (parent instanceof StandardHost) {
                    // above class FQCN
                    ((StandardHost) parent).setErrorReportValveClass(CustomErrorReportValve.class.getName());
                }
            });
        };
    }

}
