/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                           API CLIENT                                      ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  Centralized API configuration using Axios                               ║
 * ║                                                                           ║
 * ║  WHY USE THIS PATTERN?                                                    ║
 * ║  - Single place for base URL configuration                               ║
 * ║  - Easy to add interceptors (auth tokens, error handling)                ║
 * ║  - Consistent API calls across all components                            ║
 * ║                                                                           ║
 * ║  In development, Vite proxies /api to localhost:8080 (API Gateway)       ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

import axios from 'axios';

// Create axios instance with default configuration
const api = axios.create({
  baseURL: '/api',  // Vite dev server proxies this to localhost:8080
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,  // 10 second timeout
});

// ═══════════════════════════════════════════════════════════════════════════
// PRODUCT API
// ═══════════════════════════════════════════════════════════════════════════

export const productApi = {
  // Get all products
  getAll: () => api.get('/products'),
  
  // Get single product by ID
  getById: (id) => api.get(`/products/${id}`),
  
  // Create new product
  create: (productData) => api.post('/products', productData),
  
  // Update product
  update: (id, productData) => api.put(`/products/${id}`, productData),
  
  // Delete product
  delete: (id) => api.delete(`/products/${id}`),
  
  // Update stock
  updateStock: (id, quantityChange) => 
    api.patch(`/products/${id}/stock`, { quantityChange }),
  
  // Search products
  search: (keyword) => api.get(`/products/search?keyword=${keyword}`),
};

// ═══════════════════════════════════════════════════════════════════════════
// ORDER API
// ═══════════════════════════════════════════════════════════════════════════

export const orderApi = {
  // Get all orders
  getAll: () => api.get('/orders'),
  
  // Get single order by ID
  getById: (id) => api.get(`/orders/${id}`),
  
  // Get orders for a user
  getByUserId: (userId) => api.get(`/orders/user/${userId}`),
  
  // Create new order
  create: (orderData) => api.post('/orders', orderData),
  
  // Update order status
  updateStatus: (id, status) => 
    api.put(`/orders/${id}/status?status=${status}`),
};

// ═══════════════════════════════════════════════════════════════════════════
// USER API
// ═══════════════════════════════════════════════════════════════════════════

export const userApi = {
  // Get all users
  getAll: () => api.get('/users'),
  
  // Get single user by ID
  getById: (id) => api.get(`/users/${id}`),
  
  // Create new user
  create: (userData) => api.post('/users', userData),
  
  // Update user
  update: (id, userData) => api.put(`/users/${id}`, userData),
  
  // Delete user
  delete: (id) => api.delete(`/users/${id}`),
};

export default api;
