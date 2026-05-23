import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import AppLayout from '../../shared/components/AppLayout'
import { createAppointment, getDoctors } from './appointmentApi'

export default function BookAppointmentPage() {
  const navigate = useNavigate()
  const [step, setStep] = useState(1)
  const [doctors, setDoctors] = useState([])
  const [searchQuery, setSearchQuery] = useState('')
  const [specialtyFilter, setSpecialtyFilter] = useState('')
  const [loading, setLoading] = useState(false)
  const [fetchingDoctors, setFetchingDoctors] = useState(true)
  const [error, setError] = useState('')

  const specialties = [
    "General Practice",
    "Cardiology",
    "Dermatology",
    "Pediatrics",
    "Neurology",
    "Psychiatry",
    "Orthopedics",
    "Oncology",
    "Gynecology",
    "Ophthalmology"
  ]
  
  const [formData, setFormData] = useState({
    doctorId: null,
    date: '',
    time: '',
    reason: ''
  })

  useEffect(() => {
    async function fetchDocs() {
      try {
        const res = await getDoctors()
        if (res.success) {
          setDoctors(res.data || [])
        } else {
          setError(res.message || 'Failed to fetch doctors.')
        }
      } catch (err) {
        setError('Error loading doctors.')
      } finally {
        setFetchingDoctors(false)
      }
    }
    fetchDocs()
  }, [])

  const filteredDoctors = doctors.filter(doc => {
    const matchesSearch = doc.fullName.toLowerCase().includes(searchQuery.toLowerCase()) || 
                          (doc.specialty || '').toLowerCase().includes(searchQuery.toLowerCase());
    const matchesSpecialty = specialtyFilter ? doc.specialty === specialtyFilter : true;
    return matchesSearch && matchesSpecialty;
  })

  const handleNext = () => {
    if (step === 1 && !formData.doctorId) {
      setError('Please select a doctor to continue.')
      return
    }
    if (step === 2 && (!formData.date || !formData.time)) {
      setError('Please select a date and time.')
      return
    }
    setError('')
    setStep(s => s + 1)
  }

  const handleBack = () => {
    setError('')
    setStep(s => s - 1)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!formData.reason) {
      setError('Please provide a reason for the consultation.')
      return
    }
    
    setError('')
    setLoading(true)

    try {
      const datetime = `${formData.date}T${formData.time}:00`
      const res = await createAppointment({
        doctorId: formData.doctorId,
        appointmentDatetime: datetime,
        reason: formData.reason
      })

      if (res.success) {
        navigate('/my-appointments')
      } else {
        setError(res.message || 'Failed to book appointment')
      }
    } catch (err) {
      setError('An error occurred. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const getDoctor = () => doctors.find(d => d.id === formData.doctorId)

  return (
    <AppLayout>
      <div className="dash-page" style={{ maxWidth: '800px', margin: '0 auto' }}>
        
        {/* Wizard Progress Bar */}
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', marginBottom: '40px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{
              width: '30px', height: '30px', borderRadius: '50%', 
              backgroundColor: step >= 1 ? '#3b82f6' : '#e5e7eb', 
              color: step >= 1 ? 'white' : '#6b7280',
              display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold'
            }}>1</div>
            <span style={{ color: step >= 1 ? '#1f2937' : '#6b7280', fontSize: '14px', fontWeight: step >= 1 ? '500' : 'normal' }}>Select Doctor</span>
          </div>
          
          <div style={{ width: '40px', height: '2px', backgroundColor: '#e5e7eb', margin: '0 15px' }}></div>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{
              width: '30px', height: '30px', borderRadius: '50%', 
              backgroundColor: step >= 2 ? '#3b82f6' : '#e5e7eb', 
              color: step >= 2 ? 'white' : '#6b7280',
              display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold'
            }}>2</div>
            <span style={{ color: step >= 2 ? '#1f2937' : '#6b7280', fontSize: '14px', fontWeight: step >= 2 ? '500' : 'normal' }}>Choose Date & Time</span>
          </div>
          
          <div style={{ width: '40px', height: '2px', backgroundColor: '#e5e7eb', margin: '0 15px' }}></div>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{
              width: '30px', height: '30px', borderRadius: '50%', 
              backgroundColor: step >= 3 ? '#3b82f6' : '#e5e7eb', 
              color: step >= 3 ? 'white' : '#6b7280',
              display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold'
            }}>3</div>
            <span style={{ color: step >= 3 ? '#1f2937' : '#6b7280', fontSize: '14px', fontWeight: step >= 3 ? '500' : 'normal' }}>Confirm</span>
          </div>
        </div>

        <div className="dash-card">
          {error && <div className="auth-error" style={{ marginBottom: '20px' }}>{error}</div>}

          {/* STEP 1: SELECT DOCTOR */}
          {step === 1 && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', gap: '15px', flexWrap: 'wrap' }}>
                <h2 className="dash-card-title" style={{ margin: 0, flex: 1 }}>Select a Doctor</h2>
                <div style={{ display: 'flex', gap: '10px' }}>
                  <select
                    className="input"
                    value={specialtyFilter}
                    onChange={(e) => setSpecialtyFilter(e.target.value)}
                    style={{ width: '200px', margin: 0 }}
                  >
                    <option value="">All Specialties</option>
                    {specialties.map(s => (
                      <option key={s} value={s}>{s}</option>
                    ))}
                  </select>
                  <input 
                    type="text" 
                    placeholder="Search doctors..." 
                    className="search-input"
                    style={{ width: '250px' }}
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                  />
                </div>
              </div>

              {fetchingDoctors ? (
                <div style={{ textAlign: 'center', padding: '40px', color: '#6b7280' }}>Loading doctors...</div>
              ) : filteredDoctors.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '40px', color: '#6b7280' }}>No doctors found.</div>
              ) : (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                  {filteredDoctors.map(doc => (
                    <div 
                      key={doc.id}
                      onClick={() => setFormData({ ...formData, doctorId: doc.id })}
                      style={{
                        border: formData.doctorId === doc.id ? '2px solid #3b82f6' : '1px solid #e5e7eb',
                        borderRadius: '8px',
                        padding: '20px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '15px',
                        cursor: 'pointer',
                        backgroundColor: formData.doctorId === doc.id ? '#eff6ff' : 'white',
                        transition: 'all 0.2s'
                      }}
                    >
                      <div style={{
                        width: '45px', height: '45px', borderRadius: '50%',
                        backgroundColor: doc.color || '#3b82f6', color: 'white',
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                        fontSize: '16px', fontWeight: 'bold'
                      }}>
                        {doc.initials || 'DR'}
                      </div>
                      <div>
                        <div style={{ fontWeight: 'bold', color: '#1f2937', fontSize: '15px' }}>Dr. {doc.fullName}</div>
                        <div style={{ color: '#6b7280', fontSize: '13px', marginTop: '2px' }}>{doc.specialty}</div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* STEP 2: DATE & TIME */}
          {step === 2 && (
            <div>
              <h2 className="dash-card-title" style={{ marginBottom: '20px' }}>Choose Date & Time</h2>
              <div style={{ display: 'flex', gap: '20px' }}>
                <div className="field" style={{ flex: 1 }}>
                  <label htmlFor="date" className="label">Available Dates</label>
                  <input
                    type="date"
                    id="date"
                    className="input"
                    value={formData.date}
                    onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                    min={new Date().toISOString().split('T')[0]}
                  />
                </div>
                <div className="field" style={{ flex: 1 }}>
                  <label htmlFor="time" className="label">Available Times</label>
                  <input
                    type="time"
                    id="time"
                    className="input"
                    value={formData.time}
                    onChange={(e) => setFormData({ ...formData, time: e.target.value })}
                  />
                </div>
              </div>
            </div>
          )}

          {/* STEP 3: CONFIRM */}
          {step === 3 && (
            <div>
              <h2 className="dash-card-title" style={{ marginBottom: '20px' }}>Confirm Appointment</h2>
              <div style={{ backgroundColor: '#f9fafb', padding: '20px', borderRadius: '8px', marginBottom: '20px', border: '1px solid #e5e7eb' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px' }}>
                  <span style={{ color: '#6b7280' }}>Doctor:</span>
                  <span style={{ fontWeight: '500' }}>Dr. {getDoctor()?.fullName} ({getDoctor()?.specialty})</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: '#6b7280' }}>Date & Time:</span>
                  <span style={{ fontWeight: '500' }}>
                    {formData.date ? new Date(`${formData.date}T${formData.time}`).toLocaleString() : ''}
                  </span>
                </div>
              </div>

              <div className="field">
                <label htmlFor="reason" className="label">Reason for Visit <span className="required">*</span></label>
                <textarea
                  id="reason"
                  className="input textarea"
                  placeholder="Please briefly describe your symptoms or reason for consultation..."
                  value={formData.reason}
                  onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
                ></textarea>
              </div>
            </div>
          )}

          {/* Navigation Buttons */}
          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '30px', paddingTop: '20px', borderTop: '1px solid #e5e7eb' }}>
            <button 
              className="outlineBtn" 
              onClick={step === 1 ? () => navigate('/dashboard') : handleBack}
            >
              Back
            </button>
            
            {step < 3 ? (
              <button className="primaryBtn" onClick={handleNext}>
                Next &gt;
              </button>
            ) : (
              <button className="primaryBtn" onClick={handleSubmit} disabled={loading}>
                {loading ? 'Booking...' : 'Confirm Appointment'}
              </button>
            )}
          </div>
          
        </div>
      </div>
    </AppLayout>
  )
}
