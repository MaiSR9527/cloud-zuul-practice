# 一、Spring Cloud Zuul 过滤链

## 1.1 工作原理

Zuul的核心逻辑是由一系列的Filter来实现的，他们能够在进行HTTP请求或者相应的时候执行相关操作。Zuul Filter的主要特性有一下几点：

* Filter的类型：Filter的类型决定了它在Filter链中的执行顺序。路由动作发生前、路由动作发生时，路由动作发生后，也可能是路由过程发生异常时。
* Filter的执行顺序：同一种类型的Filter可以通过filterOrder()方法来设定执行顺序
* Filter的执行条件：Filter运行所需要的条件
* Filter的执行效果：符合某个Filter执行条件，产生的执行效果

Zuul内部提供了一个动态读取、编译和运行这些Filter的机制。Filter之间不能直接通信，在请求线程中通过RequestContext来共享状态，它的内部是用ThreadLocal实现的。

![](http://image.maishuren.top/springcloud/zuul-02.jpg-msr)

上图描述了Zuul关于Filter的请求生命周期。

* pre：在Zuul按照规则路由到下级服务之前执行。如果需要对请求进行预处理，比如鉴权、限流等，可在考虑在这类Filter中实现。
* route：这类Filter是Zuul路由动作的执行者，是Http客户端构建和发送HTTP请求的地方。
* post：这类Filter是在原服务返回结果或者异常信息发生后执行，如果需要对返回信息做一些处理，可以在此类Filter进行处理。
* error：在整个生命周期内如果发生异常，则会进入error Filter，可以做全局异常处理

其中post Filter抛出错误分成两种情况：

1）在post Filter抛错之前，pre、route Filter没有抛错，此时会进入ZuulException的逻辑，打印堆栈信息，然后再返回status=500的Error信息

2）再post Filter跑错之前，pre、route Filter已有跑错，此时不会打印堆栈信息，直接返回status=500的error信息。

也就是说整个责任链中重点不只是post Filter，还可能是error Filter。

在实际项目中，需要子实现以上类型的Filter来对链路进行处理，根据业务的需求，选取对应生命周期的Filter来达到目的。每个Filter之间通过RequestContext（Zuul包中）类来进行通信，内部采用ThreadLocal保存每个请求的一些信息，包括请求路由，错误信息，HttpServletRequest，HttpServletResponse，这使得一些操作十分可靠，它害扩展了ConcurrentHashMap，目的是为了在处理过程中保存任何形式的信息。

## 1.2 Zuul中的原生Filter

Zuul Server通过`@EnableZuulProxy`开启之后，搭配Spring Boot Actuator，会多两个管控断点。

在配置文件中配置一下：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: 'routes,filters'
```

> 1、/route：返回当前Zuul Server中已生成的映射规则，加上/details可查看明细。例如
>
> ![](http://image.maishuren.top/springcloud/zuul-04.png-msr)
>
> 每个路由的详细信息
>
> ![](http://image.maishuren.top/springcloud/zuul-03.png-msr)
>
> 2、/filters：返回当前Zuul Filter中已注册生效的Filter
>
> ![](http://image.maishuren.top/springcloud/zuul-05.png-msr)

从Filter的信息可以看到，所有已经注册生效的Filter的信息：Filter实现类的路径、Filter执行次序、是否被禁用、是否静态。而且很明显地可以看出Zuul内Filter的整个请求的生命流程，如下图：

![](http://image.maishuren.top/rpc/zuul-filter-time-line.jpg-msr)

Zuul中各内置的Filter：

| 名称                    | 类型  | 次序 | 描述                                                         |
| ----------------------- | ----- | ---- | ------------------------------------------------------------ |
| ServletDetectionFilter  | pre   | -3   | 通过Spring Dispatcher检查请求是否通过                        |
| Servlet30WrapperFilter  | pre   | -2   | 适配HttpServletRequest为Servlet30RequestWrapper对象          |
| FormBodyWrapperFilter   | pre   | -1   | 解析表单数据并为下游请求重新编码                             |
| DebugFiter              | pre   | 1    | Debug路由表示                                                |
| PreDecorationFilter     | pre   | 5    | 处理请求上下文共后续使用，设置下游相关信息头                 |
| RibbonRoutingFilter     | route | 10   | 使用Ribbon、Hystrix或者嵌入式HTTP客户端发送请求              |
| SimpleHostRoutingFilter | route | 100  | 使用Apache Httpclient转发请求                                |
| SendForwardFilter       | route | 500  | 使用Servlet转发请求                                          |
| SendResponseFilter      | post  | 1000 | 将代理请求的响应写入当前相应                                 |
| SendErrorFilter         | error | 0    | 如果RequestContext.getThrowable()不为空，则转发到error.path配置的路径 |

上表为使用`@EnableZuulProxy`之后安装的Filter，当使用`@EnableZuulServer`将会缺少PreDecorationFilter、RibbonRoutingFilter、SimpleHostRoutingFilter。这些原生的Filter可以关掉，例如：在配置文件里面配置`zuul.SendErrorFilter.error.disable=true`

## 1.3 多过滤器组成过滤链

在实际中我们不仅是只定义一个过滤器，而是多个过滤器组成过滤链来完成工作，除了Zuul的其他网关也是有这个功能

要在Zuul中自定义Filter子需要继承ZuulFilter即可。它是个抽象类，主要实现的几个方法：

* `String filterType()`：使用返回值定义Filter的类型，有pre、route、post、error
* `int filterOrder()`：使用返回值设置Filter的执行顺序
* `boolean shouldFilter()`：使用返回值设置Filter是否执行，即所定义Filter的开关
* `Object run()`：Filter里面的核心执行逻辑便需要写在该方法里面

自定义一个前置过滤器，如下：

```java
public class CustomPreFilter extends ZuulFilter {
    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        LOG.info("This is custom pre filter...");
        return null;
    }
}
```

将`FirstPreFilter`注入到Spring Bean容器

```java
@Configuration
public class ZuulFilterConfig {

