import logging
import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from config import settings
from api.capture_routes import router as capture_router
from api.calendar_routes import router as calendar_router

# Configure logging
logging.basicConfig(
    level=getattr(logging, settings.LOG_LEVEL.upper(), logging.INFO),
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("capture-service")

# Initialize FastAPI app
app = FastAPI(
    title="MeetingOps Capture & Diarization Pre-Processor Service",
    description="Microservice managing conferencing platform bots (Zoom, Teams, Meet), calendar sync, pre-join consent verification, and ephemeral audio capture (SRS v2.0 / Phase 2).",
    version="2.0.0"
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:5173", "http://127.0.0.1:5173", "*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register routers
app.include_router(capture_router)
app.include_router(calendar_router)

@app.get("/")
def root():
    return {
        "service": "MeetingOps Capture Service",
        "version": "2.0.0",
        "phase": "Phase 2 (Week 3)",
        "port": settings.SERVER_PORT,
        "docs": "/docs"
    }

if __name__ == "__main__":
    logger.info(f"Starting MeetingOps Capture Service on port {settings.SERVER_PORT}...")
    uvicorn.run("main:app", host="0.0.0.0", port=settings.SERVER_PORT, reload=False)
