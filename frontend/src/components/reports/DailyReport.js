import React, { useState, useEffect } from 'react';
import { FiCalendar, FiDownload, FiFilter } from 'react-icons/fi';
import { toast, ToastContainer } from 'react-toastify';
import api from '../../services/api';
import './DailyReport.css';

const DailyReport = () => {
  const [attendance, setAttendance] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [stats, setStats] = useState({ present: 0, late: 0, absent: 0, halfDay: 0 });

  useEffect(() => {
    fetchAttendance();
  }, [selectedDate]);

  const fetchAttendance = async () => {
    setLoading(true);
    try {
      const response = await api.get(`/attendance/daily?date=${selectedDate}`);
      if (response.data.success) {
        const data = response.data.data;
        setAttendance(data);
        
        // Calculate stats
        const present = data.filter(a => a.status === 'PRESENT').length;
        const late = data.filter(a => a.status === 'LATE').length;
        const halfDay = data.filter(a => a.status === 'HALF_DAY').length;
        setStats({ present, late, halfDay, absent: 0 });
      }
    } catch (error) {
      toast.error('Failed to fetch attendance data');
    } finally {
      setLoading(false);
    }
  };

  const exportToCSV = () => {
    const headers = ['Employee ID', 'Name', 'Department', 'Date', 'Check In', 'Check Out', 'Status'];
    const rows = attendance.map(a => [
      a.employeeId,
      a.fullName,
      a.department,
      a.attendanceDate,
      a.checkInTime || '-',
      a.checkOutTime || '-',
      a.status
    ]);

    let csvContent = headers.join(',') + '\n';
    rows.forEach(row => {
      csvContent += row.join(',') + '\n';
    });

    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `attendance_${selectedDate}.csv`;
    a.click();
  };

  return (
    <div className="daily-report-container">
      <ToastContainer position="top-right" autoClose={3000} />
      
      <div className="page-header">
        <h1><FiCalendar /> Daily Attendance Report</h1>
        <div className="header-actions">
          <input
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="date-picker"
          />
          <button className="export-btn" onClick={exportToCSV}>
            <FiDownload /> Export CSV
          </button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="stats-cards">
        <div className="stat-card present">
          <h4>Present</h4>
          <span>{stats.present}</span>
        </div>
        <div className="stat-card late">
          <h4>Late</h4>
          <span>{stats.late}</span>
        </div>
        <div className="stat-card half-day">
          <h4>Half Day</h4>
          <span>{stats.halfDay}</span>
        </div>
        <div className="stat-card total">
          <h4>Total Checked In</h4>
          <span>{attendance.length}</span>
        </div>
      </div>

      {/* Attendance Table */}
      {loading ? (
        <div className="loading">Loading attendance data...</div>
      ) : (
        <div className="table-container">
          <table className="report-table">
            <thead>
              <tr>
                <th>Employee ID</th>
                <th>Name</th>
                <th>Department</th>
                <th>Check In</th>
                <th>Check Out</th>
                <th>Working Hours</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {attendance.map((record, index) => (
                <tr key={index}>
                  <td><span className="emp-id">{record.employeeId}</span></td>
                  <td>{record.fullName}</td>
                  <td>{record.department || '-'}</td>
                  <td>{record.checkInTime ? new Date(record.checkInTime).toLocaleTimeString() : '-'}</td>
                  <td>{record.checkOutTime ? new Date(record.checkOutTime).toLocaleTimeString() : '-'}</td>
                  <td>{record.workingHours ? `${record.workingHours}h` : '-'}</td>
                  <td>
                    <span className={`status-badge ${record.status?.toLowerCase()}`}>
                      {record.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {attendance.length === 0 && !loading && (
        <div className="no-data">
          No attendance records found for {selectedDate}
        </div>
      )}
    </div>
  );
};

export default DailyReport;
