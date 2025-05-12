from flask import Flask, jsonify, request, render_template, Response
from flask_cors import CORS
from PIL import Image
import io
import torch
from torchvision import transforms
import cv2
import numpy as np
import logging
from model import ResNet, BasicBlock
import os

app = Flask(__name__)
CORS(app)

# Logger
logging.basicConfig(level=logging.DEBUG)
app.logger = logging.getLogger(__name__)

# Détection vidéo
video_stream = None

# Fichier autorisés
def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in {'png', 'jpg', 'jpeg', 'mp4'}

# Modèle
DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")
MODEL_PATH = './model_resnet4_classes.pth'
model = ResNet(BasicBlock, layers=[3, 4, 6, 3], num_classes=8)
model.load_state_dict(torch.load(MODEL_PATH, map_location=DEVICE))
model = model.to(DEVICE)
model.eval()

# Transformations
transform = transforms.Compose([
    transforms.Resize((64, 64)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406],
                         std=[0.229, 0.224, 0.225])
])

# Dictionnaire des classes
speed_limits = {
    0: "30 km/h", 1: "Stop", 2: "50 km/h", 3: "70 km/h",
    4: "110 km/h", 5: "90 km/h", 6: "130 km/h", 7: "30 km/h"
}

@app.route('/')
def home():
    return render_template('index2.html')

@app.route('/predict', methods=['POST', 'OPTIONS'])
def predict():
    try:
        if request.method == 'OPTIONS':
            return jsonify({'status': 'ok'}), 200

        if 'file' not in request.files:
            return jsonify({'error': 'Aucune image fournie'}), 400

        file = request.files['file']
        if file.filename == '':
            return jsonify({'error': 'Aucun fichier sélectionné'}), 400

        if not allowed_file(file.filename):
            return jsonify({'error': 'Format de fichier non supporté'}), 400
        try:
            # Lecture de l'image
            image_bytes = file.read()
            original_image = Image.open(io.BytesIO(image_bytes)).convert('RGB')
            width, height = original_image.size
            app.logger.debug(f"Image reçue: {width}x{height}")

            # Prétraitement de l'image
            if width < 200:
                processed_image = original_image
            else:
                # Logique de rognage sécurisée
                try:
                    if width == 650:
                        x0, y0, x1, y1 = int(0.70*width), int(0.20*height), width-50, int(0.70*height)
                    elif width == 594:
                        x0, y0, x1, y1 = int(0.50*width), int(0.10*height), width-50, int(0.50*height)
                    elif width == 1200:
                        x0, y0, x1, y1 = int(0.40*width), height-500, width-520, height-350
                    elif width == 1600:
                        x0, y0, x1, y1 = int(0.70*width), height-1200, width, height-800
                    else:
                        x0, y0, x1, y1 = int(0.70*width), int(0.20*height), width, height

                    # Validation des coordonnées de rognage
                    x0 = max(0, x0)
                    y0 = max(0, y0)
                    x1 = min(width, x1)
                    y1 = min(height, y1)
                    
                    cropped_image = original_image.crop((x0, y0, x1, y1))
                    processed_image = cropped_image.resize((80, 80))
                    
                except Exception as crop_error:
                    app.logger.error(f"Erreur de rognage: {str(crop_error)}")
                    processed_image = original_image.resize((80, 80))

            # Transformation pour le modèle
            transformed = transform(processed_image).unsqueeze(0).to(DEVICE)
            
            # Prédiction
            with torch.no_grad():
                outputs = model(transformed)
                _, predicted = torch.max(outputs, 1)
                
            return jsonify({
                'status': 'success',
                'data': {
                    'class': predicted.item(),
                    'speed': speed_limits.get(predicted.item(), "Inconnu")
                }
            })
        except Exception as e:
            app.logger.error(f"Erreur de traitement: {str(e)}", exc_info=True)
            return jsonify({'error': 'Erreur de traitement de l\'image'}), 500
            
    except Exception as e:
        app.logger.critical(f"Erreur critique: {str(e)}", exc_info=True)
        return jsonify({'error': 'Erreur interne du serveur'}), 500
@app.route('/predict_video', methods=['POST'])
def predict_video():
    if 'file' not in request.files:
        return jsonify({'status': 'error', 'error': 'No file uploaded'}), 400
    
    file = request.files['file']
    if file.filename == '':
        return jsonify({'status': 'error', 'error': 'No filename'}), 400

    # Sauvegarde temporaire de la vidéo
    video_path = os.path.join('static', 'temp_video.mp4')
    file.save(video_path)

    # Sauvegarde le chemin globalement si nécessaire
    global current_video_path
    current_video_path = video_path

    return jsonify({'status': 'success'})


def generate_video():
    global video_stream
    if video_stream is None:
        return

    while video_stream.isOpened():
        ret, frame = video_stream.read()
        if not ret:
            break

        try:
            # Convertir OpenCV -> PIL
            pil_image = Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
            pil_resized = pil_image.resize((80, 80))

            transformed = transform(pil_resized).unsqueeze(0).to(DEVICE)
            with torch.no_grad():
                outputs = model(transformed)
                _, predicted = torch.max(outputs, 1)

            label = speed_limits.get(predicted.item(), "Inconnu")
            cv2.putText(frame, label, (30, 50), cv2.FONT_HERSHEY_SIMPLEX, 1.2, (0, 255, 0), 2)
            cv2.rectangle(frame, (20, 20), (200, 70), (0, 255, 0), 2)

        except Exception as e:
            app.logger.error(f"Erreur dans le traitement frame: {str(e)}")

        # Encode frame en JPEG
        _, buffer = cv2.imencode('.jpg', frame)
        frame_bytes = buffer.tobytes()
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + frame_bytes + b'\r\n')

    video_stream.release()
import cv2

import time

def process_video(video_path):
    cap = cv2.VideoCapture(video_path)

    if not cap.isOpened():
        print("Erreur : Impossible d’ouvrir la vidéo.")
        return

    while True:
        success, frame = cap.read()
        if not success:
            break

        try:
            # Redimensionner le frame pour correspondre à l'entrée du modèle
            pil_image = Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
            pil_resized = pil_image.resize((80, 80))

            transformed = transform(pil_resized).unsqueeze(0).to(DEVICE)
            with torch.no_grad():
                outputs = model(transformed)
                _, predicted = torch.max(outputs, 1)
            # Dictionnaire des panneaux
            # Dictionnaire des panneaux
            speeds_limits = {0: "30 km/h",1: "90",2: "50 km/h",3: "70 km/h",4: "70 km/h",5: "stop",6: "110 km/h",7: "110 km/h"
            }
            label = speeds_limits.get(predicted.item(), "Inconnu")
            app.logger.debug(f"Prédiction vidéo: {label}")

            # Afficher le texte sur le frame
            cv2.rectangle(frame, (20, 20), (350, 80), (255, 255, 255), -1)
            cv2.putText(frame, f"Panneau: {label}", (30, 60),
                        cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2)

        except Exception as e:
            app.logger.error(f"Erreur traitement vidéo: {e}")
            cv2.putText(frame, "Erreur détection", (30, 60),
                        cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2)

        # Encode l'image et renvoie
        ret, buffer = cv2.imencode('.jpg', frame)
        if not ret:
            continue

        frame_bytes = buffer.tobytes()
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + frame_bytes + b'\r\n')

        time.sleep(1)  

    cap.release()


@app.route('/video_feed')
def video_feed():
    return Response(process_video(current_video_path),
                    mimetype='multipart/x-mixed-replace; boundary=frame')

if __name__ == '__main__':
    app.run(debug=True)