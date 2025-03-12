// db.js
const sqlite3 = require('sqlite3').verbose();

// Connect to (or create) the database file 'inventory.db'
const db = new sqlite3.Database('./inventory.db', (err) => {
  if (err) {
    console.error('Error opening database:', err.message);
  } else {
    console.log('Connected to the SQLite database.');
  }
});

// Create the "products" table if it doesn't exist
db.serialize(() => {
  db.run(`
    CREATE TABLE IF NOT EXISTS products (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      nume TEXT NOT NULL,
      categorie TEXT,
      brand TEXT,
      stoc_curent INTEGER DEFAULT 0,
      stoc_minim INTEGER DEFAULT 0,
      pret REAL
    )
  `, (err) => {
    if (err) {
      console.error('Error creating table:', err.message);
    } else {
      console.log('Table "products" is ready.');
    }
  });
});

module.exports = db;
