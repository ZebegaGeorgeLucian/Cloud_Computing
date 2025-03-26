import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'; // Keep Navigate for route redirections
import RegisterForm from './components/RegisterForm.jsx';
import LoginForm from './components/LoginForm.jsx';
import InventoryList from './components/InventoryList.jsx';
import AddItemForm from './components/AddItemForm.jsx';
import WeatherWidget from './components/WeatherWidget.jsx';

function App() {
  const [token, setToken] = useState(null);

  useEffect(() => {
    const storedToken = localStorage.getItem("idToken");
    setToken(storedToken);
  }, []);

  const handleLoginSuccess = (idToken) => {
    setToken(idToken);
    localStorage.setItem("idToken", idToken);
  };

  const handleLogout = () => {
    setToken(null);
    localStorage.removeItem("idToken");
  };

  return (
    <Router>
      <div className="App">
        <header className="App-header">
          <h1>Inventory Management</h1>
          {token && (
            <button onClick={handleLogout} style={{ marginBottom: "20px" }}>
              Logout
            </button>
          )}
          <WeatherWidget /> {/* Display the WeatherWidget */}
          <Routes>
            {/* Public routes */}
            <Route 
              path="/register" 
              element={
                <RegisterForm onRegisterSuccess={() => { console.log("Registered!"); }} /> // Removed Navigate
              } 
            />
            <Route 
              path="/login" 
              element={<LoginForm onLoginSuccess={handleLoginSuccess} />} 
            />

            {/* Protected route */}
            <Route
              path="/inventory"
              element={token ? (
                <div>
                  <InventoryList />
                  <hr />
                  <AddItemForm />
                </div>
              ) : (
                <Navigate to="/login" />
              )}
            />

            {/* Default redirect */}
            <Route path="*" element={<Navigate to={token ? "/inventory" : "/login"} />} />
          </Routes>
        </header>
      </div>
    </Router>
  );
}

export default App;
