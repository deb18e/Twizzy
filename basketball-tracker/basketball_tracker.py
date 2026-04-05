"""
basketball_tracker.py
---------------------
Module de détection et suivi de joueurs de basket.

Utilise YOLOv8-nano pré-entraîné COCO (classe « person ») combiné à
ByteTrack/BoT-SORT pour le suivi persistant.  Une heuristique de couleur HSV
tente de regrouper les joueurs en deux équipes en comparant la teinte dominante
de leurs maillots.
"""
import cv2
import numpy as np
import time
import os
import logging
from collections import defaultdict
from ultralytics import YOLO

logger = logging.getLogger(__name__)

# ── Singleton YOLO ────────────────────────────────────────────────────────────
_model = None
# Indice de la classe "person" dans le modèle COCO
_PERSON_CLASS = 0


def get_model() -> YOLO:
    """Charge le modèle YOLOv8n une seule fois (lazy)."""
    global _model
    if _model is None:
        model_path = os.path.join(os.path.dirname(__file__), "yolov8n.pt")
        if not os.path.isfile(model_path):
            # Téléchargement automatique par Ultralytics
            model_path = "yolov8n.pt"
        _model = YOLO(model_path)
    return _model


# ── Palette de couleurs pour les IDs de suivi ─────────────────────────────────
_PALETTE = [
    (255, 56, 56), (255, 157, 151), (255, 112, 31), (255, 178, 29),
    (207, 210, 49), (72, 249, 10), (146, 204, 23), (61, 219, 134),
    (26, 147, 52), (0, 212, 187), (44, 153, 168), (0, 194, 255),
    (52, 69, 147), (100, 115, 255), (0, 24, 236), (132, 56, 255),
    (82, 0, 133), (203, 56, 255), (255, 149, 200), (255, 55, 199),
]

# Couleurs fixes pour les deux équipes
_TEAM_COLORS = {
    1: (255, 100, 30),   # Équipe 1 : orange
    2: (30, 144, 255),   # Équipe 2 : bleu
    0: (200, 200, 200),  # Non attribué : gris
}


def _color_for_id(track_id: int) -> tuple:
    return _PALETTE[int(track_id) % len(_PALETTE)]


# ── Détection de couleur d'équipe ─────────────────────────────────────────────

def _dominant_hue(frame: np.ndarray, x1: int, y1: int, x2: int, y2: int) -> float | None:
    """
    Retourne la teinte médiane (0-180) du tiers supérieur de la boîte
    (zone du maillot), en excluant les tons de peau (~0-20°) et les pixels
    trop sombres ou trop clairs.
    """
    h, w = frame.shape[:2]
    x1, y1 = max(0, x1), max(0, y1)
    x2, y2 = min(w, x2), min(h, y2)
    box_h = y2 - y1
    if box_h < 20 or (x2 - x1) < 10:
        return None

    # Zone maillot : du quart supérieur aux deux-tiers de la boîte
    roi = frame[y1 + box_h // 4: y1 + 2 * box_h // 3, x1:x2]
    if roi.size == 0:
        return None

    hsv = cv2.cvtColor(roi, cv2.COLOR_BGR2HSV)
    h_ch, s_ch, v_ch = hsv[:, :, 0], hsv[:, :, 1], hsv[:, :, 2]

    # Masque : saturation suffisante (teinte non neutre) + luminosité raisonnable
    mask = (s_ch > 40) & (v_ch > 40) & (v_ch < 230)
    # Exclure les tons peau (rouge-orange clair, teinte 0-20 ou 160-180)
    skin_mask = ((h_ch < 20) | (h_ch > 160)) & (s_ch < 120)
    mask = mask & ~skin_mask

    valid = h_ch[mask]
    if valid.size < 50:
        return None
    return float(np.median(valid))


class TeamAssigner:
    """
    Associe chaque track ID à une équipe (1 ou 2) selon la couleur dominante
    de son maillot.  Utilise un clustering K-means adaptatif sur 2 centres.
    """

    def __init__(self):
        self._hues: dict[int, list[float]] = defaultdict(list)
        self._teams: dict[int, int] = {}
        self._center1: float | None = None
        self._center2: float | None = None

    def update(self, track_id: int, hue: float | None) -> int:
        """
        Met à jour l'historique des teintes et retourne l'équipe estimée (1, 2
        ou 0 si indéterminée).
        """
        if hue is None:
            return self._teams.get(track_id, 0)

        history = self._hues[track_id]
        history.append(hue)
        if len(history) > 30:
            history.pop(0)

        avg = float(np.median(history))

        # Ré-estimer les centres si on a assez de données
        all_avgs = [float(np.median(v)) for v in self._hues.values() if len(v) >= 5]
        if len(all_avgs) >= 4:
            self._fit_centers(all_avgs)

        if self._center1 is None or self._center2 is None:
            self._teams[track_id] = 0
            return 0

        d1 = abs(avg - self._center1)
        d2 = abs(avg - self._center2)
        team = 1 if d1 <= d2 else 2
        self._teams[track_id] = team
        return team

    def get_team(self, track_id: int) -> int:
        return self._teams.get(track_id, 0)

    def _fit_centers(self, avgs: list[float]):
        """Mini K-means à 2 centres sur la liste des teintes médianes."""
        data = np.array(avgs, dtype=np.float32).reshape(-1, 1)
        criteria = (cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 30, 1.0)
        _, _, centers = cv2.kmeans(data, 2, None, criteria, 5,
                                   cv2.KMEANS_PP_CENTERS)
        self._center1 = float(centers[0][0])
        self._center2 = float(centers[1][0])


# ── API publique ──────────────────────────────────────────────────────────────

def detect_image(image_bytes: bytes) -> tuple[bytes, list]:
    """
    Détecte les personnes (joueurs de basket) dans une image statique.

    Returns:
        (image annotée en JPEG bytes, liste de détections)
        Chaque détection : {"label", "confidence", "bbox": [x1,y1,x2,y2]}
    """
    nparr = np.frombuffer(image_bytes, np.uint8)
    frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    if frame is None:
        raise ValueError("Impossible de décoder l'image.")

    model = get_model()
    results = model(frame, classes=[_PERSON_CLASS], verbose=False)[0]

    detections = []
    for box in results.boxes:
        cls_id = int(box.cls[0])
        conf = float(box.conf[0])
        label = model.names[cls_id]
        x1, y1, x2, y2 = map(int, box.xyxy[0])

        color = (255, 100, 30)
        cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
        text = f"Joueur {conf:.2f}"
        (tw, th), _ = cv2.getTextSize(text, cv2.FONT_HERSHEY_SIMPLEX, 0.6, 1)
        cv2.rectangle(frame, (x1, y1 - th - 6), (x1 + tw + 4, y1), color, -1)
        cv2.putText(frame, text, (x1 + 2, y1 - 3),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 255), 1)

        detections.append({"label": label, "confidence": round(conf, 3),
                            "bbox": [x1, y1, x2, y2]})

    # Compteur total
    count_text = f"Joueurs détectés : {len(detections)}"
    cv2.putText(frame, count_text, (10, 30),
                cv2.FONT_HERSHEY_SIMPLEX, 0.9, (255, 255, 255), 2)

    _, buf = cv2.imencode(".jpg", frame)
    return buf.tobytes(), detections


