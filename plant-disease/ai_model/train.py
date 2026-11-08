import os
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader, random_split
from torchvision import datasets, transforms, models
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, confusion_matrix
import matplotlib.pyplot as plt
# 删掉seaborn，规避版本冲突
from preprocess_utils import MEAN, STD, IMAGE_SIZE

# ==================== 配置参数 ====================
DATASET_PATH = "./PlantVillage-Dataset-image"
BATCH_SIZE = 32
EPOCHS = 30
LR = 0.001
DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")
SAVE_PATH = "./best_model.pth"
NUM_CLASSES = 39

print("当前使用设备：", DEVICE)

# ==================== 1. 数据集加载与划分 ====================
def load_dataset():
    train_transform = transforms.Compose([
        transforms.Resize((IMAGE_SIZE, IMAGE_SIZE)),
        transforms.RandomRotation(30),
        transforms.RandomHorizontalFlip(),
        transforms.RandomVerticalFlip(),
        transforms.ColorJitter(brightness=0.2, contrast=0.2),
        transforms.ToTensor(),
        transforms.Normalize(mean=MEAN, std=STD)
    ])
    val_test_transform = transforms.Compose([
        transforms.Resize((IMAGE_SIZE, IMAGE_SIZE)),
        transforms.ToTensor(),
        transforms.Normalize(mean=MEAN, std=STD)
    ])

    # 修复：加上 transform=None
    full_dataset = datasets.ImageFolder(root=DATASET_PATH, transform=None)
    total_size = len(full_dataset)
    train_size = int(0.8 * total_size)
    val_size = int(0.1 * total_size)
    test_size = total_size - train_size - val_size

    train_dataset, val_dataset, test_dataset = random_split(
        full_dataset, [train_size, val_size, test_size],
        generator=torch.Generator().manual_seed(42)
    )
    train_dataset.dataset.transform = train_transform
    val_dataset.dataset.transform = val_test_transform
    test_dataset.dataset.transform = val_test_transform

    # num_workers=0 Windows防崩溃
    train_loader = DataLoader(train_dataset, batch_size=BATCH_SIZE, shuffle=True, num_workers=0)
    val_loader = DataLoader(val_dataset, batch_size=BATCH_SIZE, shuffle=False, num_workers=0)
    test_loader = DataLoader(test_dataset, batch_size=BATCH_SIZE, shuffle=False, num_workers=0)

    class_names = full_dataset.classes
    with open("./class_names.txt", "w", encoding="utf-8") as f:
        for idx, name in enumerate(class_names):
            f.write(f"{idx}\t{name}\n")
    print(f"类别数：{len(class_names)}，训练集：{train_size}，验证集：{val_size}，测试集：{test_size}")
    return train_loader, val_loader, test_loader, class_names

# ==================== 2. 模型构建 ====================
def build_model():
    # 修复废弃pretrained，用weights
    model = models.mobilenet_v2(weights=models.MobileNet_V2_Weights.IMAGENET1K_V1)
    for param in model.features.parameters():
        param.requires_grad = False
    in_features = model.classifier[1].in_features
    model.classifier = nn.Sequential(
        nn.Dropout(0.2),
        nn.Linear(in_features, NUM_CLASSES)
    )
    model = model.to(DEVICE)
    return model

