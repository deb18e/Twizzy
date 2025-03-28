from PIL import Image
import torch
from torchvision import transforms
from model import ResNet, BasicBlock
from flask import Flask, jsonify, request
from waitress import serve

app = Flask(__name__)

# Charger le modèle
MODEL_PATH = './model_resnet4_classes.pth'
DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model = ResNet(BasicBlock, layers=[3, 4, 6, 3], num_classes=8)
model.load_state_dict(torch.load(MODEL_PATH, map_location=DEVICE))
model = model.to(DEVICE)
model.eval()

# Transformation des images
transform = transforms.Compose([
    transforms.Resize((64, 64)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
])
import warnings
warnings.filterwarnings("ignore", category=UserWarning, message="Named tensors and all their associated APIs are an experimental feature")


@app.route('/')
def index():
    return 'Bienvenue sur l\'API du modèle ResNet !'

@app.route('/predict', methods=['POST'])
def predict():
    if 'image' not in request.files:
        return jsonify({'error': 'No image provided'}), 400

    file = request.files['image']
    try:
        image = Image.open(file.stream).convert('RGB')
    except Exception as e:
        return jsonify({'error': 'Invalid image'}), 400

    # Appliquer les transformations à l'image
    image = transform(image).unsqueeze(0).to(DEVICE)

    # Faire une prédiction avec le modèle
    with torch.no_grad():
        output = model(image)
        _, predicted_class = torch.max(output, 1)

    # Dictionnaire des correspondances classe -> vitesse
    speed_limits = {
        0: "30 km/h",
        1: "Stop",
        2: "50 km/h",
        3: "70 km/h",
        4: "80 km/h",
        5: "90 km/h",
        6: "110 km/h",
        7: "130 km/h"
    }

    # Récupérer la vitesse associée à la classe prédite
    predicted_speed = speed_limits.get(predicted_class.item(), "Classe inconnue")

    # Retourner la réponse en JSON
    return jsonify({
        'class': predicted_class.item(),
        'speed': predicted_speed
    })

if __name__ == '__main__':
    # Use this for development
    app.run(debug=True)

    # Use this for production
    serve(app, host='0.0.0.0', port=5000)