package com.sparrowrecsys.online.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrowrecsys.online.datamanager.DataManager;
import com.sparrowrecsys.online.datamanager.Movie;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * RecommendationService 类：根据不同的输入提供电影推荐服务
 */
public class RecommendationService extends HttpServlet {

    /**
     * 处理 GET 请求，返回推荐的电影列表
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 设置响应的内容类型为 JSON，状态码为 200（成功），字符编码为 UTF-8
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK); // HTTP 状态码 200，表示请求成功
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*"); // 允许跨域请求

            // 从请求中获取电影类别（genre）、返回的电影数量（size）、排序方式（sortby）和年份（year）
            String genre = request.getParameter("genre"); // 获取电影类别（如：动作、爱情等）
            String year = request.getParameter("year");   // 获取年份
            String size = request.getParameter("size");   // 获取返回电影的数量
            String sortby = request.getParameter("sortby"); // 获取排序方式（如：按评分排序等）

            // 调用 DataManager 获取指定类别或年份的电影数据
            List<Movie> movies = null;
            if (genre != null) {
                movies = DataManager.getInstance().getMoviesByGenre(genre, Integer.parseInt(size), sortby);
            } else if (year != null) {
                movies = DataManager.getInstance().getMoviesByYear(year, Integer.parseInt(size));
            }

            // 使用 Jackson 库将电影列表转换为 JSON 格式并返回给客户端
            ObjectMapper mapper = new ObjectMapper(); // 创建 ObjectMapper 对象用于转换 Java 对象为 JSON 格式
            String jsonMovies = mapper.writeValueAsString(movies); // 将电影列表转换为 JSON 字符串
            response.getWriter().println(jsonMovies); // 将 JSON 数据写入响应输出流

        } catch (Exception e) {
            // 如果发生异常，打印堆栈信息并返回空字符串
            e.printStackTrace();
            response.getWriter().println("");
        }
    }

}
