FROM python:3.10-slim

WORKDIR /app

# Install system dependencies for scikit-learn
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first for caching
COPY ai-support/python/requirements.txt /app/requirements.txt

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of the application
COPY ai-support/python/ /app/

# Railway uses PORT env variable
ENV PORT=8000

# Start command - use shell form to expand $PORT
CMD uvicorn main:app --host 0.0.0.0 --port $PORT
