import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { getArticles, deleteArticle } from "../api/articleApi";
import ArticleCard from "../components/ArticleCard";
import CategoryFilter from "../components/CategoryFilter";

function HomePage() {
  const [articles, setArticles] = useState([]);
  const [category, setCategory] = useState(null);

  useEffect(() => {
    getArticles(category)
      .then((res) => setArticles(res.data))
      .catch((err) => console.error(err));
  }, [category]);

  const handleDelete = (id) => {
    deleteArticle(id)
      .then(() => setArticles((prev) => prev.filter((a) => a.id !== id)))
      .catch((err) => console.error(err));
  };

  return (
    <div style={{ maxWidth: "1100px", margin: "0 auto", padding: "24px" }}>
      <h1 style={{ marginBottom: "24px" }}>⚾ Pitch Feed</h1>
      <Link to="/feeds" style={{ fontSize: "14px", color: "#1a73e8" }}>
        피드 관리
      </Link>
      <CategoryFilter selected={category} onSelect={setCategory} />
      {articles.length === 0 ? (
        <p style={{ color: "#888" }}>아티클이 없습니다.</p>
      ) : (
        articles.map((article) => (
          <ArticleCard key={article.id} article={article} onDelete={handleDelete} />
        ))
      )}
    </div>
  );
}

export default HomePage;
