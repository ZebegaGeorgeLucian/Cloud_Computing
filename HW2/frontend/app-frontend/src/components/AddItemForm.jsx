import React, { useState } from 'react';

function AddItemForm() {
  const [nume, setNume] = useState("");
  const [brand, setBrand] = useState("");
  const [categorie, setCategorie] = useState("");
  const [stocCurent, setStocCurent] = useState("");
  const [stocMinim, setStocMinim] = useState("");
  const [pret, setPret] = useState("");
  const [message, setMessage] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
  
    const newItem = {
      nume,
      brand,
      categorie,
      stoc_curent: parseInt(stocCurent, 10),
      stoc_minim: parseInt(stocMinim, 10),
      pret: parseFloat(pret),
    };
  
    try {
      const token = localStorage.getItem("idToken"); // Ia token-ul salvat la login
      if (!token) {
        setMessage("Unauthorized: No token found.");
        return;
      }
  
      const response = await fetch("http://localhost:8080/inventory/products", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}` // Adaugă token-ul
        },
        body: JSON.stringify(newItem),
      });
  
      if (response.ok) {
        setMessage("Item added successfully!");
      } else {
        const errorData = await response.text();
        setMessage("Failed to add item: " + errorData);
      }
    } catch (err) {
      setMessage("Error: " + err.message);
    }
  };
  

  return (
    <div style={{
      display: "flex",
      justifyContent: "center",
      alignItems: "center",
      minHeight: "100vh", // Full vertical centering
    }}>
      <div style={{
        textAlign: "center", // Center-align text
        display: "flex",
        flexDirection: "column", // Stack all elements vertically
        gap: "1rem", // Add spacing between elements
        width: "300px", // Optional: Set width for consistent form layout
      }}>
        <h2>Add New Item</h2>
        <form onSubmit={handleSubmit} style={{
          display: "flex",
          flexDirection: "column", // Stack form inputs vertically
          gap: "1rem", // Add spacing between inputs
        }}>
          <input type="text" placeholder="Nume" value={nume} onChange={(e) => setNume(e.target.value)} required />
          <input type="text" placeholder="Brand" value={brand} onChange={(e) => setBrand(e.target.value)} required />
          <input type="text" placeholder="Categorie" value={categorie} onChange={(e) => setCategorie(e.target.value)} required />
          <input type="number" placeholder="Stoc Curent" value={stocCurent} onChange={(e) => setStocCurent(e.target.value)} required />
          <input type="number" placeholder="Stoc Minim" value={stocMinim} onChange={(e) => setStocMinim(e.target.value)} required />
          <input type="number" placeholder="Pret" value={pret} onChange={(e) => setPret(e.target.value)} required />
          <button type="submit" style={{
            padding: "0.5rem",
            fontWeight: "bold",
            cursor: "pointer",
            backgroundColor: "#007BFF",
            color: "#fff",
            border: "none",
            borderRadius: "5px",
          }}>Add Item</button>
        </form>
        {message && <p style={{ color: "green" }}>{message}</p>}
      </div>
    </div>
  );
  
}

export default AddItemForm;
