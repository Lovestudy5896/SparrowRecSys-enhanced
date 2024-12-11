from surprise import Reader, Dataset, KNNBaseline
from surprise.model_selection import GridSearchCV
import pickle
import pandas as pd
import os
import sys
from pathlib import Path

# 设置工作目录为当前脚本所在文件夹的上层文件夹
current_script_path = Path(__file__).resolve()
parent_directory = current_script_path.parent.parent
os.chdir(parent_directory)

# 确保工作目录被添加到系统路径
sys.path.insert(0, str(parent_directory))


# 加载训练数据
reader = Reader(line_format='user item rating', sep=',')
train_data = pd.read_csv('data/train_data.csv', usecols=["userId", "movieId", "rating"])
data = Dataset.load_from_df(train_data[['userId', 'movieId', 'rating']], reader)

# 网格搜索最佳参数
param_grid = {
    'k': [40, 45, 50],  # 邻居数量
    'sim_options': {
        'name': ['msd', 'cosine', 'pearson'],  # 相似度度量方法
        'user_based': [True, False]  # 基于用户还是物品
    }
}
grid_search = GridSearchCV(KNNBaseline, param_grid, measures=['rmse'], cv=5)
grid_search.fit(data)

# 输出最佳参数和评分
print("最佳RMSE:", grid_search.best_score['rmse'])
print("最佳参数组合:", grid_search.best_params['rmse'])

# 使用最佳参数训练模型
best_algo = grid_search.best_estimator['rmse']
trainset = data.build_full_trainset()
best_algo.fit(trainset)

# 保存模型
with open('models/knn_baseline_model.pkl', 'wb') as model_file:
    pickle.dump(best_algo, model_file)

print("模型已保存到文件 'models/knn_baseline_model.pkl'")
