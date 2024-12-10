package com.sparrowrecsys.online.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowrecsys.online.recprocess.RecForYouProcess;
import com.sparrowrecsys.online.util.ABTest;
import com.sparrowrecsys.online.datamanager.DataManager;
import com.sparrowrecsys.online.datamanager.Movie;
import com.sparrowrecsys.online.util.Config;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * RecForYouService 类：为用户提供个性化电影推荐服务
 */
public class RecForYouService extends HttpServlet {

    /**
     * 处理 GET 请求，返回为用户推荐的电影列表
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 设置响应的内容类型为 JSON，状态码为 200（成功），字符编码为 UTF-8
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK); // HTTP 状态码 200，表示请求成功
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*"); // 允许跨域请求

            // 从请求中获取用户 ID（id 参数）
            String userId = request.getParameter("id");
            // 获取返回的电影数量（size 参数）
            String size = request.getParameter("size");
            // 获取排序算法（model 参数）
            String model = request.getParameter("model");

            // 如果启用了 A/B 测试，根据用户 ID 获取相应的模型
            if (Config.IS_ENABLE_AB_TEST) {
                model = ABTest.getConfigByUserId(userId);
            }

            // 使用 RecForYouProcess 进行推荐，获取指定数量的电影推荐列表
            List<Integer> movie_ids = RecForYouProcess.getRecList(Integer.parseInt(userId), Integer.parseInt(size),
                    model);
            List<Movie> movies = new ArrayList<>();
            for (int movie_id : movie_ids) {
                Movie movie = DataManager.getInstance().getMovieById(movie_id);
                if (movie != null) {
                    movies.add(movie); // 只添加非空的电影对象
                } else {
                    System.out.println("警告: 电影ID " + movie_id + " 对应的电影信息不存在，已跳过。");
                }
            }
            // List<Movie> movies = RecForYouProcess.getRecList(Integer.parseInt(userId),
            // Integer.parseInt(size), model);

            // 将电影列表转换为 JSON 格式并返回给客户端
            ObjectMapper mapper = new ObjectMapper(); // 创建 ObjectMapper 对象用于将 Java 对象转换为 JSON
            String jsonMovies = mapper.writeValueAsString(movies); // 将电影列表转换为 JSON 字符串
            response.getWriter().println(jsonMovies); // 将 JSON 字符串写入响应输出流

        } catch (Exception e) {
            // 如果发生异常，打印异常堆栈并返回空字符串
            e.printStackTrace();
            response.getWriter().println("");
        }
    }
}
