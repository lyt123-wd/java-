package com.plant.disease.service.impl;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.plant.disease.config.ProjectProperties;
import com.plant.disease.service.InferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * MobileNetV2 ONNX 推理：224x224 输入、ImageNet 归一化、39 分类输出。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InferenceServiceImpl implements InferenceService {

    private static final float[] MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] STD = {0.229f, 0.224f, 0.225f};
    private static final int IMAGE_SIZE = 224;

    private final ProjectProperties projectProperties;

    private OrtEnvironment env;
    private OrtSession session;
    private List<String> classNames;

    @PostConstruct
    public void initModel() throws OrtException, IOException {
        Path modelPath = Paths.get(projectProperties.getModelPath()).toAbsolutePath().normalize();
        if (!Files.exists(modelPath)) {
            throw new IllegalStateException("ONNX 模型文件不存在: " + modelPath
                    + "，请通过 MODEL_PATH 环境变量指定模型路径");
        }
        this.classNames = loadClassNames();
        if (classNames.size() != 39) {
            throw new IllegalStateException("class_names.txt 类别数不是 39，请检查模型与类别文件");
        }
        env = OrtEnvironment.getEnvironment();
        session = env.createSession(modelPath.toString(), new OrtSession.SessionOptions());
        log.info("ONNX 模型加载成功: {}, 类别数: {}", modelPath, classNames.size());
    }

    private List<String> loadClassNames() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("class_names.txt").getInputStream(), StandardCharsets.UTF_8))) {
            List<String> names = reader.lines()
                    .filter(line -> !line.isBlank())
                    .map(line -> line.split("\\t", 2))
                    .filter(parts -> parts.length == 2)
                    .map(parts -> parts[1].trim())
                    .toList();
            if (names.isEmpty()) {
                throw new IllegalStateException("class_names.txt 为空");
            }
            return names;
        }
    }

    @PreDestroy
    public void destroy() {
        if (session != null) {
            try {
                session.close();
                log.info("ONNX Session 已关闭");
            } catch (OrtException e) {
                log.warn("关闭 ONNX Session 失败", e);
            }
        }
    }

    @Override
    public Map<String, Object> predict(byte[] imgBytes) throws IOException, OrtException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imgBytes));
        if (image == null) {
            throw new IllegalArgumentException("上传的文件不是有效的图片格式");
        }

        float[] inputData = preprocess(image);
        String imageUrl = saveImage(image);

        long[] shape = new long[]{1, 3, IMAGE_SIZE, IMAGE_SIZE};
        try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputData), shape);
             OrtSession.Result result = session.run(Map.of("input", inputTensor))) {

            float[][] output = (float[][]) result.get(0).getValue();
            float[] scores = output[0];
            if (scores.length != classNames.size()) {
                throw new IllegalStateException("模型输出类别数与 class_names.txt 不一致: "
                        + scores.length + " vs " + classNames.size());
            }

            int maxIndex = 0;
            for (int i = 1; i < scores.length; i++) {
                if (scores[i] > scores[maxIndex]) {
                    maxIndex = i;
                }
            }
            float confidence = softmax(scores, maxIndex);

            Map<String, Object> response = new HashMap<>(4);
            response.put("classIndex", maxIndex);
            response.put("className", classNames.get(maxIndex));
            response.put("confidence", confidence);
            response.put("imageUrl", imageUrl);
            return response;
        }
    }

    private String saveImage(BufferedImage image) throws IOException {
        Path uploadDir = projectProperties.uploadDirectory();
        Files.createDirectories(uploadDir);
        String fileName = UUID.randomUUID() + ".jpg";
        Path saveFile = uploadDir.resolve(fileName);

        BufferedImage rgbImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgbImage.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        if (!ImageIO.write(rgbImage, "jpg", saveFile.toFile())) {
            throw new IOException("图片保存失败");
        }
        return "/upload/" + fileName;
    }

    private float[] preprocess(BufferedImage image) {
        BufferedImage resized = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, IMAGE_SIZE, IMAGE_SIZE, null);
        g.dispose();

        float[] inputData = new float[3 * IMAGE_SIZE * IMAGE_SIZE];
        int idx = 0;
        for (int channel = 0; channel < 3; channel++) {
            for (int h = 0; h < IMAGE_SIZE; h++) {
                for (int w = 0; w < IMAGE_SIZE; w++) {
                    int rgb = resized.getRGB(w, h);
                    float value;
                    if (channel == 0) {
                        value = ((rgb >> 16) & 0xFF) / 255.0f;
                    } else if (channel == 1) {
                        value = ((rgb >> 8) & 0xFF) / 255.0f;
                    } else {
                        value = (rgb & 0xFF) / 255.0f;
                    }
                    inputData[idx++] = (value - MEAN[channel]) / STD[channel];
                }
            }
        }
        return inputData;
    }

    private float softmax(float[] scores, int maxIndex) {
        float maxVal = scores[maxIndex];
        double sum = 0;
        for (float score : scores) {
            sum += Math.exp(score - maxVal);
        }
        return (float) (Math.exp(scores[maxIndex] - maxVal) / sum);
    }
}
