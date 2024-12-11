package com.sparrowrecsys.online.datamanager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sparrowrecsys.online.model.Embedding;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Movie类，包含从movielens的movies.csv加载的属性以及其他高级数据（如averageRating、emb等）。
 */
public class Movie {
    int movieId; // 电影ID
    String title; // 电影标题
    int releaseYear; // 上映年份
    String imdbId; // IMDB ID
    String tmdbId; // TMDB ID
    List<String> genres; // 电影类型列表
    int ratingNumber; // 电影被评分的次数
    double averageRating; // 平均评分

    @JsonIgnore // 忽略序列化，用于存储电影的嵌入向量
    Embedding emb;

    @JsonIgnore // 忽略序列化，完整评分列表不会暴露给前端
    List<Rating> ratings;

    @JsonIgnore // 忽略序列化，电影的特性数据不会暴露给前端
    Map<String, String> movieFeatures;

    final int TOP_RATING_SIZE = 10; // 前10条评分列表的大小限制

    @JsonSerialize(using = RatingListSerializer.class) // 使用自定义序列化器，仅返回前TOP_RATING_SIZE条评分
    List<Rating> topRatings;

    // 构造函数，初始化默认值
    public Movie() {
        ratingNumber = 0;
        averageRating = 0;
        this.genres = new ArrayList<>();
        this.ratings = new ArrayList<>();
        this.topRatings = new LinkedList<>();
        this.emb = null;
        this.movieFeatures = null;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public List<String> getGenres() {
        return genres;
    }

    /**
     * 添加电影类型到类型列表。
     * @param genre 新的电影类型
     */
    public void addGenre(String genre){
        this.genres.add(genre);
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    /**
     * 添加评分，并动态更新电影的平均评分。
     * @param rating 新的评分
     */
    public void addRating(Rating rating) {
        // 动态更新平均评分
        averageRating = (averageRating * ratingNumber + rating.getScore()) / (ratingNumber+1);
        ratingNumber++;
        // 添加到评分列表
        this.ratings.add(rating);
        // 更新Top评分列表
        addTopRating(rating);
    }

    /**
     * 更新Top评分列表，保持评分列表按分数排序，并限制为TOP_RATING_SIZE大小。
     * @param rating 新的评分
     */
    public void addTopRating(Rating rating){
        if (this.topRatings.isEmpty()){
            // 如果列表为空，直接添加
            this.topRatings.add(rating);
        }else{
            int index = 0;
            // 查找插入位置，保持分数降序排列
            for (Rating topRating : this.topRatings){
                if (topRating.getScore() >= rating.getScore()){
                    break;
                }
                index ++;
            }
            // 插入新评分到正确位置
            topRatings.add(index, rating);
            // 如果超过限制，移除最低分
            if (topRatings.size() > TOP_RATING_SIZE) {
                topRatings.remove(0);
            }
        }
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public String getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(String tmdbId) {
        this.tmdbId = tmdbId;
    }

    public int getRatingNumber() {
        return ratingNumber;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public Embedding getEmb() {
        return emb;
    }

    public void setEmb(Embedding emb) {
        this.emb = emb;
    }

    public Map<String, String> getMovieFeatures() {
        return movieFeatures;
    }

    public void setMovieFeatures(Map<String, String> movieFeatures) {
        this.movieFeatures = movieFeatures;
    }
}
