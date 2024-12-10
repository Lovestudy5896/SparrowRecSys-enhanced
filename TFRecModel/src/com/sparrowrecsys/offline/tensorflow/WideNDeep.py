import os

import tensorflow as tf
import keras

# 设置基础目录为指定的路径
base_dir = r"C:\Users\Windows\IdeaProjects\SparrowRecSys\src\main"

# 动态生成文件路径
training_samples_file_path = os.path.join(base_dir, "resources", "webroot", "sampledata", "trainingSamples.csv")
test_samples_file_path = os.path.join(base_dir, "resources", "webroot", "sampledata", "testSamples.csv")

# 检查文件是否存在，确保路径正确
if not os.path.exists(training_samples_file_path):
    raise FileNotFoundError(f"训练样本文件未找到: {training_samples_file_path}")
if not os.path.exists(test_samples_file_path):
    raise FileNotFoundError(f"测试样本文件未找到: {test_samples_file_path}")

# 打印确认路径
print("训练样本文件路径:", training_samples_file_path)
print("测试样本文件路径:", test_samples_file_path)


# 定义一个函数，用于加载 CSV 文件为 TensorFlow 数据集
def get_dataset(file_path):
    """
    从 CSV 文件加载数据集
    参数:
        file_path: CSV 文件路径
    返回:
        TensorFlow 数据集对象
    """
    dataset = tf.data.experimental.make_csv_dataset(
        file_path,
        batch_size=12,  # 每批次的数据量
        label_name='label',  # 标签列名称
        na_value="0",  # 缺失值填充为 0
        num_epochs=1,  # 读取一次数据
        ignore_errors=True)  # 忽略错误
    return dataset


# 分别加载训练集和测试集
train_dataset = get_dataset(training_samples_file_path)
test_dataset = get_dataset(test_samples_file_path)

# 定义用户和电影的类型特征（类别特征的词汇表）
genre_vocab = ['Film-Noir', 'Action', 'Adventure', 'Horror', 'Romance', 'War', 'Comedy', 'Western', 'Documentary',
               'Sci-Fi', 'Drama', 'Thriller',
               'Crime', 'Fantasy', 'Animation', 'IMAX', 'Mystery', 'Children', 'Musical']

GENRE_FEATURES = {
    'userGenre1': genre_vocab,  # 用户喜好类型 1
    'userGenre2': genre_vocab,  # 用户喜好类型 2
    'userGenre3': genre_vocab,  # 用户喜好类型 3
    'userGenre4': genre_vocab,  # 用户喜好类型 4
    'userGenre5': genre_vocab,  # 用户喜好类型 5
    'movieGenre1': genre_vocab,  # 电影类型 1
    'movieGenre2': genre_vocab,  # 电影类型 2
    'movieGenre3': genre_vocab   # 电影类型 3
}

# 定义所有类别特征并嵌入
categorical_columns = []
for feature, vocab in GENRE_FEATURES.items():
    # 为每个特征创建类别列
    cat_col = tf.feature_column.categorical_column_with_vocabulary_list(
        key=feature, vocabulary_list=vocab)
    # 嵌入类别特征
    emb_col = tf.feature_column.embedding_column(cat_col, 10)  # 嵌入维度为 10
    categorical_columns.append(emb_col)

# 定义电影 ID 的嵌入特征
movie_col = tf.feature_column.categorical_column_with_identity(key='movieId', num_buckets=1001)  # ID 范围
movie_emb_col = tf.feature_column.embedding_column(movie_col, 10)  # 嵌入维度为 10
categorical_columns.append(movie_emb_col)

# 定义用户 ID 的嵌入特征
user_col = tf.feature_column.categorical_column_with_identity(key='userId', num_buckets=30001)  # ID 范围
user_emb_col = tf.feature_column.embedding_column(user_col, 10)  # 嵌入维度为 10
categorical_columns.append(user_emb_col)

# 定义数值特征
numerical_columns = [tf.feature_column.numeric_column('releaseYear'),       # 电影上映年份
                     tf.feature_column.numeric_column('movieRatingCount'),  # 电影评分次数
                     tf.feature_column.numeric_column('movieAvgRating'),    # 电影平均评分
                     tf.feature_column.numeric_column('movieRatingStddev'), # 电影评分标准差
                     tf.feature_column.numeric_column('userRatingCount'),   # 用户评分次数
                     tf.feature_column.numeric_column('userAvgRating'),     # 用户平均评分
                     tf.feature_column.numeric_column('userRatingStddev')]  # 用户评分标准差

