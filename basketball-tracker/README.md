# 🏀 Basketball Player Tracker

Application Flask de **détection et suivi de joueurs de basket** en temps réel,
basée sur YOLOv8 + ByteTrack/BoT-SORT avec détection automatique des équipes par
analyse de couleur de maillot.

---

## Fonctionnalités

| Fonctionnalité | Description |
|---|---|
| **Détection de joueurs** | YOLOv8-nano COCO, filtre la classe `person` |
| **Suivi persistant** | ByteTrack ou BoT-SORT — ID unique par joueur entre les frames |
| **Trajectoires** | Polyline des 50 dernières positions de chaque joueur |
| **Détection d'équipes** | K-means HSV sur la teinte du maillot → 2 équipes automatiques |
| **Stats en temps réel** | Comptage par équipe affiché dans le bandeau vidéo et le scoreboard |
| **Analyse d'image** | Upload d'une photo et retour JSON + image annotée |

---

## Structure du projet

```
basketball-tracker/
├── app_basket.py           # Serveur Flask (routes API + interface)
├── basketball_tracker.py   # Module de détection/suivi
├── requirements.txt        # Dépendances Python
├── yolov8n.pt              # Modèle YOLOv8n (téléchargé automatiquement)
├── static/                 # Vidéos temporaires uploadées
└── templates/
    └── basket.html         # Interface web
```

---

## Prérequis

- Python 3.10+
- (Optionnel) GPU NVIDIA avec CUDA pour accélération

---

## Installation

```bash
# 1. Se placer dans le dossier
cd basketball-tracker

# 2. Créer un environnement virtuel (recommandé)
python -m venv venv
source venv/bin/activate      # Linux/macOS
# venv\Scripts\activate       # Windows

# 3. Installer les dépendances
pip install -r requirements.txt
```

Le modèle `yolov8n.pt` sera **téléchargé automatiquement** par Ultralytics au
premier lancement (~6 Mo).

---

## Lancement

```bash
python app_basket.py
```

L'application est disponible sur **http://localhost:5001**

Pour la production :

```bash
gunicorn -w 1 -b 0.0.0.0:5001 app_basket:app
```

> ⚠️ Utiliser **1 worker** (`-w 1`) car le modèle YOLO est chargé en mémoire
> globale. Plusieurs workers créeraient plusieurs instances du modèle.

---

## Utilisation

### Analyse d'image

1. Cliquez sur l'onglet **🖼️ Analyse Image**
2. Déposez ou sélectionnez une photo de match de basket
3. L'image annotée s'affiche avec les boîtes englobantes et le score de confiance

### Suivi vidéo

1. Cliquez sur l'onglet **📹 Suivi Vidéo**
2. Déposez une vidéo de match (MP4, AVI, MOV…)
3. Choisissez l'algorithme de suivi (ByteTrack recommandé)
4. Cliquez **▶ Lancer le suivi**
5. Le flux MJPEG annoté s'affiche avec :
   - Boîtes englobantes colorées par équipe (🟠 Équipe 1, 🔵 Équipe 2)
   - ID persistant `#N` pour chaque joueur
   - Trajectoire des derniers déplacements
   - Bandeau de stats avec comptage par équipe

---

## Routes API

| Méthode | Route | Description |
|---|---|---|
| `GET` | `/` | Interface web |
| `POST` | `/upload` | Upload d'une vidéo (form-data, champ `file`) |
| `GET` | `/video_feed?tracker=bytetrack` | Flux MJPEG annoté |
| `POST` | `/detect_image` | Détection image → `{image: base64, detections: [...]}` |

---

## Amélioration possible : dataset basket spécifique

Pour de meilleures performances (détection de la balle, des arbitres, des numéros
de maillot), il est possible de fine-tuner YOLOv8 sur un dataset spécialisé :

- **Roboflow Universe** : [Basketball Players Detection](https://universe.roboflow.com/search?q=basketball+players)
- **Kaggle** : datasets NBA broadcast

```python
from ultralytics import YOLO
model = YOLO("yolov8n.pt")
model.train(data="basketball.yaml", epochs=50, imgsz=640)
```

Remplacez ensuite `"yolov8n.pt"` par `"runs/detect/train/weights/best.pt"` dans
`basketball_tracker.py`.

---

## Licence

Ce projet est une extension du dépôt [Twizzy](https://github.com/deb18e/Twizzy).