    @Bean
    public CustomPreFilter customPreFilter() {
        return new CustomPreFilter();
    }
}
```

然后启动分别启动`eureka`、`zuul`、`service-a`，访问`http://localhost:88/servicea/add?a=1&b=2`。观察网关的日志输出

> INFO 20260 --- [  XNIO-1 task-1] c.m.better.zuul.filter.CustomPreFilter   : This is custom pre filter...

到这可以看到定义一个Zuul过滤器其实很简单，对于微服务网关来说不仅是Zuul，其他的微服务网关也是，很大部分的开发工作都是开发各种过滤器来达到我们目的。现在来实现一个简单的参数校验功能：

**FirstPreFilter:**

```java
public class FirstPreFilter extends ZuulFilter {
    private Logger log = LoggerFactory.getLogger(FirstPreFilter.class);

    @Override
    public String filterType() {
        // 自定义的过滤器类型为前置过滤器
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        // 自定义过滤器的执行次序
        return 2;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        log.info("first pre filter...");
        // 拿到请求上下文
        RequestContext requestContext = RequestContext.getCurrentContext();
        // 拿到HttpServletRequest
        HttpServletRequest request = requestContext.getRequest();
        // 获取传入的参数值
        String a = request.getParameter("a");
        if (StringUtils.isBlank(a)) {
            // 禁止路由，也就是不允许访问下游服务
            requestContext.setSendZuulResponse(false);
            // 设置响应结果，供PostFilter使用，参数是字符串，序列化一下返回对象也行。
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = new HashMap<>();
            map.put("code", -1);
            map.put("msg", "参数a不能为空");
            String result = null;
            try {
                result = mapper.writeValueAsString(map);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            requestContext.setResponseBody(result);
            // parameter-check-success保存于上下文，作为同类型下游Filter的执行开关
            requestContext.set("parameter-check-success", false);
            return null;
        }
        // 设置避免报空
        requestContext.set("parameter-check-success", true);
        return null;
    }
}
```

**SecondPreFilter:**

```java
public class SecondPreFilter extends ZuulFilter {
    private Logger log = LoggerFactory.getLogger(SecondPreFilter.class);

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 3;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        // 参数a是否检验成功，不成功那就没必要继续执行下去
        return (boolean) requestContext.get("parameter-check-success");
    }

    @Override
    public Object run() throws ZuulException {
        log.info("second pre filter...");
        // 拿到请求上下文
        RequestContext requestContext = RequestContext.getCurrentContext();
        // 拿到HttpServletRequest
        HttpServletRequest request = requestContext.getRequest();
        // 获取传入的参数值
        String b = request.getParameter("b");
        if (StringUtils.isBlank(b)) {
            // 禁止路由，也就是不允许访问下游服务
            requestContext.setSendZuulResponse(false);
            // 设置响应结果，供PostFilter使用，参数是字符串，序列化一下返回对象也行。
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = new HashMap<>();
            map.put("code", -1);
            map.put("msg", "参数b不能为空");
            String result = null;
            try {
                result = mapper.writeValueAsString(map);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            requestContext.setResponseBody(result);
            // parameter-check-success保存于上下文，作为同类型下游Filter的执行开关
            requestContext.set("parameter-check-success", false);
            return null;
        }
        return null;
    }
}
```

