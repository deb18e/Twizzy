from PIL import Image

image = Image.open("C:/Users/pc/Desktop/Twizzy/archive/Test/p7.png")
width, height = image.size

# Définir un crop approximatif centré-droit (25% largeur, 50% hauteur centré)
x0 = int(0.70 * width)
y0 = height-1200 # Upper starts at 75% of height
x1 = width
y1 = height-800
cropped_panel_zone = image.crop((x0, y0, x1, y1))
cropped_panel_zone.show()  # Affiche la zone probable du panneau
