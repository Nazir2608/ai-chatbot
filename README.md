# AI Chatbot for Handwritten Notes

Production-ready Spring Boot application that uses Vision-Language Models (VLM) to understand handwritten notes, diagrams, and corrections—not just OCR text.

## 🏗️ Architecture Highlights

- **Storage Strategy Pattern**: Switch between Local filesystem and AWS S3 via config
- **LLM Strategy Pattern**: Switch between Ollama (local) and OpenAI (cloud) via config
- **Image Understanding**: Uses LLaVA or GPT-4V to extract semantic meaning (crossed-out text, diagrams, arrows)
- **Zero Code Changes**: Switching providers requires only YAML changes

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose (optional)
- Ollama (for local AI) OR OpenAI API key

### 1. Local Development (IntelliJ/VS Code)

```bash
# Clone and enter directory
cd ai-chatbot

# Build
./mvnw clean package

# Run with defaults (Local storage + Ollama)
java -jar target/ai-chatbot-1.0.0.jar
