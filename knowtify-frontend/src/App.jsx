import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './store/authStore'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import Dashboard from './pages/Dashboard'
import KnowledgeMap from './pages/KnowledgeMap'
import Navbar from './components/common/Navbar'
import './index.css';

function App() {
  const { isAuthenticated } = useAuthStore()

  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        {isAuthenticated && <Navbar />}
        
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={
            !isAuthenticated ? <LoginPage /> : <Navigate to="/dashboard" />
          } />
          <Route path="/register" element={
            !isAuthenticated ? <RegisterPage /> : <Navigate to="/dashboard" />
          } />
          
          {/* Protected Routes */}
          <Route path="/dashboard" element={
            isAuthenticated ? <Dashboard /> : <Navigate to="/login" />
          } />
          <Route path="/knowledge-map" element={
            isAuthenticated ? <KnowledgeMap /> : <Navigate to="/login" />
          } />
          
          {/* Default Route */}
          <Route path="/" element={
            <Navigate to={isAuthenticated ? "/dashboard" : "/login"} />
          } />
        </Routes>
      </div>
    </Router>
  )
}

export default App
