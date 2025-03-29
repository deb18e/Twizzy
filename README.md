# Classification de Panneaux Routiers avec CNN

Ce projet implémente un modèle de classification de panneaux routiers utilisant une architecture ResNet, avec un backend Flask et un frontend HTML/JS.

## URL du projet déployé

L'application est déployée sur Render :  
🌐 [https://cnn-2.onrender.com/](https://cnn-2.onrender.com/)

## Fonctionnalités

- Classification d'images de panneaux routiers (format PNG)
- Détection des limitations de vitesse
- Interface simple avec glisser-déposer ou sélection de fichier
- Exemples intégrés pour tester rapidement

## Technologies utilisées

### Backend

- Python 3.9+
- Flask (serveur web)
- PyTorch (modèle de deep learning)
- ResNet (architecture CNN)
- Waitress (serveur WSGI pour la production)

### Frontend

- HTML/CSS
- JavaScript vanilla
- API Fetch pour les requêtes
