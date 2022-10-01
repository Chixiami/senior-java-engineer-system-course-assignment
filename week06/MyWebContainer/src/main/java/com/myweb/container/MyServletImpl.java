package com.myweb.container;

import com.myweb.servlet.MyRequest;
import com.myweb.servlet.MyResponse;
import com.myweb.servlet.MyServlet;

/***
 * <p>Description: </p>
 *
 *
 * @return
 * @author chenhan
 * @date 2022/10/1 14:05
 * @version 1.0.0
 *
 */
public class MyServletImpl extends MyServlet {
    @Override
    public void doGet(MyRequest request, MyResponse response) throws Exception {
        String uri = request.getUri();
//         http://localhost:8080/aaa/bbb/userservlet?name=xiong
//         path：/aaa/bbb/userservlet?name=xiong
        response.write("404 - no this servlet : " + (uri.contains("?")?uri.substring(0,uri.lastIndexOf("?")):uri));
    }

    @Override
    public void doPost(MyRequest request, MyResponse response) throws Exception {
        doGet(request, response);
    }
}
