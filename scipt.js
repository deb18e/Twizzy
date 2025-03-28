document.addEventListener('DOMContentLoaded', function() {
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
    
    // URL de votre API sur Render
    const API_URL = 'https://votre-api-render-url.onrender.com/predict';
    
    // Gestionnaire pour le bouton de téléchargement
    uploadBtn.addEventListener('click', () => imageInput.click());
    
    // Gestionnaire pour la sélection de fichier
    imageInput.addEventListener('change', handleFileSelect);
    
    // Gestionnaires pour le glisser-déposer
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, preventDefaults, false);
    });
    
    function preventDefaults(e) {
        e.preventDefault();
        e.stopPropagation();
    }
    
    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, highlight, false);
    });
    
    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, unhighlight, false);
    });
    
    function highlight() {
        dropZone.classList.add('highlight');
    }
    
    function unhighlight() {
        dropZone.classList.remove('highlight');
    }
    
    dropZone.addEventListener('drop', handleDrop, false);
    
    function handleDrop(e) {
        const dt = e.dataTransfer;
        const files = dt.files;
        
        if (files.length) {
            handleFileSelect({ target: { files } });
        }
    }
    
    // Gestionnaire pour les images d'exemple
    exampleImages.forEach(img => {
        img.addEventListener('click', function() {
            const imgUrl = this.src;
            const imgAlt = this.alt;
            
            // Créer un objet fichier à partir de l'URL de l'image
            fetch(imgUrl)
                .then(res => res.blob())
                .then(blob => {
                    const file = new File([blob], imgAlt, { type: blob.type });
                    handleFileSelect({ target: { files: [file] } });
                });
        });
    });
    
    // Gestionnaire pour le bouton "Essayer une autre image"
    tryAgainBtn.addEventListener('click', resetForm);
    
    function resetForm() {
        resultArea.style.display = 'none';
        uploadArea.style.display = 'block';
        imageInput.value = '';
    }
    
    function handleFileSelect(event) {
        const file = event.target.files[0];
        
        if (!file) return;
        
        if (!file.type.match('image.*')) {
            alert('Veuillez sélectionner une image valide (JPG, PNG, JPEG)');
            return;
        }
        
        // Afficher la prévisualisation
        const reader = new FileReader();
        
        reader.onload = function(e) {
            previewImage.src = e.target.result;
            uploadArea.style.display = 'none';
            loading.style.display = 'block';
            
            // Envoyer l'image à l'API après un court délai pour que la prévisualisation s'affiche
            setTimeout(() => sendImageToAPI(file), 100);
        };
        
        reader.readAsDataURL(file);
    }
    
    function sendImageToAPI(file) {
        const formData = new FormData();
        formData.append('image', file);
        
        fetch(API_URL, {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Erreur lors de la requête API');
            }
            return response.json();
        })
        .then(data => {
            // Afficher les résultats
            predictionClass.textContent = data.class;
            predictionSpeed.textContent = data.speed;
            
            loading.style.display = 'none';
            resultArea.style.display = 'block';
        })
        .catch(error => {
            console.error('Erreur:', error);
            loading.style.display = 'none';
            alert('Une erreur est survenue lors de l\'analyse de l\'image. Veuillez réessayer.');
            resetForm();
        });
    }
});