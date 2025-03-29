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
    const errorMsg = document.getElementById('errorMsg');
    const exampleImages = document.querySelectorAll('.example-img');
  
    const API_URL = 'https://cnn-1-78qu.onrender.com/predict';
  
    // Quand on clique sur le bouton "Parcourir"
    uploadBtn.addEventListener("click", function(e) {
        e.preventDefault(); // Empêche le comportement par défaut
        e.stopPropagation(); // Empêche la propagation
        imageInput.click(); // Déclenche le clic sur l'input
    });
  
    // Quand un fichier est sélectionné
    imageInput.addEventListener('change', function(e) {
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
  
    function handleFileSelect(files) {
        const file = files[0];
        if (!file) return;

        // Vérification du type PNG
        if (!file.type.match('image/png')) {
            return showError('Seuls les fichiers PNG sont acceptés');
        }

        const reader = new FileReader();
        reader.onload = function(e) {
            previewImage.src = e.target.result;
            dropZone.style.display = 'none';
            loading.style.display = 'block';
            sendImageToAPI(file);
        };
        reader.onerror = function() {
            showError('Erreur de lecture du fichier');
        };
        reader.readAsDataURL(file);
    
    }
  
    

    async function sendImageToAPI(file) {
        const formData = new FormData();
        formData.append('image', file);
    
        try {
            const response = await fetch(API_URL, {
                method: 'POST',
                body: formData,
                headers: {
                    'Accept': 'application/json'
                },
                mode: 'cors'
            });
    
            if (!response.ok) {
                throw new Error(`Erreur HTTP: ${response.status}`);
            }
    
            const data = await response.json();
            return data;
        } catch (error) {
            console.error('Erreur API:', error);
            throw error;
        }
    }
  
    function showError(message) {
      errorMsg.textContent = message;
      errorMsg.style.display = 'block';
      toggleLoading(false);
    }
  
    function toggleLoading(isLoading, showResult = false) {
      loading.style.display = isLoading ? 'block' : 'none';
      dropZone.style.display = isLoading ? 'none' : 'block';
      resultArea.style.display = showResult ? 'block' : 'none';
    }
  
    tryAgainBtn.addEventListener('click', () => {
      errorMsg.style.display = 'none';
      toggleLoading(false);
      previewImage.src = '';
      predictionClass.textContent = '-';
      predictionSpeed.textContent = '-';
    });
  
    exampleImages.forEach(img => {
      img.addEventListener('click', function() {
        toggleLoading(true);
        fetch(this.src)
          .then(res => res.blob())
          .then(blob => handleFileSelect([new File([blob], "example.png", { type: 'image/png' })]))
          .catch(() => showError('Impossible de charger l\'image exemple'));
      });
    });
  });
  