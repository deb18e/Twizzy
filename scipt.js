document.addEventListener('DOMContentLoaded', function () {
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
    const errorMsg = document.getElementById('errorMsg');
    const exampleImages = document.querySelectorAll('.example-img');

    // Modifier l'input pour accepter tous les formats d'image
    imageInput.setAttribute('accept', 'image/*');

    // Quand on clique sur le bouton "Parcourir"
    uploadBtn.addEventListener("click", function (e) {
        e.preventDefault();
        e.stopPropagation();
        imageInput.click();
    });

    // Quand un fichier est sélectionné
    imageInput.addEventListener('change', function (e) {
        if (e.target.files && e.target.files[0]) {
            handleFileSelect(e.target.files[0]);
        }
    });

    // Drag & Drop
    dropZone.addEventListener("dragover", (event) => {
        event.preventDefault();
        dropZone.classList.add("highlight");
    });

    dropZone.addEventListener("dragleave", () => dropZone.classList.remove("highlight"));

    dropZone.addEventListener("drop", (event) => {
        event.preventDefault();
        dropZone.classList.remove("highlight");
        if (event.dataTransfer.files.length) {
            handleFileSelect(event.dataTransfer.files[0]);
        }
    });

    // Fonction pour convertir une image en PNG
    function convertToPNG(file, callback) {
        const reader = new FileReader();
        reader.onload = function (e) {
            const img = new Image();
            img.onload = function () {
                const canvas = document.createElement('canvas');
                canvas.width = img.width;
                canvas.height = img.height;
                const ctx = canvas.getContext('2d');
                ctx.drawImage(img, 0, 0);
                
                canvas.toBlob(function (blob) {
                    const pngFile = new File([blob], file.name.replace(/\.[^/.]+$/, "") + ".png", {
                        type: "image/png"
                    });
                    callback(pngFile);
                }, 'image/png');
            };
            img.src = e.target.result;
        };
        reader.readAsDataURL(file);
    }

    // Fonction pour gérer la sélection de fichiers
    function handleFileSelect(file) {
        if (!file) return;

        // Vérification que c'est bien une image
        if (!file.type.match('image.*')) {
            return showError('Seuls les fichiers image sont acceptés');
        }

        // Conversion en PNG
        convertToPNG(file, function(pngFile) {
            const reader = new FileReader();
            reader.onload = function (e) {
                previewImage.src = e.target.result;
                dropZone.style.display = 'none';
                loading.style.display = 'block';
                sendImageToAPI(pngFile);
            };
            reader.onerror = function () {
                showError('Erreur de lecture du fichier');
            };
            reader.readAsDataURL(pngFile);
        });
    }

    // Fonction pour envoyer l'image à l'API
    const API_URL = 'http://localhost:5000/predict'; // Changez cette URL en production

    async function sendImageToAPI(file) {
        const formData = new FormData();
        formData.append('file', file);
        try {
            const response = await fetch(API_URL, {
                method: 'POST',
                body: formData,
                headers: {
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Erreur inconnue du serveur');
            }

            const data = await response.json();
            
            // Afficher les résultats
            predictionClass.textContent = data.data.class || '0';
            predictionSpeed.textContent = data.data.speed || 'Non spécifié';
            
            // Basculer vers l'affichage des résultats
            loading.style.display = 'none';
            resultArea.style.display = 'block';
            
            return data;
            
        } catch (error) {
            showError(`Erreur lors de la prédiction: ${error.message}`);
            console.error('Erreur API:', error);
            
            // Essayer avec le proxy CORS en cas d'échec
            if (error.name === 'TypeError' && API_URL !== 'http://localhost:5000/predict') {
                console.log('Tentative avec proxy CORS...');
                tryWithProxy(formData)
                    .then(data => {
                        predictionClass.textContent = data.data.class || '0';
                        predictionSpeed.textContent = data.data.speed || 'Non spécifié';
                        loading.style.display = 'none';
                        resultArea.style.display = 'block';
                    })
                    .catch(proxyError => {
                        showError(`Échec avec proxy: ${proxyError.message}`);
                    });
            }
            
            throw error;
        }
    }
    
    async function tryWithProxy(formData) {
        const PROXY_URL = 'https://cors-anywhere.herokuapp.com/';
        try {
            const response = await fetch(PROXY_URL + API_URL, {
                method: 'POST',
                body: formData,
                headers: {
                    'Accept': 'application/json'
                }
            });
            return await response.json();
        } catch (proxyError) {
            throw new Error(`Échec même avec proxy: ${proxyError.message}`);
        }
    }

    // Fonction pour afficher les erreurs
    function showError(message) {
        errorMsg.textContent = message;
        errorMsg.style.display = 'block';
        toggleLoading(false);
    }

    // Fonction pour gérer l'état de chargement
    function toggleLoading(isLoading, showResult = false) {
        loading.style.display = isLoading ? 'block' : 'none';
        dropZone.style.display = isLoading ? 'none' : 'block';
        resultArea.style.display = showResult ? 'block' : 'none';
    }

    // Fonction pour réinitialiser l'interface utilisateur
    function resetInterface() {
        errorMsg.style.display = 'none';
        toggleLoading(false);
        previewImage.src = '';
        predictionClass.textContent = '-';
        predictionSpeed.textContent = '-';
    }

    // Réessayer après une erreur
    tryAgainBtn.addEventListener('click', () => {
        resetInterface();
    });

    // Gestion des clics sur les images d'exemple
    exampleImages.forEach(img => {
        img.addEventListener('click', function () {
            toggleLoading(true);
            fetch(this.src)
                .then(res => res.blob())
                .then(blob => {
                    const file = new File([blob], "example.png", { type: 'image/png' });
                    handleFileSelect(file);
                })
                .catch(() => showError('Impossible de charger l\'image exemple'));
        });
    });
});