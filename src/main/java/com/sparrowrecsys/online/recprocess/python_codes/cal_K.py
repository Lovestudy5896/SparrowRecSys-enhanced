#!/usr/bin/env python
# coding: utf-8

# In[1]:


import pandas as pd
import pickle
import os
import sys
from pathlib import Path


# In[2]:


# 设置工作目录为当前脚本所在文件夹的上层文件夹
current_script_path = Path(__file__).resolve()
parent_directory = current_script_path.parent.parent
os.chdir(parent_directory)

# 确认当前工作目录
print(f"当前工作目录: {os.getcwd()}")


# In[3]:


# 确保工作目录被添加到系统路径
sys.path.insert(0, str(parent_directory))


# In[4]:


def calculate_precision_recall(predictions, test_df, k=10, threshold=4.0):
    """
    计算 Precision@K 和 Recall@K
    :param predictions: Surprise 的预测结果列表，每个元素为 (userId, movieId, rating, est, details)
    :param test_df: 测试集的 DataFrame，包含用户真实评分数据
    :param k: 推荐列表的大小，即每个用户取前 K 个推荐结果
    :param threshold: 评分阈值，大于等于此值的电影被认为是用户喜欢的相关电影
    :return: precision 和 recall 分别表示精确率和召回率
    """
    # 将预测结果转换为 DataFrame 便于操作
    pred_df = pd.DataFrame(predictions, columns=['userId', 'movieId', 'rating', 'est', 'details'])

    precision, recall = 0.0, 0.0  # 初始化 Precision 和 Recall

    # 遍历每个用户进行 Precision@K 和 Recall@K 的计算
    for user_id, group in test_df.groupby('userId'):
        # 获取该用户实际喜欢的电影集合（真实评分大于等于 threshold）
        relevant_items = set(group[group['rating'] >= threshold]['movieId'])

        # 获取该用户的推荐电影列表（按预测评分从高到低排序，取前 K 个电影）
        recommended_items = set(
            pred_df[pred_df['userId'] == user_id]
            .sort_values('est', ascending=False)  # 按预测评分从高到低排序
            .head(k)['movieId']  # 取前 K 个推荐电影
        )

        # 计算该用户的命中数（推荐中与实际相关的交集数量）
        true_positives = len(relevant_items & recommended_items)

        # 更新 Precision 和 Recall
        precision += true_positives / k  # 精确率 = 推荐中命中的比例
        recall += true_positives / len(relevant_items) if len(relevant_items) > 0 else 0  # 召回率 = 实际相关中被推荐的比例

    # 计算所有用户的平均 Precision 和 Recall
    num_users = test_df['userId'].nunique()  # 测试集中用户总数
    precision /= num_users  # 平均精确率
    recall /= num_users  # 平均召回率

    return precision, recall


# In[5]:


# 加载训练好的模型和测试数据
with open('models/knn_baseline_model.pkl', 'rb') as model_file:
    model = pickle.load(model_file)  # 加载训练好的模型


# In[6]:


# 读取测试数据并指定列的数据类型
test_data = pd.read_csv(
    'data/test_data.csv',
    dtype={
        'userId': str,      # 用户ID读取为字符串
        'movieId': str,     # 电影ID读取为字符串
        'rating': float,    # 评分读取为浮点数
        'timestamp': int    # 时间戳读取为整数
    }
)


# In[7]:


# 将测试数据转换为 Surprise 格式的列表 [(userId, movieId, rating), ...]
testset = [(str(row['userId']), str(row['movieId']), row['rating']) for _, row in test_data.iterrows()]
predictions = model.test(testset)  # 使用模型进行预测


# In[8]:


# 计算 Precision@K 和 Recall@K
precision, recall = calculate_precision_recall(predictions, test_data, k=32, threshold=4.0)


# In[9]:


# 输出结果
print(f"Precision@10: {precision:.4f}")
print(f"Recall@10: {recall:.4f}")

