import { BrowserRouter, Routes, Route } from 'react-router-dom'
import HomePage from './pages/HomePage'
import FeedManagePage from './pages/FeedManagePage';

function App() {
  return (
      <BrowserRouter>
          <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/feeds" element={<FeedManagePage />} />
          </Routes>
      </BrowserRouter>
  );
}

export default App;

