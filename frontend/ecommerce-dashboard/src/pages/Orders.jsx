import { useState, useEffect } from "react";
import { orderApi, userApi, productApi } from "../api/apiClient";

/**
 * Orders Page - View and manage orders
 *
 * This page demonstrates how the frontend interacts with the Order Service,
 * which in turn calls Product and User services behind the scenes
 */
function Orders() {
  const [orders, setOrders] = useState([]);
  const [users, setUsers] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    userId: "",
    shippingAddress: "",
    items: [{ productId: "", quantity: 1 }],
  });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [ordersRes, usersRes, productsRes] = await Promise.all([
        orderApi.getAll(),
        userApi.getAll(),
        productApi.getAll(),
      ]);
      setOrders(ordersRes.data);
      setUsers(usersRes.data);
      setProducts(productsRes.data);
      setError(null);
    } catch (err) {
      setError("Failed to load data. Make sure all services are running.");
      console.error("Error:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleItemChange = (index, field, value) => {
    setFormData((prev) => {
      const newItems = [...prev.items];
      newItems[index] = { ...newItems[index], [field]: value };
      return { ...prev, items: newItems };
    });
  };

  const addItem = () => {
    setFormData((prev) => ({
      ...prev,
      items: [...prev.items, { productId: "", quantity: 1 }],
    }));
  };

  const removeItem = (index) => {
    if (formData.items.length > 1) {
      setFormData((prev) => ({
        ...prev,
        items: prev.items.filter((_, i) => i !== index),
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const orderData = {
        userId: parseInt(formData.userId),
        shippingAddress: formData.shippingAddress,
        items: formData.items.map((item) => ({
          productId: parseInt(item.productId),
          quantity: parseInt(item.quantity),
        })),
      };

      await orderApi.create(orderData);
      setShowModal(false);
      setFormData({
        userId: "",
        shippingAddress: "",
        items: [{ productId: "", quantity: 1 }],
      });
      fetchData();
    } catch (err) {
      console.error("Error creating order:", err);
      alert(
        "Failed to create order: " +
          (err.response?.data?.message || err.message),
      );
    }
  };

  const updateStatus = async (orderId, newStatus) => {
    try {
      await orderApi.updateStatus(orderId, newStatus);
      fetchData();
    } catch (err) {
      console.error("Error updating status:", err);
      alert("Failed to update status");
    }
  };

  const getStatusOptions = (currentStatus) => {
    const allStatuses = [
      "PENDING",
      "CONFIRMED",
      "SHIPPED",
      "DELIVERED",
      "CANCELLED",
    ];
    return allStatuses.filter((s) => s !== currentStatus);
  };

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
      </div>
    );
  }

  return (
    <div>
      <header className="page-header">
        <h1 className="page-title">Orders</h1>
        <p className="page-subtitle">Manage customer orders</p>
      </header>

      {error && <div className="error-message">{error}</div>}

      <div className="card">
        <div className="card-header">
          <h2 className="card-title">All Orders ({orders.length})</h2>
          <button
            className="btn btn-primary"
            onClick={() => setShowModal(true)}
            disabled={users.length === 0 || products.length === 0}
          >
            + Create Order
          </button>
        </div>

        <div className="table-container">
          {orders.length === 0 ? (
            <div className="empty-state">
              <div className="empty-state-icon">📋</div>
              <p>No orders yet. Create your first order!</p>
            </div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Order ID</th>
                  <th>Customer</th>
                  <th>Items</th>
                  <th>Total</th>
                  <th>Status</th>
                  <th>Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => (
                  <tr key={order.id}>
                    <td>#{order.id}</td>
                    <td>
                      <strong>
                        {order.userName || `User #${order.userId}`}
                      </strong>
                      {order.userEmail && (
                        <small
                          style={{
                            display: "block",
                            color: "var(--text-muted)",
                          }}
                        >
                          {order.userEmail}
                        </small>
                      )}
                    </td>
                    <td>
                      {order.items?.map((item, i) => (
                        <div key={i} style={{ fontSize: "0.875rem" }}>
                          {item.productName} × {item.quantity}
                        </div>
                      ))}
                    </td>
                    <td>
                      <strong>${order.totalAmount?.toFixed(2)}</strong>
                    </td>
                    <td>
                      <span
                        className={`badge badge-${order.status?.toLowerCase()}`}
                      >
                        {order.status}
                      </span>
                    </td>
                    <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                    <td>
                      {order.status !== "DELIVERED" &&
                        order.status !== "CANCELLED" && (
                          <select
                            className="form-select"
                            style={{
                              width: "auto",
                              padding: "0.25rem 0.5rem",
                              fontSize: "0.8rem",
                            }}
                            value=""
                            onChange={(e) =>
                              updateStatus(order.id, e.target.value)
                            }
                          >
                            <option value="" disabled>
                              Change status
                            </option>
                            {getStatusOptions(order.status).map((status) => (
                              <option key={status} value={status}>
                                {status}
                              </option>
                            ))}
                          </select>
                        )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Create Order Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Create Order</h3>
              <button
                className="modal-close"
                onClick={() => setShowModal(false)}
              >
                ×
              </button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label">Customer *</label>
                  <select
                    className="form-select"
                    name="userId"
                    value={formData.userId}
                    onChange={handleChange}
                    required
                  >
                    <option value="">Select a customer</option>
                    {users.map((user) => (
                      <option key={user.id} value={user.id}>
                        {user.firstName} {user.lastName} ({user.email})
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Shipping Address</label>
                  <input
                    className="form-input"
                    type="text"
                    name="shippingAddress"
                    value={formData.shippingAddress}
                    onChange={handleChange}
                    placeholder="Enter shipping address"
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Order Items *</label>
                  {formData.items.map((item, index) => (
                    <div
                      key={index}
                      style={{
                        display: "flex",
                        gap: "0.5rem",
                        marginBottom: "0.5rem",
                      }}
                    >
                      <select
                        className="form-select"
                        style={{ flex: 2 }}
                        value={item.productId}
                        onChange={(e) =>
                          handleItemChange(index, "productId", e.target.value)
                        }
                        required
                      >
                        <option value="">Select product</option>
                        {products
                          .filter((p) => p.stockQuantity > 0)
                          .map((product) => (
                            <option key={product.id} value={product.id}>
                              {product.name} - ${product.price} (Stock:{" "}
                              {product.stockQuantity})
                            </option>
                          ))}
                      </select>
                      <input
                        className="form-input"
                        style={{ flex: 1 }}
                        type="number"
                        min="1"
                        value={item.quantity}
                        onChange={(e) =>
                          handleItemChange(index, "quantity", e.target.value)
                        }
                        required
                      />
                      {formData.items.length > 1 && (
                        <button
                          type="button"
                          className="btn btn-danger btn-sm"
                          onClick={() => removeItem(index)}
                        >
                          ×
                        </button>
                      )}
                    </div>
                  ))}
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    onClick={addItem}
                  >
                    + Add Item
                  </button>
                </div>
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowModal(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  Create Order
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Orders;
