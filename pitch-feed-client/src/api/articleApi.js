import axios from 'axios';

const api = axios.create({
   baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api',
});

// 요청마다 토큰 자동 주입
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// 401/403 응답 시 로그인 페이지로 이동 (로그인 요청 자체는 제외)
api.interceptors.response.use(
    (response) => response,
    (error) => {
        const status = error.response?.status;
        const isLoginRequest = error.config?.url.includes('/auth/login');
        console.log('[auth interceptor] status:', status, '/ message:', error.message);
        if ((status === 401 || status === 403) && !isLoginRequest) {
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export const login = (username, password) =>
    api.post('/auth/login', { username, password });

export const getArticles = (category) => {
    const params = category ? { category } : {};
    return api.get('/articles', { params });
};

export const getArticle = (id) => api.get(`/articles/${id}`);

export const getFeeds = () => api.get('/feeds');

export const addFeed = (feed) => api.post('/feeds', feed);

export const deleteFeed = (id) => api.delete(`/feeds/${id}`);

export const deleteArticle = (id) => api.delete(`/articles/${id}`);
