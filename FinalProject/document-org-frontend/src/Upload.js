import { useState} from 'react';

export default function Upload() {
  const [fileSelected, setFileSelected] = useState(false);
  const [file, setFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);

  const handleChange = (e) => {
    const selected = e.target.files[0];
    setFileSelected(!!selected);
    setFile(selected);

    if (selected) {
      const type = selected.type;
      if (type.startsWith('image/') || type === 'application/pdf') {
        setPreviewUrl(URL.createObjectURL(selected));
      } 

      else {
        setPreviewUrl(null);
      }
    } 

    else {
      setPreviewUrl(null);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await fetch("https://rich-surge-455615-u1.ey.r.appspot.com/api/documents/upload", {
        method: "POST",
        body: formData,
      });

      if (response.ok) {
        setPreviewUrl(null);
        setFile(null);
        setFileSelected(false);
      } 
      
      else {
        const errorText = await response.text();
        alert("Upload failed: " + errorText);
      }
    } 
    
    catch (error) {
      alert("Upload error: " + error.message);
    }
  };

  const handleClosePreview = () => {
    setPreviewUrl(null);
    setFile(null);
    setFileSelected(false);
  };

  return (
    <div style={{ display: 'flex', gap: '2rem', justifyContent: 'center'}}>
      <form className="upload-form" onSubmit={handleSubmit} encType="multipart/form-data"
        style={{display: 'flex',flexDirection: 'column',gap: '1rem', alignItems: 'center'}}
      >
        <label htmlFor="file-upload" className="custom-file-upload">Select File</label>
        <input id="file-upload" type="file" name="file" accept="image/*,application/pdf" onChange={handleChange}/>
        <input type="submit" value="Upload" disabled={!fileSelected} />
      </form>

      {previewUrl && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', maxWidth: '600px' }}>
          {file.type.startsWith('image/') ? (
            <img src={previewUrl} alt="Preview" style={{ maxWidth: '100%', maxHeight: '400px' }} />
          ) : (
            <iframe src={previewUrl} title="PDF Preview" style={{ width: '600px', height: 'calc(100vh - 80px)', border: '1px solid #ccc' }} />
          )}

          <button id="close-btn" onClick={handleClosePreview}>
            Close Preview
          </button>
        </div>
      )}

    </div>
  );
}
