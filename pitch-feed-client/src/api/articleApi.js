import axios from 'axios';

const api = axios.create({
   baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api',
});

export const getArticles = (category) => {
    const params = category ? { category } : {};
    return api.get('/articles', { params });
};

export const getArticle = (id) => api.get(`/articles/${id}`);

export const getFeeds = () => api.get('/feeds');

export const addFeed = (feed) => api.post('/feeds', feed);

export const deleteFeed = (id) => api.delete(`/feeds/${id}`);

export const deleteArticle = (id) => api.delete(`/articles/${id}`);


