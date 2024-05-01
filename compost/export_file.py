from transformers import AutoModelForQuestionAnswering
import torch
from llama.generation import Llama, Dialog
from llama.model import ModelArgs, Transformer
from llama.tokenizer import Tokenizer

# Initialize the Llama tokenizer
tokenizer = Tokenizer(model_path="C:\\Users\\fionn\\Course\\ASG2\\DSCP_grpA\\compost\\tokenizer.model")

input_text = "hi my vermicompost bin is infested with fruit fly"

# Tokenize input text and generate input IDs and attention mask
inputs = tokenizer.encode(input_text, bos=True, eos=True)

# Convert token IDs to a PyTorch tensor
input_ids = torch.tensor([inputs])

# Generate attention mask based on presence of tokens
attention_mask = torch.ones_like(input_ids)

print("Input IDs:", input_ids)
print("Attention Mask:", attention_mask)

model = AutoModelForQuestionAnswering.from_pretrained("ccfai/llama-2-7b-compost-chatbot")

# Pass inputs and attention mask as keyword arguments to the model
outputs = model(input_ids=input_ids, attention_mask=attention_mask)
start_logits, end_logits = outputs.start_logits, outputs.end_logits
start_index = torch.argmax(start_logits)
end_index = torch.argmax(end_logits)
answer_span = input_text[start_index:end_index+1]
print("Predicted Answer:", answer_span)

# # Export the model to ONNX
# torch.onnx.export(model,
#                   (input_ids, attention_mask),
#                   "path/to/save/model.onnx",
#                   input_names=["input_ids", "attention_mask"],
#                   output_names=["output"],
#                   dynamic_axes={"input_ids": {0: "batch_size"}, "attention_mask": {0: "batch_size"}, "output": {0: "batch_size"}})
