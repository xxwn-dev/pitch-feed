import { Link, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { getArticles, deleteArticle } from "../api/articleApi";
import ArticleCard from "../components/ArticleCard";
import CategoryFilter from "../components/CategoryFilter";

function HomePage({ isAdmin, onLogout }) {
  const [articles, setArticles] = useState([]);
  const [category, setCategory] = useState(null);
  const navigate = useNavigate();

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
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "24px" }}>
        <h1>⚾ Pitch Feed</h1>
        {isAdmin ? (
          <button
            onClick={onLogout}
            style={{
              padding: "6px 14px",
              backgroundColor: "#fff",
              color: "#888",
              border: "1px solid #ccc",
              borderRadius: "4px",
              cursor: "pointer",
              fontSize: "13px",
            }}
          >
            로그아웃
          </button>
        ) : null}
      </div>
      <Link to="/feeds" style={{ fontSize: "14px", color: "#1a73e8" }}>
        피드 관리
      </Link>
      <CategoryFilter selected={category} onSelect={setCategory} />
      {articles.length === 0 ? (
        <p style={{ color: "#888" }}>아티클이 없습니다.</p>
      ) : (
        articles.map((article) => (
          <ArticleCard
            key={article.id}
            article={article}
            onDelete={handleDelete}
          />
        ))
      )}
    </div>
  );
}

export default HomePage;
