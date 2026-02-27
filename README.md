# Enterprise RAG CLI - Java-Based Document Intelligence

An advanced Retrieval-Augmented Generation (RAG) system built in Java. This tool allows users to upload PDF/Text documents, convert them into high-dimensional vector embeddings, and chat with the data using Google's Gemini AI.

## Key Features
* **Multi-Format Support:** Intelligent text extraction from `.pdf` and `.txt` files.
* **Vector Database:** Custom implementation for storing and searching document embeddings.
* **Smart Chunking:** Recursive text slicing to maintain context during AI retrieval.
* **Resilient Network Client:** Built-in rate limiting and exponential backoff for API stability.
* **Gemini AI Integration:** Uses state-of-the-art LLMs to provide grounded, context-aware answers.

## Tech Stack
* **Language:** Java (JDK 17+)
* **AI Engine:** Google Gemini API
* **Library:** Apache PDFBox (for PDF parsing)
* **OS Environment:** Ubuntu / Linux

## Commands
| Command | Description |
| :--- | :--- |
| `/upload <path>` | Extracts, chunks, and memorizes a PDF or TXT file. |
| `/chat <query>` | Performs a vector search and generates an AI answer based on context. |
| `/exit` | Safely shuts down the application. |

## System Architecture
The system follows the RAG pattern:
1. **Ingestion:** Documents are parsed and split into 500-character chunks.
2. **Embedding:** Each chunk is converted into a vector via the Gemini Embedding API.
3. **Retrieval:** User queries are vectorized to find the most mathematically similar chunk.
4. **Generation:** The context + query are sent to the LLM for a hallucination-free response.

## Author
**Govindraj Pravin Wattamwar** BTech IT | AI & ML Enthusiast 
Registration: 2023BIT035
