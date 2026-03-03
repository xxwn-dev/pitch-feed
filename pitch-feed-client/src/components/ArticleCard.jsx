function ArticleCard({ article }) {
  return (
    <div
      style={{
        border: "1px solid #e0e0e0",
        borderRadius: "8px",
        padding: "16px",
        marginBottom: "12px",
      }}
    >
      <div style={{ fontSize: "12px", color: "#888", marginBottom: "6px" }}>
        {article.feedName} | {article.category} |{" "}
        {new Date(article.publishedAt).toLocaleDateString("ko-KR")}
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
