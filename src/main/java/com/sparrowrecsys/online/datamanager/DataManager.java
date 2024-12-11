package com.sparrowrecsys.online.datamanager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// 移除或注释掉 opencsv 的 CSVParser 导入，因为我们使用的是 Apache Commons CSV
// import au.com.bytecode.opencsv.CSVParser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DataManager是一个工具类，负责所有数据加载逻辑。
 */
public class DataManager {
    private static volatile DataManager instance;

    // 电影映射，key为电影ID，value为电影对象
    HashMap<Integer, Movie> movieMap;
    // 用户映射，key为用户ID，value为用户对象
    HashMap<Integer, User> userMap;
    // 按类型反向索引，用于快速查询某个类型的所有电影
    HashMap<String, List<Movie>> genreReverseIndexMap;
    // 新增：电影ID到中文/英文名称的映射
    HashMap<Integer, String> movieIdToNameMap;

    private DataManager() {
        this.movieMap = new HashMap<>();
        this.userMap = new HashMap<>();
        this.genreReverseIndexMap = new HashMap<>();
        this.movieIdToNameMap = new HashMap<>(); // 初始化映射表
        instance = this;
    }

    public static DataManager getInstance() {
        if (null == instance) {
            synchronized (DataManager.class) {
                if (null == instance) {
                    instance = new DataManager();
                }
            }
        }
        return instance;
    }

    public void loadData(String movieDataPath, String linkDataPath, String ratingDataPath) throws Exception {
        loadMovieNameMapping(movieDataPath); // 新增：加载 movie_id_to_name_sample.csv 文件
        loadMovieData(movieDataPath); // 加载电影数据
        loadLinkData(linkDataPath); // 加载链接数据
        loadRatingData(ratingDataPath); // 加载评分数据
    }

    private void loadMovieNameMapping(String movieDataPath) throws Exception {
        // 检查 movieDataPath 是否为空或无效
        if (movieDataPath == null || movieDataPath.isEmpty()) {
            throw new IllegalArgumentException("电影数据路径不能为空");
        }

        // 手动查找最后一个斜杠的位置（无论是正斜杠还是反斜杠）
        int lastSlashIndex = Math.max(movieDataPath.lastIndexOf('/'), movieDataPath.lastIndexOf('\\'));
        if (lastSlashIndex == -1) {
            throw new IllegalArgumentException("无法找到路径中的斜杠: " + movieDataPath);
        }

        // 构建 movie_id_to_name_sample.csv 的路径
        String parentDir = movieDataPath.substring(0, lastSlashIndex);
        String movieNamePath = parentDir + "/movie_id_to_name_sample.csv";
        System.out.println("从 " + movieNamePath + " 加载电影ID到名称的映射...");

        File movieFile = new File(movieNamePath);

        // 检查文件是否存在
        if (!movieFile.exists() || !movieFile.isFile()) {
            throw new FileNotFoundException("文件不存在或无法读取: " + movieNamePath);
        }

        // 检查文件大小
        if (movieFile.length() == 0) {
            throw new IOException("文件为空: " + movieNamePath);
        }

        // 使用 BufferedReader 逐行读取文件，并指定 UTF-8 编码
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(movieFile), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            String[] headers = null;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    // 假设第一行是标题行，提取列名
                    headers = line.split(",");
                    isFirstLine = false;
                    continue;
                }

                // 解析每一行的数据
                String[] values = line.split(",", -1); // -1 表示保留空字段

                if (headers == null || headers.length != values.length) {
                    System.err.println("数据行和标题行列数不匹配，跳过此行: " + line);
                    continue;
                }

