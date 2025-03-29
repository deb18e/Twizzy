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

    // Utilisation d'un proxy CORS pour contourner le problème CORS en développement
    



    // Quand on clique sur le bouton "Parcourir"
    uploadBtn.addEventListener("click", function (e) {
        e.preventDefault(); // Empêche le comportement par défaut
        e.stopPropagation(); // Empêche la propagation
        imageInput.click(); // Déclenche le clic sur l'input
    });

    // Quand un fichier est sélectionné
    imageInput.addEventListener('change', function (e) {
        if (e.target.files && e.target.files[0]) {
            handleFileSelect(e.target.files);
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
        handleFileSelect(event.dataTransfer.files);
    });

    // Fonction pour gérer la sélection de fichiers
    function handleFileSelect(files) {
        const file = files[0];
        if (!file) return;

        // Vérification du type PNG
        if (!file.type.match('image/png')) {
            return showError('Seuls les fichiers PNG sont acceptés');
        }

        const reader = new FileReader();
        reader.onload = function (e) {
            previewImage.src = e.target.result;
            dropZone.style.display = 'none';
            loading.style.display = 'block';
            sendImageToAPI(file);
        };
        reader.onerror = function () {
            showError('Erreur de lecture du fichier');
        };
        reader.readAsDataURL(file);
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
            throw error;
        }
    }
    
    async function tryWithProxy(formData) {
        const PROXY_URL = 'https://cors-anywhere.herokuapp.com/';
        try {
            const response = await fetch(PROXY_URL + API_URL, {
                method: 'POST',
                body: formData
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

    // Fonction pour gérer l'état de chargement et l'affichage du résultat
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
                .then(blob => handleFileSelect([new File([blob], "example.png", { type: 'image/png' })]))
                .catch(() => showError('Impossible de charger l\'image exemple'));
        });
    });
});
