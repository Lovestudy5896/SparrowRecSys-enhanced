package com.sparrowrecsys.online.service;

import com.sparrowrecsys.online.datamanager.DataManager;
import com.sparrowrecsys.online.datamanager.Movie;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class SearchService extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 获取搜索关键字
        String query = req.getParameter("query");
        if (query == null || query.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing query parameter.");
            return;
        }

        // 搜索电影
        List<Movie> matchedMovies = DataManager.getInstance().searchMovies(query);

        // 返回搜索结果
        resp.setContentType("application/json;charset=utf-8");
        resp.getWriter().write(toJson(matchedMovies));
//        // 如果没有找到匹配的电影，返回一个错误消息
//        if (matchedMovies.isEmpty()) {
//            String errorMessage = "{\"error\": \"抱歉，当前网页暂时没有该电影的资源\"}";
//            resp.getWriter().write(errorMessage);
//        } else {
//            // 否则返回匹配的电影列表
//            resp.getWriter().write(toJson(matchedMovies));
//        }
    }

    // 将搜索结果转换为 JSON 格式
    private String toJson(List<Movie> movies) {
        StringBuilder jsonBuilder = new StringBuilder("[");
        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            jsonBuilder.append(String.format(
                    "{\"movieId\": %d, \"title\": \"%s\", \"year\": \"%d\", \"rating\": %.1f, \"rateNumber\": %d, \"genres\": \"%s\"}",
                    movie.getMovieId(),
                    movie.getTitle().replace("\"", "\\\""), // 转义双引号
                    movie.getReleaseYear(),
                    movie.getAverageRating(),
                    movie.getRatingNumber(),
                    movie.getGenres()
            ));
            if (i < movies.size() - 1) {
                jsonBuilder.append(","); // 添加逗号分隔项
            }
        }
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }


}