**CustomPostFilter:**

```java
public class CustomPostFilter extends ZuulFilter {

    private static final Logger LOG = LoggerFactory.getLogger(CustomPostFilter.class);

    @Override
    public String filterType() {
        return POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        System.out.println("这是PostFilter！");
        // 从RequestContext获取上下文
        RequestContext requestContext = RequestContext.getCurrentContext();
        // 处理返回中文乱码
        requestContext.getResponse().setCharacterEncoding("UTF-8");
        // 获取上下文中保存的responseBody
        String responseBody = requestContext.getResponseBody();
        // 如果responseBody不为空，则说明流程有异常发生
        if (null != responseBody) {
            //设定返回状态码
            requestContext.setResponseStatusCode(500);
            //替换响应报文
            requestContext.setResponseBody(responseBody);
        }
        return null;
    }
}
```

这整个小功能实现下来，体验到了Zuul中过滤器的执行顺序，以及通过`RequestContext`来获取`HttpServletRequest`得到请求信息。

# 二、Spring Cloud Zuul整合OAuth2+JWT入门实战

作为一个微服务网关，一般我们会在网关上进行鉴权，对于网关后面众多的无状态服务常用的授权和认证便是基于OAuth2。

## 2.1 什么是OAuth2和JWT

OAuth2是OAuth协议的第二个版本，是对授权认证比较成熟地面向资源的授权协议，在业界中广泛应用。出了定义了常用的用户名密码登录之后，还可以使用第三方一个用登录。例如在某些网站上可以使用QQ、微信、Github等进行登录。其主要流程如下：

