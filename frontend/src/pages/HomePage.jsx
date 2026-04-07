import { logout } from '../services/authService';

function HomePage({ user }) {
    return (
        <div className="page">
            <div className="card">
                <h1>Bienvenido</h1>
                <p><strong>Nombre:</strong> {user.name}</p>
                <p><strong>Email:</strong> {user.email}</p>
                <p><strong>Dominio:</strong> {user.domain}</p>
                <button onClick={logout}>Cerrar sesión</button>
            </div>
        </div>
    );
}

export default HomePage;