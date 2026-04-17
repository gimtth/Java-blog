export interface Category {
  id: number;
  name: string;
  slug: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
  pages: number;
}

export interface PostSummary {
  id: number;
  title: string;
  slug: string;
  excerpt: string;
  categoryName: string;
  categorySlug: string;
  status: string;
  publishedAt: string | null;
}

export interface Comment {
  id: number;
  authorName: string;
  authorEmail: string;
  content: string;
  status: string;
  createdAt: string;
  postId: number;
  postTitle: string;
}

export interface PostDetail {
  id: number;
  title: string;
  slug: string;
  excerpt: string;
  content: string;
  coverImage: string | null;
  category: Category;
  status: string;
  publishedAt: string | null;
  comments: Comment[];
}

export interface Dashboard {
  totalPosts: number;
  publishedPosts: number;
  totalCategories: number;
  pendingComments: number;
}

export interface PostForm {
  title: string;
  slug: string;
  excerpt: string;
  content: string;
  coverImage: string;
  categoryId: number | null;
  status: "DRAFT" | "PUBLISHED";
}

export interface CategoryForm {
  name: string;
  slug: string;
}
