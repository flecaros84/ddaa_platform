import { useEffect, useState } from 'react';
import { fetchCurrentUser } from './services/authService';
import LoginPage from './pages/LoginPage';
import HomePage from './pages/HomePage';
import './app.css';

function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadUser() {
      try {
        const data = await fetchCurrentUser();

        if (data.authenticated) {
          setUser(data);
        } else {
          setUser(null);
        }
      } catch (error) {
        setUser(null);
      } finally {
        setLoading(false);
      }
    }

    loadUser();
  }, []);

  if (loading) {
    return (
        <div className="page">
          <div className="card">
            <p>Cargando...</p>
          </div>
        </div>
    );
  }

  return user ? <HomePage user={user} /> : <LoginPage />;
}

export default App;