package com.xue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xue.util.HttpUtil;
import org.apache.catalina.connector.Connector;
import org.mybatis.spring.annotation.MapperScan;
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

@EnableScheduling
@SpringBootApplication()
@MapperScan("com.xue.repository.dao") //自动扫描com.xue.repository.dao下的文件
public class SpringbootLoginApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootLoginApplication.class, args);
	}

	@Scheduled(fixedRate = 60*1000)
	public void sendClassRemind(){
		String result = null;
		String token = null;
		String url = "https://api.weixin.qq.com/cgi-bin/token";
		String MOMO2B_param = "appid=wxc61d8f694d20f083&secret=ed083522ff79ac7dad24e115aecfbc08&grant_type=client_credential";
		result = HttpUtil.sendPost(url,MOMO2B_param);
		JSONObject jsonObject = JSON.parseObject(result);
		token = jsonObject.getString("access_token");

		String result_send = null;
		String openid = "oRRfU5TCmjXtbw9WsxnekwJAa72M";
		String tample3 ="{\"page\": \"pages/index/index\",\"touser\":\"openid\",\"template_id\":\"3BPMQuajTekT04oI8rCTKMB2iNO4XWdlDiMqR987TQk\",\"data\":{\"date1\":{\"value\": \"2022-11-01 10:30-11:30\"},\"thing2\":{\"value\": \"A1\"},\"name3\":{\"value\": \"小明\"},\"thing5\":{\"value\": \"记得来上课哦\"}}}";

		String url_send = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
		JSONObject queryJson = JSONObject.parseObject(tample3);
		queryJson.put("touser",openid);
		queryJson.getJSONObject("data").getJSONObject("date1").put("value","2023-02-02"+" " + "11:25-00:25".split("-")[0]);
		queryJson.getJSONObject("data").getJSONObject("thing2").put("value","书法_小班");
		queryJson.getJSONObject("data").getJSONObject("name3").put("value","李冰冰");

		String param="access_token="+ token +"&data=" + queryJson.toJSONString();
		System.out.printf("param:"+param);
		try {
			result_send = HttpUtil.sendPostJson(url_send,queryJson.toJSONString());
			System.out.printf("res:" + result_send);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.printf("sendClassRemind");
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
