package com.sparrowrecsys.online.recprocess;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 为用户推荐电影的过程。
 */
public class RecForYouProcess {

    /**
     * 获取推荐的电影列表
     *
     * @param userId 用户 ID
     * @param size   推荐列表的大小
     * @param model  使用的推荐模型（当前仅支持 "knn"）
     * @return 推荐的电影ID列表
     */
    public static List<Integer> getRecList(int userId, int size, String model) {
        if (!"knn".equalsIgnoreCase(model)) {
            throw new IllegalArgumentException("仅支持 KNN 模型进行推荐");
        }

        List<Integer> recommendedMovieIds = new ArrayList<>();
        try {
            // 构造 Python 脚本路径列表
            // TODO:在列表项中添加本地路径
            List<String> scriptPaths = Arrays.asList(
                    "D:\\FileRecv\\SparrowRecSys-enhanced\\src\\main\\java\\com\\sparrowrecsys\\online\\recprocess\\python_codes\\recommend.py",
                    "C:\\Users\\Windows\\Documents\\codes\\SparrowRecSys-enhanced\\src\\main\\java\\com\\sparrowrecsys\\online\\recprocess\\python_codes\\recommend.py");

            String scriptPath = null;
            for (String path : scriptPaths) {
                if (new File(path).exists()) {
                    scriptPath = new File(path).getAbsolutePath();
                    break;
                }
            }

            if (scriptPath == null) {
                throw new IllegalStateException("未找到有效的 Python 脚本路径");
            }

            // 构造 Python 解释器路径列表
            // TODO:在列表项中添加本地路径
            List<String> pythonPaths = Arrays.asList("C:\\Users\\Lenovo\\miniconda3\\envs\\tf\\python.exe",
                    "C:\\Users\\Windows\\miniconda3\\envs\\movies-recommender\\python.exe");

            String pythonPath = null;
            for (String path : pythonPaths) {
                if (new File(path).exists()) {
                    pythonPath = path;
                    break;
                }
            }

            if (pythonPath == null) {
                throw new IllegalStateException("未找到有效的 Python 解释器路径");
            }

            // 构造 Python 脚本调用命令
            String[] command = new String[] { pythonPath, scriptPath, String.valueOf(userId), String.valueOf(size) };

            // 启动进程执行 Python 脚本
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // 读取 Python 脚本的标准输出
            StringBuilder jsonOutput = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonOutput.append(line);
            }

            // 输出 Python 脚本的错误信息
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            if (errorOutput.length() > 0) {
                System.out.println("[ERROR] Python脚本返回的错误信息: " + errorOutput.toString());
            }

            // 确保输出非空并以 "[" 开头
            String output = jsonOutput.toString().trim();
            if (output.isEmpty()) {
                throw new IllegalStateException("Python脚本未返回任何数据");
            }
            if (!output.startsWith("[")) {
                throw new IllegalStateException("Python脚本返回的数据不是有效的JSON数组: " + output);
            }

            // 解析 JSON 格式的推荐结果
            JSONArray jsonArray = new JSONArray(output);
            for (int i = 0; i < jsonArray.length(); i++) {
                recommendedMovieIds.add(jsonArray.getInt(i));
            }

        } catch (Exception e) {
            System.err.println("[ERROR] 获取推荐列表时发生异常: " + e.getMessage());
            e.printStackTrace();
        }

        return recommendedMovieIds;
    }
}
