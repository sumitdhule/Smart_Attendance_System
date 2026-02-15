import React, { useState, useEffect } from 'react';
import { FiEdit2, FiTrash2, FiUser, FiSearch, FiX, FiCheck } from 'react-icons/fi';
import { toast, ToastContainer } from 'react-toastify';
import api from '../../services/api';
import './UserList.css';

const UserList = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterDept, setFilterDept] = useState('');
  
  // Edit Modal State
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [editFormData, setEditFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    department: '',
    designation: '',
    isActive: true
  });
  const [isUpdating, setIsUpdating] = useState(false);

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      const response = await api.get('/users');
      if (response.data.success) {
        setUsers(response.data.data);
      }
    } catch (error) {
      toast.error('Failed to fetch users');
    } finally {
      setLoading(false);
    }
  };

  // Open Edit Modal
  const handleEdit = (user) => {
    setEditingUser(user);
    setEditFormData({
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      email: user.email || '',
      phone: user.phone || '',
      department: user.department || '',
      designation: user.designation || '',
      isActive: user.isActive !== false
    });
    setShowEditModal(true);
  };

  // Close Edit Modal
  const handleCloseModal = () => {
    setShowEditModal(false);
    setEditingUser(null);
    setEditFormData({
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      department: '',
      designation: '',
      isActive: true
    });
  };

  // Handle form input changes
  const handleEditChange = (e) => {
    const { name, value, type, checked } = e.target;
    setEditFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  // Submit Edit Form
  const handleEditSubmit = async (e) => {
    e.preventDefault();
    if (!editingUser) return;

    setIsUpdating(true);
    try {
      const response = await api.put(`/users/${editingUser.userId}`, editFormData);
      
      if (response.data.success) {
        toast.success('User updated successfully!');
        fetchUsers(); // Refresh the list
        handleCloseModal();
      }
    } catch (error) {
      const errorMsg = error.response?.data?.message || 'Failed to update user';
      toast.error(errorMsg);
    } finally {
      setIsUpdating(false);
    }
  };

  // Delete User
  const handleDelete = async (userId) => {
    if (!window.confirm('Are you sure you want to delete this user?')) return;

    try {
      await api.delete(`/users/${userId}`);
      toast.success('User deleted successfully');
      fetchUsers();
    } catch (error) {
      toast.error('Failed to delete user');
    }
  };

  // Toggle User Status
  const handleToggleStatus = async (user) => {
    try {
      const response = await api.put(`/users/${user.userId}`, {
        ...user,
        isActive: !user.isActive
      });
      
      if (response.data.success) {
        toast.success(`User ${!user.isActive ? 'activated' : 'deactivated'}`);
        fetchUsers();
      }
    } catch (error) {
      toast.error('Failed to update status');
    }
  };

  const filteredUsers = users.filter(user => {
    const matchesSearch = 
      (user.firstName?.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (user.lastName?.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (user.employeeId?.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (user.email?.toLowerCase().includes(searchTerm.toLowerCase()));
    const matchesDept = !filterDept || user.department === filterDept;
    return matchesSearch && matchesDept;
  });

  const departments = [...new Set(users.map(u => u.department).filter(Boolean))];

  if (loading) {
    return <div className="loading">Loading users...</div>;
  }

  return (
    <div className="user-list-container">
      <ToastContainer position="top-right" autoClose={3000} />
      
      <div className="page-header">
        <h1><FiUser /> User List</h1>
        <div className="filters">
          <div className="search-box">
            <FiSearch />
            <input
              type="text"
              placeholder="Search by name, ID, email..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <select
            value={filterDept}
            onChange={(e) => setFilterDept(e.target.value)}
          >
            <option value="">All Departments</option>
            {departments.map(dept => (
              <option key={dept} value={dept}>{dept}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="table-container">
        <table className="user-table">
          <thead>
            <tr>
              <th>Employee ID</th>
              <th>Name</th>
              <th>Email</th>
              <th>Department</th>
              <th>Face Registered</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredUsers.map(user => (
              <tr key={user.userId}>
                <td><span className="emp-id">{user.employeeId}</span></td>
                <td>{user.firstName} {user.lastName}</td>
                <td>{user.email}</td>
                <td>{user.department || '-'}</td>
                <td>
                  <span className={`badge ${user.isFaceRegistered ? 'success' : 'warning'}`}>
                    {user.isFaceRegistered ? 'Yes' : 'No'}
                  </span>
                </td>
                <td>
                  <span 
                    className={`badge ${user.isActive ? 'success' : 'danger'}`}
                    style={{ cursor: 'pointer' }}
                    onClick={() => handleToggleStatus(user)}
                    title="Click to toggle"
                  >
                    {user.isActive ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td>
                  <div className="action-btns">
                    <button 
                      className="btn-icon edit" 
                      title="Edit"
                      onClick={() => handleEdit(user)}
                    >
                      <FiEdit2 />
                    </button>
                    <button 
                      className="btn-icon delete" 
                      title="Delete"
                      onClick={() => handleDelete(user.userId)}
                    >
                      <FiTrash2 />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {filteredUsers.length === 0 && (
        <div className="no-data">No users found</div>
      )}

      {/* Edit Modal */}
      {showEditModal && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2><FiEdit2 /> Edit User</h2>
              <button className="close-btn" onClick={handleCloseModal}>
                <FiX />
              </button>
            </div>
            
            <form onSubmit={handleEditSubmit}>
              <div className="modal-body">
                <div className="form-row">
                  <div className="form-group">
                    <label>Employee ID</label>
                    <input
                      type="text"
                      value={editingUser?.employeeId || ''}
                      disabled
                      className="disabled-input"
                    />
                  </div>
                  <div className="form-group">
                    <label>Status</label>
                    <label className="checkbox-label">
                      <input
                        type="checkbox"
                        name="isActive"
                        checked={editFormData.isActive}
                        onChange={handleEditChange}
                      />
                      <span className="checkmark"></span>
                      Active
                    </label>
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>First Name *</label>
                    <input
                      type="text"
                      name="firstName"
                      value={editFormData.firstName}
                      onChange={handleEditChange}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Last Name *</label>
                    <input
                      type="text"
                      name="lastName"
                      value={editFormData.lastName}
                      onChange={handleEditChange}
                      required
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>Email</label>
                    <input
                      type="email"
                      name="email"
                      value={editFormData.email}
                      onChange={handleEditChange}
                    />
                  </div>
                  <div className="form-group">
                    <label>Phone</label>
                    <input
                      type="text"
                      name="phone"
                      value={editFormData.phone}
                      onChange={handleEditChange}
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>Department</label>
                    <select
                      name="department"
                      value={editFormData.department}
                      onChange={handleEditChange}
                    >
                      <option value="">Select Department</option>
                      <option value="IT">IT</option>
                      <option value="HR">HR</option>
                      <option value="Finance">Finance</option>
                      <option value="Operations">Operations</option>
                      <option value="Marketing">Marketing</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Designation</label>
                    <input
                      type="text"
                      name="designation"
                      value={editFormData.designation}
                      onChange={handleEditChange}
                    />
                  </div>
                </div>
              </div>

              <div className="modal-footer">
                <button 
                  type="button" 
                  className="btn-secondary"
                  onClick={handleCloseModal}
                >
                  Cancel
                </button>
                <button 
                  type="submit" 
                  className="btn-primary"
                  disabled={isUpdating}
                >
                  {isUpdating ? 'Updating...' : (
                    <>
                      <FiCheck /> Update User
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserList;
