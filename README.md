# Projet Twizzy
La détection des panneaux de signalisation routière est un élément essentiel pour le déplacement sécurisé des véhicules autonomes. Cette technologie permet d’intégrer des informations en temps réel sur les limitations de vitesse et autres signalisations, optimisant ainsi la conduite automatique et assistée.
L'objectif de ce projet est de créer une interface pour détecter les panneaux de signalisation avec OpenCV en première partie et avec Deep Learning CNN en deuxième partie.

## Détection des panneaux de signalisation avec OpenCV
Dans cette partie, nous avons pour objectif de concevoir une solution basée sur OpenCV et un noyau logiciel en Java afin d’identifier et de reconnaître automatiquement les panneaux de signalisation.

### Installation
-Le projet Twizzy se traduit par une interface qui exige l'installation de OpenCV 2.4.9 et le changement du Path du fichier .dll dans OpenCV pour que cette partie marche
Il suffit de cloner le projet, switcher sur la branche master, changer le path du fichier .dll dans /TP1/Activité1/LectureVidéo, finalement lancer /TP1/Activité1/HomePage

  ```bash
  git clone git@github.com:deb18e/Twizzy.git
  git switch Master
  ```




## Classification de Panneaux Routiers avec CNN

Ce projet implémente un modèle de classification de panneaux routiers utilisant une architecture ResNet.

### URL du projet déployé partie Deep Learning

L'application est déployée sur Render :  
🌐 [https://cnn-3.onrender.com/](https://cnn-3.onrender.com/)

### Technologies utilisées

#### Backend

- Python
- Flask (serveur web)
- PyTorch (modèle de deep learning)
- ResNet (architecture CNN)
- Waitress (serveur WSGI pour la production)

#### Frontend

- HTML/CSS
- JavaScript vanilla
- API Fetch pour les requêtes

### Installation
-Il suffit de lancer le site directement

  ```bash
  git clone git@github.com:deb18e/Twizzy.git
  git switch Main
  ```



## Détection et Suivi d'Objets (Choix 1)

Ce module ajoute la détection et le suivi multi-objets dans des séquences vidéo, en utilisant
**YOLOv8** (Ultralytics) pour la détection et **ByteTrack** ou **BoT-SORT** pour le suivi persistant.

### Fonctionnalités

- **Détection sur image** : upload d'une image, les objets sont encadrés avec leur classe et leur score de confiance.
- **Suivi vidéo** : upload d'une vidéo, chaque objet reçoit un identifiant unique (ID) persistant d'une frame à l'autre, et sa trajectoire est dessinée.
- **Choix du tracker** : ByteTrack (rapide, robuste) ou BoT-SORT (avec ré-identification apparence).
- Détection sur **80 classes COCO** (personnes, véhicules, animaux, etc.).

### Technologies utilisées

- **YOLOv8-nano** (ultralytics) — détecteur léger et rapide
- **ByteTrack / BoT-SORT** — algorithmes de suivi multi-objets intégrés dans Ultralytics
- **OpenCV** — traitement vidéo frame par frame
- **Flask** — serveur web, streaming MJPEG

### Accès

Une fois le serveur lancé (`python app.py`), la page de détection/suivi est disponible à :

```
http://localhost:5000/tracker
```

### Fichiers

| Fichier | Rôle |
|---|---|
| `tracker.py` | Module de détection (YOLO) et suivi (ByteTrack/BoT-SORT), générateur de flux vidéo |
| `templates/tracker.html` | Interface web pour upload image/vidéo et affichage des résultats |
| `app.py` | Routes Flask : `/tracker`, `/tracker_upload`, `/tracker_feed`, `/tracker_detect_image` |
