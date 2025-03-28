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
    uploadArea.addEventListener("dragover", (event) => {
      event.preventDefault();
      uploadArea.classList.add("highlight");
    });
  
    uploadArea.addEventListener("dragleave", () => uploadArea.classList.remove("highlight"));
  
    uploadArea.addEventListener("drop", (event) => {
      event.preventDefault();
      uploadArea.classList.remove("highlight");
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
            uploadArea.style.display = 'none';
            loading.style.display = 'block';
            sendImageToAPI(file);
        };
        reader.onerror = function() {
            showError('Erreur de lecture du fichier');
        };
        reader.readAsDataURL(file);
    
    }
  
    function sendImageToAPI(file) {
      const formData = new FormData();
      formData.append('image', file);
  
      fetch(API_URL, { method: 'POST', body: formData })
        .then(response => response.json())
        .then(data => {
          if (!data || !data.class || !data.speed) throw new Error('Réponse API invalide');
          predictionClass.textContent = data.class;
          predictionSpeed.textContent = data.speed;
          toggleLoading(false, true);
        })
        .catch(error => showError(`Erreur API: ${error.message}`));
    }
  
    function showError(message) {
      errorMsg.textContent = message;
      errorMsg.style.display = 'block';
      toggleLoading(false);
    }
  
    function toggleLoading(isLoading, showResult = false) {
      loading.style.display = isLoading ? 'block' : 'none';
      uploadArea.style.display = isLoading ? 'none' : 'block';
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
  