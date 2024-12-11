package com.sparrowrecsys.online.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowrecsys.online.datamanager.DataManager;
import com.sparrowrecsys.online.datamanager.Movie;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * MovieService类，用于返回特定电影的信息
 */

public class MovieService extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        try {
            // 设置响应内容的类型为JSON格式
            response.setContentType("application/json");
            // 设置响应状态为200 OK
            response.setStatus(HttpServletResponse.SC_OK);
            // 设置响应的字符编码为UTF-8
            response.setCharacterEncoding("UTF-8");
            // 设置跨域允许，允许所有来源访问
            response.setHeader("Access-Control-Allow-Origin", "*");

            // 通过URL参数获取电影ID
            String movieId = request.getParameter("id");

            // 从DataManager获取对应ID的电影对象
            Movie movie = DataManager.getInstance().getMovieById(Integer.parseInt(movieId));

            // 将电影对象转换为JSON格式并返回给客户端
            if (null != movie) {
                ObjectMapper mapper = new ObjectMapper();
                String jsonMovie = mapper.writeValueAsString(movie);
                response.getWriter().println(jsonMovie);
            } else {
                // 如果电影对象为null，返回空字符串
                response.getWriter().println("");
            }

        } catch (Exception e) {
            // 捕获异常并打印堆栈信息，同时返回空字符串
            e.printStackTrace();
            response.getWriter().println("");
        }
    }
}
