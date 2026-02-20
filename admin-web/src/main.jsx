import React from 'react'
import ReactDOM from 'react-dom/client'
import './index.css'

// Dev mode: Full admin panel + public site
// Production: Public site only (no admin routes exist)
const isDev = import.meta.env.DEV

async function bootstrap() {
  let AppComponent
  if (isDev) {
    const mod = await import('./AppFull.jsx')
    AppComponent = mod.default
  } else {
    const mod = await import('./App.jsx')
    AppComponent = mod.default
  }

  ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
      <AppComponent />
    </React.StrictMode>,
  )
}

bootstrap()
