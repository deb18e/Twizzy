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
Dans cette partie, on a implémenté un modèle de classification de panneaux routiers utilisant une architecture ResNet.

### Technologies utilisées
- Python
- PyTorch (modèle de deep learning)
- ResNet (architecture CNN)
- HTML/CSS
- JavaScript vanilla

### Installation
-Pour cette partie, il faut tout simplement reswitcher sur la branche Main et lancer l'interface.
