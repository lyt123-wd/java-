import numpy as np
from PIL import Image
import torch
from torchvision import transforms

# ImageNet 预训练模型标准归一化参数，Java端必须完全复用
MEAN = [0.485, 0.456, 0.406]
STD = [0.229, 0.224, 0.225]
IMAGE_SIZE = 224

def preprocess_image(image_path):
    """
    图像预处理：与Java端严格对齐
    步骤：RGB转换 → 双线性插值resize → 转张量 → 归一化 → 增加batch维度
    """
    # 读取图片并转为RGB格式
    img = Image.open(image_path).convert('RGB')
    # 定义预处理流程
    transform = transforms.Compose([
        transforms.Resize((IMAGE_SIZE, IMAGE_SIZE)),
        transforms.ToTensor(),  # 像素值归一化到 [0,1]，通道转为 CHW
        transforms.Normalize(mean=MEAN, std=STD)
    ])
    img_tensor = transform(img)
    # 增加batch维度，变为 [1, 3, 224, 224]
    img_tensor = img_tensor.unsqueeze(0)
    return img_tensor

def preprocess_numpy(image_path):
    """返回numpy格式，用于ONNX推理校验，与Java端输出格式完全一致"""
    img_tensor = preprocess_image(image_path)
    return img_tensor.numpy().astype(np.float32)
