# Complete Full-Stack Deployment Guide

This project is configured for cloud deployment:
- **Frontend (React + Vite)**: Deployed on **Netlify**
- **Backend (Java Spring Boot 3 + Maven)**: Deployed as a Container on **Render**
- **Database (PostgreSQL)**: Hosted on **Supabase**

---

## 1. Supabase (Database Setup)

1. Create a project on [Supabase](https://supabase.com).
2. Go to **SQL Editor** -> Click **New Query**.
3. Paste the contents of `supabase_schema.sql` and click **Run**.
4. Go to **Project Settings** -> **Database** -> **Connection string** -> Select **URI** (or JDBC format).
   Your JDBC URL format will be:
   `jdbc:postgresql://db.[YOUR-PROJECT-REF].supabase.co:5432/postgres?sslmode=require`
   Keep your database password handy.

---

## 2. Render (Backend Deployment)

1. Push your repository to GitHub.
2. Sign in to [Render](https://render.com) -> Click **New** -> **Web Service**.
3. Connect your GitHub repository.
4. Select **Docker** as Runtime:
   - **Dockerfile Path**: `./Backend/Dockerfile`
   - **Root Directory**: Leave blank or set to `./`
5. Under **Environment Variables**, add:
   - `DATABASE_URL`: `jdbc:postgresql://db.[YOUR-PROJECT-REF].supabase.co:5432/postgres?sslmode=require`
   - `DATABASE_USERNAME`: `postgres`
   - `DATABASE_PASSWORD`: `your-supabase-db-password`
   - `JWT_SECRET`: `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970`
   - `CORS_ALLOWED_ORIGINS`: `http://localhost:5173,https://*.netlify.app`
6. Click **Create Web Service**.
   Render will build the container using Maven and launch it. Copy your Render backend URL (e.g. `https://moneymanager-api.onrender.com`).

---

## 3. Netlify (Frontend Deployment)

The file `netlify.toml` in the root directory is already configured:
```toml
[build]
  base = "Frontend"
  publish = "dist"
  command = "npm run build"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200
```

1. Log in to [Netlify](https://www.netlify.com).
2. Click **Add new site** -> **Import an existing project** from GitHub.
3. Select your repository.
4. In **Build Settings**:
   - Base directory: `Frontend`
   - Build command: `npm run build`
   - Publish directory: `Frontend/dist`
5. In **Environment variables**:
   - `VITE_API_BASE_URL`: `https://moneymanager-api.onrender.com/api` (Replace with your Render URL)
6. Click **Deploy Site**.

