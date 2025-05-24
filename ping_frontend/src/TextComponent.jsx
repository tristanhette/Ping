import React, { useRef, useState } from 'react';

export function TextComponent() {
  const [textArea1, setTextArea1] = useState('');
  const [textArea2, setTextArea2] = useState('');
  const textarea1Ref = useRef(null);
  const textarea2Ref = useRef(null);

  const handleChangeTextArea1 = (event) => {
    const newText = event.target.value;
    setTextArea1(newText);
    setTextArea2(newText); // Met à jour le contenu du deuxième textarea
  };

  const handleChangeTextArea2 = (event) => {
    const newText = event.target.value;
    setTextArea2(newText);
    setTextArea1(newText); // Met à jour le contenu du premier textarea
  };

  const handleScrollTextarea1 = () => {
    if (textarea2Ref.current) {
      textarea2Ref.current.scrollTop = textarea1Ref.current.scrollTop;
    }
  };

  const handleScrollTextarea2 = () => {
    if (textarea1Ref.current) {
      textarea1Ref.current.scrollTop = textarea2Ref.current.scrollTop;
    }
  };

  return (
    <>
      <textarea
        value={textArea1}
        ref={textarea1Ref}
        onScroll={handleScrollTextarea1}
        onChange={handleChangeTextArea1}
        style={{
          width: '30%',
          height: '50%',
          background: 'url(/lignePartition.png) center fixed',
          backgroundSize: '100%', // Taille fixe du fond d'écran
          color: 'black',
          backgroundAttachment: 'local',
          resize: 'none',
          padding: '1%',
          backgroundPositionY: '0.5%',
          paddingLeft: '4%', // Décalage de 12px à gauche
          border: 'none',
          outline: 'none',
          fontSize: '16px',
          fontFamily: 'monospace',
          overflowY: 'scroll',
        }}
      />

      <textarea
        value={textArea2}
        ref={textarea2Ref}
        onScroll={handleScrollTextarea2}
        onChange={handleChangeTextArea2}
        style={{
          right: '0',
          bottom: '0',
          width: '8%',
          height: '20%',
          position: 'fixed',
          background: 'url(/lignePartition.png) right',
          backgroundSize: '100%', // Taille fixe du fond d'écran
          color: 'black',
          backgroundAttachment: 'local',
          resize: 'none',
          padding: '0.5%',
          backgroundPositionY: '0.5%',
          paddingLeft: '1%', // Décalage de 12px à gauche
          border: 'none',
          outline: 'none',
          fontSize: '8px',
          fontFamily: 'monospace',
          overflowY: 'scroll',
        }}
      />
    </>
  );
}
