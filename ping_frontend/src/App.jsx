  import React, { useEffect, useState, useRef  } from 'react';
import './App.css';
import './toggle.css'
import { FileTreeComp } from './FileTreeComp';
import metronomeSound from './metronome2.mp3'; // Chemin vers le fichier audio du métronome

function App() {
  const [message, setMessage] = useState('');
  const [currentFile, setCurrentFile] = useState('');
  const [project, setProject] = useState('');
  const [project_object, setProject_Object] = useState(null);
  const [commandLines, setCommandLines] = useState(['42sh$ ']);

  const [text, setText] = useState('');
  const [scrollTop, setScrollTop] = useState(0);
  const [sliderValue1, setSliderValue1] = useState(0); // Ajoutez un état pour le slider
  const [sliderValue2, setSliderValue2] = useState(0); // Ajoutez un état pour le slider
  const [sliderValueR, setSliderValueR] = useState(33); // Initial R value
  const [sliderValueG, setSliderValueG] = useState(46); // Initial G value
  const [sliderValueB, setSliderValueB] = useState(63); // Initial B value
  const [metronomePlaying, setMetronomePlaying] = useState(false); // État du métronome
  const [metronomeSpeed, setMetronomeSpeed] = useState(1); // Vitesse du métronome

  const metronomeAudio = useRef(new Audio(metronomeSound)); // Référence pour l'élément audio du métronome

  const musicSheetRef = useRef(null);
  const subMusicSheetRef = useRef(null);

  const [btn1Checked, setBtn1Checked] = useState(false);
  const [btn2Checked, setBtn2Checked] = useState(false);

  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  const handleToggleDropdown = () => {
    setIsDropdownOpen(!isDropdownOpen);
  };
  
  const handleOutsideClick = (event) => {
    // Fermer le menu déroulant si l'utilisateur clique en dehors de celui-ci
    if (menuRef.current && !menuRef.current.contains(event.target)) {
      setIsDropdownOpen(false);
    }
  };

  const buttonRef = useRef(null);
  const menuRef = useRef(null);

  useEffect(() => {
    document.addEventListener('mousedown', handleOutsideClick);
    return () => {
      document.removeEventListener('mousedown', handleOutsideClick);
    };
  }, []);
  
  const handleMouseEnter = () => {
    setIsDropdownOpen(true);
  };
  
  const handleMouseLeave = () => {
    setIsDropdownOpen(false);
  };

  const handleBtn1Change = (event) => {
      setBtn1Checked(event.target.checked);
      console.log('Button 1 is ' + (event.target.checked ? 'on' : 'off'));
  };

  const handleBtn2Change = (event) => {
    setBtn2Checked(event.target.checked);
    console.log('Button 2 is ' + (event.target.checked ? 'on' : 'off'));
    if (event.target.checked) {
      startMetronome();
    } else {
      stopMetronome();
    }
  };

  const handleTextChange = async (e) => {
    setText(e.target.value);
    console.log(currentFile + " et " + e.target.value);
    if (btn1Checked) {
        if (currentFile) {
            try {
                const response = await fetch('http://localhost:8080/api/updateContent', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ path: currentFile, content: e.target.value }),
                });
                
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
            } catch (error) {
                console.error('Error updating content:', error);
            }
        } else {
            console.warn('currentFile is null or empty');
        }
    }
};

  const handleScroll = (e) => {
    setScrollTop(e.target.scrollTop);
    if (e.target === musicSheetRef.current) {
      subMusicSheetRef.current.scrollTop = e.target.scrollTop;
    } else {
      musicSheetRef.current.scrollTop = e.target.scrollTop;
    }
  };

  useEffect(() => {
    fetch('http://localhost:8080/api/hello')
      .then(response => response.text())
      .then(data => setMessage(data));
  }, []);

  const handleCommandLineChange = (event) => {
    // Splitting command lines by newline character
    const lines = event.target.value.split('\n');
    setCommandLines(lines);
  };

  const handleKeyDown = async (event) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      // Get the command entered by the user
      const commandIndex = commandLines.length - 1; // Last line
      const command = commandLines[commandIndex].replace('42sh$', '').trim(); // Extract the command after '42sh$'
      // Fetch data from API
      try {
        const response = await fetch('http://localhost:8080/api/cli', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ command: command })
        });
        
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }

        const output = await response.text();
        //const output = data.output; // Assuming API returns { output: "some output" }

        // Update command-lines state with new output and '42sh$'
        const updatedCommandLines = [...commandLines, `${output.replace(/\n$/, '')}`, '42sh$ '];
        setCommandLines(updatedCommandLines);
      } catch (error) {
        setCommandLines([...commandLines, `Error: ${error.message}`, '42sh$ ']);
        console.error('Error fetching data:', error);
      }
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const response = await fetch('http://localhost:8080/api/open/project', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ path: project })
    }).then((response) => response.json())
    .then((data) =>
      {
        console.log(data)
        var array = []
        for(var line of data)
        {
          array.push(line)
          console.log(line)
        }
        
        setProject_Object(array)
      }
    );

  }
  
  const HandleC = (event) =>{
    setProject(event.target.value)
  }

  function HandleText(content){
    setText(content);
  }

  const getColorFromSliderValue = (value) => {
      if (value < 51) return 'black';
      if (value < 102) return '#212E3F'; // Blue
      if (value < 153) return 'red';
      if (value < 204) return 'green';
      return 'yellow';
    };
    const handleSliderChangeR = (event) => {
      const newValue = event.target.value;
      setSliderValueR(newValue);
      updateBackgroundColor(newValue, sliderValueG, sliderValueB);
    };
  
    const handleSliderChangeG = (event) => {
      const newValue = event.target.value;
      setSliderValueG(newValue);
      updateBackgroundColor(sliderValueR, newValue, sliderValueB);
    };
  
    const handleSliderChangeB = (event) => {
      const newValue = event.target.value;
      setSliderValueB(newValue);
      updateBackgroundColor(sliderValueR, sliderValueG, newValue);
    };
  
    const updateBackgroundColor = (r, g, b) => {
      const color = `rgb(${r}, ${g}, ${b})`;
      return color;
    };
    const startMetronome = () => {
      setMetronomePlaying(true);
      metronomeAudio.current.currentTime = 0; // Rembobiner l'audio au début
      metronomeAudio.current.playbackRate = metronomeSpeed; // Ajuster la vitesse de lecture
      metronomeAudio.current.loop = true; // Activer la lecture en boucle
      metronomeAudio.current.play(); // Démarrer la lecture
    };
  
    const stopMetronome = () => {
      setMetronomePlaying(false);
      metronomeAudio.current.pause(); // Mettre en pause la lecture
    };
  
    // Effet pour mettre à jour la vitesse du métronome lorsque metronomeSpeed change
    useEffect(() => {
      if (metronomePlaying) {
        metronomeAudio.current.playbackRate = metronomeSpeed;
      }
    }, [metronomeSpeed]);
    const handleAdd = async (event) => {
      const response = await fetch('http://localhost:8080/api/execFeature', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({feature:"ADD" , params: [project] , project : ""})
      })
    }
    const handleCommit = async (event) => {
      const response = await fetch('http://localhost:8080/api/execFeature', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ feature:"COMMIT" ,params: [project] , project : ""})
      })
    }
    const handlePush = async (event) => {
      const response = await fetch('http://localhost:8080/api/execFeature', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({feature:"PUSH",params: [] , project : ""})
      })
    }
    const handlePull = async (event) => {
      const response = await fetch('http://localhost:8080/api/execFeature', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({feature:"PULL",params: [] , project : ""})
      })
    }
  
  return (
    <div className="App" style={{ backgroundColor: updateBackgroundColor(sliderValueR,sliderValueG,sliderValueB) }}>
      <div className="toolbar">
        <button className="toolbar-button folder-button"></button>
        <button className="toolbar-button git-add-button" onClick={handleAdd}></button>
        <button className="toolbar-button git-commit-button" onClick={handleCommit}></button>
        <button className="toolbar-button git-push-button" onClick={handlePush}></button>
        <button className="toolbar-button git-pull-button" onClick={handlePull}></button>
        <button className="toolbar-button param-button"></button>
      </div>
      <div className="sidebar">
        <label>
          Open file/project
          <input type="text" onChange={HandleC}/>
          <button onClick={handleSubmit}>Submit</button>
        </label>
        <FileTreeComp project_object={project_object} setContent={HandleText} setCurrentFile={setCurrentFile}></FileTreeComp>
      </div>
      <div className="new-div">
          <div style={{ width: '100%', height: '100vh', overflow: 'hidden' }}>
          <iframe 
            src="https://www.youtube.com/embed/RPXgxEgFrSQ" 
            style={{ width: '100%', height: '100%', border: 'none' }}
            title="YouTube Video"
          />
          </div>
      </div>
      <div className="content">
        <div className="music-sheet-container">
        <textarea
            className="music-sheet"
            value={text}
            onChange={handleTextChange}
            onScroll={handleScroll}
            ref={musicSheetRef}
            style={{ color: updateBackgroundColor(sliderValueR,sliderValueG,sliderValueB) }} // Appliquez la couleur basée sur la valeur du slider
          ></textarea>
        </div>
        <div className="additional-div-container">
          <div className="additional-div">
          <textarea className="command-line"
              value={commandLines.join('\n')}
              onChange={handleCommandLineChange}
              onKeyDown={handleKeyDown}
              ></textarea>
          </div>
          <div className="additional-div lower-additional-div">
            <div style={{ width: '100%', height: '100vh', overflow: 'hidden' }}>
            <iframe 
              src="https://www.youtube.com/embed/MNeX4EGtR5Y" 
              style={{ width: '100%', height: '100%', border: 'none' }}
              title="YouTube Video"
            />
            </div>
        </div>
        </div>
      </div>
      <div className="slider-container">
        <div class="range-slider">
          <input type="range" min="0" max="255" defaultValue="200" range="true" value={sliderValueR} onChange={handleSliderChangeR}/>
        </div>
        <div class="range-slider">
          <input type="range" min="0" max="255" defaultValue="200" range="true" value={sliderValueG} onChange={handleSliderChangeG}/>
        </div>
        <div class="range-slider">
          <input type="range" min="0" max="255" defaultValue="200" range="true" value={sliderValueB} onChange={handleSliderChangeB}/>
        </div>
        <div class="range-slider">
          <input type="range" min="0.5" max="2" step="0.1" defaultValue="200" range="true" value={metronomeSpeed} onChange={(e) => setMetronomeSpeed(parseFloat(e.target.value))}/>
        </div>
      </div>
      <div className="sub-sheet-container">
      <textarea
          className="sub-music-sheet"
          placeholder="Écrivez ici..."
          value={text}
          onChange={handleTextChange}
          onScroll={handleScroll}
          ref={subMusicSheetRef}
        ></textarea>
        </div>
      <div class="toggle-container">
        <div className="toggle">
          <input type="checkbox" id="btn1" checked={btn1Checked} onChange={handleBtn1Change} />
          <label htmlFor="btn1">
            <span className="thumb"></span>
          </label>
          <div className="light"></div>
        </div>
      </div>
      <div class="toggle-container2">
        <div className="toggle">
          <input type="checkbox" id="btn2" checked={btn2Checked} onChange={handleBtn2Change}/>
          <label htmlFor="btn2">
            <span className="thumb"></span>
          </label>
          <div className="light"></div>
        </div>
      </div>
    </div>
  );
}

export default App;
