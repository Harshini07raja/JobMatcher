# AI Resume-Job Matcher

A full-stack AI-powered Resume-Job Matcher web application that evaluates how well a candidate's resume (PDF format) matches a specific job description. The application utilizes a Spring Boot backend, a React frontend, Apache PDFBox for text extraction, and the Google Gemini API (gemini-2.5-flash) for intelligent match assessment.

## Project Structure

```text
/
├── backend/        # Spring Boot (Java 17+) REST API
├── frontend/       # React (Vite) functional components & plain CSS
└── README.md       # Project setup & execution guide
```

---

## Getting Started

### 1. Get a Gemini API Key
To use the application, you need a Gemini API Key. You can get a free API key by signing up at [Google AI Studio](https://aistudio.google.com/).

### 2. Set Up the Gemini API Key

Configure the `GEMINI_API_KEY` environment variable on your system.

#### On Windows (PowerShell):
```powershell
$env:GEMINI_API_KEY="your-api-key-here"
```

#### On Windows (CMD):
```cmd
set GEMINI_API_KEY=your-api-key-here
```

#### On macOS/Linux:
```bash
export GEMINI_API_KEY="your-api-key-here"
```

*Note: The backend reads this key at runtime. If it is missing or empty, the API will fail gracefully and display an error message on the frontend.*

---

### 3. Run the Backend (Spring Boot)

Navigate to the `backend` directory and run the application using the Maven wrapper.

```bash
cd backend
./mvnw spring-boot:run
```
*(On Windows, use `.\mvnw.cmd spring-boot:run`)*

The backend server will start on **`http://localhost:8085`** (reconfigured to avoid conflicts on port 8080).

---

### 4. Run the Frontend (React + Vite)

Navigate to the `frontend` directory, install dependencies, and start the Vite development server.

```bash
cd frontend
npm install
npm run dev
```

The frontend application will be hosted on **`http://localhost:5173`**. Open this URL in your browser to interact with the application.

---

## Core Features

1. **Sleek, Responsive UX**: A premium dark-themed interface built using glassmorphism, responsive grids, CSS variables, and modern Outfit typography.
2. **PDF Resume Parsing**: Uses Apache PDFBox to read and extract text content from the uploaded resume file.
3. **Gemini AI Match Analysis**: Passes the resume text and job description to the Google Gemini API (`gemini-2.5-flash`) utilizing JSON mode to calculate a match score (0-100), identify missing keywords, highlight candidate strengths, and generate actionable improvement suggestions.
4. **Stateless Operations**: No database required—all data is processed in-memory and returned immediately.
5. **Robust Error Handling**: Handles cases with empty fields, invalid file formats (non-PDF), unselectable PDF texts (scanned files), and API errors gracefully by rendering user-friendly error banners.
