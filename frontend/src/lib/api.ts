import type { Category, CategoryForm, Comment, Dashboard, PostDetail, PostForm, PostSummary } from "../types";

const API_BASE = "http://localhost:8080/api";

const TOKEN_KEY = "blog-admin-token";

const readToken = () => localStorage.getItem(TOKEN_KEY) ?? "";

const request = async <T>(path: string, init?: RequestInit, requireAuth = false): Promise<T> => {
  const headers = new Headers(init?.headers);
  if (!headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  if (requireAuth) {
    const token = readToken();
    if (token) headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE}${path}`, { ...init, headers });
  if (!response.ok) {
    const text = await response.text();
    try {
      const parsed = JSON.parse(text) as { message?: string };
      throw new Error(parsed.message || "Request failed");
    } catch {
      throw new Error(text || "Request failed");
    }
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
};

export const login = async (username: string, password: string) => {
  const data = await request<{ token: string }>("/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });
  localStorage.setItem(TOKEN_KEY, data.token);
};

export const clearToken = () => {
  localStorage.removeItem(TOKEN_KEY);
};

export const isAuthed = () => Boolean(readToken());

export const fetchPublicPosts = (category = "", keyword = "") => {
  const search = new URLSearchParams();
  if (category) search.set("category", category);
  if (keyword) search.set("keyword", keyword);
  const suffix = search.size ? `?${search}` : "";
  return request<PostSummary[]>(`/public/posts${suffix}`);
};

export const fetchPublicCategories = () => request<Category[]>("/public/categories");
export const fetchPostDetail = (slug: string) => request<PostDetail>(`/public/posts/${slug}`);
export const submitComment = (postId: number, payload: { authorName: string; authorEmail: string; content: string }) =>
  request<Comment>(`/public/posts/${postId}/comments`, { method: "POST", body: JSON.stringify(payload) });

export const fetchDashboard = () => request<Dashboard>("/admin/dashboard", undefined, true);
export const fetchAdminPosts = () => request<PostSummary[]>("/admin/posts", undefined, true);
export const fetchAdminPost = (id: number) => request<PostDetail>(`/admin/posts/${id}`, undefined, true);
export const fetchAdminCategories = () => request<Category[]>("/admin/categories", undefined, true);
export const fetchAdminComments = () => request<Comment[]>("/admin/comments", undefined, true);
export const createPost = (payload: PostForm) => request<PostSummary>("/admin/posts", { method: "POST", body: JSON.stringify(payload) }, true);
export const updatePost = (id: number, payload: PostForm) =>
  request<PostSummary>(`/admin/posts/${id}`, { method: "PUT", body: JSON.stringify(payload) }, true);
export const deletePost = (id: number) => request<void>(`/admin/posts/${id}`, { method: "DELETE" }, true);
export const createCategory = (payload: CategoryForm) =>
  request<Category>("/admin/categories", { method: "POST", body: JSON.stringify(payload) }, true);
export const approveComment = (id: number) => request<Comment>(`/admin/comments/${id}/approve`, { method: "PATCH" }, true);
export const deleteComment = (id: number) => request<void>(`/admin/comments/${id}`, { method: "DELETE" }, true);
