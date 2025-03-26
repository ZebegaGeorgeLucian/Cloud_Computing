import React, { useState, useEffect } from 'react';

function InventoryList() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const fetchItems = async () => {
    try {
      const response = await fetch("http://localhost:8080/inventory/products", {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${localStorage.getItem("idToken")}`,
          "Content-Type": "application/json",
        },
      });
      if (!response.ok) {
        throw new Error("Failed to fetch inventory items");
      }
      const data = await response.json();
      setItems(data);
    } catch (err) {
      console.error(err);
      setError(err.message);
    }
  };

  const deleteItem = async (id) => {
    try {
      const response = await fetch(`http://localhost:8080/inventory/products/${id}`, {
        method: "DELETE",
        headers: {
          "Authorization": `Bearer ${localStorage.getItem("idToken")}`,
        },
      });
      if (response.ok) {
        setMessage(`Item with ID ${id} deleted successfully.`);
        // Refresh the item list
        fetchItems();
      } else {
        const errorText = await response.text();
        throw new Error(`Failed to delete item: ${errorText}`);
      }
    } catch (err) {
      console.error(err);
      setMessage(`Error: ${err.message}`);
    }
  };

  useEffect(() => {
    fetchItems();
  }, []);

  return (
    <div style={{
      display: "flex",
      justifyContent: "center",
      alignItems: "center",
      minHeight: "100vh", // Full vertical height
    }}>
      <div style={{ textAlign: "center" }}>
        <h2>Inventory List</h2>
        {error && <p style={{ color: "red" }}>{error}</p>}
        {message && <p style={{ color: "green" }}>{message}</p>}
        <table border="1" style={{ margin: "0 auto" }}> {/* Center the table */}
          <thead>
            <tr>
              <th>ID</th>
              <th>Nume</th>
              <th>Brand</th>
              <th>Pret</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id}>
                <td>{item.id}</td>
                <td>{item.nume}</td>
                <td>{item.brand}</td>
                <td>{item.pret}</td>
                <td>
                  <button onClick={() => deleteItem(item.id)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
  
}

export default InventoryList;
