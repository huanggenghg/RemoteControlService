package com.lumostech.remotecontrol.token;

import com.lumostech.remotecontrol.controller.RemoteControlController;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("token-only")
@EnableAutoConfiguration(
        exclude = DataSourceAutoConfiguration.class,
        excludeName = "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration"
)
@Import({ZegoTokenConfiguration.class, RemoteControlController.class, TokenHealthController.class})
public class TokenRuntimeConfiguration {}
