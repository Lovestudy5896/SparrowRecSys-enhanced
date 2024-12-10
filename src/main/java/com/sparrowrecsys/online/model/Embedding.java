package com.sparrowrecsys.online.model;

import java.util.ArrayList;

/**
 * Embedding 类，包含嵌入向量及相关计算方法
 */
public class Embedding {
    // 嵌入向量
    ArrayList<Float> embVector;

    // 无参构造方法，初始化嵌入向量为空的 ArrayList
    public Embedding(){
        this.embVector = new ArrayList<>();
    }

    // 带参构造方法，初始化嵌入向量为传入的 embVector
    public Embedding(ArrayList<Float> embVector){
        this.embVector = embVector;
    }

    // 向嵌入向量中添加一个维度的元素
    public void addDim(Float element){
        this.embVector.add(element);
    }

    // 获取嵌入向量
    public ArrayList<Float> getEmbVector() {
        return embVector;
    }

    // 设置嵌入向量
    public void setEmbVector(ArrayList<Float> embVector) {
        this.embVector = embVector;
    }

    /**
     * 计算两个嵌入向量之间的余弦相似度
     * @param otherEmb 另一个嵌入向量
     * @return 余弦相似度，取值范围在 -1 到 1 之间，-1 表示完全相反，1 表示完全相同
     */
    public double calculateSimilarity(Embedding otherEmb){
        // 如果当前嵌入向量或传入的嵌入向量为 null，或者嵌入向量的维度不同，返回 -1
        if (null == embVector || null == otherEmb || null == otherEmb.getEmbVector()
                || embVector.size() != otherEmb.getEmbVector().size()){
            return -1;
        }

        // 初始化点积和分母的变量
        double dotProduct = 0;
        double denominator1 = 0;
        double denominator2 = 0;

        // 遍历嵌入向量的每个维度，计算点积和每个向量的平方和
        for (int i = 0; i < embVector.size(); i++){
            dotProduct += embVector.get(i) * otherEmb.getEmbVector().get(i); // 计算点积
            denominator1 += embVector.get(i) * embVector.get(i); // 计算当前嵌入向量的平方和
            denominator2 += otherEmb.getEmbVector().get(i) * otherEmb.getEmbVector().get(i); // 计算另一个嵌入向量的平方和
        }

        // 返回余弦相似度：点积除以两个向量的模长积
        return dotProduct / (Math.sqrt(denominator1) * Math.sqrt(denominator2));
    }
}
