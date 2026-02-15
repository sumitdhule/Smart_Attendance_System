import React from 'react';
import { BrowserRouter as Router, Routes, Route, NavLink } from 'react-router-dom';
import { FiHome, FiUserPlus, FiList, FiBarChart2, FiCamera } from 'react-icons/fi';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

// Components
import MarkAttendance from './components/attendance/MarkAttendance';
import RegisterUser from './components/users/RegisterUser';
import UserList from './components/users/UserList';
import DailyReport from './components/reports/DailyReport';

function App() {
  return (
    <Router>
      <div className="app-container">
        {/* Sidebar */}
        <nav className="sidebar">
          <div className="sidebar-header">
            <FiCamera className="logo-icon" />
            <h2>Smart Attendance</h2>
          </div>
          
          <ul className="nav-menu">
            <li>
              <NavLink to="/" className={({ isActive }) => isActive ? 'active' : ''}>
                <FiHome /> <span>Dashboard</span>
              </NavLink>
            </li>
            <li>
              <NavLink to="/mark-attendance" className={({ isActive }) => isActive ? 'active' : ''}>
                <FiCamera /> <span>Mark Attendance</span>
              </NavLink>
            </li>
            <li>
              <NavLink to="/register-user" className={({ isActive }) => isActive ? 'active' : ''}>
                <FiUserPlus /> <span>Register User</span>
              </NavLink>
            </li>
            <li>
              <NavLink to="/users" className={({ isActive }) => isActive ? 'active' : ''}>
                <FiList /> <span>User List</span>
              </NavLink>
            </li>
            <li>
              <NavLink to="/reports" className={({ isActive }) => isActive ? 'active' : ''}>
                <FiBarChart2 /> <span>Reports</span>
              </NavLink>
            </li>
          </ul>
        </nav>

        {/* Main Content */}
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/mark-attendance" element={<MarkAttendance />} />
            <Route path="/register-user" element={<RegisterUser />} />
            <Route path="/users" element={<UserList />} />
            <Route path="/reports" element={<DailyReport />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

// Dashboard Component
function Dashboard() {
  return (
    <div className="dashboard">
      <h1>Dashboard</h1>
      <div className="stats-grid">
        <div className="stat-card">
          <h3>Total Employees</h3>
          <p className="stat-number">150</p>
        </div>
        <div className="stat-card">
          <h3>Present Today</h3>
          <p className="stat-number present">142</p>
        </div>
        <div className="stat-card">
          <h3>Late Arrivals</h3>
          <p className="stat-number late">8</p>
        </div>
        <div className="stat-card">
          <h3>Absent</h3>
          <p className="stat-number absent">8</p>
        </div>
      </div>
      
      <div className="quick-actions">
        <h2>Quick Actions</h2>
        <div className="action-buttons">
          <NavLink to="/mark-attendance" className="action-btn primary">
            <FiCamera /> Mark Attendance
          </NavLink>
          <NavLink to="/register-user" className="action-btn secondary">
            <FiUserPlus /> Register New User
          </NavLink>
        </div>
      </div>
    </div>
  );
}

export default App;
