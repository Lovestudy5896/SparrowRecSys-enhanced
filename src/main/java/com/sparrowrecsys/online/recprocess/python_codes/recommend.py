#!/usr/bin/env python
# coding: utf-8

import pandas as pd
import pickle
import os
import sys
import json
from pathlib import Path

def load_model(model_path):
    """加载训练好的模型"""
    with open(model_path, 'rb') as model_file:
        return pickle.load(model_file)

def load_test_data(data_path):
   """加载测试数据"""
   return pd.read_csv(
       data_path,
       names=['userId', 'movieId', 'rating', 'timestamp'],
       dtype={
           'userId': str,
           'movieId': str, 
           'rating': float,
           'timestamp': int
       }
   )

def prepare_testset(test_data):
    """准备测试数据集"""
    return [(str(row['userId']), str(row['movieId']), row['rating']) 
            for _, row in test_data.iterrows()]

def generate_recommendation(predictions, user_id, k=10):
    """生成电影推荐列表"""
    pred_df = pd.DataFrame(predictions, columns=['userId', 'movieId', 'rating', 'est', 'details'])
    
    return list(
        pred_df[pred_df['userId'] == user_id]
        .sort_values('est', ascending=False)
        .head(k)['movieId']
    )

def movie_recommender(user_id, k=10):
    """电影推荐系统主函数"""
    # 设置工作目录为当前脚本所在文件夹的上层文件夹
    current_script_path = Path(__file__).resolve()
    parent_directory = current_script_path.parent.parent
    os.chdir(parent_directory)
    sys.path.insert(0, str(parent_directory))
    
    # 检查模型文件是否存在
    model_path = 'models/knn_baseline_model.pkl'
    if not os.path.exists(model_path):
        raise FileNotFoundError(
            f"模型文件 {model_path} 不存在！\n"
            "请按以下顺序运行脚本以生成模型文件：\n"
            "1. 运行 dataset_split.py\n"
            "2. 运行 train.py\n"
            "（可选）运行 cal_K.py 进行评估\n"
            "然后再次运行本脚本"
        )
    
    # 加载数据和模型
    model = load_model(model_path)
    data_path = 'data/ratings.csv'
    test_data = load_test_data(data_path)
    testset = prepare_testset(test_data)
    
    predictions = model.test(testset)
    return generate_recommendation(predictions, user_id, k)

if __name__ == "__main__":
   try:
       if len(sys.argv) < 2:
           sys.exit(1)
           
       user_id = sys.argv[1]
       k = int(sys.argv[2]) if len(sys.argv) > 2 else 10
       
       recommendations = movie_recommender(user_id, k)
       print(json.dumps(recommendations))
   except Exception as e:
       import traceback
       print(f"Error: {traceback.format_exc()}", file=sys.stderr)