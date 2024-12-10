package com.sparrowrecsys.online.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowrecsys.online.datamanager.DataManager;
import com.sparrowrecsys.online.datamanager.User;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * UserService类：提供特定用户信息的API服务
 */
public class UserService extends HttpServlet {

    /**
     * 处理GET请求，返回特定用户的信息
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 设置响应的内容类型为JSON，响应状态码为200，字符编码为UTF-8
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK); // HTTP状态码200，表示请求成功
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*"); // 允许跨域访问

            // 从请求中获取用户ID（通过URL参数）
            String userId = request.getParameter("id");

            // 从DataManager获取指定ID的用户对象
            User user = DataManager.getInstance().getUserById(Integer.parseInt(userId));

            // 如果用户对象不为空，将用户信息转换为JSON格式并返回给客户端
            if (null != user) {
                ObjectMapper mapper = new ObjectMapper(); // 使用Jackson库的ObjectMapper将Java对象转换为JSON字符串
                String jsonUser = mapper.writeValueAsString(user); // 将User对象转为JSON字符串
                response.getWriter().println(jsonUser); // 将JSON字符串写入响应输出流
            } else {
                // 如果用户对象为空，则返回空字符串
                response.getWriter().println("");
            }

        } catch (Exception e) {
            // 如果发生异常，打印异常堆栈并返回空字符串
            e.printStackTrace();
            response.getWriter().println("");
        }
    }
}
