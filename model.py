import os
import pandas as pd
from PIL import Image
import torch
from torch.utils.data import Dataset, DataLoader
from torchvision import transforms
import matplotlib.pyplot as plt
import torch.nn as nn
import torch.optim as optim

# Transformation des images
transform = transforms.Compose([
    transforms.Resize((64, 64)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
])

# Créer un dataset personnalisé
class ImageDataset(Dataset):
    def __init__(self, root, labels, transform=None):
        self.root = root
        self.labels = labels
        self.transform = transform

    def __len__(self):
        return len(self.labels)

    def __getitem__(self, idx):
        img_path = os.path.join(self.root, self.labels.iloc[idx]['Path'])
        image = Image.open(img_path).convert('RGB')  # Ouvrir l'image et la convertir en RGB
        label = self.labels.iloc[idx]['ClassId']  # Récupérer l'étiquette

        if self.transform:
            image = self.transform(image)  # Appliquer les transformations (si présentes)

        return image, label
    
class BasicBlock(nn.Module):
    def __init__(self, in_channels, out_channels, stride=1):
        super(BasicBlock, self).__init__()
        self.conv1 = nn.Conv2d(in_channels, out_channels, kernel_size=3, stride=stride, padding=1, bias=False)
        self.bn1 = nn.BatchNorm2d(out_channels)
        self.relu = nn.ReLU(inplace=True)
        self.conv2 = nn.Conv2d(out_channels, out_channels, kernel_size=3, stride=1, padding=1, bias=False)
        self.bn2 = nn.BatchNorm2d(out_channels)
        
        self.shortcut = nn.Sequential()
        if stride != 1 or in_channels != out_channels:
            self.shortcut = nn.Sequential(
                nn.Conv2d(in_channels, out_channels, kernel_size=1, stride=stride, bias=False),
                nn.BatchNorm2d(out_channels)
            )

    def forward(self, x):
        out = self.conv1(x)
        out = self.bn1(out)
        out = self.relu(out)
        out = self.conv2(out)
        out = self.bn2(out)
        out += self.shortcut(x)
        out = self.relu(out)
        return out


class ResNet(nn.Module):
    def __init__(self, block, layers, num_classes=8):
        super(ResNet, self).__init__()
        self.in_channels = 64
        self.conv1 = nn.Conv2d(3, 64, kernel_size=7, stride=2, padding=3, bias=False)
        self.bn1 = nn.BatchNorm2d(64)
        self.relu = nn.ReLU(inplace=True)
        self.maxpool = nn.MaxPool2d(kernel_size=3, stride=2, padding=1)
        self.layer1 = self._make_layer(block, 64, layers[0])
        self.layer2 = self._make_layer(block, 128, layers[1], stride=2)
        self.layer3 = self._make_layer(block, 256, layers[2], stride=2)
        self.layer4 = self._make_layer(block, 512, layers[3], stride=2)
        self.avgpool = nn.AdaptiveAvgPool2d((1, 1))
        self.fc = nn.Linear(512, num_classes)

    def _make_layer(self, block, out_channels, num_blocks, stride=1):
        layers = []
        layers.append(block(self.in_channels, out_channels, stride))
        self.in_channels = out_channels
        for _ in range(1, num_blocks):
            layers.append(block(self.in_channels, out_channels))
        return nn.Sequential(*layers)

    def forward(self, x):
        x = self.conv1(x)
        x = self.bn1(x)
        x = self.relu(x)
        x = self.maxpool(x)
        x = self.layer1(x)
        x = self.layer2(x)
        x = self.layer3(x)
        x = self.layer4(x)
        x = self.avgpool(x)
        x = torch.flatten(x, 1)
        x = self.fc(x)
        return x
# Chemins des fichiers sauvegardés
MODEL_PATH = './model_resnet4_classes.pth'
OPTIMIZER_PATH = './optimizer_resnet4_classes.pth'

# Définir l'appareil (CPU ou GPU)
DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# Instancier le modèle avec la même architecture
model = ResNet(BasicBlock, layers=[3, 4, 6, 3], num_classes=8)
model.load_state_dict(torch.load(MODEL_PATH, map_location=DEVICE))
model = model.to(DEVICE)

# Instancier l'optimiseur avec les mêmes paramètres
optimizer = torch.optim.SGD(model.parameters(), momentum=0.9, lr=0.01)
optimizer.load_state_dict(torch.load(OPTIMIZER_PATH, map_location=DEVICE))
model.eval()
# Mode évaluation (désactive le dropout, batchnorm)
model.eval()

# Exemple d'utilisation pour une image de test
test_image_path = './archive/Test/Capture d’écran 2025-05-11 111819.png'
test_image = Image.open(test_image_path).convert('RGB')

# Appliquer les transformations
test_image = transform(test_image).unsqueeze(0).to(DEVICE)

# Effectuer la prédiction
with torch.no_grad():  # Désactive le calcul de gradients pour l'inférence
    output = model(test_image)
    _, predicted_class = torch.max(output, 1)

# Dictionnaire des correspondances classe -> vitesse
speed_limits = {
    0: "30 km/h",
    1: "Stop",
    2: "50 km/h",
    3: "70 km/h",
    4: "80 km/h",
    6: "110 km/h",
    5: "90 km/h" ,
    7: "130mn/h"

}

# Récupérer la vitesse correspondante
predicted_speed = speed_limits.get(predicted_class.item(), "Classe inconnue")

#print(f'Classe prédite : {predicted_class.item()} - Vitesse associée : {predicted_speed}')