# ==================== 3. 训练与验证 ====================
def train_model(model, train_loader, val_loader):
    criterion = nn.CrossEntropyLoss()
    optimizer = optim.Adam(model.classifier.parameters(), lr=LR)
    best_acc = 0.0

    train_loss_list, val_loss_list = [], []
    train_acc_list, val_acc_list = [], []

    for epoch in range(EPOCHS):
        model.train()
        train_loss = 0.0
        train_correct = 0
        train_total = 0

        for images, labels in train_loader:
            images, labels = images.to(DEVICE), labels.to(DEVICE)
            optimizer.zero_grad()
            outputs = model(images)
            loss = criterion(outputs, labels)
            loss.backward()
            optimizer.step()

            train_loss += loss.item() * images.size(0)
            _, predicted = torch.max(outputs.data, 1)
            train_total += labels.size(0)
            train_correct += (predicted == labels).sum().item()

        train_avg_loss = train_loss / train_total
        train_avg_acc = train_correct / train_total

        model.eval()
        val_loss = 0.0
        val_correct = 0
        val_total = 0
        with torch.no_grad():
            for images, labels in val_loader:
                images, labels = images.to(DEVICE), labels.to(DEVICE)
                outputs = model(images)
                loss = criterion(outputs, labels)
                val_loss += loss.item() * images.size(0)
                _, predicted = torch.max(outputs.data, 1)
                val_total += labels.size(0)
                val_correct += (predicted == labels).sum().item()

        val_avg_loss = val_loss / val_total
        val_avg_acc = val_correct / val_total

        if val_avg_acc > best_acc:
            best_acc = val_avg_acc
            torch.save(model.state_dict(), SAVE_PATH)
            print(f"Epoch {epoch+1}/{EPOCHS} 最佳模型已保存，验证集准确率：{best_acc:.4f}")

        print(f"Epoch {epoch+1}/{EPOCHS}")
        print(f"训练集 Loss:{train_avg_loss:.4f} Acc:{train_avg_acc:.4f}")
        print(f"验证集 Loss:{val_avg_loss:.4f} Acc:{val_avg_acc:.4f}")
        print("-" * 50)

        train_loss_list.append(train_avg_loss)
        val_loss_list.append(val_avg_loss)
        train_acc_list.append(train_avg_acc)
        val_acc_list.append(val_avg_acc)

    plot_curves(train_loss_list, val_loss_list, train_acc_list, val_acc_list)
    return model

# ==================== 4. 测试集评估 ====================
def evaluate_model(model, test_loader, class_names):
    model.eval()
    all_preds = []
    all_labels = []
    with torch.no_grad():
        for images, labels in test_loader:
            images = images.to(DEVICE)
            outputs = model(images)
            _, predicted = torch.max(outputs.data, 1)
            all_preds.extend(predicted.cpu().numpy())
            all_labels.extend(labels.numpy())

    acc = accuracy_score(all_labels, all_preds)
    precision = precision_score(all_labels, all_preds, average='macro', zero_division=0)
    recall = recall_score(all_labels, all_preds, average='macro', zero_division=0)
    f1 = f1_score(all_labels, all_preds, average='macro', zero_division=0)

    print("="*50)
    print(f"测试集准确率：{acc:.4f}")
    print(f"测试集精确率：{precision:.4f}")
    print(f"测试集召回率：{recall:.4f}")
    print(f"测试集F1值：{f1:.4f}")
    print("="*50)

    # 纯matplotlib混淆矩阵，无seaborn
    cm = confusion_matrix(all_labels, all_preds)
    plt.figure(figsize=(12, 10))
    plt.imshow(cm, cmap="Blues")
    plt.xlabel("预测类别")
    plt.ylabel("真实类别")
    plt.title("混淆矩阵")
    plt.savefig("./confusion_matrix.png", dpi=300, bbox_inches="tight")
    print("混淆矩阵已保存为 confusion_matrix.png")

# ==================== 辅助：绘制训练曲线 ====================
def plot_curves(train_loss, val_loss, train_acc, val_acc):
    epochs_range = range(1, EPOCHS+1)
    plt.figure(figsize=(12, 5))
    plt.subplot(1, 2, 1)
    plt.plot(epochs_range, train_loss, label="Train Loss")
    plt.plot(epochs_range, val_loss, label="Val Loss")
    plt.title("Loss曲线")
    plt.legend()
    plt.subplot(1, 2, 2)
    plt.plot(epochs_range, train_acc, label="Train Acc")
    plt.plot(epochs_range, val_acc, label="Val Acc")
    plt.title("准确率曲线")
    plt.legend()
    plt.savefig("./train_curve.png", dpi=300, bbox_inches="tight")
    print("训练曲线已保存为 train_curve.png")

# ==================== 主函数 ====================
if __name__ == "__main__":
    train_loader, val_loader, test_loader, class_names = load_dataset()
    model = build_model()
    model = train_model(model, train_loader, val_loader)
    model.load_state_dict(torch.load(SAVE_PATH, map_location=DEVICE))
    evaluate_model(model, test_loader, class_names)