![](http://image.maishuren.top/rpc/oauth2.jpg-msr)

至于JWT则是一种使用JSON格式来规约Token和Session的协议。因为传统的认证方式中会产生一个凭证，比如Session会话是保存在服务端，然后依赖于Cookie返回给客户端，Session是有状态的。但是对于众多的微服务来说又是无状态，便诞生像JWT这样的解决方案。

JWT通常有三部分组成：

* Header：头部，指定JWT使用的签名算法
* Payload：载荷，包含一些自定义或非自定义的认证信息
* Signature：签名，将头部和载荷用`.`连接之后，使用头部的签名算法生成的签名信息并拼接到末尾

OAuth2 + JWT 就是服务端使用OAuth2的方式进行认证，然后颁发一个Token，而这个Token使用JWT。客户端拿着这个Token，便可以访问系统，一般我们会给这个Token设置一个有效期，因为服务端并不会保存这个Token。OAuth2的实现有很多，这里使用Spring社区的基于`Spring Security`实现的OAuth2

## 2.2 Zuul + OAuth2 + JWT 入门实操

### 2.2.1 修改cloud-zuul-gateway

在Zuul网关中我们需要对接口的请求进行保护，判断是否登录鉴权。如果未登录需要重定向到登录页面，登录成功由认证服务器颁发JWT Token；把JWT Token放到请求头传递到下游服务器。

引入Maven依赖：

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-oauth2</artifactId>
</dependency>
```

配置文件：

* 首先定义了`service-a`服务的路由规则
* 注册中心Eureka的地址
* 验证授权端点：`http://localhost:7788/uaa/oauth/authorize`
* Token的颁发端点：`http://localhost:7788/uaa/oauth/token`
* 默认是使用HS256加密算法，密钥是`hahaha`。加密算法的话建议使用安全性更高的非堆成加密

```yaml
server:
  port: 88
spring:
  application:
    name: zuul-gateway
eureka:
  client:
    serviceUrl:
      defaultZone: http://${eureka.host:127.0.0.1}:${eureka.port:8671}/eureka/
  instance:
    prefer-ip-address: true
zuul:
  routes:
    service-a:
      path: /servicea/**
      serviceId: service-a
security:
  oauth2:
    client:
      access-token-uri: http://localhost:7788/uaa/oauth/token #令牌端点
      user-authorization-uri: http://localhost:7788/uaa/oauth/authorize #授权端点
      client-id: zuul-gateway #OAuth2客户端ID
      client-secret: my-secret #OAuth2客户端密钥
    resource:
      jwt:
        key-value: hahaha #使用对称加密方式，默认算法为HS256
```

`WebSecurity`的配置：主要是声明

```java
@Configuration
@Order(101)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/login", "/servicea/**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .csrf()
                .disable();
    }
}

```

在启动类上添加`@EnableOAuth2Sso`注解

```java
@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
@EnableOAuth2Sso
public class ZuulServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZuulServerApplication.class, args);
    }
}
```

### 2.2.2 编写认证服务器cloud-auth-server

创建`cloud-auth-server`来基于OAuth2 实现我们的认证服务器。依赖如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>cloud-zuul-practice-intermediate</artifactId>
        <groupId>com.msr.better</groupId>
        <version>1.0</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-auth-server</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-oauth2</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

配置文件`application.yml`

```yaml
spring:
  application:
    name: cloud-auth-server
server:
  port: 7788
  servlet:
    contextPath: /uaa
eureka:
  client:
    serviceUrl:
      defaultZone: http://${eureka.host:127.0.0.1}:${eureka.port:8671}/eureka/
  instance:
    prefer-ip-address: true
```

认证服务器配置：继承`AuthorizationServerConfigurerAdapter`编写认证授权服务器配置。主要是指定clientId、密钥、以及权限定义和作用域声明，指定`JwtTokenStore`，类似的实现Spring Security还有`RedisTokenStore`等。

```java
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
                .inMemory()
                .withClient("zuul-gateway")
                .secret("my-secret")
                .scopes("write", "read").autoApprove(true)
                .authorities("WRIGTH_READ", "WRIGTH_WRITE")
                .authorizedGrantTypes("implicit", "refresh_token", "password", "authorization_code")
                .redirectUris("http://localhost:88/login");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .tokenStore(jwtTokenStore())
                .tokenEnhancer(jwtTokenConverter())
                .authenticationManager(authenticationManager);
    }

    @Bean
    public TokenStore jwtTokenStore() {
        return new JwtTokenStore(jwtTokenConverter());
    }

    @Bean
    protected JwtAccessTokenConverter jwtTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("hahaha");
        return converter;
    }
}
```

Web Security 相关配置：声明guest用户，密码为guest，拥有READ权限。admin用户，密码为admin，拥有READ、WRITE权限。

`AuthenticationManager`是认证管理器，需要注入到Spring容器中。`passwordEncoder()`声明密码的加密方式，在Spring Security中要求需要对密码进行加密，因此需要向Spring容器中注入。但是这里使用了内存的方式存放用户信息，而且密码是原值保存，所以使用`NoOpPasswordEncoder`，即不做加密处理。

```java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public static NoOpPasswordEncoder passwordEncoder() {
        return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .inMemoryAuthentication()
                .withUser("guest").password("guest").authorities("READ")
                .and()
                .withUser("admin").password("admin").authorities("READ", "WRITE");
    }
}
```

认证服务器启动类：

```java
@SpringBootApplication
@EnableDiscoveryClient
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
```

### 2.2.3 cloud-service-a服务整合资源服务器

service-a的编写相对简单，在Spring Security OAuth2中，每个服务都是一个资源服务器，拥有者该服务的资源。

引入依赖：

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-security</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-oauth2</artifactId>
</dependency>
```

配置文件：

```yaml
server:
  port: 8080
  servlet:
    context-path: /servicea
spring:
  application:
    name: service-a
eureka:
  client:
    serviceUrl:
      defaultZone: http://${eureka.host:127.0.0.1}:${eureka.port:8671}/eureka/
  instance:
    prefer-ip-address: true
```

编写资源服务器：

```java
@Configuration
public class ServiceAResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/**").authenticated()
                .antMatchers(HttpMethod.GET, "/servicea/test")
                .hasAuthority("WRIGHT_READ");
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("WRIGHT")
                .tokenStore(jwtTokenStore());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter tokenConverter = new JwtAccessTokenConverter();
        tokenConverter.setSigningKey("hahaha");
        return tokenConverter;
    }

    @Bean
    public TokenStore jwtTokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }
}
```

编写`ClientController`:

```java
@RestController
@RequestMapping
public class ClientController {

    @GetMapping("/test")
    public String test(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("================header================");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            System.out.println(key + ": " + request.getHeader(key));
        }
        System.out.println("================header================");
        return "hello word!";
    }
}
```

servicea的启动类：启用资源服务器`@EnableResourceServer`

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableResourceServer
public class ServiceAApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceAApplication.class, args);
    }
}
```

### 2.2.4 测试

先启动注册中心Eureka、然后启动Zuul网关、serivce-a、auth-server。

请求访问：

> http://localhost:88/service/test

## OAuth2 + JWT 实战小总结

这里关于Zuul整合OAuth2 + JWT 的介绍就到这，后面会写一篇详细的`Spring Security`实现的OAuth2文章。本文这里用到的认证服务器和资源服务器是较为早期的写法了，前年`Spring Security`开了一个新项目专门来编写认证服务器。

Github链接：

> https://github.com/spring-projects/spring-security/tree/main/oauth2  Spring Security实现的OAuth2
>
> https://github.com/spring-projects/spring-authorization-server  Spring Security团队维护的认证服务器

