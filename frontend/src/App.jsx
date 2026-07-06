import { useState, useRef } from 'react';
import './App.css';

function App() {
  const [file, setFile] = useState(null);
  const [dragActive, setDragActive] = useState(false);
  const [jobDescription, setJobDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [result, setResult] = useState(null);
  
  const fileInputRef = useRef(null);

  // Handle drag events
  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  // Handle drop events
  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const droppedFile = e.dataTransfer.files[0];
      validateAndSetFile(droppedFile);
    }
  };

  // Handle file select
  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      validateAndSetFile(e.target.files[0]);
    }
  };

  const validateAndSetFile = (selectedFile) => {
    if (selectedFile.type !== "application/pdf" && !selectedFile.name.toLowerCase().endsWith(".pdf")) {
      setError("Only PDF files are supported.");
      setFile(null);
      return;
    }
    setError(null);
    setFile(selectedFile);
  };

  const removeFile = (e) => {
    e.stopPropagation();
    setFile(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const onButtonClick = () => {
    fileInputRef.current.click();
  };

  // Submit form to backend
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!file) {
      setError("Please upload your resume PDF.");
      return;
    }
    if (!jobDescription.trim()) {
      setError("Please paste the job description.");
      return;
    }

    setLoading(true);
    setError(null);
    setResult(null);

    const formData = new FormData();
    formData.append("resume", file);
    formData.append("jobDescription", jobDescription);

    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/api/analyze`, {
  method: "POST",
  body: formData,
});

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.error || `Server returned error status ${response.status}`);
      }

      setResult(data);
    } catch (err) {
      console.error(err);
      setError(err.message || "An unexpected error occurred. Please ensure the backend is running and try again.");
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setFile(null);
    setJobDescription('');
    setResult(null);
    setError(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  // Calculate rating status
  const getScoreStatus = (score) => {
    if (score >= 85) return { label: "Excellent Match", class: "status-excellent", desc: "Your resume is highly aligned with this job description. You have a great chance of passing ATS checks." };
    if (score >= 70) return { label: "Good Match", class: "status-good", desc: "Your resume matches most requirements. A few tweaks could make it stand out even more." };
    if (score >= 50) return { label: "Fair Match", class: "status-fair", desc: "Your resume matches some core aspects, but has notable gaps. Review missing keywords and suggestions." };
    return { label: "Low Match", class: "status-poor", desc: "Your resume has significant gaps compared to this job. Consider substantial edits to match the key requirements." };
  };

  const scoreInfo = result ? getScoreStatus(result.matchScore) : null;

  // Circular progress math
  const radius = 60;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = result ? circumference - (result.matchScore / 100) * circumference : circumference;

  return (
    <div className="app-container">
      <header>
        <div className="logo-container">
          <div className="logo-icon">AI</div>
          <h1 className="logo-text">Resume-Job Matcher</h1>
        </div>
        <p>
          Compare your resume PDF against any job description to analyze keywords, identify gaps, and get instant suggestions to optimize your application.
        </p>
      </header>

      {!result ? (
        <form className="card" onSubmit={handleSubmit}>
          <div className="input-grid">
            {/* Left: Resume upload */}
            <div className="upload-section">
              <div className="panel-header">
                <span className="panel-icon">📄</span>
                <h2>Upload Resume (PDF)</h2>
              </div>
              
              <div 
                className={`upload-zone ${dragActive ? 'drag-active' : ''}`}
                onDragEnter={handleDrag}
                onDragOver={handleDrag}
                onDragLeave={handleDrag}
                onDrop={handleDrop}
                onClick={onButtonClick}
              >
                <input 
                  ref={fileInputRef}
                  type="file" 
                  className="file-input" 
                  accept=".pdf"
                  onChange={handleFileChange}
                />
                
                {!file ? (
                  <>
                    <span className="upload-icon">📤</span>
                    <p>Drag and drop your PDF resume here, or <strong>click to browse</strong></p>
                    <span>Supports PDF files only</span>
                  </>
                ) : (
                  <div className="file-meta-card" onClick={(e) => e.stopPropagation()}>
                    <div className="file-info">
                      <span className="file-icon-pdf">📕</span>
                      <div className="file-details">
                        <div className="file-name">{file.name}</div>
                        <div className="file-size">{(file.size / 1024).toFixed(1)} KB</div>
                      </div>
                    </div>
                    <button type="button" className="remove-file-btn" onClick={removeFile}>
                      ✕
                    </button>
                  </div>
                )}
              </div>
            </div>

            {/* Right: Job Description */}
            <div className="job-desc-section">
              <div className="panel-header">
                <span className="panel-icon">💼</span>
                <h2>Job Description</h2>
              </div>
              <div className="textarea-container">
                <textarea
                  className="job-desc-textarea"
                  placeholder="Paste the target job description here..."
                  value={jobDescription}
                  onChange={(e) => setJobDescription(e.target.value)}
                  disabled={loading}
                />
              </div>
            </div>
          </div>

          {error && (
            <div className="error-alert" style={{ marginTop: '24px' }}>
              <span className="error-icon">⚠️</span>
              <div>{error}</div>
            </div>
          )}

          <div className="action-container" style={{ marginTop: '32px' }}>
            <button 
              type="submit" 
              className="btn-primary" 
              disabled={loading || !file || !jobDescription.trim()}
            >
              {loading ? (
                <>
                  <div className="spinner"></div>
                  Analyzing Resume...
                </>
              ) : (
                <>
                  <span>⚡</span>
                  Analyze Match
                </>
              )}
            </button>
          </div>
        </form>
      ) : (
        <div className="card results-card">
          <div className="results-header">
            <div className="results-title">
              <h2>Analysis Report</h2>
              <p>Based on Claude AI analysis of your resume and the job requirements.</p>
            </div>
            <button type="button" className="reset-btn" onClick={resetForm}>
              Analyze Another Job
            </button>
          </div>

          {/* Prominent Score Visual */}
          <div className="score-section">
            <div className="score-circle-wrapper">
              <svg className="score-svg">
                <circle className="score-bg-ring" cx="70" cy="70" r={radius} />
                <circle 
                  className="score-progress-ring" 
                  cx="70" 
                  cy="70" 
                  r={radius} 
                  stroke={result.matchScore >= 85 ? 'var(--success)' : result.matchScore >= 70 ? 'var(--accent-primary)' : result.matchScore >= 50 ? 'var(--warning)' : 'var(--error)'}
                  strokeDasharray={circumference}
                  strokeDashoffset={strokeDashoffset}
                />
              </svg>
              <div className="score-value">
                {result.matchScore}%
                <span className="score-label">Match</span>
              </div>
            </div>

            <div className="score-text-details">
              <span className={`score-status-badge ${scoreInfo.class}`}>
                {scoreInfo.label}
              </span>
              <h3>ATS Score Assessment</h3>
              <p>{scoreInfo.desc}</p>
            </div>
          </div>

          {/* Three Lists */}
          <div className="lists-grid">
            {/* Strengths */}
            <div className="list-container strengths-container">
              <div className="list-container-header">
                <span className="list-title-icon">✓</span>
                <h3>Key Strengths</h3>
              </div>
              {result.strengths && result.strengths.length > 0 ? (
                <ul className="bullet-list">
                  {result.strengths.map((strength, index) => (
                    <li key={index} className="bullet-item">
                      <span className="bullet-icon">✦</span>
                      <span>{strength}</span>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="empty-list-msg">No strengths detected.</p>
              )}
            </div>

            {/* Missing Keywords */}
            <div className="list-container keywords-container">
              <div className="list-container-header">
                <span className="list-title-icon">✗</span>
                <h3>Missing Keywords</h3>
              </div>
              {result.missingKeywords && result.missingKeywords.length > 0 ? (
                <ul className="bullet-list">
                  {result.missingKeywords.map((keyword, index) => (
                    <li key={index} className="bullet-item">
                      <span className="bullet-icon">✦</span>
                      <span>{keyword}</span>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="empty-list-msg">No missing keywords detected!</p>
              )}
            </div>

            {/* Suggestions */}
            <div className="list-container suggestions-container">
              <div className="list-container-header">
                <span className="list-title-icon">💡</span>
                <h3>Improvement Suggestions</h3>
              </div>
              {result.suggestions && result.suggestions.length > 0 ? (
                <ul className="bullet-list">
                  {result.suggestions.map((suggestion, index) => (
                    <li key={index} className="bullet-item">
                      <span className="bullet-icon">✦</span>
                      <span>{suggestion}</span>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="empty-list-msg">No improvement suggestions needed.</p>
              )}
            </div>
          </div>
        </div>
      )}

      <footer>
        &copy; {new Date().getFullYear()} AI Resume-Job Matcher &bull; Built with React &amp; Spring Boot
      </footer>
    </div>
  );
}

export default App;
