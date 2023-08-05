package com.xue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xue.entity.model.User;
import com.xue.repository.dao.UserMapper;
import com.xue.service.LoginService;
import com.xue.util.HttpUtil;
import org.apache.catalina.connector.Connector;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@EnableScheduling
@SpringBootApplication()
@MapperScan("com.xue.repository.dao") //自动扫描com.xue.repository.dao下的文件
public class SpringbootLoginApplication {

	@Autowired
	private UserMapper dao;

	@Autowired
	private LoginService loginService;

	public static void main(String[] args) {
		SpringApplication.run(SpringbootLoginApplication.class, args);
	}

	@Scheduled(cron = "0 */1 * * * ?")
	public void sendClassRemind(){
		List<String> apps = new ArrayList<>();
		apps.add("MOMO2B");
		apps.add("MOMO");
		apps.add("MOMO_OFFICIAL");
		for(int i=0;i<apps.size();i++){
			String app = apps.get(i);
			loginService.sendClassRemind(app);
		}
	}

	@Scheduled(cron = "0 0 * * * ?")
	public void getOpenidOfficial(){
		List<String> apps = new ArrayList<>();
		apps.add("MOMO_OFFICIAL");
		for(int i=0;i<apps.size();i++){
			String app = apps.get(i);
			loginService.getOpenidOfficial(app);
		}
	}

	@Bean
	public ConfigurableServletWebServerFactory webServerFactory() {
		TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
		factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
			@Override
			public void customize(Connector connector) {
				connector.setProperty("relaxedQueryChars", "|{}[]%");
			}
		});
		return factory;
	}


}
