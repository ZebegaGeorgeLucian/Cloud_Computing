import { useState } from 'react';

export default function Search() {
  const [wordsInputed, setWordsInputed] = useState('');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pdfOnly, setPdfOnly] = useState(false); 

  const handleChange = (e) => {
    setWordsInputed(e.target.value);
  };

  const getFileNameFromUrl = (url) => {
    return url.split('/').pop().split('?')[0];
  };

  const handleSearch = async () => {
    const query = wordsInputed.trim();
    if (!query) return;

    setLoading(true);
    setResults([]);

    try {
      const params = new URLSearchParams({ query });
      if (pdfOnly) {
        params.append('documentType', 'application/pdf'); 
      }
      const response = await fetch(`https://rich-surge-455615-u1.ey.r.appspot.com/api/documents/search?${params.toString()}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      if (response.status === 204) {
        alert('No documents found.');
      } 
      
      else if (response.ok) {
        const data = await response.json();
        setResults(data.hits || []);
      } 
      
      else {
        const text = await response.text();
        alert('Search failed: ' + text);
      }
    } 
    
    catch (err) {
      alert('Error during search: ' + err.message);
    } 
    
    finally {
      setLoading(false);
    }
  };

  return (
    <div className="search-container">
      <label htmlFor="search-input" className="search">Search documents by words</label>
      <input id="search-input" type="text" placeholder="Words separated by space..." value={wordsInputed} onChange={handleChange}/>
      <button disabled={wordsInputed.trim() === ''} onClick={handleSearch}>Search</button>

      <button disabled={wordsInputed.trim() === ''} style={{ marginLeft: '10px' }} onClick={() => setPdfOnly(!pdfOnly)}>{pdfOnly ? 'Show All' : 'Search PDFs only'}</button>

      {loading && <p>Searching...</p>}

      <ul className="results-list">
        {results.map((hit, index) => (
          <li key={index} style={{ marginBottom: '1rem' }}>
            <p><strong>ID:</strong> {hit.id || 'N/A'}</p>
            <p><strong>Document Type:</strong> {hit.source.documentType || 'N/A'}</p>
            {hit.source.downloadUrl ? (
              <a href={hit.source.downloadUrl} target="_blank" rel="noopener noreferrer" 
              download={getFileNameFromUrl(hit.source.downloadUrl)}>Download</a>
            ) : (
              <p style={{ color: 'red' }}>No download URL</p>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
