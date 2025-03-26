import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function LoginForm({ onLoginSuccess }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });
      const data = await response.text();
      if (response.ok) {
        setMessage("Logged in successfully!");
        const tokenMatch = data.match(/Token:\s*(.*)/);
        const fetchedToken = tokenMatch ? tokenMatch[1] : "";
        localStorage.setItem("idToken", fetchedToken); // Save token
        onLoginSuccess(fetchedToken);
        navigate("/inventory"); // Redirect
      } else {
        setMessage("Login failed: " + data);
      }
    } catch (err) {
      setMessage("Error: " + err.message);
    }
  };

  const handleSwitchToRegister = () => {
    navigate("/register"); // Navigate to the register route
  };

  return (
    <div style={{ textAlign: "center" }}>
      <h2>Login</h2>
      <form onSubmit={handleLogin} style={{
        display: "flex",
        flexDirection: "column",
        gap: "1rem",
        width: "300px",
        margin: "0 auto",
      }}>
        <input 
          type="email" 
          placeholder="Email" 
          value={email} 
          onChange={(e) => setEmail(e.target.value)} 
          required 
        />
        <input 
          type="password" 
          placeholder="Password" 
          value={password} 
          onChange={(e) => setPassword(e.target.value)} 
          required 
        />
        <button type="submit">Login</button>
      </form>
      {message && <p>{message}</p>}
      <button 
        onClick={handleSwitchToRegister} 
        style={{ marginTop: "1rem", cursor: "pointer" }}
      >
        Switch to Register
      </button>
    </div>
  );
}

export default LoginForm;
