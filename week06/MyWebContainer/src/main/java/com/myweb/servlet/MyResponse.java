package com.myweb.servlet;

/***
 * <p>Description: Servlet规范之响应规范</p>
 *
 *
 * @return
 * @author chenhan
 * @date 2022/10/1 13:56
 * @version 1.0.0
 *
 */
public interface MyResponse {
    // 将响应写入到Channel
    void write(String content) throws Exception;
}
