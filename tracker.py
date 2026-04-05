import cv2
import numpy as np
import time
import os
import logging
from collections import defaultdict
from ultralytics import YOLO

logger = logging.getLogger(__name__)

# Charger le modèle YOLOv8n (nano) au démarrage - léger et rapide
_model = None


def get_model():
    """Retourne le modèle YOLO (chargé en lazy)."""
    global _model
    if _model is None:
        _model = YOLO("yolov8n.pt")
    return _model


# Palette de couleurs pour les IDs de suivi
_PALETTE = [
    (255, 56, 56), (255, 157, 151), (255, 112, 31), (255, 178, 29),
    (207, 210, 49), (72, 249, 10), (146, 204, 23), (61, 219, 134),
    (26, 147, 52), (0, 212, 187), (44, 153, 168), (0, 194, 255),
    (52, 69, 147), (100, 115, 255), (0, 24, 236), (132, 56, 255),
    (82, 0, 133), (203, 56, 255), (255, 149, 200), (255, 55, 199),
]


def _color_for_id(track_id: int):
    return _PALETTE[int(track_id) % len(_PALETTE)]


def detect_image(image_bytes: bytes) -> tuple[bytes, list]:
    """
    Détecte les objets dans une image.

    Args:
        image_bytes: Image brute en bytes.

    Returns:
        Tuple (image annotée en bytes JPEG, liste des détections).
    """
    nparr = np.frombuffer(image_bytes, np.uint8)
    frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    if frame is None:
        raise ValueError("Impossible de décoder l'image.")

    model = get_model()
    results = model(frame, verbose=False)[0]

    detections = []
    for box in results.boxes:
        cls_id = int(box.cls[0])
        conf = float(box.conf[0])
        label = model.names[cls_id]
        x1, y1, x2, y2 = map(int, box.xyxy[0])

        color = _PALETTE[cls_id % len(_PALETTE)]
        cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
        text = f"{label} {conf:.2f}"
        cv2.putText(frame, text, (x1, max(y1 - 8, 12)),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)

        detections.append({"label": label, "confidence": round(conf, 3),
                            "bbox": [x1, y1, x2, y2]})

    _, buf = cv2.imencode(".jpg", frame)
    return buf.tobytes(), detections


def generate_tracked_video(video_path: str, tracker: str = "bytetrack"):
    """
    Générateur qui lit une vidéo, effectue la détection+suivi YOLO et
    renvoie les frames annotées en multipart JPEG.

    Args:
        video_path: Chemin vers le fichier vidéo.
        tracker: Algorithme de suivi ('bytetrack' ou 'botsort').
    """
    if not os.path.isfile(video_path):
        logger.error("Fichier vidéo introuvable : %s", video_path)
        return

    # Ultralytics attend un fichier .yaml pour le tracker
    tracker_yaml = "bytetrack.yaml" if tracker != "botsort" else "botsort.yaml"

    model = get_model()
    cap = cv2.VideoCapture(video_path)

    if not cap.isOpened():
        logger.error("Impossible d'ouvrir la vidéo : %s", video_path)
        return

    # Historique des traces pour chaque ID (liste de centres)
    track_history: dict[int, list] = defaultdict(list)

    fps = cap.get(cv2.CAP_PROP_FPS) or 25
    delay = 1.0 / fps

    try:
        while cap.isOpened():
            ret, frame = cap.read()
            if not ret:
                break

            # Détection + suivi sur la frame courante
            results = model.track(frame, persist=True, tracker=tracker_yaml,
                                  verbose=False)[0]

            if results.boxes.id is not None:
                boxes = results.boxes.xyxy.cpu().numpy().astype(int)
                track_ids = results.boxes.id.cpu().numpy().astype(int)
                cls_ids = results.boxes.cls.cpu().numpy().astype(int)
                confs = results.boxes.conf.cpu().numpy()

                for box, tid, cls_id, conf in zip(boxes, track_ids, cls_ids, confs):
                    x1, y1, x2, y2 = box
                    color = _color_for_id(tid)
                    label = model.names[cls_id]

                    # Boîte englobante
                    cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)

                    # Étiquette : nom + ID + confiance
                    text = f"#{tid} {label} {conf:.2f}"
                    (tw, th), _ = cv2.getTextSize(
                        text, cv2.FONT_HERSHEY_SIMPLEX, 0.55, 1)
                    cv2.rectangle(frame, (x1, y1 - th - 8),
                                  (x1 + tw + 4, y1), color, -1)
                    cv2.putText(frame, text, (x1 + 2, y1 - 4),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.55,
                                (255, 255, 255), 1)

                    # Tracer la trajectoire
                    cx, cy = (x1 + x2) // 2, (y1 + y2) // 2
                    track_history[tid].append((cx, cy))
                    if len(track_history[tid]) > 40:
                        track_history[tid].pop(0)

                    pts = np.array(track_history[tid], dtype=np.int32)
                    if len(pts) > 1:
                        cv2.polylines(frame, [pts], False, color, 2)

            _, buf = cv2.imencode(".jpg", frame)
            yield (b"--frame\r\n"
                   b"Content-Type: image/jpeg\r\n\r\n"
                   + buf.tobytes() + b"\r\n")

            time.sleep(delay)

    finally:
        cap.release()
