<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import {
  approveComment,
  clearToken,
  createCategory,
  createPost,
  deleteComment,
  deletePost,
  fetchAdminCategories,
  fetchAdminComments,
  fetchAdminPost,
  fetchAdminPosts,
  fetchDashboard,
  fetchPostDetail,
  fetchPublicCategories,
  fetchPublicPosts,
  isAuthed,
  login,
  submitComment,
  updatePost,
} from "./lib/api";
import type { Category, CategoryForm, Comment, Dashboard, PostDetail, PostForm, PostSummary } from "./types";

const loading = ref(false);
const activeView = ref<"public" | "admin">("public");
const activeAdminSection = ref<"overview" | "posts" | "categories" | "comments">("overview");
const adminReady = ref(isAuthed());
const errorMessage = ref("");
const successMessage = ref("");

const categories = ref<Category[]>([]);
const posts = ref<PostSummary[]>([]);
const selectedPost = ref<PostDetail | null>(null);
const adminDashboard = ref<Dashboard | null>(null);
const adminPosts = ref<PostSummary[]>([]);
const adminCategories = ref<Category[]>([]);
const adminComments = ref<Comment[]>([]);
const editingPostId = ref<number | null>(null);

const filters = reactive({ category: "", keyword: "" });
const loginForm = reactive({ username: "admin", password: "admin123" });
const commentForm = reactive({ authorName: "", authorEmail: "", content: "" });
const postForm = reactive<PostForm>({
  title: "",
  slug: "",
  excerpt: "",
  content: "",
  coverImage: "",
  categoryId: null,
  status: "PUBLISHED",
});
const categoryForm = reactive<CategoryForm>({ name: "", slug: "" });

const featuredPost = computed(() => posts.value[0] ?? null);
const postRows = computed(() => posts.value.slice(1));

const run = async (task: () => Promise<void>) => {
  loading.value = true;
  errorMessage.value = "";
  successMessage.value = "";
  try {
    await task();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : "操作失败";
  } finally {
    loading.value = false;
  }
};

const loadPublicData = async () => {
  categories.value = await fetchPublicCategories();
  posts.value = await fetchPublicPosts(filters.category, filters.keyword);
  if (posts.value.length > 0) {
    selectedPost.value = await fetchPostDetail(posts.value[0].slug);
  } else {
    selectedPost.value = null;
  }
};

const openPost = async (slug: string) => {
  selectedPost.value = await fetchPostDetail(slug);
};

const loadAdminData = async () => {
  adminDashboard.value = await fetchDashboard();
  adminPosts.value = await fetchAdminPosts();
  adminCategories.value = await fetchAdminCategories();
  adminComments.value = await fetchAdminComments();
  if (!postForm.categoryId && adminCategories.value.length > 0) {
    postForm.categoryId = adminCategories.value[0].id;
  }
};

const resetPostForm = () => {
  editingPostId.value = null;
  postForm.title = "";
  postForm.slug = "";
  postForm.excerpt = "";
  postForm.content = "";
  postForm.coverImage = "";
  postForm.categoryId = adminCategories.value[0]?.id ?? null;
  postForm.status = "PUBLISHED";
};

const submitLogin = async () => {
  await run(async () => {
    await login(loginForm.username, loginForm.password);
    adminReady.value = true;
    activeView.value = "admin";
    activeAdminSection.value = "overview";
    await loadAdminData();
    successMessage.value = "登录成功";
  });
};

const logout = () => {
  clearToken();
  adminReady.value = false;
  activeView.value = "public";
  activeAdminSection.value = "overview";
};

const savePostForm = async () => {
  await run(async () => {
    if (!postForm.categoryId) {
      throw new Error("请选择分类");
    }
    if (editingPostId.value) {
      await updatePost(editingPostId.value, postForm);
    } else {
      await createPost(postForm);
    }
    await loadAdminData();
    await loadPublicData();
    resetPostForm();
    successMessage.value = "文章已保存";
  });
};

const editPost = async (row: PostSummary) => {
  await run(async () => {
    const detail = await fetchAdminPost(row.id);
    editingPostId.value = row.id;
    postForm.title = row.title;
    postForm.slug = row.slug;
    postForm.excerpt = row.excerpt;
    postForm.content = detail.content;
    postForm.coverImage = detail.coverImage ?? "";
    postForm.categoryId = adminCategories.value.find((item) => item.slug === row.categorySlug)?.id ?? null;
    postForm.status = row.status as "DRAFT" | "PUBLISHED";
  });
};

const removePost = async (id: number) => {
  await run(async () => {
    await deletePost(id);
    await loadAdminData();
    await loadPublicData();
    successMessage.value = "文章已删除";
  });
};

const saveCategoryForm = async () => {
  await run(async () => {
    await createCategory(categoryForm);
    categoryForm.name = "";
    categoryForm.slug = "";
    await loadAdminData();
    successMessage.value = "分类已创建";
  });
};

