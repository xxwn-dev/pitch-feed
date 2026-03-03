import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getArticle } from '../api/articleApi';

function ArticleDetailPage() {
    const { id } = useParams();
    const [article, setArticle] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        getArticle(id)
            .then((res) => setArticle(res.data))
            .catch(() => setError('기사를 불러오지 못했습니다.'));
    }, [id]);

    if (error) return <p style={{ padding: '24px' }}>{error}</p>;
    if (!article) return <p style={{ padding: '24px' }}>불러오는 중...</p>;

    return (
        <div style={{ maxWidth: '720px', margin: '0 auto', padding: '24px' }}>
            <div style={{ fontSize: '13px', color: '#888', marginBottom: '8px' }}>
                {article.feedName} | {new Date(article.publishedAt).toLocaleDateString('ko-KR')}
            </div>
            <h1 style={{ fontSize: '24px', fontWeight: 'bold', marginBottom: '12px' }}>
                {article.title}
            </h1>
            {article.summary && (
                <p style={{ fontSize: '16px', color: '#444', lineHeight: '1.6', marginBottom: '16px' }}>
                    {article.summary}
                </p>
            )}
            {article.tags && (
                <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap', marginBottom: '20px' }}>
                    {article.tags.split(',').map((tag) => (
                        <span
                            key={tag}
                            style={{
                                fontSize: '12px',
                                backgroundColor: '#f1f3f4',
                                padding: '2px 8px',
                                borderRadius: '12px',
                                color: '#555',
                            }}
                        >
                            #{tag.trim()}
                        </span>
                    ))}
                </div>
            )}
            <a
                href={article.url}
                target="_blank"
                rel="noreferrer"
                style={{
                    display: 'inline-block',
                    padding: '10px 20px',
                    backgroundColor: '#1a73e8',
                    color: '#fff',
                    borderRadius: '6px',
                    textDecoration: 'none',
                    fontSize: '14px',
                }}
            >
                원문 보기
            </a>
        </div>
    );
}

export default ArticleDetailPage;
