# 模型训练与导出

## 环境

```bash
python -m venv .venv
# Windows
.venv\Scripts\activate
pip install -r requirements.txt
```

## 数据集

使用 [PlantVillage](https://github.com/spMohanty/PlantVillage-Dataset) 数据集，按 `ImageFolder` 目录结构放置：

```text
PlantVillage-Dataset-image/
├── Apple___Apple_scab/
│   ├── xxx.jpg
│   └── ...
├── Apple___Black_rot/
├── ...
└── x_Removed_from_Healthy_leaves/
```

仓库不包含数据集本身（体积较大），请自行下载后放到上述目录。

## 训练

```bash
python train.py
```

- 输入尺寸 224×224，ImageNet 归一化
- MobileNetV2 预训练权重，分类头输出 39 类
- 数据增强：随机旋转 30°、水平/垂直翻转、颜色抖动
- 训练/验证/测试 = 8:1:1，随机种子 42
- 30 epochs，Adam，学习率 1e-3

训练完成后输出：

- `best_model.pth`：最佳验证集权重
- `train_curve.png`：训练/验证损失与准确率曲线
- `confusion_matrix.png`：测试集混淆矩阵
- `class_names.txt`：类别 id 与名称映射

## 导出 ONNX

```bash
python export_onnx.py
```

输出 `plant_disease_mobilenetv2.onnx`（opset 12，输入名 `input`，输出名 `output`），脚本会做一次 ONNX Runtime 推理校验。

后端使用的是**仓库根目录**的 `plant_disease_mobilenetv2.onnx`；重新训练/导出后，把生成的文件复制到根目录即可。`ai_model/` 下的导出文件属于生成产物，不进入 Git。

后端启动时会读取 `class_names.txt`，把模型输出的类别索引转换成类名，再按类名查询 `disease_info.en_name`。因此重新训练后如果类别顺序变化，只需要同步更新 `class_names.txt`，不需要改数据库 id。

## 推理预处理

`preprocess_utils.py` 与 Java 端 `InferenceServiceImpl` 保持一致：

```text
Resize 224×224 -> RGB / 255 -> (x - mean) / std
mean = [0.485, 0.456, 0.406]
std  = [0.229, 0.224, 0.225]
```
