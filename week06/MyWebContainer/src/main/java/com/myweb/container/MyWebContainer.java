package com.myweb.container;

/***
 * <p>Description: </p>
 *
 *
 * @return
 * @author chenhan
 * @date 2022/10/1 14:06
 * @version 1.0.0
 *
 */
public class MyWebContainer {
    public static void run(String[] args) throws Exception{
        MyWebContainerServer server = new MyWebContainerServer("com.myweb.webapp");
        server.start();
    }
}
