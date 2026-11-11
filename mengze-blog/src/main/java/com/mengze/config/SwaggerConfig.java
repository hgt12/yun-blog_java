package com.mengze.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig
{
    @Bean
    public Docket customDocket()
    {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.mengze.controller"))
                .build();
    }
    private ApiInfo apiInfo()
    {
        //url改为最终部署后的网页url
        Contact contact = new Contact("梦泽", "https://example.com", "admin@example.com");
        return new ApiInfoBuilder()
                .title("前台接口文档")
                .description("这是云梦泽个人博客前台接口文档")
                .contact(contact)   // 联系方式
                .version("1.0")  // 版本
                .build();
    }
}
