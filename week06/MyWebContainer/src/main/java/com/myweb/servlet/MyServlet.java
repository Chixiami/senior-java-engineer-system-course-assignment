package com.myweb.servlet;

/***
 * <p>Description: 定义servlet规范</p>
 *
 *
 * @return
 * @author chenhan
 * @date 2022/10/1 13:55
 * @version 1.0.0
 *
 */
public abstract class MyServlet {
    public abstract void doGet(MyRequest request, MyResponse response) throws Exception;
    public abstract void doPost(MyRequest request, MyResponse response) throws Exception;
}