const approvePendingComment = async (id: number) => {
  await run(async () => {
    await approveComment(id);
    await loadAdminData();
    await loadPublicData();
    successMessage.value = "评论已审核";
  });
};

const removeCommentRow = async (id: number) => {
  await run(async () => {
    await deleteComment(id);
    await loadAdminData();
    await loadPublicData();
    successMessage.value = "评论已删除";
  });
};

const submitCommentForm = async () => {
  if (!selectedPost.value) return;
  await run(async () => {
    await submitComment(selectedPost.value.id, commentForm);
    commentForm.authorName = "";
    commentForm.authorEmail = "";
    commentForm.content = "";
    successMessage.value = "评论已提交，等待审核";
  });
};

onMounted(() => {
  run(async () => {
    await loadPublicData();
    if (adminReady.value) {
      await loadAdminData();
    }
  });
});
</script>

<template>
  <div class="page">
      <header class="header">
        <div>
          <h1>Java 博客项目</h1>
          <p>Spring Boot + MySQL + JWT + Vue 3</p>
        </div>
        <div class="header-actions">
          <el-button plain @click="activeView = 'public'">博客</el-button>
          <el-button plain @click="activeView = 'admin'">管理台</el-button>
        </div>
      </header>

      <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" class="notice" />
      <el-alert v-if="successMessage" :title="successMessage" type="success" show-icon :closable="false" class="notice" />

      <section v-if="activeView === 'public'" class="public-section">
        <aside class="sidebar">
          <el-card shadow="never">
            <template #header>文章筛选</template>
            <el-form label-position="top">
              <el-form-item label="分类">
                <el-select v-model="filters.category" placeholder="全部分类" clearable>
                  <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.slug" />
                </el-select>
              </el-form-item>
              <el-form-item label="关键词">
                <el-input v-model="filters.keyword" placeholder="搜索标题或摘要" clearable />
              </el-form-item>
              <el-button type="primary" :loading="loading" @click="run(loadPublicData)">搜索</el-button>
            </el-form>
          </el-card>
        </aside>

        <main class="content">
          <el-card v-if="featuredPost" shadow="never">
            <template #header>推荐文章</template>
            <h2 class="section-title">{{ featuredPost.title }}</h2>
            <p class="muted">{{ featuredPost.excerpt }}</p>
            <el-button type="primary" link @click="run(() => openPost(featuredPost.slug))">查看详情</el-button>
          </el-card>

          <el-row :gutter="16">
            <el-col v-for="item in postRows" :key="item.id" :xs="24" :md="12">
              <el-card shadow="never" class="post-card" @click="run(() => openPost(item.slug))">
                <div class="tag-line">{{ item.categoryName }} / {{ item.status }}</div>
                <h3>{{ item.title }}</h3>
                <p class="muted">{{ item.excerpt }}</p>
              </el-card>
            </el-col>
          </el-row>

          <el-card v-if="selectedPost" shadow="never">
            <template #header>文章详情</template>
            <h2 class="section-title">{{ selectedPost.title }}</h2>
            <p class="muted">{{ selectedPost.category.name }} · {{ selectedPost.publishedAt || "未发布" }}</p>
            <p class="article">{{ selectedPost.content }}</p>

            <el-divider />

            <h3>评论</h3>
            <el-empty v-if="selectedPost.comments.length === 0" description="暂无已审核评论" />
            <div v-else class="comment-list">
              <div v-for="comment in selectedPost.comments" :key="comment.id" class="comment-item">
                <strong>{{ comment.authorName }}</strong>
                <p>{{ comment.content }}</p>
              </div>
            </div>

            <el-divider />

            <el-form label-position="top" @submit.prevent="submitCommentForm">
              <el-form-item label="昵称">
                <el-input v-model="commentForm.authorName" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="commentForm.authorEmail" />
              </el-form-item>
              <el-form-item label="评论内容">
                <el-input v-model="commentForm.content" type="textarea" :rows="4" />
              </el-form-item>
              <el-button type="primary" :loading="loading" @click="submitCommentForm">提交评论</el-button>
            </el-form>
          </el-card>
        </main>
      </section>

      <section v-else class="admin-section">
        <el-card v-if="!adminReady" shadow="never" class="login-card">
          <template #header>管理员登录</template>
          <el-form label-position="top" @submit.prevent="submitLogin">
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" show-password />
            </el-form-item>
            <el-button type="primary" :loading="loading" @click="submitLogin">登录</el-button>
          </el-form>
        </el-card>

        <template v-else>
          <div class="admin-layout">
            <aside class="admin-sidebar">
              <el-card shadow="never">
                <div class="admin-sidebar-header">
                  <h2>博客管理台</h2>
                  <p>基础内容维护</p>
                </div>
                <div class="admin-nav">
                  <el-button
                    :type="activeAdminSection === 'overview' ? 'primary' : 'default'"
                    plain
                    @click="activeAdminSection = 'overview'"
                  >
                    概览
                  </el-button>
                  <el-button
                    :type="activeAdminSection === 'posts' ? 'primary' : 'default'"
                    plain
                    @click="activeAdminSection = 'posts'"
                  >
                    文章
                  </el-button>
                  <el-button
                    :type="activeAdminSection === 'categories' ? 'primary' : 'default'"
                    plain
                    @click="activeAdminSection = 'categories'"
                  >
                    分类
                  </el-button>
                  <el-button
                    :type="activeAdminSection === 'comments' ? 'primary' : 'default'"
                    plain
                    @click="activeAdminSection = 'comments'"
                  >
                    评论
                  </el-button>
                </div>
                <el-button plain class="logout-button" @click="logout">退出登录</el-button>
              </el-card>
            </aside>

            <main class="admin-content">
              <el-card v-if="activeAdminSection === 'overview'" shadow="never">
                <template #header>数据概览</template>
                <el-row v-if="adminDashboard" :gutter="16" class="dashboard-row">
                  <el-col :xs="12" :md="6">
                    <el-statistic title="文章总数" :value="adminDashboard.totalPosts" />
                  </el-col>
                  <el-col :xs="12" :md="6">
                    <el-statistic title="已发布" :value="adminDashboard.publishedPosts" />
                  </el-col>
                  <el-col :xs="12" :md="6">
                    <el-statistic title="分类数" :value="adminDashboard.totalCategories" />
                  </el-col>
                  <el-col :xs="12" :md="6">
                    <el-statistic title="待审核评论" :value="adminDashboard.pendingComments" />
                  </el-col>
                </el-row>
              </el-card>

              <template v-if="activeAdminSection === 'posts'">
                <el-card shadow="never">
                  <template #header>{{ editingPostId ? "编辑文章" : "新建文章" }}</template>
                  <el-form label-position="top">
                    <el-form-item label="标题">
                      <el-input v-model="postForm.title" />
                    </el-form-item>
                    <el-form-item label="Slug">
                      <el-input v-model="postForm.slug" />
                    </el-form-item>
                    <el-form-item label="摘要">
                      <el-input v-model="postForm.excerpt" />
                    </el-form-item>
                    <el-form-item label="分类">
                      <el-select v-model="postForm.categoryId" placeholder="请选择分类">
                        <el-option v-for="item in adminCategories" :key="item.id" :label="item.name" :value="item.id" />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="状态">
                      <el-select v-model="postForm.status">
                        <el-option label="发布" value="PUBLISHED" />
                        <el-option label="草稿" value="DRAFT" />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="正文">
                      <el-input v-model="postForm.content" type="textarea" :rows="10" />
                    </el-form-item>
                    <div class="form-actions">
                      <el-button type="primary" :loading="loading" @click="savePostForm">保存</el-button>
                      <el-button @click="resetPostForm">重置</el-button>
                    </div>
                  </el-form>
                </el-card>

                <el-card shadow="never" class="table-card">
                  <template #header>文章列表</template>
                  <el-table :data="adminPosts" stripe>
                    <el-table-column prop="title" label="标题" min-width="220" />
                    <el-table-column prop="categoryName" label="分类" width="120" />
                    <el-table-column prop="status" label="状态" width="100" />
                    <el-table-column label="操作" width="180">
                      <template #default="{ row }">
                        <el-button link type="primary" @click="editPost(row)">编辑</el-button>
                        <el-button link type="danger" @click="removePost(row.id)">删除</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-card>
              </template>

              <template v-if="activeAdminSection === 'categories'">
                <el-card shadow="never" class="category-card">
                  <template #header>新建分类</template>
                  <el-form label-position="top">
                    <el-form-item label="分类名">
                      <el-input v-model="categoryForm.name" />
                    </el-form-item>
                    <el-form-item label="Slug">
                      <el-input v-model="categoryForm.slug" />
                    </el-form-item>
                    <el-button type="primary" :loading="loading" @click="saveCategoryForm">保存分类</el-button>
                  </el-form>
                </el-card>

                <el-card shadow="never" class="table-card">
                  <template #header>分类列表</template>
                  <el-table :data="adminCategories" stripe>
                    <el-table-column prop="name" label="分类名" min-width="180" />
                    <el-table-column prop="slug" label="Slug" min-width="180" />
                  </el-table>
                </el-card>
              </template>

              <el-card v-if="activeAdminSection === 'comments'" shadow="never" class="table-card">
                <template #header>评论管理</template>
                <el-table :data="adminComments" stripe>
                  <el-table-column prop="authorName" label="作者" width="120" />
                  <el-table-column prop="postTitle" label="文章" min-width="220" />
                  <el-table-column prop="status" label="状态" width="100" />
                  <el-table-column prop="content" label="内容" min-width="260" />
                  <el-table-column label="操作" width="180">
                    <template #default="{ row }">
                      <el-button v-if="row.status === 'PENDING'" link type="primary" @click="approvePendingComment(row.id)">通过</el-button>
                      <el-button link type="danger" @click="removeCommentRow(row.id)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </main>
          </div>
        </template>
      </section>
  </div>
</template>
