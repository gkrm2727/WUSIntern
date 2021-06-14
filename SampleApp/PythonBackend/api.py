# upload : endpoint 
# save image
# predict results
from flask import Flask
from flask import request
import os

import torch
from collections import Counter
import pandas as pd
from sklearn.model_selection import train_test_split
import os
from glob import glob
from torchvision import transforms
from torchvision import datasets
from torch.utils.data import DataLoader,sampler,Dataset
from torchvision import models
import torch.nn as nn
from torch import Tensor
from torch.nn import functional as F
from torch import optim
import tqdm
from PIL import Image
import matplotlib.pyplot as plt
import torch
from collections import Counter
import pandas as pd
from sklearn.model_selection import train_test_split
import os
from glob import glob
import warnings
import numpy as np
from PIL import Image
import matplotlib.pyplot as plt


def load_checkpoint(path):
    """Load a PyTorch model checkpoint

    Params
    --------
        path (str): saved model checkpoint. Must start with `model_name-` and end in '.pth'

    Returns
    --------
        None, save the `model` to `path`

    """
    multi_gpu = False
    train_on_gpu=False
    # Get the model name
    model_name = path.split('-')[0]
    assert (model_name in ['vgg16', 'resnet50'
                           ]), "Path must have the correct model name"

    # Load in checkpoint
    checkpoint = torch.load(path)

    if model_name == 'vgg16':
        model = models.vgg16(pretrained=True)
        # Make sure to set parameters as not trainable
        for param in model.parameters():
            param.requires_grad = False
        model.classifier = checkpoint['classifier']

    elif model_name == 'resnet50':
        model = models.resnet50(pretrained=True)
        # Make sure to set parameters as not trainable
        for param in model.parameters():
            param.requires_grad = False
        model.fc = checkpoint['fc']

    # Load in the state dict
    model.load_state_dict(checkpoint['state_dict'])

    total_params = sum(p.numel() for p in model.parameters())
    print(f'{total_params:,} total parameters.')
    total_trainable_params = sum(
        p.numel() for p in model.parameters() if p.requires_grad)
    print(f'{total_trainable_params:,} total gradient parameters.')

    # Move to gpu
    if multi_gpu:
        model = nn.DataParallel(model)

    if train_on_gpu:
        model = model.to('cuda')

    # Model basics
    model.epochs = checkpoint['epochs']

    # Optimizer
    optimizer = checkpoint['optimizer']
    optimizer.load_state_dict(checkpoint['optimizer_state_dict'])

    return model, optimizer

def process_image(image_path):
    """Process an image path into a PyTorch tensor"""

    image = Image.open(image_path)
    # Resize
    img = image.resize((256, 256))

    # Center crop
    width = 256
    height = 256
    new_width = 224
    new_height = 224

    left = (width - new_width) / 2
    top = (height - new_height) / 2
    right = (width + new_width) / 2
    bottom = (height + new_height) / 2
    img = img.crop((left, top, right, bottom))

    # Convert to numpy, transpose color dimension and normalize
    img = np.array(img).transpose((2, 0, 1)) / 256

    # Standardization
    means = np.array([0.485, 0.456, 0.406]).reshape((3, 1, 1))
    stds = np.array([0.229, 0.224, 0.225]).reshape((3, 1, 1))

    img = img - means
    img = img / stds

    img_tensor = torch.Tensor(img)

    return img_tensor

def predict(image_path, model, topk=5):
    """Make a prediction for an image using a trained model

    Params
    --------
        image_path (str): filename of the image
        model (PyTorch model): trained model for inference
        topk (int): number of top predictions to return

    Returns
        
    """
    train_on_gpu=False
    # Convert to pytorch tensor
    img_tensor = process_image(image_path)

    # Resize
    if train_on_gpu:
        img_tensor = img_tensor.view(1, 3, 224, 224).cuda()
    else:
        img_tensor = img_tensor.view(1, 3, 224, 224)

    # Set to evaluation
    with torch.no_grad():
        model.eval()
        # Model outputs log probabilities
        out = model(img_tensor)
        ps = torch.exp(out)

        # Find the topk predictions
        topk, topclass = ps.topk(topk, dim=1)

        
        return topclass[0][0]


predictions_index = {
    0:'Engine Block',
    1:'Cylinder Head',
    2:'Timing Belt',
    3:'Crank Shaft',
    4:'Piston'
}

def get_prediction(image_path,model):
    im = Image.open(image_path)
    return (predictions_index[int(predict(image_path,model))])

model,optimizer = 0,0
checkpoint_path = 'resnet50-transfer.pth'


app = Flask(__name__)
UPLOAD_FOLDER = './images_uploaded'

@app.route('/',methods=['GET','POST'])
def upload_predict():
	if request.method == "POST":
		image_file = request.files['image']
		if image_file :
			image_location = os.path.join(UPLOAD_FOLDER,image_file.filename)
			try:
				image_file.save(image_location)
				return get_prediction(image_location,model)
			except Exception as e:
				return "Error uploading..."
			return "Uploaded Successfully"
		else :
			return "No image found"

if __name__ == "__main__":
	model, optimizer = load_checkpoint(path=checkpoint_path)

	app.run(host="0.0.0.0", port=5000, debug=True)