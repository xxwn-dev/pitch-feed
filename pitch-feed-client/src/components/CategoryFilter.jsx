function CategoryFilter({ selected, onSelect }) {
  const categories = ["ALL", "KBO", "MLB", "NPB"];

  return (
    <div style={{ display: "flex", gap: "8px", marginBottom: "24px" }}>
      {categories.map((cat) => (
        <button
          key={cat}
          onClick={() => onSelect(cat === "ALL" ? null : cat)}
          style={{
            padding: "6px 16px",
            borderRadius: "20px",
            border: "pointer",
            backgroundColor:
              selected === (cat === "ALL" ? null : cat) ? "#1a73e8" : "#fff",
            color: selected === (cat === "전체" ? null : cat) ? "#fff" : "#333",
          }}
        >
          {cat}
        </button>
      ))}
    </div>
  );
}

export default CategoryFilter;
