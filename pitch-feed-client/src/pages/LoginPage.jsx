import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login } from '../api/articleApi';

function LoginPage({ onLogin }) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            const res = await login(username, password);
            onLogin(res.data.token);
            navigate(-1);
        } catch {
            setError('아이디 또는 비밀번호가 올바르지 않습니다.');
        }
    };

    return (
        <div style={{ maxWidth: '400px', margin: '100px auto', padding: '24px' }}>
            <h1 style={{ marginBottom: '8px' }}>관리자 로그인</h1>
            <p style={{ color: '#888', fontSize: '14px', marginBottom: '24px' }}>
                관리 기능은 관리자 계정이 필요합니다.
            </p>
            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '12px' }}>
                    <input
                        type="text"
                        placeholder="아이디"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        style={{
                            width: '100%',
                            padding: '10px',
                            border: '1px solid #ccc',
                            borderRadius: '4px',
                            boxSizing: 'border-box',
                            fontSize: '14px',
                        }}
                    />
                </div>
                <div style={{ marginBottom: '16px' }}>
                    <input
                        type="password"
                        placeholder="비밀번호"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        style={{
                            width: '100%',
                            padding: '10px',
                            border: '1px solid #ccc',
                            borderRadius: '4px',
                            boxSizing: 'border-box',
                            fontSize: '14px',
                        }}
                    />
                </div>
                {error && (
                    <p style={{ color: '#e53935', marginBottom: '12px', fontSize: '14px' }}>
                        {error}
                    </p>
                )}
                <button
                    type="submit"
                    style={{
                        width: '100%',
                        padding: '10px',
                        backgroundColor: '#1a73e8',
                        color: '#fff',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: 'pointer',
                        fontSize: '14px',
                    }}
                >
                    로그인
                </button>
            </form>
        </div>
    );
}

export default LoginPage;
