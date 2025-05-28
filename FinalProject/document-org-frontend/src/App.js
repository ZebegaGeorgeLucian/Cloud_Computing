import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Upload from './Upload';
import Search from './Search';

import './App.css';

function App() {
  return (
    <Router>
      <nav className="navbar" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <img src="/icon.png" alt="Logo" style={{ width: '40px', height: '40px' }} />
        <h1 style={{ margin: 0, fontSize: '1.5rem' }}>Document Organiser</h1>
        <div style={{ marginLeft: 'auto' }}>
          <Link to="/">Home</Link> | <Link to="/upload">Upload</Link> | <Link to="/search">Search</Link>
        </div>
      </nav>

      <Routes>
        <Route path="/" element={<div className="content">
        <h2>About Document Organizer</h2>
        <p>
          Use the navigation links above to upload documents or search through them.
          This tool is designed to help you manage your digital files efficiently.
          Whether you're storing personal notes, academic papers, or business documents,
          Document Organiser keeps everything accessible and searchable.
        </p>
        <p>
          To get started, click on "Upload" to add new documents.
          Once uploaded, you can find them anytime using the "Search" feature,
          which supports keyword and metadata-based lookup.
        </p>
        <p>
          All your documents are securely stored and organized for fast retrieval.
          Enjoy a smoother, smarter way to manage your files.
        </p>
        
        <h2>Contacts</h2>
        <p>
          For any issues or feedback, please reach out to us at:
          <ul>
            <li><p><b>George Zebega's email</b>: georgezgl12@gmail.com</p></li>
            <li><p><b>Enache Adrian's email</b>: enacheadrian788@gmail.com</p></li>
          </ul>
        </p>
      </div>} />
        <Route path="/upload" element={<Upload />} />
        <Route path="/search" element={<Search />} />
      </Routes>
    </Router>
  );
}

export default App;
