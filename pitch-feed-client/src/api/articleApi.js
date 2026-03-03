import axios from 'axios';

const api = axios.create({
   baseURL: 'http://localhost:8080/api',
});

export const getArticles = (category) => {
    const params = category ? { category } : {};
    return api.get('/articles', { params });
};

export const getFeeds = () => api.get('/feeds');

export const addFeed = (feed) => api.post('/feeds', feed);

export const deleteFeed = (id) => api.delete(`/feeds/${id}`);