                try {
                    int movieId = Integer.parseInt(values[0].trim());
                    String movieName = values[1].trim();
                    movieIdToNameMap.put(movieId, movieName); // 存储映射关系
                } catch (NumberFormatException e) {
                    System.err.println("无法解析的电影ID，跳过此行: " + line);
                }
            }
        } catch (IOException e) {
            throw new IOException("读取文件时出错: " + e.getMessage(), e);
        }

        System.out.println("电影名称映射加载完成，共加载 " + movieIdToNameMap.size() + " 条映射。");
    }

    private void loadMovieData(String movieDataPath) throws Exception {
        System.out.println("从 " + movieDataPath + " 加载电影数据...");

        int totalLines = 0;
        int skippedLines = 0;
        int invalidIdCount = 0;
        int missingGenreCount = 0;
        List<String> errorLines = new ArrayList<>(); // 保存有问题的行，用于调试或进一步分析

        try (FileReader fileReader = new FileReader(new File(movieDataPath));
             CSVParser parser = CSVParser.parse(fileReader, CSVFormat.DEFAULT.withHeader().withTrim())) {

            for (CSVRecord record : parser) {
                totalLines++;

                try {
                    Movie movie = new Movie();
                    String movieIdStr = record.get(0);
                    String title = record.get(1);
                    String genres = record.get(2);

                    try {
                        int movieId = Integer.parseInt(movieIdStr.trim());
                        movie.setMovieId(movieId);
                    } catch (NumberFormatException e) {
                        skippedLines++;
                        invalidIdCount++;
                        errorLines.add(record.toString());
                        continue;
                    }

                    // 优先从 movieIdToNameMap 获取名称
                    String mappedTitle = movieIdToNameMap.get(movie.getMovieId());
                    if (mappedTitle == null) {
                        // 如果没有映射，使用原始数据中的标题
                        int releaseYear = parseReleaseYear(title.trim());
                        if (releaseYear != -1) {
                            movie.setReleaseYear(releaseYear);
                            movie.setTitle(title.trim().substring(0, title.trim().length() - 6).trim());
                        } else {
                            movie.setTitle(title.trim());
                        }
                    } else {
                        movie.setTitle(mappedTitle);
                    }

                    if (!genres.trim().isEmpty()) {
                        String[] genreArray = genres.split("\\|");
                        for (String genre : genreArray) {
                            movie.addGenre(genre.trim()); // 添加类型
                            addMovie2GenreIndex(genre.trim(), movie); // 添加到类型反向索引
                        }
                    } else {
                        missingGenreCount++;
                    }

                    this.movieMap.put(movie.getMovieId(), movie);

                } catch (Exception e) {
                    // 捕获所有其他异常，并记录错误信息
                    skippedLines++;
                    errorLines.add(record.toString());
                }
            }
        } catch (IOException e) {
            System.err.println("文件读取错误: " + movieDataPath);
            throw e;
        }

        // 打印统计信息
        System.out.println("电影数据加载完成，共加载 " + this.movieMap.size() + " 部电影。");
        System.out.println("总共处理了 " + totalLines + " 行数据。");
        if (skippedLines > 0) {
            System.out.println("跳过了 " + skippedLines + " 行数据，原因如下：");
            if (invalidIdCount > 0) {
                System.out.println("- 无法解析movieId: " + invalidIdCount + " 行");
            }
            if (missingGenreCount > 0) {
                System.out.println("- 缺失类型信息: " + missingGenreCount + " 行");
            }
            if (errorLines.size() > 0) {
                System.out.println("部分有问题的行（前10行）：");
                for (int i = 0; i < Math.min(errorLines.size(), 10); i++) {
                    System.out.println("  " + errorLines.get(i));
                }
            }
        } else {
            System.out.println("所有行数据均成功加载。");
        }
    }

    private int parseReleaseYear(String rawTitle) {
        if (null == rawTitle || rawTitle.trim().length() < 6) {
            return -1;
        } else {
            String yearString = rawTitle.trim().substring(rawTitle.length() - 5, rawTitle.length() - 1);
            try {
                return Integer.parseInt(yearString);
            } catch (NumberFormatException exception) {
                return -1;
            }
        }
    }

    private void loadLinkData(String linkDataPath) throws Exception {
        System.out.println("从 " + linkDataPath + " 加载链接数据...");
        int count = 0;
        boolean skipFirstLine = true;
        try (Scanner scanner = new Scanner(new File(linkDataPath))) {
            while (scanner.hasNextLine()) {
                String linkRawData = scanner.nextLine();
                if (skipFirstLine) {
                    skipFirstLine = false;
                    continue;
                }
                String[] linkData = linkRawData.split(",");
                if (linkData.length == 3) {
                    int movieId = Integer.parseInt(linkData[0]);
                    Movie movie = this.movieMap.get(movieId);
                    if (null != movie) {
                        count++;
                        movie.setImdbId(linkData[1].trim()); // 设置IMDB ID
                        movie.setTmdbId(linkData[2].trim()); // 设置TMDB ID
                    }
                }
            }
        }
        System.out.println("链接数据加载完成，共加载 " + count + " 条链接。");
    }

    private void loadRatingData(String ratingDataPath) throws Exception {
        System.out.println("从 " + ratingDataPath + " 加载评分数据...");
        boolean skipFirstLine = true;
        int count = 0;
        try (Scanner scanner = new Scanner(new File(ratingDataPath))) {
            while (scanner.hasNextLine()) {
                String ratingRawData = scanner.nextLine();
                if (skipFirstLine) {
                    skipFirstLine = false;
                    continue;
                }
                String[] linkData = ratingRawData.split(",");
                if (linkData.length == 4) {
                    count++;
                    Rating rating = new Rating();
                    rating.setUserId(Integer.parseInt(linkData[0]));
                    rating.setMovieId(Integer.parseInt(linkData[1]));
                    rating.setScore(Float.parseFloat(linkData[2]));
                    rating.setTimestamp(Long.parseLong(linkData[3]));
                    Movie movie = this.movieMap.get(rating.getMovieId());
                    if (null != movie) {
                        movie.addRating(rating); // 添加到电影评分列表
                    }
                    if (!this.userMap.containsKey(rating.getUserId())) {
                        User user = new User();
                        user.setUserId(rating.getUserId());
                        this.userMap.put(user.getUserId(), user);
                    }
                    this.userMap.get(rating.getUserId()).addRating(rating); // 添加到用户评分列表
                }
            }
        }
        System.out.println("评分数据加载完成，共加载 " + count + " 条评分。");
    }

    private void addMovie2GenreIndex(String genre, Movie movie) {
        if (!this.genreReverseIndexMap.containsKey(genre)) {
            this.genreReverseIndexMap.put(genre, new ArrayList<>());
        }
        this.genreReverseIndexMap.get(genre).add(movie);
    }


    public List<Movie> getMoviesByYear(String year, int size) {
        // 获取日志记录器
        Logger logger = LoggerFactory.getLogger(getClass());

        if (year != null) {
            // 输出接收到的参数
            logger.info("Received year: " + year);

            try {
                // 将年份字符串转换为整数
                int targetYear = Integer.parseInt(year);
                logger.info("Parsed targetYear: " + targetYear);

                // 使用 Stream API 过滤出指定年份的电影
                List<Movie> movies = this.movieMap.values().stream()
                        .filter(movie -> movie.getReleaseYear() == targetYear) // 按年份过滤
                        .collect(Collectors.toList());
                logger.info("Filtered movies by year: " + movies.size());

                // 过滤掉评分次数小于5次的电影
                movies.removeIf(movie -> movie.getRatingNumber() < 5);
                logger.info("Movies after removing low rating counts: " + movies.size());

                // 默认按评分从高到低排序
                movies.sort((m1, m2) -> Double.compare(m2.getAverageRating(), m1.getAverageRating()));
                logger.info("Movies sorted by rating.");

                // 如果电影数量超过 size，返回前 size 个电影
                if (movies.size() > size) {
                    logger.info("Returning top " + size + " movies.");
                    return movies.subList(0, size);
                }
                return movies;
            } catch (NumberFormatException e) {
                logger.error("Invalid year format: " + year);
            }
        } else {
            logger.warn("Year is null.");
        }
        return null;
    }


    public List<Movie> getMoviesByGenre(String genre, int size, String sortBy) {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("Loading Genre: " + genre);
        if (null != genre) {
            // 获取指定类型的所有电影
            List<Movie> movies = new ArrayList<>(this.genreReverseIndexMap.get(genre));
            
            // 过滤掉评分次数小于5次的电影
            // TODO:可以更改过滤条件
            movies.removeIf(movie -> movie.getRatingNumber() < 5);
            
            // 根据sortBy参数排序
            switch (sortBy) {
                case "rating":
                    movies.sort((m1, m2) -> Double.compare(m2.getAverageRating(), m1.getAverageRating()));
                    break;
                case "releaseYear":
                    movies.sort((m1, m2) -> Integer.compare(m2.getReleaseYear(), m1.getReleaseYear()));
                    break;
                default:
            }
            
            // 如果电影数量超过size，返回前size个电影
            if (movies.size() > size) {
                return movies.subList(0, size);
            }
            return movies;
        }
        return null;
    }

    public List<Movie> getMovies(int size, String sortBy) {
        List<Movie> movies = new ArrayList<>(movieMap.values());
        switch (sortBy) {
            case "rating":
                movies.sort((m1, m2) -> Double.compare(m2.getAverageRating(), m1.getAverageRating()));
                break;
            case "releaseYear":
                movies.sort((m1, m2) -> Integer.compare(m2.getReleaseYear(), m1.getReleaseYear()));
                break;
            default:
        }

        if (movies.size() > size) {
            return movies.subList(0, size);
        }
        return movies;
    }

    public Movie getMovieById(int movieId) {
        return this.movieMap.get(movieId);
    }

    public User getUserById(int userId) {
        return this.userMap.get(userId);
    }

    // 搜索电影
    public List<Movie> searchMovies(String query) {
        List<Movie> matchedMovies = this.movieMap.values().stream()
                .filter(movie -> movie.getTitle().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        return matchedMovies;
    }

}


