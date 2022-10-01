package com.myweb.container;

import com.alibaba.fastjson.JSON;
import com.myweb.servlet.MyRequest;
import com.myweb.servlet.MyResponse;
import com.myweb.servlet.MyServlet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.ResponseCache;
import java.util.Map;

/***
 * <p>Description: </p>
 *   1）从用户请求URI中解析出要访问的Servlet名称
 *   2）从nameToServletMap中查找是否存在该名称的key。若存在，则直接使用该实例，否则执行第3）步
 *   3）从nameToClassNameMap中查找是否存在该名称的key，若存在，则获取到其对应的全限定性类名，
 *      使用反射机制创建相应的serlet实例，并写入到nameToServletMap中，若不存在，则直接访问默认Servlet
 * @return
 * @author chenhan
 * @date 2022/10/1 14:07
 * @version 1.0.0
 *
 */
public class MyWebHandler extends ChannelInboundHandlerAdapter {

    private Map<String, MyServlet> nameToServletMap;//线程安全  servlet--> 对象
    private Map<String, String> nameToClassNameMap;//线程不安全  servlet--> 全限定名称

    public MyWebHandler(Map<String, MyServlet> nameToServletMap, Map<String, String> nameToClassNameMap) {
        this.nameToServletMap = nameToServletMap;
        this.nameToClassNameMap = nameToClassNameMap;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        System.out.println("ctx" + JSON.toJSONString(ctx));

        System.out.println("msg" + JSON.toJSONString(msg));

        System.out.println("msg instanceof HttpRequest:" + (msg instanceof HttpRequest));

        if(msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            String uri = request.uri();
            // 从请求中解析出要访问的Servlet名称
            //aaa/bbb/twoservlet?name=aa
            System.out.println("解析出的uri为：" + uri);
            if(uri.startsWith("/static/index.html")) {
                handleStatic(ctx, uri, request);
            } else if (uri.contains("?") && uri.contains("/")){
                handleServlet(request, ctx, uri);
            }

        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    // 处理servlet
    private void handleServlet(HttpRequest request, ChannelHandlerContext ctx, String uri) throws Exception {
        String servletName = "";
        servletName= uri.substring(uri.lastIndexOf("/") + 1, uri.indexOf("?"));
        System.out.println("解析出的servlet名称为：" + servletName);
        System.out.println("nameToServletMap" + JSON.toJSONString(nameToServletMap));
        System.out.println("nameToClassNameMap" + JSON.toJSONString(nameToClassNameMap));
        MyServlet servlet = new MyServletImpl();
        //第一次访问，Servlet是不会被加载的
        //初始化加载的只是类全限定名称，懒加载
        //如果访问Servlet才会去初始化它对象
        if (nameToServletMap.containsKey(servletName)) {
            servlet = nameToServletMap.get(servletName);
        } else if (nameToClassNameMap.containsKey(servletName)) {

            if (nameToServletMap.get(servletName) == null) {
                synchronized (this) {
                    if (nameToServletMap.get(servletName) == null) {
                        // 获取当前Servlet的全限定性类名
                        String className = nameToClassNameMap.get(servletName);
                        // 使用反射机制创建Servlet实例
                        servlet = (MyServlet) Class.forName(className).newInstance();
                        // 将Servlet实例写入到nameToServletMap
                        nameToServletMap.put(servletName, servlet);
                    }
                }
            }
        } //  end-else if
        // 代码走到这里，servlet肯定不空
        MyRequest req = new MyRequestImpl(request);
        MyResponse res = new MyResponseImpl(request, ctx);
        // 根据不同的请求类型，调用servlet实例的不同方法
        if (request.method().name().equalsIgnoreCase("GET")) {
            servlet.doGet(req, res);
        } else if(request.method().name().equalsIgnoreCase("POST")) {
            servlet.doPost(req, res);
        }
        ctx.close();
    }

    // 处理静态资源
    private void handleStatic(ChannelHandlerContext ctx, String uri, HttpRequest msg) throws Exception {

        String url =  this.getClass().getResource("/").getPath() + "com/myweb/webapp" + uri;
        System.out.println("文件路径：" + url);
        File file = new File(url);
        if (!file.exists()) {
            handleNotFound(ctx, msg);
            return;
        }
        if (file.isDirectory()) {
            handleDirectory(ctx, file, msg);
            return;
        }
        handleFile(ctx, file, msg);
    }

    /**
     * <p>Description: 处理文件</p>
     *
     * @author chenhan
     * @date 2022/10/1 20:00
     * @version 1.0.0
     */
    private void handleFile(ChannelHandlerContext ctx, File file, HttpRequest msg) throws Exception {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        HttpHeaders headers = getContentTypeHeader(file);
        HttpResponse response = new DefaultHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK, headers);
        ctx.write(response);
        ctx.write(new DefaultFileRegion(raf.getChannel(), 0, raf.length()));
        ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * <p>Description: 处理目录</p>
     *
     * @author chenhan
     * @date 2022/10/1 20:00
     * @version 1.0.0
     */
    private void handleDirectory(ChannelHandlerContext ctx, File file, HttpRequest msg) {
        StringBuilder sb = new StringBuilder();
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isHidden() || !f.canRead()) {
                    continue;
                }
                String name = f.getName();
                sb.append(name).append("<br/>");
            }
        }
        ByteBuf buffer = ctx.alloc().buffer(sb.length());
        buffer.writeCharSequence(sb.toString(), CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK, buffer);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        ChannelFuture future = ctx.writeAndFlush(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * <p>Description: 处理未找到文件</p>
     *
     * @author chenhan
     * @date 2022/10/1 20:05
     * @version 1.0.0
     */
    private void handleNotFound(ChannelHandlerContext ctx, HttpRequest msg) {
        ByteBuf content = Unpooled.copiedBuffer("URL not found", CharsetUtil.UTF_8);
        HttpResponse response = new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.NOT_FOUND, content);
        ChannelFuture future = ctx.writeAndFlush(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    private HttpHeaders getContentTypeHeader(File file) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        HttpHeaders headers = new DefaultHttpHeaders();
        String contentType = mimeTypesMap.getContentType(file);
        if (contentType.equals("text/plain")) {
            //由于文本在浏览器中会显示乱码，此处指定为utf-8编码
            contentType = "text/plain;charset=utf-8";
        }
        headers.set(HttpHeaderNames.CONTENT_TYPE, contentType);
        return headers;
    }


}
