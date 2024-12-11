import pandas as pd
from sklearn.model_selection import train_test_split
import os
from pathlib import Path

# 设置工作目录为脚本所在文件夹的上层文件夹
current_script_path = Path(__file__).resolve()
parent_directory = current_script_path.parent.parent  # 上层目录
os.chdir(parent_directory)

# 确认当前工作目录
print(f"当前工作目录: {os.getcwd()}")

# 检查文件是否存在
if not Path("data/ratings.csv").exists():
    raise FileNotFoundError("ratings.csv 文件未找到，请确保文件位于正确的目录。")

# 读取原始数据
data = pd.read_csv('data/ratings.csv', names=["userId", "movieId", "rating", "timestamp"])

# 划分数据集：8:2 初步分割
train_data, temp_data = train_test_split(data, test_size=0.2, random_state=42)

# 再将 20% 分割为验证集和测试集（各占 10%）
val_data, test_data = train_test_split(temp_data, test_size=0.5, random_state=42)

# 保存数据集到文件
train_data.to_csv('data/train_data.csv', index=False)
val_data.to_csv('data/val_data.csv', index=False)
test_data.to_csv('data/test_data.csv', index=False)

print("数据集划分完成：")
print(f"训练集大小: {len(train_data)}, 验证集大小: {len(val_data)}, 测试集大小: {len(test_data)}")
