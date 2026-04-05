"""
app_basket.py
-------------
Serveur Flask dédié à la détection et au suivi de joueurs de basket.

Routes :
    GET  /                    → Interface web
    POST /upload              → Upload vidéo (sauvegarde temporaire)
    GET  /video_feed          → Flux MJPEG annoté
    POST /detect_image        → Détection sur image statique (JSON)
"""
import os
import uuid
import base64
import logging

from flask import Flask, jsonify, render_template, request, Response
from flask_cors import CORS
from werkzeug.utils import secure_filename

import basketball_tracker as bt

# ── App setup ─────────────────────────────────────────────────────────────────
app = Flask(__name__)
CORS(app)
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

STATIC_DIR = os.path.join(os.path.dirname(__file__), "static")
os.makedirs(STATIC_DIR, exist_ok=True)

ALLOWED_VIDEO_EXT = {"mp4", "avi", "mov", "mkv", "webm"}
ALLOWED_IMAGE_EXT = {"png", "jpg", "jpeg", "bmp", "webp"}


def _allowed_video(filename: str) -> bool:
    return "." in filename and filename.rsplit(".", 1)[1].lower() in ALLOWED_VIDEO_EXT


def _allowed_image(filename: str) -> bool:
    return "." in filename and filename.rsplit(".", 1)[1].lower() in ALLOWED_IMAGE_EXT


# Chemin courant de la vidéo uploadée
current_video_path: str | None = None


# ── Routes ────────────────────────────────────────────────────────────────────

@app.route("/")
def index():
    return render_template("basket.html")


@app.route("/upload", methods=["POST"])
def upload():
    """Reçoit une vidéo et la sauvegarde avec un nom UUID unique."""
    global current_video_path

    if "file" not in request.files:
        return jsonify({"error": "Aucun fichier envoyé"}), 400

    file = request.files["file"]
    if not file.filename:
        return jsonify({"error": "Nom de fichier manquant"}), 400

    filename = secure_filename(file.filename)
    if not _allowed_video(filename):
        return jsonify({"error": "Format vidéo non supporté"}), 400

    # Supprimer l'ancienne vidéo temporaire
    if current_video_path and os.path.isfile(current_video_path):
        try:
            os.remove(current_video_path)
        except OSError:
            pass

    unique_name = f"basket_{uuid.uuid4().hex}.mp4"
    save_path = os.path.join(STATIC_DIR, unique_name)
    file.save(save_path)
    current_video_path = save_path

    return jsonify({"status": "success"})


@app.route("/video_feed")
def video_feed():
    """Flux MJPEG avec détection et suivi des joueurs."""
    if not current_video_path:
        return jsonify({"error": "Aucune vidéo disponible"}), 404

    tracker_name = request.args.get("tracker", "bytetrack")
    if tracker_name not in ("bytetrack", "botsort"):
        tracker_name = "bytetrack"

    return Response(
        bt.generate_tracked_video(current_video_path, tracker=tracker_name),
        mimetype="multipart/x-mixed-replace; boundary=frame",
    )


@app.route("/detect_image", methods=["POST"])
def detect_image():
    """Détecte les joueurs dans une image statique."""
    if "file" not in request.files:
        return jsonify({"error": "Aucun fichier envoyé"}), 400

    file = request.files["file"]
    if not file.filename:
        return jsonify({"error": "Nom de fichier manquant"}), 400

    filename = secure_filename(file.filename)
    if not _allowed_image(filename):
        return jsonify({"error": "Format image non supporté"}), 400

    image_bytes = file.read()
    try:
        annotated_bytes, detections = bt.detect_image(image_bytes)
        encoded = base64.b64encode(annotated_bytes).decode("utf-8")
        return jsonify({"image": encoded, "detections": detections})
    except Exception as exc:
        logger.error("Erreur détection image : %s", exc, exc_info=True)
        return jsonify({"error": "Erreur lors de la détection"}), 500


if __name__ == "__main__":
    app.run(debug=False, port=5001)
