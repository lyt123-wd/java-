import torch
import numpy as np
import time
import onnx
import onnxruntime as ort
from torchvision import models
import torch.nn as nn
from preprocess_utils import preprocess_numpy, IMAGE_SIZE

MODEL_PATH = "./best_model.pth"
ONNX_PATH = "./plant_disease_mobilenetv2.onnx"
NUM_CLASSES = 39

def export_onnx():
    # 重建模型结构
    model = models.mobilenet_v2(pretrained=False)
    in_features = model.classifier[1].in_features
    model.classifier = nn.Sequential(
        nn.Dropout(0.2),
        nn.Linear(in_features, NUM_CLASSES)
    )
    # 加载训练好的权重
    model.load_state_dict(torch.load(MODEL_PATH, map_location="cpu"))
    model.eval()

    # 构造虚拟输入 [batch, channel, height, width]
    dummy_input = torch.randn(1, 3, IMAGE_SIZE, IMAGE_SIZE)
    # 导出ONNX
    torch.onnx.export(
        model,
        dummy_input,
        ONNX_PATH,
        opset_version=12,
        input_names=["input"],
        output_names=["output"],
        dynamic_axes={"input": {0: "batch_size"}, "output": {0: "batch_size"}}
    )
    print("ONNX模型导出完成")

    # 校验模型合法性
    onnx_model = onnx.load(ONNX_PATH)
    onnx.checker.check_model(onnx_model)
    print("ONNX模型校验通过")

    # 测试推理速度
    session = ort.InferenceSession(ONNX_PATH, providers=["CPUExecutionProvider"])
    test_input = np.random.randn(1, 3, 224, 224).astype(np.float32)
    # 预热
    for _ in range(10):
        session.run(None, {"input": test_input})
    # 测速
    times = []
    for _ in range(100):
        start = time.time()
        session.run(None, {"input": test_input})
        times.append(time.time() - start)
    avg_time = np.mean(times) * 1000
    print(f"CPU单图平均推理耗时：{avg_time:.2f} ms")

    # 验证Python与ONNX推理结果一致性
    test_img = preprocess_numpy("./test.jpg")
    with torch.no_grad():
        torch_out = model(torch.from_numpy(test_img)).numpy()
    onnx_out = session.run(None, {"input": test_img})[0]
    diff = np.max(np.abs(torch_out - onnx_out))
    print(f"PyTorch与ONNX输出最大差值：{diff:.8f}（小于1e-5即为正常）")

if __name__ == "__main__":
    export_onnx()
