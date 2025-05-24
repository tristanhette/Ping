import './App.css'
export function FileTreeComp(project_object) {
  const handleOnClick = async (path) => {
      const response = await fetch('http://localhost:8080/api/fileContent', {
          method: 'POST',
          headers: {
              'Content-Type': 'application/json'
          },
          body: JSON.stringify({ path: path })
      });
      
      const data = await response.text();
      project_object.setContent(data);
      setCurrentFile(path);
      console.log(data);
  }

  if (project_object.project_object === null) {
      return <></>
  } else {
      return (
          <ul>
              {
                  project_object.project_object.map(pp => (
                      <li key={pp} className="file-tree">
                          <button onClick={() => {handleOnClick(pp); project_object.setCurrentFile(pp)}}>{pp}</button>
                      </li>
                  ))
              }
          </ul>
      );
  }
}