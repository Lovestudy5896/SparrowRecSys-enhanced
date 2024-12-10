package com.sparrowrecsys.online.recprocess;

import com.sparrowrecsys.online.datamanager.DataManager;
import com.sparrowrecsys.online.datamanager.Movie;
import java.util.*;

/**
 * 推荐相似电影的过程
 */
public class SimilarMovieProcess {

    /**
     * 获取推荐的相似电影列表
     * @param movieId 输入的电影ID
     * @param size 返回的相似电影数量
     * @param model 计算相似度时使用的模型（例如：embedding、graph-embedding）
     * @return 相似电影列表
     */
    public static List<Movie> getRecList(int movieId, int size, String model){
        Movie movie = DataManager.getInstance().getMovieById(movieId);
        if (null == movie){
            return new ArrayList<>();
        }
        // 生成候选电影列表
        List<Movie> candidates = candidateGenerator(movie);
        // 对候选电影进行排序
        List<Movie> rankedList = ranker(movie, candidates, model);

        // 返回排序后的前size部电影
        if (rankedList.size() > size){
            return rankedList.subList(0, size);
        }
        return rankedList;
    }

    /**
     * 生成候选电影列表用于推荐
     * @param movie 输入的电影对象
     * @return 候选电影列表
     */
    public static List<Movie> candidateGenerator(Movie movie){
        HashMap<Integer, Movie> candidateMap = new HashMap<>();
        // 遍历电影的所有类型，获取每种类型下的电影作为候选电影
        for (String genre : movie.getGenres()){
            List<Movie> oneCandidates = DataManager.getInstance().getMoviesByGenre(genre, 100, "rating");
            for (Movie candidate : oneCandidates){
                candidateMap.put(candidate.getMovieId(), candidate);
            }
        }
        // 移除当前电影本身
        candidateMap.remove(movie.getMovieId());
        return new ArrayList<>(candidateMap.values());
    }

    /**
     * 多重检索候选电影生成方法
     * @param movie 输入的电影对象
     * @return 候选电影列表
     */
    public static List<Movie> multipleRetrievalCandidates(Movie movie){
        if (null == movie){
            return null;
        }

        // 使用 HashSet 去重，确保每个类型只计算一次
        HashSet<String> genres = new HashSet<>(movie.getGenres());

        HashMap<Integer, Movie> candidateMap = new HashMap<>();
        // 遍历每个类型并获取对应的候选电影
        for (String genre : genres){
            List<Movie> oneCandidates = DataManager.getInstance().getMoviesByGenre(genre, 20, "rating");
            for (Movie candidate : oneCandidates){
                candidateMap.put(candidate.getMovieId(), candidate);
            }
        }

        // 获取评分较高的候选电影
        List<Movie> highRatingCandidates = DataManager.getInstance().getMovies(100, "rating");
        for (Movie candidate : highRatingCandidates){
            candidateMap.put(candidate.getMovieId(), candidate);
        }

        // 获取最新的候选电影
        List<Movie> latestCandidates = DataManager.getInstance().getMovies(100, "releaseYear");
        for (Movie candidate : latestCandidates){
            candidateMap.put(candidate.getMovieId(), candidate);
        }

        // 移除当前电影本身
        candidateMap.remove(movie.getMovieId());
        return new ArrayList<>(candidateMap.values());
    }

    /**
     * 基于嵌入向量的候选电影生成方法
     * @param movie 输入的电影对象
     * @param size 候选池的大小
     * @return 电影候选列表
     */
    public static List<Movie> retrievalCandidatesByEmbedding(Movie movie, int size){
        if (null == movie || null == movie.getEmb()){
            return null;
        }

        // 获取所有电影作为候选集合
        List<Movie> allCandidates = DataManager.getInstance().getMovies(10000, "rating");
        HashMap<Movie, Double> movieScoreMap = new HashMap<>();
        // 计算每部候选电影与输入电影的相似度
        for (Movie candidate : allCandidates){
            double similarity = calculateEmbSimilarScore(movie, candidate);
            movieScoreMap.put(candidate, similarity);
        }

        // 按照相似度对电影进行排序
        List<Map.Entry<Movie, Double>> movieScoreList = new ArrayList<>(movieScoreMap.entrySet());
        movieScoreList.sort(Map.Entry.comparingByValue());

        List<Movie> candidates = new ArrayList<>();
        for (Map.Entry<Movie, Double> movieScoreEntry : movieScoreList){
            candidates.add(movieScoreEntry.getKey());
        }

        // 返回前size部相似电影
        return candidates.subList(0, Math.min(candidates.size(), size));
    }

    /**
     * 对候选电影进行排名
     * @param movie 输入的电影
     * @param candidates 候选电影列表
     * @param model 使用的排名模型（如embedding等）
     * @return 排序后的电影列表
     */
    public static List<Movie> ranker(Movie movie, List<Movie> candidates, String model){
        HashMap<Movie, Double> candidateScoreMap = new HashMap<>();
        // 计算每个候选电影与输入电影的相似度
        for (Movie candidate : candidates){
            double similarity;
            switch (model){
                case "emb":
                    similarity = calculateEmbSimilarScore(movie, candidate);
                    break;
                default:
                    similarity = calculateSimilarScore(movie, candidate);
            }
            candidateScoreMap.put(candidate, similarity);
        }
        // 根据相似度对候选电影进行排序
        List<Movie> rankedList = new ArrayList<>();
        candidateScoreMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEach(m -> rankedList.add(m.getKey()));
        return rankedList;
    }

    /**
     * 计算基于类型和评分的相似度得分
     * @param movie 输入的电影
     * @param candidate 候选电影
     * @return 相似度得分
     */
    public static double calculateSimilarScore(Movie movie, Movie candidate){
        int sameGenreCount = 0;
        // 计算相同类型的数量
        for (String genre : movie.getGenres()){
            if (candidate.getGenres().contains(genre)){
                sameGenreCount++;
            }
        }
        // 计算类型相似度
        double genreSimilarity = (double) sameGenreCount / (movie.getGenres().size() + candidate.getGenres().size()) / 2;
        // 计算评分相似度
        double ratingScore = candidate.getAverageRating() / 5;

        // 设置类型和评分的权重
        double similarityWeight = 0.7;
        double ratingScoreWeight = 0.3;

        // 返回加权后的相似度得分
        return genreSimilarity * similarityWeight + ratingScore * ratingScoreWeight;
    }

    /**
     * 计算基于嵌入向量的相似度得分
     * @param movie 输入的电影
     * @param candidate 候选电影
     * @return 相似度得分
     */
    public static double calculateEmbSimilarScore(Movie movie, Movie candidate){
        if (null == movie || null == candidate){
            return -1;
        }
        // 计算并返回嵌入向量的相似度得分
        return movie.getEmb().calculateSimilarity(candidate.getEmb());
    }
}
