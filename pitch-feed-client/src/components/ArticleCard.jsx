function ArticleCard({ article, onDelete }) {
  return (
    <div
      style={{
        border: "1px solid #e0e0e0",
        borderRadius: "8px",
        padding: "16px",
        marginBottom: "12px",
      }}
    >
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "flex-start",
        }}
      >
        <div style={{ fontSize: "12px", color: "#888", marginBottom: "6px" }}>
          {article.feedName} | {article.category} |{" "}
          {new Date(article.publishedAt).toLocaleDateString("ko-KR")}
        </div>
        {onDelete && (
          <button
            onClick={() => onDelete(article.id)}
            style={{
              padding: "2px 10px",
              backgroundColor: "#fff",
              color: "#e53935",
              border: "1px solid #e53935",
              borderRadius: "4px",
              cursor: "pointer",
              fontSize: "12px",
              flexShrink: 0,
              marginLeft: "8px",
            }}
          >
            삭제
          </button>
        )}
      </div>
      <a
        href={article.url}
        target="_blank"
        rel="noreferrer"
        style={{
          fontSize: "16px",
          fontWeight: "bold",
          color: "#1a1a1a",
          textDecoration: "none",
        }}
      >
        {article.title}
      </a>
      {article.summary && (
        <p style={{ fontSize: "14px", color: "#555", marginTop: "8px" }}>
          {article.summary}
        </p>
      )}
      {article.tags && (
        <div
          style={{
            marginTop: "8px",
            display: "flex",
            gap: "6px",
            flexWrap: "wrap",
          }}
        >
          {article.tags.split(",").map((tag) => (
            <span
              key={tag}
              style={{
                fontSize: "12px",
                backgroundColor: "#f1f3f4",
                padding: "2px 8px",
                borderRadius: "12px",
                color: "#555",
              }}
            >
              #{tag.trim()}
            </span>
          ))}
        </div>
      )}
    </div>
  );
}

export default ArticleCard;
