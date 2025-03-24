FROM python:3.9-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
CMD ["waitress-serve", "--host=0.0.0.0", "--port=5000", "app:app"]