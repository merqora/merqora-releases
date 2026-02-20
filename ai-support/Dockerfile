# Rendly AI Support - Docker Configuration
# Multi-stage build for Python + C++ + Rust

# ==========================================
# Stage 1: Build C++ scoring engine
# ==========================================
FROM python:3.11-slim as cpp-builder

RUN apt-get update && apt-get install -y \
    build-essential \
    cmake \
    git \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /build/cpp

# Copy C++ source
COPY cpp/ .

# Build with CMake
RUN pip install pybind11 && \
    mkdir build && cd build && \
    cmake .. -DPYTHON_EXECUTABLE=$(which python3) && \
    make -j$(nproc)

# ==========================================
# Stage 2: Build Rust security service
# ==========================================
FROM rust:1.75-slim as rust-builder

RUN apt-get update && apt-get install -y \
    python3-dev \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /build/rust

# Copy Rust source
COPY rust/ .

# Build with maturin for Python bindings
RUN cargo install maturin && \
    maturin build --release

# ==========================================
# Stage 3: Final Python runtime
# ==========================================
FROM python:3.11-slim

WORKDIR /app

# Install runtime dependencies
RUN apt-get update && apt-get install -y \
    libstdc++6 \
    && rm -rf /var/lib/apt/lists/*

# Copy Python source
COPY python/ .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy built C++ extension
COPY --from=cpp-builder /build/cpp/build/*.so /app/

# Copy built Rust extension
COPY --from=rust-builder /build/rust/target/wheels/*.whl /tmp/
RUN pip install /tmp/*.whl || true

# Environment
ENV PYTHONPATH=/app
ENV PYTHONUNBUFFERED=1

# Expose port
EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8000/health || exit 1

# Run the application
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
