FROM python:3.9-slim

WORKDIR /app

# Installation explicite de Waitress avant les autres dépendances
RUN pip install --no-cache-dir waitress==2.1.2

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

CMD ["python", "-m", "waitress", "--host=0.0.0.0", "--port=5000", "app:app"]