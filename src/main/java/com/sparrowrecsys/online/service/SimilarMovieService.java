package com.sparrowrecsys.online.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowrecsys.online.datamanager.Movie;
import com.sparrowrecsys.online.recprocess.SimilarMovieProcess;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * SimilarMovieService 类：根据指定的电影推荐相似电影
 */
public class SimilarMovieService extends HttpServlet {

    /**
     * 处理 GET 请求，返回与指定电影相似的电影列表
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 设置响应的内容类型为 JSON，状态码为 200（成功），字符编码为 UTF-8
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK); // HTTP 状态码 200，表示请求成功
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*"); // 允许跨域请求

            // 获取请求参数：电影 ID、返回的电影数量、相似度计算的模型类型
            String movieId = request.getParameter("movieId"); // 从请求中获取电影 ID
            String size = request.getParameter("size"); // 获取返回的电影数量
            String model = request.getParameter("model"); // 获取计算相似度的模型（例如：embedding、graph-embedding）

            // 使用 SimilarMovieProcess 处理逻辑获取相似电影列表
            List<Movie> movies = SimilarMovieProcess.getRecList(
                    Integer.parseInt(movieId), // 将 movieId 转换为整数
                    Integer.parseInt(size), // 将 size 转换为整数
                    model // 使用指定的模型类型（如：embedding 或 graph-embedding）
            );

            // 将相似电影列表转换为 JSON 格式并返回给客户端
            ObjectMapper mapper = new ObjectMapper(); // 创建 ObjectMapper 用于转换为 JSON
            String jsonMovies = mapper.writeValueAsString(movies); // 将电影列表转换为 JSON 字符串
            response.getWriter().println(jsonMovies); // 将 JSON 写入响应输出流

        } catch (Exception e) {
            // 如果发生异常，打印堆栈信息并返回空字符串
            e.printStackTrace();
            response.getWriter().println("");
        }
    }
}