# 定义交叉特征，用于宽模型部分
rated_movie = tf.feature_column.categorical_column_with_identity(key='userRatedMovie1', num_buckets=1001)
crossed_feature = tf.feature_column.indicator_column(tf.feature_column.crossed_column([movie_col, rated_movie], 10000))

# 定义模型的输入
inputs = {
    'movieAvgRating': keras.layers.Input(name='movieAvgRating', shape=(), dtype='float32'),
    'movieRatingStddev': keras.layers.Input(name='movieRatingStddev', shape=(), dtype='float32'),
    'movieRatingCount': keras.layers.Input(name='movieRatingCount', shape=(), dtype='int32'),
    'userAvgRating': keras.layers.Input(name='userAvgRating', shape=(), dtype='float32'),
    'userRatingStddev': keras.layers.Input(name='userRatingStddev', shape=(), dtype='float32'),
    'userRatingCount': keras.layers.Input(name='userRatingCount', shape=(), dtype='int32'),
    'releaseYear': keras.layers.Input(name='releaseYear', shape=(), dtype='int32'),
    'movieId': keras.layers.Input(name='movieId', shape=(), dtype='int32'),
    'userId': keras.layers.Input(name='userId', shape=(), dtype='int32'),
    'userRatedMovie1': keras.layers.Input(name='userRatedMovie1', shape=(), dtype='int32'),
    'userGenre1': keras.layers.Input(name='userGenre1', shape=(), dtype='string'),
    'userGenre2': keras.layers.Input(name='userGenre2', shape=(), dtype='string'),
    'userGenre3': keras.layers.Input(name='userGenre3', shape=(), dtype='string'),
    'userGenre4': keras.layers.Input(name='userGenre4', shape=(), dtype='string'),
    'userGenre5': keras.layers.Input(name='userGenre5', shape=(), dtype='string'),
    'movieGenre1': keras.layers.Input(name='movieGenre1', shape=(), dtype='string'),
    'movieGenre2': keras.layers.Input(name='movieGenre2', shape=(), dtype='string'),
    'movieGenre3': keras.layers.Input(name='movieGenre3', shape=(), dtype='string'),
}

# 构建 Wide & Deep 模型
# Deep 部分
deep = keras.layers.DenseFeatures(numerical_columns + categorical_columns)(inputs)
deep = keras.layers.Dense(128, activation='relu')(deep)
deep = keras.layers.Dense(128, activation='relu')(deep)

# Wide 部分
wide = keras.layers.DenseFeatures(crossed_feature)(inputs)

# 合并 Wide 和 Deep 部分
both = keras.layers.concatenate([deep, wide])

# 输出层，使用 sigmoid 激活函数
output_layer = keras.layers.Dense(1, activation='sigmoid')(both)

# 构建模型
model = keras.Model(inputs, output_layer)

# 编译模型，设置损失函数、优化器和评估指标
model.compile(
    loss='binary_crossentropy',  # 二元交叉熵损失
    optimizer='adam',           # Adam 优化器
    metrics=['accuracy',        # 准确率
             keras.metrics.AUC(curve='ROC'),  # ROC 曲线下的面积
             keras.metrics.AUC(curve='PR')]  # PR 曲线下的面积
)

# 训练模型
model.fit(train_dataset, epochs=5)

# 测试模型
test_loss, test_accuracy, test_roc_auc, test_pr_auc = model.evaluate(test_dataset)
print('\n\nTest Loss {}, Test Accuracy {}, Test ROC AUC {}, Test PR AUC {}'.format(test_loss, test_accuracy,
                                                                                   test_roc_auc, test_pr_auc))

# 打印部分预测结果
predictions = model.predict(test_dataset)
for prediction, goodRating in zip(predictions[:12], list(test_dataset)[0][1][:12]):
    print("Predicted good rating: {:.2%}".format(prediction[0]),  # 输出预测概率
          " | Actual rating label: ",
          ("Good Rating" if bool(goodRating) else "Bad Rating"))  # 实际标签
