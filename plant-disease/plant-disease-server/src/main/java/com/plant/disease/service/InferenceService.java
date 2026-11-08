package com.plant.disease.service;

import ai.onnxruntime.OrtException;

import java.io.IOException;
import java.util.Map;

public interface InferenceService {
    Map<String, Object> predict(byte[] imgBytes) throws IOException, OrtException;
}
