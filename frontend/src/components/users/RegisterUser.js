import React, { useState, useRef, useCallback, useEffect } from 'react';
import Webcam from 'react-webcam';
import { FiCamera, FiUser, FiCheck, FiArrowRight } from 'react-icons/fi';
import { toast, ToastContainer } from 'react-toastify';
import api from '../../services/api';
import './RegisterUser.css';

const RegisterUser = () => {
  const webcamRef = useRef(null);
  const [step, setStep] = useState('form'); // form, capture, complete
  const [formData, setFormData] = useState({
    employeeId: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    department: '',
    designation: ''
  });
  const [userId, setUserId] = useState(null);
  const [capturedImages, setCapturedImages] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const videoConstraints = {
    width: 480,
    height: 360,
    facingMode: 'user'
  };

  const handleInputChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmitForm = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      const response = await api.post('/users', formData);
      if (response.data.success) {
        setUserId(response.data.data.userId);
        setStep('capture');
        toast.success('User created! Now capture face images.');
      }
    } catch (error) {
      const errorMsg = error.response?.data?.message || 'Failed to create user';
      toast.error(errorMsg);
    } finally {
      setIsSubmitting(false);
    }
  };

  const captureImage = useCallback(() => {
    const imageSrc = webcamRef.current?.getScreenshot();
    if (imageSrc && capturedImages.length < 5) {
      setCapturedImages(prev => [...prev, imageSrc]);
    }
  }, [webcamRef, capturedImages.length]);

  const submitFaces = async () => {
    setIsSubmitting(true);

    try {
      const formDataObj = new FormData();
      
      for (let i = 0; i < capturedImages.length; i++) {
        const response = await fetch(capturedImages[i]);
        const blob = await response.blob();
        formDataObj.append('images', blob, `face_${i}.jpg`);
      }

      const response = await api.post(`/users/${userId}/face`, formDataObj, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });

      if (response.data.success) {
        setStep('complete');
        toast.success('Face registration completed successfully!');
      }
    } catch (error) {
      toast.error('Failed to register faces. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const resetForm = () => {
    setStep('form');
    setFormData({
      employeeId: '',
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      department: '',
      designation: ''
    });
    setUserId(null);
    setCapturedImages([]);
  };

  return (
    <div className="register-user-container">
      <ToastContainer position="top-right" autoClose={3000} />

      {/* Progress Steps */}
      <div className="progress-steps">
        <div className={`step ${step === 'form' ? 'active' : ''} ${step !== 'form' ? 'completed' : ''}`}>
          <div className="step-number">1</div>
          <span>User Details</span>
        </div>
        <div className="step-line"></div>
        <div className={`step ${step === 'capture' ? 'active' : ''} ${step === 'complete' ? 'completed' : ''}`}>
          <div className="step-number">2</div>
          <span>Face Capture</span>
        </div>
        <div className="step-line"></div>
        <div className={`step ${step === 'complete' ? 'active completed' : ''}`}>
          <div className="step-number">3</div>
          <span>Complete</span>
        </div>
      </div>

      {/* Step 1: User Form */}
      {step === 'form' && (
        <div className="form-section">
          <h2><FiUser /> Register New User</h2>
          <form onSubmit={handleSubmitForm}>
            <div className="form-row">
              <div className="form-group">
                <label>Employee ID *</label>
                <input
                  type="text"
                  name="employeeId"
                  value={formData.employeeId}
                  onChange={handleInputChange}
                  required
                  placeholder="EMP001"
                />
              </div>
              <div className="form-group">
                <label>Email *</label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  required
                  placeholder="john@company.com"
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>First Name *</label>
                <input
                  type="text"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleInputChange}
                  required
                  placeholder="John"
                />
              </div>
              <div className="form-group">
                <label>Last Name *</label>
                <input
                  type="text"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleInputChange}
                  required
                  placeholder="Doe"
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Phone</label>
                <input
                  type="text"
                  name="phone"
                  value={formData.phone}
                  onChange={handleInputChange}
                  placeholder="9876543210"
                />
              </div>
              <div className="form-group">
                <label>Department *</label>
                <select
                  name="department"
                  value={formData.department}
                  onChange={handleInputChange}
                  required
                >
                  <option value="">Select Department</option>
                  <option value="IT">IT</option>
                  <option value="HR">HR</option>
                  <option value="Finance">Finance</option>
                  <option value="Operations">Operations</option>
                  <option value="Marketing">Marketing</option>
                </select>
              </div>
            </div>

            <div className="form-group">
              <label>Designation</label>
              <input
                type="text"
                name="designation"
                value={formData.designation}
                onChange={handleInputChange}
                placeholder="Software Engineer"
              />
            </div>

            <button type="submit" className="submit-btn" disabled={isSubmitting}>
              {isSubmitting ? 'Creating...' : 'Continue to Face Registration'}
              <FiArrowRight />
            </button>
          </form>
        </div>
      )}

      {/* Step 2: Face Capture */}
      {step === 'capture' && (
        <div className="capture-section">
          <h2><FiCamera /> Capture Face Images</h2>
          <p>Please capture at least 3 face images from different angles</p>

          <div className="capture-content">
            <div className="webcam-container">
              <Webcam
                ref={webcamRef}
                audio={false}
                screenshotFormat="image/jpeg"
                videoConstraints={videoConstraints}
                mirrored={true}
                className="webcam-preview"
              />
              <button
                className="capture-btn"
                onClick={captureImage}
                disabled={capturedImages.length >= 5}
              >
                <FiCamera /> Capture ({capturedImages.length}/5)
              </button>
            </div>

            <div className="captured-images">
              <h4>Captured Images</h4>
              <div className="images-grid">
                {[...Array(5)].map((_, index) => (
                  <div key={index} className={`image-slot ${capturedImages[index] ? 'filled' : ''}`}>
                    {capturedImages[index] ? (
                      <img src={capturedImages[index]} alt={`Capture ${index + 1}`} />
                    ) : (
                      <span>{index + 1}</span>
                    )}
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="action-buttons">
            <button className="secondary-btn" onClick={() => setStep('form')}>
              Back
            </button>
            <button
              className="primary-btn"
              onClick={submitFaces}
              disabled={capturedImages.length < 3 || isSubmitting}
            >
              {isSubmitting ? 'Registering...' : 'Complete Registration'}
              <FiCheck />
            </button>
          </div>
        </div>
      )}

      {/* Step 3: Complete */}
      {step === 'complete' && (
        <div className="complete-section">
          <div className="success-icon">
            <FiCheck />
          </div>
          <h2>Registration Complete!</h2>
          <p>User has been successfully registered and can now mark attendance using face recognition.</p>
          <button className="primary-btn" onClick={resetForm}>
            Register Another User
          </button>
        </div>
      )}
    </div>
  );
};

export default RegisterUser;
