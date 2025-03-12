// controllers.js
const db = require('./db');

// Helper to parse the incoming request body
const parseRequestBody = (req) => {
  return new Promise((resolve, reject) => {
    let body = "";
    req.on("data", chunk => {
      body += chunk;
    });
    req.on("end", () => {
      try {
        const data = JSON.parse(body);
        resolve(data);
      } catch (err) {
        reject(err);
      }
    });
  });
};

// GET /products
const handleGetProducts = (req, res) => {
  db.all("SELECT * FROM products", [], (err, rows) => {
    if (err) {
      res.writeHead(500, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ error: "Database error" }));
      return;
    }
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify(rows));
  });
};

// POST /products
const handleCreateProduct = (req, res) => {
  parseRequestBody(req)
    .then(data => {
      if (!data.nume) {
        res.writeHead(400, { "Content-Type": "application/json" });
        return res.end(JSON.stringify({ error: "Field 'nume' is required" }));
      }
      const { nume, categorie, brand, stoc_curent, stoc_minim, pret } = data;
      const sql = `
        INSERT INTO products (nume, categorie, brand, stoc_curent, stoc_minim, pret)
        VALUES (?, ?, ?, ?, ?, ?)
      `;
      db.run(sql, [nume, categorie || null, brand || null, stoc_curent || 0, stoc_minim || 0, pret || null], function (err) {
        if (err) {
          res.writeHead(500, { "Content-Type": "application/json" });
          return res.end(JSON.stringify({ error: "Database error during insertion" }));
        }
        // Retrieve the newly inserted product using this.lastID
        db.get("SELECT * FROM products WHERE id = ?", [this.lastID], (err, row) => {
          if (err) {
            res.writeHead(500, { "Content-Type": "application/json" });
            return res.end(JSON.stringify({ error: "Database error after insertion" }));
          }
          res.writeHead(201, { "Content-Type": "application/json" });
          res.end(JSON.stringify(row));
        });
      });
    })
    .catch(err => {
      res.writeHead(400, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ error: "Invalid JSON" }));
    });
};

// GET /products/:id
const handleGetProductById = (req, res, id) => {
  db.get("SELECT * FROM products WHERE id = ?", [id], (err, row) => {
    if (err) {
      res.writeHead(500, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ error: "Database error" }));
    }
    if (!row) {
      res.writeHead(404, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ error: "Product not found" }));
    }
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify(row));
  });
};

// PUT /products/:id
const handleUpdateProduct = (req, res, id) => {
  parseRequestBody(req)
    .then(data => {
      // Build dynamic query for partial update
      let fields = [];
      let values = [];
      if (data.nume !== undefined) {
        fields.push("nume = ?");
        values.push(data.nume);
      }
      if (data.categorie !== undefined) {
        fields.push("categorie = ?");
        values.push(data.categorie);
      }
      if (data.brand !== undefined) {
        fields.push("brand = ?");
        values.push(data.brand);
      }
      if (data.stoc_curent !== undefined) {
        fields.push("stoc_curent = ?");
        values.push(data.stoc_curent);
      }
      if (data.stoc_minim !== undefined) {
        fields.push("stoc_minim = ?");
        values.push(data.stoc_minim);
      }
      if (data.pret !== undefined) {
        fields.push("pret = ?");
        values.push(data.pret);
      }
      if (fields.length === 0) {
        res.writeHead(400, { "Content-Type": "application/json" });
        return res.end(JSON.stringify({ error: "No fields to update" }));
      }
      const sql = `UPDATE products SET ${fields.join(", ")} WHERE id = ?`;
      values.push(id);
      db.run(sql, values, function (err) {
        if (err) {
          res.writeHead(500, { "Content-Type": "application/json" });
          return res.end(JSON.stringify({ error: "Database error on update" }));
        }
        // Return the updated product
        db.get("SELECT * FROM products WHERE id = ?", [id], (err, row) => {
          if (err) {
            res.writeHead(500, { "Content-Type": "application/json" });
            return res.end(JSON.stringify({ error: "Database error retrieving updated product" }));
          }
          res.writeHead(200, { "Content-Type": "application/json" });
          res.end(JSON.stringify(row));
        });
      });
    })
    .catch(err => {
      res.writeHead(400, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ error: "Invalid JSON" }));
    });
};

// DELETE /products/:id
const handleDeleteProduct = (req, res, id) => {
  db.run("DELETE FROM products WHERE id = ?", [id], function (err) {
    if (err) {
      res.writeHead(500, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ error: "Database error on deletion" }));
    }
    if (this.changes === 0) {
      res.writeHead(404, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ error: "Product not found" }));
    }
    res.writeHead(204);
    res.end();
  });
};

// GET /inventory/check/:productId
const handleCheckInventory = (req, res, productId) => {
  db.get("SELECT id, nume, stoc_curent FROM products WHERE id = ?", [productId], (err, row) => {
    if (err) {
      res.writeHead(500, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ error: "Database error" }));
    }
    if (!row) {
      res.writeHead(404, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ error: "Product not found" }));
    }
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify(row));
  });
};

// PUT /inventory/update/:productId
const handleUpdateInventory = (req, res, productId) => {
  parseRequestBody(req)
    .then(data => {
      if (data.stoc_curent === undefined) {
        res.writeHead(400, { "Content-Type": "application/json" });
        return res.end(JSON.stringify({ error: "Field 'stoc_curent' is required" }));
      }
      db.run("UPDATE products SET stoc_curent = ? WHERE id = ?", [data.stoc_curent, productId], function (err) {
        if (err) {
          res.writeHead(500, { "Content-Type": "application/json" });
          return res.end(JSON.stringify({ error: "Database error on inventory update" }));
        }
        // Return the updated product's stock information
        db.get("SELECT id, nume, stoc_curent FROM products WHERE id = ?", [productId], (err, row) => {
          if (err) {
            res.writeHead(500, { "Content-Type": "application/json" });
            return res.end(JSON.stringify({ error: "Database error retrieving updated product" }));
          }
          res.writeHead(200, { "Content-Type": "application/json" });
          res.end(JSON.stringify(row));
        });
      });
    })
    .catch(err => {
      res.writeHead(400, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ error: "Invalid JSON" }));
    });
};

// GET /inventory/notify
const handleNotifyInventory = (req, res) => {
  db.all("SELECT * FROM products WHERE stoc_curent < stoc_minim", [], (err, rows) => {
    if (err) {
      res.writeHead(500, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ error: "Database error" }));
    }
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify(rows));
  });
};

module.exports = {
  handleGetProducts,
  handleCreateProduct,
  handleGetProductById,
  handleUpdateProduct,
  handleDeleteProduct,
  handleCheckInventory,
  handleUpdateInventory,
  handleNotifyInventory,
};
