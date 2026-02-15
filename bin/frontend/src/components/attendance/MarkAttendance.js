import React, { useRef, useState, useCallback, useEffect } from 'react';
import Webcam from 'react-webcam';
import { FiCamera, FiCheckCircle, FiXCircle, FiRefreshCw } from 'react-icons/fi';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import api from '../../services/api';
import './MarkAttendance.css';

const MarkAttendance = () => {
  const webcamRef = useRef(null);
  const [isCapturing, setIsCapturing] = useState(false);
  const [message, setMessage] = useState('');
  const [status, setStatus] = useState('idle'); // idle, success, error
  const [lastAttendance, setLastAttendance] = useState(null);

  const videoConstraints = {
    width: 640,
    height: 480,
    facingMode: 'user'
  };

  const captureAndMark = useCallback(async () => {
    const imageSrc = webcamRef.current?.getScreenshot();
    if (!imageSrc) {
      toast.error('Could not capture image from camera');
      return;
    }

    setIsCapturing(true);
    setStatus('idle');

    try {
      // Convert base64 to blob
      const response = await fetch(imageSrc);
      const blob = await response.blob();

      const formData = new FormData();
      formData.append('image', blob, 'capture.jpg');

      const apiResponse = await api.post('/attendance/mark', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });

      if (apiResponse.data.success) {
        const attendance = apiResponse.data.data;
        setLastAttendance(attendance);
        setStatus('success');
        setMessage(`Welcome, ${attendance.fullName}! ${attendance.checkOutTime ? 'Check-out' : 'Check-in'} successful.`);
        toast.success(`Attendance marked for ${attendance.fullName}`);
      }
    } catch (error) {
      setStatus('error');
      const errorMsg = error.response?.data?.message || 'Face not recognized. Please try again.';
      setMessage(errorMsg);
      toast.error(errorMsg);
    } finally {
      setIsCapturing(false);
    }
  }, [webcamRef]);

  return (
    <div className="mark-attendance-container">
      <ToastContainer position="top-right" autoClose={3000} />
      
      <div className="attendance-header">
        <h2>Mark Attendance</h2>
        <p>Position your face in the camera and click the button below</p>
      </div>

      <div className="camera-section">
        <div className="webcam-wrapper">
          <Webcam
            ref={webcamRef}
            audio={false}
            screenshotFormat="image/jpeg"
            videoConstraints={videoConstraints}
            mirrored={true}
            className="webcam"
          />
          <div className="face-guide"></div>
        </div>

        <button
          className={`capture-btn ${isCapturing ? 'capturing' : ''}`}
          onClick={captureAndMark}
          disabled={isCapturing}
        >
          {isCapturing ? (
            <>
              <FiRefreshCw className="spin" /> Processing...
            </>
          ) : (
            <>
              <FiCamera /> Mark Attendance
            </>
          )}
        </button>
      </div>

      {message && (
        <div className={`message-box ${status}`}>
          {status === 'success' ? <FiCheckCircle /> : <FiXCircle />}
          <span>{message}</span>
        </div>
      )}

      {lastAttendance && (
        <div className="attendance-details">
          <h3>Attendance Details</h3>
          <div className="details-grid">
            <div className="detail-item">
              <label>Name</label>
              <span>{lastAttendance.fullName}</span>
            </div>
            <div className="detail-item">
              <label>Employee ID</label>
              <span>{lastAttendance.employeeId}</span>
            </div>
            <div className="detail-item">
              <label>Date</label>
              <span>{lastAttendance.attendanceDate}</span>
            </div>
            <div className="detail-item">
              <label>Check-in</label>
              <span>{lastAttendance.checkInTime || '-'}</span>
            </div>
            <div className="detail-item">
              <label>Check-out</label>
              <span>{lastAttendance.checkOutTime || '-'}</span>
            </div>
            <div className="detail-item">
              <label>Status</label>
              <span className={`status-badge ${lastAttendance.status?.toLowerCase()}`}>
                {lastAttendance.status}
              </span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MarkAttendance;
