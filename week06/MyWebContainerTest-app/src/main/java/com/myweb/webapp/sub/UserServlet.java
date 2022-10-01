package com.myweb.webapp.sub;

import com.myweb.servlet.MyRequest;
import com.myweb.servlet.MyResponse;
import com.myweb.servlet.MyServlet;

/**
 * 业务Servlet-Demo
 */
public class UserServlet extends MyServlet {
    @Override
    public void doGet(MyRequest request, MyResponse response) throws Exception {
        String uri = request.getUri();
        String path = request.getPath();
        String method = request.getMethod();
        String name = request.getParameter("name");

        String content = "uri = " + uri + "\n" +
                         "path = " + path + "\n" +
                         "method = " + method + "\n" +
                         "param = " + name;
        response.write(content);
    }

    @Override
    public void doPost(MyRequest request, MyResponse response) throws Exception {
        doGet(request, response);
    }
}