def generate_tracked_video(video_path: str, tracker: str = "bytetrack"):
    """
    Générateur MJPEG : lit la vidéo, détecte et suit les joueurs de basket,
    annote les frames avec ID, équipe et trajectoire.

    Yields:
        Blocs multipart JPEG (b'--frame\\r\\n...')
    """
    if not os.path.isfile(video_path):
        logger.error("Fichier vidéo introuvable : %s", video_path)
        return

    tracker_yaml = "bytetrack.yaml" if tracker != "botsort" else "botsort.yaml"
    model = get_model()
    cap = cv2.VideoCapture(video_path)

    if not cap.isOpened():
        logger.error("Impossible d'ouvrir la vidéo : %s", video_path)
        return

    track_history: dict[int, list] = defaultdict(list)
    assigner = TeamAssigner()
    fps = cap.get(cv2.CAP_PROP_FPS) or 25
    delay = 1.0 / fps
    frame_count = 0

    try:
        while cap.isOpened():
            ret, frame = cap.read()
            if not ret:
                break

            frame_count += 1
            results = model.track(frame, persist=True, tracker=tracker_yaml,
                                   classes=[_PERSON_CLASS], verbose=False)[0]

            team_counts = {1: 0, 2: 0, 0: 0}

            if results.boxes.id is not None:
                boxes = results.boxes.xyxy.cpu().numpy().astype(int)
                track_ids = results.boxes.id.cpu().numpy().astype(int)
                confs = results.boxes.conf.cpu().numpy()

                for box, tid, conf in zip(boxes, track_ids, confs):
                    x1, y1, x2, y2 = box

                    hue = _dominant_hue(frame, x1, y1, x2, y2)
                    team = assigner.update(int(tid), hue)
                    team_counts[team] = team_counts.get(team, 0) + 1

                    color = _TEAM_COLORS.get(team, _color_for_id(int(tid)))

                    # Boîte englobante
                    cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)

                    # Étiquette
                    team_label = f"Éq.{team}" if team != 0 else "?"
                    text = f"#{tid} {team_label} {conf:.2f}"
                    (tw, th), _ = cv2.getTextSize(
                        text, cv2.FONT_HERSHEY_SIMPLEX, 0.55, 1)
                    cv2.rectangle(frame,
                                  (x1, y1 - th - 8), (x1 + tw + 4, y1),
                                  color, -1)
                    cv2.putText(frame, text, (x1 + 2, y1 - 4),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.55,
                                (255, 255, 255), 1)

                    # Trajectoire
                    cx, cy = (x1 + x2) // 2, (y1 + y2) // 2
                    history = track_history[int(tid)]
                    history.append((cx, cy))
                    if len(history) > 50:
                        history.pop(0)
                    if len(history) > 1:
                        pts = np.array(history, dtype=np.int32)
                        cv2.polylines(frame, [pts], False, color, 2)

            # Bandeau de stats en bas de l'image
            _draw_stats_bar(frame, team_counts, frame_count)

            _, buf = cv2.imencode(".jpg", frame)
            yield (b"--frame\r\n"
                   b"Content-Type: image/jpeg\r\n\r\n"
                   + buf.tobytes() + b"\r\n")

            time.sleep(delay)

    finally:
        cap.release()


def _draw_stats_bar(frame: np.ndarray, team_counts: dict, frame_no: int):
    """Affiche un bandeau semi-transparent avec les comptes par équipe."""
    h, w = frame.shape[:2]
    bar_h = 36
    overlay = frame.copy()
    cv2.rectangle(overlay, (0, h - bar_h), (w, h), (20, 20, 20), -1)
    cv2.addWeighted(overlay, 0.6, frame, 0.4, 0, frame)

    total = team_counts.get(1, 0) + team_counts.get(2, 0)
    text = (
        f"  🏀 Équipe 1 : {team_counts.get(1, 0)} joueurs   "
        f"Équipe 2 : {team_counts.get(2, 0)} joueurs   "
        f"Total : {total}   Frame #{frame_no}"
    )
    cv2.putText(frame, text, (8, h - 10),
                cv2.FONT_HERSHEY_SIMPLEX, 0.55, (255, 255, 255), 1)
