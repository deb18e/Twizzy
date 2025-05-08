import cv2
import torch
import numpy as np
from PIL import Image
from torchvision import transforms
from collections import defaultdict

# Configuration
MODEL_PATH = './model_resnet4_classes.pth'
DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")
VIDEO_PATH = 'video1.avi'
VIDEO_SPEED = 0.5  # Facteur de ralentissement (0.5 = 2x plus lent)

# Transformation des images
transform = transforms.Compose([
    transforms.Resize((64, 64)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
])

# Dictionnaire des panneaux
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

from model import ResNet, BasicBlock  # Assure-toi que ton fichier model.py contient bien ça

model = ResNet(BasicBlock, layers=[3, 4, 6, 3], num_classes=8)
model.load_state_dict(torch.load(MODEL_PATH, map_location=DEVICE))
model = model.to(DEVICE)
model.eval()


def preprocess_frame(frame):
    """Prétraitement de l'image pour détection des ROI"""
    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
    
    # Masques pour les couleurs caractéristiques des panneaux
    lower_red1 = np.array([0, 70, 50])
    upper_red1 = np.array([10, 255, 255])
    lower_red2 = np.array([170, 70, 50])
    upper_red2 = np.array([180, 255, 255])
    mask_red = cv2.inRange(hsv, lower_red1, upper_red1) + cv2.inRange(hsv, lower_red2, upper_red2)
    
    lower_blue = np.array([100, 70, 50])
    upper_blue = np.array([130, 255, 255])
    mask_blue = cv2.inRange(hsv, lower_blue, upper_blue)
    
    combined_mask = cv2.bitwise_or(mask_red, mask_blue)
    
    kernel = np.ones((5,5), np.uint8)
    combined_mask = cv2.morphologyEx(combined_mask, cv2.MORPH_OPEN, kernel)
    combined_mask = cv2.morphologyEx(combined_mask, cv2.MORPH_CLOSE, kernel)
    
    contours, _ = cv2.findContours(combined_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    
    min_area = 500
    max_area = 50000
    potential_rois = []
    
    for cnt in contours:
        area = cv2.contourArea(cnt)
        if min_area < area < max_area:
            x,y,w,h = cv2.boundingRect(cnt)
            aspect_ratio = w / float(h)
            if 0.8 < aspect_ratio < 1.2:
                potential_rois.append((x,y,w,h))
    
    return potential_rois

def detect_sign(cropped_img):
    """Détecte un panneau dans une image recadrée"""
    pil_img = Image.fromarray(cv2.cvtColor(cropped_img, cv2.COLOR_BGR2RGB))
    img_tensor = transform(pil_img).unsqueeze(0).to(DEVICE)
    
    with torch.no_grad():
        output = model(img_tensor)
        _, predicted = torch.max(output, 1)
    
    return speed_limits.get(predicted.item(), "Inconnu")

def process_video():
    cap = cv2.VideoCapture(VIDEO_PATH)
    
    if not cap.isOpened():
        print("Erreur: Impossible d'ouvrir la vidéo")
        return
    
    # Dictionnaire pour stocker les détections
    detected_signs = defaultdict(int)
    
    print("Traitement en cours.......")
    
    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break
        
        # Ralentir la vidéo
        delay = int(30 * (1 / VIDEO_SPEED))  # Temps entre les frames
        
        # Détection des ROI
        rois = preprocess_frame(frame)
        
        for (x,y,w,h) in rois:
            roi = frame[y:y+h, x:x+w]
            
            try:
                label = detect_sign(roi)
                detected_signs[label] += 1
                
                cv2.rectangle(frame, (x,y), (x+w,y+h), (0,255,0), 2)
                cv2.putText(frame, label, (x, y-10), 
                          cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0,255,0), 2)
            except:
                continue
        
        cv2.imshow('Detection Panneaux', frame)
        
        if cv2.waitKey(delay) & 0xFF == ord('q'):
            break
    
    cap.release()
    cv2.destroyAllWindows()
    
    # Affichage du récapitulatif
    print("\nRécapitulatif des panneaux détectés:")
    print("----------------------------------")
    for sign, count in detected_signs.items():
        print(f"{sign}: {count} fois")
    print("----------------------------------")

if __name__ == "__main__":
    process_video()