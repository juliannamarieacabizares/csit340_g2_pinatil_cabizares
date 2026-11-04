import { useState } from 'react';
import Login from './components/Login';
import Register from './components/Register';
import './App.css';

function App() {
  const [currentView, setCurrentView] = useState('register');

  return (
    <div className="App">
      {currentView === 'register' ? (
        <Register onSwitchToLogin={() => setCurrentView('login')} />
      ) : (
        <Login onSwitchToRegister={() => setCurrentView('register')} />
      )}
    </div>
  );
}

export default App;