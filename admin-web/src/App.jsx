import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'

// Pages — Public only
import LandingPage from './pages/LandingPage'
import DownloadPage from './pages/DownloadPage'

/* ═══════════ Main App (Public Website Only) ═══════════ */
function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/download" element={<DownloadPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
