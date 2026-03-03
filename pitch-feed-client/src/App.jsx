import { BrowserRouter, Routes, Route } from 'react-router-dom'
import HomePage from './pages/HomePage'
import FeedManagePage from './pages/FeedManagePage';
import ArticleDetailPage from './pages/ArticleDetailPage';

function App() {
  return (
      <BrowserRouter>
          <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/feeds" element={<FeedManagePage />} />
              <Route path="/articles/:id" element={<ArticleDetailPage />} />
          </Routes>
      </BrowserRouter>
  );
}

export default App;

