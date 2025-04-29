import numpy as np
# Températures quotidiennes (min, max) pour 7 jours
temps = np.array([
    [12, 25], [15, 28], [14, 26], 
    [16, 30], [13, 27], [15, 29], 
    [11, 24]
])


import time

# Méthode Python standard
start = time.time()
result_py = [x**2 for x in range(1000000)]
print("Python standard:", time.time() - start, "secondes")

# Méthode NumPy
start = time.time()
result_np = np.arange(1000000)**2
print("Avec NumPy:", time.time() - start, "secondes")