import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { useState } from 'react';
import HomePage from './pages/HomePage';
import FeedManagePage from './pages/FeedManagePage';
import ArticleDetailPage from './pages/ArticleDetailPage';
import LoginPage from './pages/LoginPage';

function App() {
    const [isAdmin, setIsAdmin] = useState(!!localStorage.getItem('token'));

    const handleLogin = (token) => {
        localStorage.setItem('token', token);
        setIsAdmin(true);
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        setIsAdmin(false);
    };

    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<HomePage isAdmin={isAdmin} onLogout={handleLogout} />} />
                <Route path="/feeds" element={<FeedManagePage isAdmin={isAdmin} onLogout={handleLogout} />} />
                <Route path="/articles/:id" element={<ArticleDetailPage />} />
                <Route path="/login" element={<LoginPage onLogin={handleLogin} />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
