import os

# Spécifie le répertoire parent contenant les dossiers "4" et "9"
parent_directory = "C:/Users/pc/Desktop/Twizzy/archive/Train"


# Liste les dossiers dans le répertoire parent
for folder in os.listdir(parent_directory):
    folder_path = os.path.join(parent_directory, folder)
    
    # Vérifie si c'est un dossier
    if os.path.isdir(folder_path):
        for filename in os.listdir(folder_path):
            old_path = os.path.join(folder_path, filename)
            
            # Vérifie si c'est un fichier
            if os.path.isfile(old_path):
                new_filename = f"{folder}_{filename}"  # Ajoute le nom du dossier en préfixe
                new_path = os.path.join(folder_path, new_filename)
                
                # Renomme le fichier
                os.rename(old_path, new_path)
                print(f"Renommé : {old_path} ➝ {new_path}")
