import { useState, useEffect } from "react";
import { productApi, orderApi, userApi } from "../api/apiClient";

/**
 * Dashboard Page - Overview with stats and recent activity
 */
function Dashboard() {
  const [stats, setStats] = useState({
    totalProducts: 0,
    totalOrders: 0,
    totalUsers: 0,
    pendingOrders: 0,
  });
  const [recentOrders, setRecentOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);

      // Fetch all data in parallel
      const [productsRes, ordersRes, usersRes] = await Promise.all([
        productApi.getAll(),
        orderApi.getAll(),
        userApi.getAll(),
      ]);

      const products = productsRes.data;
      const orders = ordersRes.data;
      const users = usersRes.data;

      // Calculate stats
      setStats({
        totalProducts: products.length,
        totalOrders: orders.length,
        totalUsers: users.length,
        pendingOrders: orders.filter((o) => o.status === "PENDING").length,
      });

      // Get recent orders (last 5)
      setRecentOrders(orders.slice(-5).reverse());
    } catch (err) {
      setError(
        "Failed to load dashboard data. Make sure all services are running.",
      );
      console.error("Dashboard error:", err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div>
        <header className="page-header">
          <h1 className="page-title">Dashboard</h1>
        </header>
        <div className="error-message">{error}</div>
        <button className="btn btn-primary" onClick={fetchDashboardData}>
          Retry
        </button>
      </div>
    );
  }

  return (
    <div>
      <header className="page-header">
        <h1 className="page-title">Dashboard</h1>
        <p className="page-subtitle">
          Welcome to your E-Commerce Admin Dashboard
        </p>
      </header>

      {/* Stats Grid */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon primary">📦</div>
          <div className="stat-info">
            <h3>Total Products</h3>
            <div className="stat-value">{stats.totalProducts}</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon secondary">📋</div>
          <div className="stat-info">
            <h3>Total Orders</h3>
            <div className="stat-value">{stats.totalOrders}</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon warning">⏳</div>
          <div className="stat-info">
            <h3>Pending Orders</h3>
            <div className="stat-value">{stats.pendingOrders}</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon danger">👥</div>
          <div className="stat-info">
            <h3>Total Users</h3>
            <div className="stat-value">{stats.totalUsers}</div>
          </div>
        </div>
      </div>

      {/* Recent Orders */}
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">Recent Orders</h2>
        </div>
        <div className="table-container">
          {recentOrders.length === 0 ? (
            <div className="empty-state">
              <div className="empty-state-icon">📋</div>
              <p>No orders yet</p>
            </div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Order ID</th>
                  <th>Customer</th>
                  <th>Total</th>
                  <th>Status</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {recentOrders.map((order) => (
                  <tr key={order.id}>
                    <td>#{order.id}</td>
                    <td>{order.userName || `User #${order.userId}`}</td>
                    <td>${order.totalAmount?.toFixed(2)}</td>
                    <td>
                      <span
                        className={`badge badge-${order.status?.toLowerCase()}`}
                      >
                        {order.status}
                      </span>
                    </td>
                    <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
