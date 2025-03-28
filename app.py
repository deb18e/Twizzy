from PIL import Image
import torch
from torchvision import transforms
from model import ResNet, BasicBlock
from flask import Flask, jsonify, request
from waitress import serve
from flask_cors import CORS
import warnings

# Initialisation de l'application Flask
app = Flask(__name__)
CORS(app)  # Activation CORS - Doit être placé juste après la création de l'app

# Suppression des avertissements indésirables
warnings.filterwarnings("ignore", category=UserWarning, message="Named tensors and all their associated APIs are an experimental feature")

# Charger le modèle
MODEL_PATH = './model_resnet4_classes.pth'
DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# Initialisation du modèle
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

@app.route('/')
def index():
    return 'Bienvenue sur l\'API du modèle ResNet !'

@app.route('/predict', methods=['POST'])
def predict():
    # Vérification de la présence de l'image
    if 'image' not in request.files:
        return jsonify({'error': 'Aucune image fournie'}), 400

    file = request.files['image']
    
    # Vérification du type de fichier (uniquement PNG)
    if not file.filename.lower().endswith('.png'):
        return jsonify({'error': 'Seuls les fichiers PNG sont acceptés'}), 400

    try:
        # Ouverture et conversion de l'image
        image = Image.open(file.stream).convert('RGB')
    except Exception as e:
        return jsonify({'error': 'Image invalide', 'details': str(e)}), 400

    try:
        # Application des transformations
        image = transform(image).unsqueeze(0).to(DEVICE)
        
        # Prédiction
        with torch.no_grad():
            output = model(image)
            _, predicted_class = torch.max(output, 1)

        # Correspondance classe -> vitesse
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

        predicted_speed = speed_limits.get(predicted_class.item(), "Classe inconnue")

        return jsonify({
            'class': predicted_class.item(),
            'speed': predicted_speed,
            'status': 'success'
        })

    except Exception as e:
        return jsonify({
            'error': 'Erreur lors de la prédiction',
            'details': str(e)
        }), 500

if __name__ == '__main__':
    # Mode développement
    app.run(debug=True, host='0.0.0.0', port=5000)
    
    # Mode production (décommenter pour utiliser)
    # serve(app, host='0.0.0.0', port=5000)