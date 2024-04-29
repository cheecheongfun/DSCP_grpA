from transformers import AutoModelForSequenceClassification
import torch
model = AutoModelForSequenceClassification.from_pretrained("ccfai/llama-2-7b-compost-chatbot")
input_ids = ...
attention_mask = ...

# Export the model to ONNX
torch.onnx.export(model,
                  (input_ids, attention_mask),
                  "path/to/save/model.onnx",
                  input_names=["input_ids", "attention_mask"],
                  output_names=["output"],
                  dynamic_axes={"input_ids": {0: "batch_size"}, "attention_mask": {0: "batch_size"}, "output": {0: "batch_size"}})
