# 拦截器，过滤器的使用 #

在开发过程中，会经常遇到一些情况，比如 要将请求中的token，转换成userId,或者判断权限，参数加解密

这些问题都可以使用拦截器配合过滤器 或者 使用`@ControllerAdvice `配合`RequestBodyAdviceAdapter `与`ResponseBodyAdvice`

## 将token转换为userId ##

一般来说 ，token都是存放在request的header中，在后续逻辑代码中，我们需要将其转换为userId进行一些逻辑的操作。

### 处理逻辑 ###

request中的inputStream流只能被读取一次，为了后续MVC的读取，是需要进行一个流的包装的，同时，如果只配置一个拦截器是没有作用的，拦截器只是起到对于请求的拦截，没法对于请求进行变化，所以，还需要使用一个过滤器，请求会先被过滤器进行拦截，这时候可以对流进行一个包装，如果只是一个token的问题，就不需要使用拦截器了，后续有一个权限的判断，可以使用拦截器。

## 参数的加解密 ##

参数的加解密 有两种方法，一种是使用过滤器来完成，如果使用过滤器的话，是根据url的判断是否需要对于参数进行加解密，基本的做法就还是对于request流进行二次的封装，完成参数的解密，之后就返回的流再进行二次加密的处理

```java
@Slf4j
@WebFilter("/test/*")
public class MyFilter implements Filter {

    @Autowired
    private CipherProperties cipherProperties;
    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.info("进入过滤器，进行解密");
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        
        CustomHttpServletRequestWrapper requestWrapper = new CustomHttpServletRequestWrapper(httpRequest) ;
        
        String body = requestWrapper.getBody();
        byte[] decrypt = AESUtil.decrypt(body.getBytes(), cipherProperties.getKey().getBytes());
        requestWrapper.setBody(new String(decrypt));
        ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) servletResponse);
				//往下执行的请求，已经被改成封装后的请求了
        filterChain.doFilter(requestWrapper, responseWrapper);

        //进行加密
        byte[] bytes = responseWrapper.getBytes();

        String encrypt = AESUtil.encrypt(bytes, cipherProperties.getKey().getBytes());
        log.info("原本的内容:{},加密后的内容:{}",new String(bytes),encrypt);
        servletResponse.setContentType("application/json;charset=utf-8");
        servletResponse.setContentLength(JSON.toJSONBytes(encrypt).length);
        servletResponse.getOutputStream().write(JSON.toJSONBytes(encrypt));

    }
}
```



第二种是使用`@ControllerAdvice `配合`RequestBodyAdviceAdapter `与`ResponseBodyAdvice`

要使用这个方法的话，请求都要是使用 `@RequestBody`来请求，接收的数据也要是返回一个json字符串的

这个方法 配合注解的效果，个人感觉要在第一种方法之上。

```java
//注解类
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cipher {
    /**
     * 入参解密
     * @return
     */
    boolean requestDecrypt() default true;

    /**
     * 出参加密
     * @return
     */
    boolean responsEncrypt() default true;
}

//解密类
@Slf4j
@ControllerAdvice
public class DecryptRequestBodyAdvice extends RequestBodyAdviceAdapter {

    @Autowired
    private CipherProperties cipherProperties;
    /**
     * 判断这个接口是否支持
     * @param methodParameter
     * @param type
     * @param aClass
     * @return
     */
    @Override
    public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return methodParameter.getMethod().isAnnotationPresent(Cipher.class);
    }

    @SneakyThrows
    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) throws IOException {

        Cipher annotation = methodParameter.getMethod().getAnnotation(Cipher.class);
        log.info("开始解密");
        boolean decrypt = annotation.requestDecrypt();
        if(decrypt){
            try {
                byte[] body = new byte[httpInputMessage.getBody().available()];
                httpInputMessage.getBody().read(body);
                byte[] bytes = AESUtil.decrypt(body, cipherProperties.getKey().getBytes());
                final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                return new HttpInputMessage() {
                    @Override
                    public InputStream getBody() throws IOException {
                        return bais;
                    }

                    @Override
                    public HttpHeaders getHeaders() {
                        return httpInputMessage.getHeaders();
                    }
                };
            } catch (Exception e) {
                log.error("出错了",e);
                e.printStackTrace();
            }
        }

        return super.beforeBodyRead(httpInputMessage, methodParameter, type, aClass);
    }

}

//加密类
@ControllerAdvice
@Slf4j
public class EncryptResponse implements ResponseBodyAdvice<BaseResult> {

    @Autowired
    private CipherProperties cipherProperties;
    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return methodParameter.getMethod().isAnnotationPresent(Cipher.class);
    }

    @Override
    public BaseResult beforeBodyWrite(BaseResult o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        log.info("开始对返回值进行加密操作!");
        Cipher annotation = methodParameter.getMethod().getAnnotation(Cipher.class);
        boolean encrtpt = annotation.responsEncrypt();
        try {
            if(encrtpt){
                if(o.getMessage()!=null){
                    o.setMessage(AESUtil.encrypt(o.getMessage().getBytes(),cipherProperties.getKey().getBytes()));
                }
                if(o.getData()!=null){
                    byte[] bytes = StreamUtil.toByteArray(o.getData());
                    o.setMessage(AESUtil.encrypt(bytes,cipherProperties.getKey().getBytes()));
                }
            }
        } catch (Exception e) {
            log.error("加密出错",e);
        }
        return o;
    }
}

```

