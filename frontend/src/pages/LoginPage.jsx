import { loginWithGoogle } from '../services/authService';

function LoginPage() {
    return (
        <div className="page">
            <div className="card">
                <h1>DDAA Platform</h1>
                <p>Inicia sesión con tu cuenta corporativa de Google.</p>
                <button onClick={loginWithGoogle}>Iniciar sesión con Google</button>
            </div>
        </div>
    );
}

export default LoginPage;