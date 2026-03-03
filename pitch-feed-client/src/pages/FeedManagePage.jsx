import { useEffect, useState } from "react";
import { getFeeds, addFeed, deleteFeed } from "../api/articleApi";

function FeedManagePage() {
  const [feeds, setFeeds] = useState([]);
  const [name, setName] = useState("");
  const [url, setUrl] = useState("");
  const [category, setCategory] = useState("");

  const fetchFeeds = () => {
    getFeeds()
      .then((res) => setFeeds(res.data))
      .catch((err) => console.error(err));
  };

  useEffect(() => {
    fetchFeeds();
  }, []);

  const handleAdd = () => {
    if (!name || !url) return;
    addFeed({ name, url, category })
      .then(() => {
        setName("");
        setUrl("");
        setCategory("");
        fetchFeeds();
      })
      .catch((err) => console.error(err));
  };

  const handleDelete = (id) => {
    deleteFeed(id)
      .then(() => fetchFeeds())
      .catch((err) => console.error(err));
  };

  return (
    <div style={{ maxWidth: "800px", margin: "0 auto", padding: "24px" }}>
      <h1 style={{ marginBottom: "24px" }}> 피드 관리</h1>
      {/* 피드 추가 */}
      <div
        style={{
          display: "flex",
          gap: "8px",
          marginBottom: "32px",
          flexWrap: "wrap",
        }}
      >
        <input
          placeholder="피드 이름"
          value={name}
          onChange={(e) => setName(e.target.value)}
          style={{
            padding: "8px",
            border: "1px solid #ccc",
            borderRadius: "4px",
            flex: 1,
          }}
        />
        <input
          placeholder="RSS URL"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          style={{
            padding: "8px",
            border: "1px solid #ccc",
            borderRadius: "4px",
            flex: 2,
          }}
        />
        <input
          placeholder="카테고리 (예: KBO)"
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          style={{
            padding: "8px",
            border: "1px solid #ccc",
            borderRadius: "4px",
            flex: 1,
          }}
        />
        <button
          onClick={handleAdd}
          style={{
            padding: "8px 16px",
            backgroundColor: "#1a73e8",
            color: "#fff",
            border: "none",
            borderRadius: "4px",
            cursor: "pointer",
          }}
        >
          추가
        </button>
      </div>

      {/* 피드 목록 */}
      {feeds.map((feed) => (
        <div
          key={feed.id}
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            padding: "12px 16px",
            border: "1px solid #e0e0e0",
            borderRadius: "8px",
            marginBottom: "8px",
          }}
        >
          <div>
            <span style={{ fontWeight: "bold", marginRight: "8px" }}>
              {feed.name}
            </span>
            <span
              style={{ fontSize: "12px", color: "#888", marginRight: "8px" }}
            >
              {feed.category}
            </span>
            <span style={{ fontSize: "12px", color: "#aaa" }}>{feed.url}</span>
          </div>
          <button
            onClick={() => handleDelete(feed.id)}
            style={{
              padding: "4px 12px",
              backgroundColor: "#fff",
              color: "#e53935",
              border: "1px solid #e53935",
              borderRadius: "4px",
              cursor: "pointer",
            }}
          >
            삭제
          </button>
        </div>
      ))}
    </div>
  );
}

export default FeedManagePage;
