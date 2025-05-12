from flask import Flask, request, send_file, jsonify
from flask_cors import CORS
import os
import uuid
from process_video import process_video  # Attention au nom du fichier

app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": "*"}})

UPLOAD_FOLDER = 'uploads'
OUTPUT_FOLDER = 'outputs'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)
os.makedirs(OUTPUT_FOLDER, exist_ok=True)

@app.route('/process_video', methods=['POST'])
def handle_video():
    if 'video' not in request.files:
        return jsonify({'error': 'Aucune vidéo fournie'}), 400

    file = request.files['video']
    if file.filename == '':
        return jsonify({'error': 'Fichier vide'}), 400

    input_filename = os.path.join(UPLOAD_FOLDER, f"{uuid.uuid4()}.avi")
    output_filename = os.path.join(OUTPUT_FOLDER, f"{uuid.uuid4()}.avi")
    
    file.save(input_filename)

    try:
        # Traitement de la vidéo
        process_video(input_filename, output_filename)  # on modifie cette fonction dans traitement.py
        return send_file(output_filename, mimetype='video/x-msvideo')
    
    except Exception as e:
        return jsonify({'error': str(e)}), 500

    finally:
        if os.path.exists(input_filename):
            os.remove(input_filename)
        # Optionnel : supprimer aussi output_filename après envoi si nécessaire

if __name__ == '__main__':
    app.run(port=5001)