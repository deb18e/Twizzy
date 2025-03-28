document.addEventListener('DOMContentLoaded', function() {
    // Sélection des éléments avec vérification
    const uploadArea = document.getElementById('uploadArea');
    const dropZone = document.getElementById('dropZone');
    const uploadBtn = document.getElementById('uploadBtn');
    const imageInput = document.getElementById('imageInput');
    const resultArea = document.getElementById('resultArea');
    const previewImage = document.getElementById('previewImage');
    const predictionClass = document.getElementById('predictionClass');
    const predictionSpeed = document.getElementById('predictionSpeed');
    const tryAgainBtn = document.getElementById('tryAgainBtn');
    const loading = document.getElementById('loading');
    const exampleImages = document.querySelectorAll('.example-img');

    // Vérification que tous les éléments existent
    if (!uploadArea || !dropZone || !uploadBtn || !imageInput || !resultArea || 
        !previewImage || !predictionClass || !predictionSpeed || !tryAgainBtn || !loading) {
        console.error("Un ou plusieurs éléments HTML n'ont pas été trouvés");
        return;
    }

    // URL de votre API sur Render - À REMPLACER PAR VOTRE URL RÉELLE
    const API_URL = 'https://cnn-1-78qu.onrender.com/predict';

    // 1. Gestion du bouton "Parcourir" (pour fichiers PNG uniquement)
    uploadBtn.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        imageInput.click();
    });

    // 2. Gestion de la sélection de fichier (PNG seulement)
    imageInput.addEventListener('change', function(event) {
        const file = event.target.files[0];
        if (file && file.type === 'image/png') {
            handleFileSelect(event);
        } else if (file) {
            showError('Format non supporté. Veuillez utiliser un fichier PNG.');
            resetForm();
        }
    });

    // 3. Configuration du glisser-déposer
    function setupDragAndDrop() {
        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            dropZone.addEventListener(eventName, preventDefaults, false);
            document.body.addEventListener(eventName, preventDefaults, false);
        });

        ['dragenter', 'dragover'].forEach(eventName => {
            dropZone.addEventListener(eventName, highlight, false);
        });

        ['dragleave', 'drop'].forEach(eventName => {
            dropZone.addEventListener(eventName, unhighlight, false);
        });

        dropZone.addEventListener('drop', handleDrop, false);
    }

    function preventDefaults(e) {
        e.preventDefault();
        e.stopPropagation();
    }

    function highlight() {
        dropZone.classList.add('highlight');
        dropZone.style.borderColor = '#1abc9c';
    }

    function unhighlight() {
        dropZone.classList.remove('highlight');
        dropZone.style.borderColor = '#3498db';
    }

    function handleDrop(e) {
        const dt = e.dataTransfer;
        const files = dt.files;
        
        if (files.length > 0) {
            // Vérification immédiate du type de fichier
            if (files[0].type === 'image/png') {
                handleFileSelect({ target: { files } });
            } else {
                showError('Seuls les fichiers PNG sont acceptés');
            }
        }
    }

    // 4. Gestion des images d'exemple
    function setupExampleImages() {
        if (exampleImages.length > 0) {
            exampleImages.forEach(img => {
                img.addEventListener('click', function() {
                    const imgUrl = this.src;
                    if (!imgUrl) return;
                    
                    loading.style.display = 'block';
                    uploadArea.style.display = 'none';

                    fetch(imgUrl)
                        .then(res => {
                            if (!res.ok) throw new Error('Erreur de chargement de l\'image');
                            return res.blob();
                        })
                        .then(blob => {
                            // Créer un fichier PNG même pour les exemples
                            const file = new File([blob], "exemple.png", { type: 'image/png' });
                            handleFileSelect({ target: { files: [file] } });
                        })
                        .catch(error => {
                            console.error('Erreur:', error);
                            loading.style.display = 'none';
                            uploadArea.style.display = 'block';
                            showError('Impossible de charger l\'image exemple');
                        });
                });
            });
        }
    }

    // 5. Réinitialisation du formulaire
    tryAgainBtn.addEventListener('click', function() {
        resetForm();
    });

    function resetForm() {
        resultArea.style.display = 'none';
        uploadArea.style.display = 'block';
        imageInput.value = '';
        previewImage.src = '';
        predictionClass.textContent = '-';
        predictionSpeed.textContent = '-';
    }

    // 6. Affichage des erreurs
    function showError(message) {
        alert(message); // Vous pouvez remplacer par un affichage plus élégant dans l'UI
    }

    // 7. Gestion de la sélection de fichier (PNG seulement)
    function handleFileSelect(event) {
        const file = event.target.files[0];
        
        if (!file) return;
        
        // Double vérification du type PNG
        if (file.type !== 'image/png') {
            showError('Seuls les fichiers PNG sont acceptés');
            resetForm();
            return;
        }

        // Vérification de la taille (max 5MB)
        if (file.size > 5 * 1024 * 1024) {
            showError('Le fichier est trop volumineux (max 5MB)');
            resetForm();
            return;
        }

        // Afficher la prévisualisation
        const reader = new FileReader();
        
        reader.onloadstart = function() {
            loading.style.display = 'block';
            uploadArea.style.display = 'none';
        };
        
        reader.onload = function(e) {
            previewImage.src = e.target.result;
            sendImageToAPI(file);
        };
        
        reader.onerror = function() {
            showError('Erreur lors de la lecture du fichier PNG');
            resetForm();
        };
        
        reader.readAsDataURL(file);
    }

    // 8. Envoi à l'API
    function sendImageToAPI(file) {
        const formData = new FormData();
        formData.append('image', file);

        fetch(API_URL, {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Erreur HTTP: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            if (!data || typeof data.class === 'undefined' || !data.speed) {
                throw new Error('Réponse API invalide');
            }
            
            // Afficher les résultats
            predictionClass.textContent = data.class;
            predictionSpeed.textContent = data.speed;
            
            loading.style.display = 'none';
            resultArea.style.display = 'block';
        })
        .catch(error => {
            console.error('Erreur API:', error);
            loading.style.display = 'none';
            uploadArea.style.display = 'block';
            showError(`Erreur lors de l'analyse: ${error.message}`);
        });
    }

    // Initialisation
    setupDragAndDrop();
    setupExampleImages();
});