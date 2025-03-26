import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function RegisterForm({ onRegisterSuccess }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });
      const data = await response.text();
      if (response.ok) {
        setMessage("Registered successfully!");
        onRegisterSuccess && onRegisterSuccess(); // Call additional logic if provided
        navigate("/login"); // Redirect to login
      } else {
        setMessage("Registration failed: " + data);
      }
    } catch (err) {
      setMessage("Error: " + err.message);
    }
  };

  return (
    <div style={{ textAlign: "center" }}>
      <h2>Register</h2>
      <form onSubmit={handleRegister} style={{
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
        <button type="submit">Register</button>
      </form>
      {message && <p>{message}</p>}
      <button 
        onClick={() => navigate("/login")} 
        style={{ marginTop: "1rem", cursor: "pointer" }}
      >
        Switch to Login
      </button>
    </div>
  );
}

export default RegisterForm;
