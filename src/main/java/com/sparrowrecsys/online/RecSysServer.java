package com.sparrowrecsys.online;

import com.sparrowrecsys.online.datamanager.DataManager;
import com.sparrowrecsys.online.service.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;

/***
 * 推荐系统服务器（RecSys Server），提供在线推荐服务的端点
 */
public class RecSysServer {

    public static void main(String[] args) throws Exception {
        new RecSysServer().run(); // 启动服务器
    }

    // 推荐系统服务器的默认端口号
    private static final int DEFAULT_PORT = 6010;

    public void run() throws Exception {

        int port = DEFAULT_PORT;
        try {
            // 尝试从环境变量中获取端口号，如果未设置则使用默认端口号
            port = Integer.parseInt(System.getenv("PORT"));
        } catch (NumberFormatException ignored) {}

        // 设置服务器的 IP 地址和端口号
        InetSocketAddress inetAddress = new InetSocketAddress("127.0.0.1", port);
        Server server = new Server(inetAddress);

        // 获取 index.html 的路径
        URL webRootLocation = this.getClass().getResource("/webroot/index.html");
        if (webRootLocation == null) {
            throw new IllegalStateException("无法确定 webroot 的 URL 位置");
        }

        // 设置 index.html 为根页面
        URI webRootUri = URI.create(webRootLocation.toURI().toASCIIString().replaceFirst("/index.html$", "/"));
        System.out.printf("Web 根路径 URI: %s%n", webRootUri.getPath());

        // 加载数据到 DataManager
        DataManager.getInstance().loadData(
                webRootUri.getPath() + "sampledata/movies.csv", // 电影数据
                webRootUri.getPath() + "sampledata/links.csv",  // 链接数据
                webRootUri.getPath() + "sampledata/ratings.csv" // 用户评分数据
        );


        // 创建服务器上下文
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/"); // 设置根路径为 "/"
        context.setBaseResource(Resource.newResource(webRootUri)); // 设置根资源路径
        context.setWelcomeFiles(new String[] { "index.html" }); // 设置默认欢迎页面为 index.html
        context.getMimeTypes().addMimeMapping("txt", "text/plain;charset=utf-8"); // 设置 MIME 类型

        // 绑定不同的服务到不同的 Servlet
        context.addServlet(DefaultServlet.class, "/"); // 默认服务，返回静态页面
        context.addServlet(new ServletHolder(new MovieService()), "/getmovie"); // 电影服务
        context.addServlet(new ServletHolder(new UserService()), "/getuser"); // 用户服务
        context.addServlet(new ServletHolder(new SimilarMovieService()), "/getsimilarmovie"); // 获取相似电影服务
        context.addServlet(new ServletHolder(new RecommendationService()), "/getrecommendation"); // 获取推荐服务
        context.addServlet(new ServletHolder(new RecForYouService()), "/getrecforyou"); // 获取个性化推荐服务
        context.addServlet(new ServletHolder(new SearchService()), "/search"); // 搜索服务

        // 设置 URL 处理器
        server.setHandler(context);
        System.out.println("推荐系统服务器已启动。");

        // 启动服务器
        server.start();
        server.join();
    }
}
