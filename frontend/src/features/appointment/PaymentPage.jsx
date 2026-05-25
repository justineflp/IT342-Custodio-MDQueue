import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import AppLayout from '../../shared/components/AppLayout'
import { getAppointmentDetails, processPayment } from './appointmentApi'

export default function PaymentPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [appointment, setAppointment] = useState(null)
  const [loading, setLoading] = useState(true)
  const [processing, setProcessing] = useState(false)
  const [error, setError] = useState('')

  // Main payment option selection: 'ONLINE' or 'IN_PERSON'
  const [payOption, setPayOption] = useState('ONLINE')
  // Online payment mode selection: 'GCASH' or 'CARD'
  const [onlineMethod, setOnlineMethod] = useState('GCASH')

  // GCash checkout wizard state
  const [gcashStep, setGcashStep] = useState(1) // Step 1: Mobile No, Step 2: SMS OTP, Step 3: MPIN
  const [gcashPhone, setGcashPhone] = useState('')
  const [gcashOtp, setGcashOtp] = useState('')
  const [gcashMpin, setGcashMpin] = useState('')
  const [gcashError, setGcashError] = useState('')

  // Credit Card checkout state
  const [cardNumber, setCardNumber] = useState('')
  const [cardName, setCardName] = useState('')
  const [cardExpiry, setCardExpiry] = useState('')
  const [cardCvv, setCardCvv] = useState('')
  const [cardError, setCardError] = useState('')

  useEffect(() => {
    async function load() {
      setLoading(true)
      const res = await getAppointmentDetails(id)
      if (res.success) {
        setAppointment(res.data)
        if (res.data.status !== 'CONFIRMED') {
          // Patient can only pay for confirmed appointments (unpaid)
          navigate(`/appointments/${id}`)
        }
      } else {
        setError('Appointment details not found')
      }
      setLoading(false)
    }
    load()
  }, [id, navigate])

  // Helper text formats
  const handleGcashPhoneChange = (e) => {
    const val = e.target.value.replace(/\D/g, '')
    setGcashPhone(val.substring(0, 11))
    setGcashError('')
  }

  const handleGcashOtpChange = (e) => {
    const val = e.target.value.replace(/\D/g, '')
    setGcashOtp(val.substring(0, 6))
    setGcashError('')
  }

  const handleGcashMpinChange = (e) => {
    const val = e.target.value.replace(/\D/g, '')
    setGcashMpin(val.substring(0, 4))
    setGcashError('')
  }

  const handleCardNumberChange = (e) => {
    const val = e.target.value.replace(/\D/g, '') // remove non-digits
    const formatted = val.match(/.{1,4}/g)?.join(' ') || val
    setCardNumber(formatted.substring(0, 19)) // 16 digits + 3 spaces
    setCardError('')
  }

  const handleCardExpiryChange = (e) => {
    const val = e.target.value.replace(/\D/g, '')
    let formatted = val
    if (val.length > 2) {
      formatted = val.substring(0, 2) + '/' + val.substring(2, 4)
    }
    setCardExpiry(formatted.substring(0, 5))
    setCardError('')
  }

  const handleCardCvvChange = (e) => {
    const val = e.target.value.replace(/\D/g, '')
    setCardCvv(val.substring(0, 3))
    setCardError('')
  }

  // Verification triggers
  const triggerGcashStep1 = () => {
    if (!gcashPhone || gcashPhone.length !== 11 || !gcashPhone.startsWith('09')) {
      setGcashError('Please enter a valid 11-digit GCash number starting with 09.')
      return
    }
    setGcashStep(2)
  }

  const triggerGcashStep2 = () => {
    if (gcashOtp !== '123456') {
      setGcashError('Incorrect OTP code. Enter the sandbox code "123456" to proceed.')
      return
    }
    setGcashStep(3)
  }

  const triggerGcashStep3 = async () => {
    if (gcashMpin !== '8888') {
      setGcashError('Incorrect MPIN. Enter the sandbox MPIN "8888" to authorize payment.')
      return
    }
    await executeBackendPayment('GCASH')
  }

  const handleCardPaymentSubmit = async (e) => {
    e.preventDefault()
    const rawNumber = cardNumber.replace(/\s/g, '')
    if (rawNumber.length < 16) {
      setCardError('Please enter a complete 16-digit credit card number.')
      return
    }
    if (!cardName || cardName.trim().length < 3) {
      setCardError('Please enter the cardholder\'s full name.')
      return
    }
    if (cardExpiry.length !== 5) {
      setCardError('Please enter a valid expiry date (MM/YY).')
      return
    }
    const [month, year] = cardExpiry.split('/')
    const mNum = parseInt(month, 10)
    if (mNum < 1 || mNum > 12) {
      setCardError('Expiry month must be between 01 and 12.')
      return
    }
    if (cardCvv.length !== 3) {
      setCardError('Please enter a 3-digit security CVV code.')
      return
    }

    await executeBackendPayment('CARD')
  }

  const handlePayInPersonSubmit = async () => {
    await executeBackendPayment('PAY_IN_PERSON')
  }

  const executeBackendPayment = async (methodId) => {
    setProcessing(true)
    setError('')
    try {
      const res = await processPayment(id, methodId)
      if (res.success) {
        navigate(`/appointments/${id}`, { state: { paymentSuccess: true } })
      } else {
        setError(res.message || 'Payment processing failed. Please verify sandbox inputs and try again.')
      }
    } catch (err) {
      setError('A connection error occurred during payment processing.')
    }
    setProcessing(false)
  }

  if (loading) return <AppLayout><div className="loading-card">Preparing sandbox environment...</div></AppLayout>
  if (!appointment) return <AppLayout><div className="dash-page"><p className="auth-error">{error || 'Appointment not found.'}</p></div></AppLayout>

  const formattedDate = new Date(appointment.appointmentDatetime).toLocaleDateString(undefined, {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
  const formattedTime = new Date(appointment.appointmentDatetime).toLocaleTimeString(undefined, {
    hour: '2-digit',
    minute: '2-digit'
  })

  const amountDue = appointment.amountDue != null ? parseFloat(appointment.amountDue) : 0
  const formattedFee = `₱${amountDue.toLocaleString('en-US', { minimumFractionDigits: 2 })}`

  const getCardBrand = () => {
    if (cardNumber.startsWith('4')) return 'Visa'
    if (cardNumber.startsWith('5')) return 'Mastercard'
    return 'Credit Card'
  }

  return (
    <AppLayout>
      <div className="dash-page" style={{ maxWidth: '650px', margin: '0 auto' }}>
        <button 
          className="secondaryBtn small-btn" 
          onClick={() => navigate(`/appointments/${id}`)}
          style={{ marginBottom: '20px', display: 'inline-flex', alignItems: 'center', gap: '6px' }}
        >
          &larr; Back to Details
        </button>

        <div className="dash-card" style={{ padding: '0', overflow: 'hidden', border: '1px solid var(--border-color)', boxShadow: '0 10px 25px rgba(0,0,0,0.05)' }}>
          {/* Header Gradients */}
          <div style={{
            background: 'linear-gradient(135deg, var(--blue) 0%, #1e40af 100%)',
            padding: '30px 24px',
            color: '#ffffff',
            textAlign: 'center'
          }}>
            <span style={{ fontSize: '0.8rem', textTransform: 'uppercase', letterSpacing: '0.15em', opacity: '0.8', fontWeight: '700' }}>Secure Checkout Portal</span>
            <h2 style={{ margin: '8px 0 4px 0', fontSize: '1.7rem', color: '#ffffff', fontWeight: '800' }}>Confirm & Settle Payment</h2>
            <p style={{ margin: '0', opacity: '0.9', fontSize: '0.9rem' }}>Choose your preferred payment method below</p>
          </div>

          <div style={{ padding: '28px' }}>
            {error && (
              <div className="auth-error" style={{ marginBottom: '20px', borderRadius: '8px', padding: '12px 16px', animation: 'shake 0.3s ease' }}>
                ⚠️ {error}
              </div>
            )}

            {/* Appointment Summary Card */}
            <div style={{
              backgroundColor: '#f8fafc',
              borderRadius: '10px',
              padding: '16px 20px',
              border: '1px solid #e2e8f0',
              marginBottom: '24px'
            }}>
              <h4 style={{ margin: '0 0 10px 0', color: 'var(--text)', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em', fontWeight: '700' }}>Appointment Details</h4>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                <div>
                  <strong>Doctor:</strong>
                  <div style={{ color: 'var(--text)', marginTop: '2px', fontWeight: '600' }}>Dr. {appointment.doctorName}</div>
                </div>
                <div>
                  <strong>Specialty:</strong>
                  <div style={{ color: 'var(--text)', marginTop: '2px', fontWeight: '600' }}>{appointment.doctorSpecialty}</div>
                </div>
                <div style={{ gridColumn: 'span 2' }}>
                  <strong>Schedule:</strong>
                  <div style={{ color: 'var(--text)', marginTop: '2px', fontWeight: '600' }}>{formattedDate} at {formattedTime}</div>
                </div>
              </div>
            </div>

            {/* MAIN PAYMENT OPTIONS SELECTOR */}
            <div style={{ marginBottom: '28px' }}>
              <label style={{ display: 'block', fontWeight: '700', fontSize: '0.85rem', color: 'var(--text)', marginBottom: '12px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                How would you like to pay?
              </label>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                {/* Pay Online Box */}
                <div 
                  onClick={() => !processing && setPayOption('ONLINE')}
                  style={{
                    border: payOption === 'ONLINE' ? '2px solid var(--blue)' : '2px solid #e2e8f0',
                    backgroundColor: payOption === 'ONLINE' ? '#f0f9ff' : '#ffffff',
                    borderRadius: '12px',
                    padding: '16px',
                    cursor: 'pointer',
                    transition: 'all 0.2s ease',
                    boxShadow: payOption === 'ONLINE' ? '0 4px 12px rgba(37,99,235,0.06)' : 'none',
                    textAlign: 'center'
                  }}
                >
                  <span style={{ fontSize: '1.8rem', display: 'block', marginBottom: '8px' }}>⚡</span>
                  <strong style={{ display: 'block', fontSize: '0.95rem', color: payOption === 'ONLINE' ? 'var(--blue)' : 'var(--text)', marginBottom: '4px' }}>Pay Online</strong>
                  <span style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', lineHeight: '1.3', display: 'block' }}>GCash or Credit Card instant checkout</span>
                </div>

                {/* Pay in Person Box */}
                <div 
                  onClick={() => !processing && setPayOption('IN_PERSON')}
                  style={{
                    border: payOption === 'IN_PERSON' ? '2px solid var(--blue)' : '2px solid #e2e8f0',
                    backgroundColor: payOption === 'IN_PERSON' ? '#f0f9ff' : '#ffffff',
                    borderRadius: '12px',
                    padding: '16px',
                    cursor: 'pointer',
                    transition: 'all 0.2s ease',
                    boxShadow: payOption === 'IN_PERSON' ? '0 4px 12px rgba(37,99,235,0.06)' : 'none',
                    textAlign: 'center'
                  }}
                >
                  <span style={{ fontSize: '1.8rem', display: 'block', marginBottom: '8px' }}>🏥</span>
                  <strong style={{ display: 'block', fontSize: '0.95rem', color: payOption === 'IN_PERSON' ? 'var(--blue)' : 'var(--text)', marginBottom: '4px' }}>Pay in Person</strong>
                  <span style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', lineHeight: '1.3', display: 'block' }}>Pay at the clinic on consultation date</span>
                </div>
              </div>
            </div>

            {/* PAY ONLINE INTERACTIVE SHEETS */}
            {payOption === 'ONLINE' && (
              <div style={{ borderTop: '1px solid #e2e8f0', paddingTop: '24px', animation: 'fadeIn 0.25s ease-out' }}>
                <label style={{ display: 'block', fontWeight: '700', fontSize: '0.85rem', color: 'var(--text)', marginBottom: '12px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  Select Online Method
                </label>
                
                {/* ONLINE SUB-TOGGLES */}
                <div style={{ display: 'flex', gap: '12px', marginBottom: '24px' }}>
                  <button
                    type="button"
                    onClick={() => setOnlineMethod('GCASH')}
                    disabled={processing}
                    style={{
                      flex: 1,
                      height: '44px',
                      borderRadius: '8px',
                      border: onlineMethod === 'GCASH' ? '2px solid #005afb' : '1px solid #cbd5e1',
                      backgroundColor: onlineMethod === 'GCASH' ? '#005afb' : '#ffffff',
                      color: onlineMethod === 'GCASH' ? '#ffffff' : '#475569',
                      fontWeight: '700',
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '8px',
                      transition: 'all 0.2s ease'
                    }}
                  >
                    🔵 GCash Checkout
                  </button>
                  <button
                    type="button"
                    onClick={() => setOnlineMethod('CARD')}
                    disabled={processing}
                    style={{
                      flex: 1,
                      height: '44px',
                      borderRadius: '8px',
                      border: onlineMethod === 'CARD' ? '2px solid #0f172a' : '1px solid #cbd5e1',
                      backgroundColor: onlineMethod === 'CARD' ? '#0f172a' : '#ffffff',
                      color: onlineMethod === 'CARD' ? '#ffffff' : '#475569',
                      fontWeight: '700',
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '8px',
                      transition: 'all 0.2s ease'
                    }}
                  >
                    💳 Credit/Debit Card
                  </button>
                </div>

                {/* GCASH WIZARD */}
                {onlineMethod === 'GCASH' && (
                  <div style={{
                    border: '1px solid #bfdbfe',
                    borderRadius: '12px',
                    overflow: 'hidden',
                    backgroundColor: '#ffffff',
                    boxShadow: '0 4px 15px rgba(0,90,251,0.05)',
                    marginBottom: '24px',
                    animation: 'fadeIn 0.2s ease'
                  }}>
                    {/* GCash Blue Header */}
                    <div style={{
                      backgroundColor: '#005afb',
                      padding: '16px 20px',
                      color: '#ffffff',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <span style={{ fontSize: '1.2rem', fontWeight: '900', fontStyle: 'italic', letterSpacing: '-0.5px' }}>GCash</span>
                        <span style={{ fontSize: '0.75rem', opacity: '0.85', border: '1px solid rgba(255,255,255,0.4)', borderRadius: '4px', padding: '1px 5px', textTransform: 'uppercase', letterSpacing: '0.5px', fontWeight: '700' }}>Sandbox</span>
                      </div>
                      <span style={{ fontSize: '0.8rem', fontWeight: '600' }}>Step {gcashStep} of 3</span>
                    </div>

                    <div style={{ padding: '24px' }}>
                      {gcashError && (
                        <div style={{ color: '#dc2626', backgroundColor: '#fef2f2', border: '1px solid #fecaca', borderRadius: '8px', padding: '10px 14px', fontSize: '0.85rem', marginBottom: '16px', fontWeight: '500' }}>
                          ⚠️ {gcashError}
                        </div>
                      )}

                      {/* STEP 1: Enter GCash Mobile */}
                      {gcashStep === 1 && (
                        <div>
                          <p style={{ margin: '0 0 16px 0', fontSize: '0.88rem', color: '#475569', lineHeight: '1.4' }}>
                            Enter your 11-digit GCash mobile number to fetch your sandbox account and authorize the charge of <strong>{formattedFee}</strong>.
                          </p>
                          <div style={{ marginBottom: '20px' }}>
                            <label htmlFor="gcashPhone" style={{ display: 'block', fontSize: '0.8rem', color: '#64748b', fontWeight: '700', marginBottom: '6px', textTransform: 'uppercase' }}>Mobile Number</label>
                            <input
                              type="tel"
                              id="gcashPhone"
                              placeholder="09XXXXXXXXX"
                              value={gcashPhone}
                              onChange={handleGcashPhoneChange}
                              style={{
                                width: '100%',
                                height: '46px',
                                border: '1px solid #cbd5e1',
                                borderRadius: '8px',
                                padding: '0 14px',
                                fontSize: '1.05rem',
                                color: '#1e293b',
                                outline: 'none',
                                letterSpacing: '0.05em'
                              }}
                            />
                            <span style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '6px', display: 'block' }}>Format: e.g. 09171234567</span>
                          </div>
                          <button
                            type="button"
                            className="primaryBtn full-width"
                            onClick={triggerGcashStep1}
                            style={{ height: '46px', backgroundColor: '#005afb', border: 'none', color: '#ffffff', fontWeight: '700' }}
                          >
                            Next
                          </button>
                        </div>
                      )}

                      {/* STEP 2: Verify OTP */}
                      {gcashStep === 2 && (
                        <div>
                          <p style={{ margin: '0 0 16px 0', fontSize: '0.88rem', color: '#475569', lineHeight: '1.4' }}>
                            An authentication code was simulated to <strong>{gcashPhone.substring(0,4)}***{gcashPhone.substring(8)}</strong>. Enter the sandbox mock verification code below.
                          </p>
                          <div style={{
                            backgroundColor: '#eff6ff',
                            border: '1px solid #bfdbfe',
                            borderRadius: '8px',
                            padding: '10px 14px',
                            fontSize: '0.8rem',
                            color: '#1e3a8a',
                            marginBottom: '16px',
                            fontWeight: '600'
                          }}>
                            ℹ️ Sandbox Verification Code: <span style={{ fontFamily: 'monospace', fontSize: '0.95rem', color: '#2563eb' }}>123456</span>
                          </div>
                          
                          <div style={{ marginBottom: '20px' }}>
                            <label htmlFor="gcashOtp" style={{ display: 'block', fontSize: '0.8rem', color: '#64748b', fontWeight: '700', marginBottom: '6px', textTransform: 'uppercase' }}>6-Digit Code</label>
                            <input
                              type="tel"
                              id="gcashOtp"
                              maxLength="6"
                              placeholder="XXXXXX"
                              value={gcashOtp}
                              onChange={handleGcashOtpChange}
                              style={{
                                width: '100%',
                                height: '46px',
                                border: '1px solid #cbd5e1',
                                borderRadius: '8px',
                                padding: '0 14px',
                                fontSize: '1.1rem',
                                color: '#1e293b',
                                outline: 'none',
                                letterSpacing: '0.25em',
                                textAlign: 'center',
                                fontWeight: '700'
                              }}
                            />
                          </div>

                          <div style={{ display: 'flex', gap: '12px' }}>
                            <button
                              type="button"
                              className="outlineBtn"
                              onClick={() => { setGcashStep(1); setGcashError('') }}
                              style={{ flex: 1, height: '44px', fontSize: '0.9rem' }}
                            >
                              Back
                            </button>
                            <button
                              type="button"
                              className="primaryBtn"
                              onClick={triggerGcashStep2}
                              style={{ flex: 2, height: '44px', backgroundColor: '#005afb', border: 'none', color: '#ffffff', fontWeight: '700' }}
                            >
                              Verify OTP
                            </button>
                          </div>
                        </div>
                      )}

                      {/* STEP 3: Enter MPIN */}
                      {gcashStep === 3 && (
                        <div>
                          <p style={{ margin: '0 0 16px 0', fontSize: '0.88rem', color: '#475569', lineHeight: '1.4' }}>
                            Enter your 4-digit GCash security MPIN to authorize the sandbox charge of <strong>{formattedFee}</strong>.
                          </p>
                          <div style={{
                            backgroundColor: '#eff6ff',
                            border: '1px solid #bfdbfe',
                            borderRadius: '8px',
                            padding: '10px 14px',
                            fontSize: '0.8rem',
                            color: '#1e3a8a',
                            marginBottom: '16px',
                            fontWeight: '600'
                          }}>
                            ℹ️ Sandbox MPIN: <span style={{ fontFamily: 'monospace', fontSize: '0.95rem', color: '#2563eb' }}>8888</span>
                          </div>
                          
                          <div style={{ marginBottom: '20px' }}>
                            <label htmlFor="gcashMpin" style={{ display: 'block', fontSize: '0.8rem', color: '#64748b', fontWeight: '700', marginBottom: '6px', textTransform: 'uppercase' }}>4-Digit MPIN</label>
                            <input
                              type="password"
                              id="gcashMpin"
                              maxLength="4"
                              placeholder="••••"
                              value={gcashMpin}
                              onChange={handleGcashMpinChange}
                              style={{
                                width: '100%',
                                height: '46px',
                                border: '1px solid #cbd5e1',
                                borderRadius: '8px',
                                padding: '0 14px',
                                fontSize: '1.2rem',
                                color: '#1e293b',
                                outline: 'none',
                                letterSpacing: '0.4em',
                                textAlign: 'center',
                                fontWeight: '700'
                              }}
                            />
                          </div>

                          <div style={{ display: 'flex', gap: '12px' }}>
                            <button
                              type="button"
                              className="outlineBtn"
                              onClick={() => { setGcashStep(2); setGcashError('') }}
                              disabled={processing}
                              style={{ flex: 1, height: '44px', fontSize: '0.9rem' }}
                            >
                              Back
                            </button>
                            <button
                              type="button"
                              className="primaryBtn"
                              onClick={triggerGcashStep3}
                              disabled={processing}
                              style={{ flex: 2, height: '44px', backgroundColor: '#005afb', border: 'none', color: '#ffffff', fontWeight: '700', display: 'flex', justifyContent: 'center', alignItems: 'center' }}
                            >
                              {processing ? 'Processing Pay...' : `Pay ${formattedFee}`}
                            </button>
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                )}

                {/* CREDIT CARD CHECKOUT */}
                {onlineMethod === 'CARD' && (
                  <div>
                    {/* Live-Updating Visual Card Credit Card Mockup */}
                    <div style={{
                      background: cardNumber.startsWith('5') 
                        ? 'linear-gradient(135deg, #0f172a 0%, #334155 100%)' 
                        : 'linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%)',
                      borderRadius: '16px',
                      padding: '24px',
                      color: '#ffffff',
                      position: 'relative',
                      boxShadow: '0 8px 24px rgba(30,58,138,0.18)',
                      marginBottom: '24px',
                      height: '180px',
                      display: 'flex',
                      flexDirection: 'column',
                      justifyContent: 'space-between',
                      transition: 'all 0.3s ease',
                      textShadow: '0 1px 2px rgba(0,0,0,0.3)'
                    }}>
                      {/* Top Row: Chip and Card Brand */}
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        {/* Mock Gold Chip */}
                        <div style={{ 
                          width: '42px', 
                          height: '32px', 
                          background: 'linear-gradient(135deg, #fbbf24, #d97706)', 
                          borderRadius: '6px',
                          position: 'relative',
                          overflow: 'hidden',
                          boxShadow: 'inset 0 1px 2px rgba(255,255,255,0.3)'
                        }}>
                          {/* Chip Grid Lines */}
                          <div style={{ position: 'absolute', top: 0, bottom: 0, left: '33%', width: '1px', backgroundColor: 'rgba(0,0,0,0.15)' }}></div>
                          <div style={{ position: 'absolute', top: 0, bottom: 0, left: '66%', width: '1px', backgroundColor: 'rgba(0,0,0,0.15)' }}></div>
                          <div style={{ position: 'absolute', left: 0, right: 0, top: '50%', height: '1px', backgroundColor: 'rgba(0,0,0,0.15)' }}></div>
                        </div>
                        
                        {/* Card Brand Logo */}
                        <span style={{ fontSize: '1.25rem', fontWeight: '800', fontStyle: 'italic', letterSpacing: '0.05em', color: '#ffffff' }}>
                          {getCardBrand()}
                        </span>
                      </div>

                      {/* Card Number Display */}
                      <div style={{ fontSize: '1.35rem', letterSpacing: '0.15em', fontFamily: 'monospace', margin: '20px 0 10px 0' }}>
                        {cardNumber || '•••• •••• •••• ••••'}
                      </div>

                      {/* Card Footer */}
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                        <div>
                          <div style={{ opacity: 0.6, fontSize: '0.62rem', textTransform: 'uppercase', marginBottom: '2px', letterSpacing: '0.05em' }}>Cardholder</div>
                          <div style={{ fontWeight: '600', letterSpacing: '0.05em', textTransform: 'uppercase', fontSize: '0.85rem' }}>
                            {cardName || 'YOUR FULL NAME'}
                          </div>
                        </div>
                        <div style={{ textAlign: 'right' }}>
                          <div style={{ opacity: 0.6, fontSize: '0.62rem', textTransform: 'uppercase', marginBottom: '2px', letterSpacing: '0.05em' }}>Expires</div>
                          <div style={{ fontWeight: '600', fontFamily: 'monospace', fontSize: '0.85rem' }}>
                            {cardExpiry || 'MM/YY'}
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* CREDIT CARD INPUTS FORM */}
                    <form onSubmit={handleCardPaymentSubmit} style={{
                      backgroundColor: '#ffffff',
                      border: '1px solid #cbd5e1',
                      borderRadius: '12px',
                      padding: '24px',
                      boxShadow: '0 2px 8px rgba(0,0,0,0.02)',
                      marginBottom: '24px'
                    }}>
                      {cardError && (
                        <div style={{ color: '#dc2626', backgroundColor: '#fef2f2', border: '1px solid #fecaca', borderRadius: '8px', padding: '10px 14px', fontSize: '0.85rem', marginBottom: '16px', fontWeight: '500' }}>
                          ⚠️ {cardError}
                        </div>
                      )}

                      <div style={{
                        backgroundColor: '#eff6ff',
                        border: '1px solid #bfdbfe',
                        borderRadius: '8px',
                        padding: '10px 14px',
                        fontSize: '0.78rem',
                        color: '#1e3a8a',
                        marginBottom: '16px',
                        fontWeight: '500',
                        lineHeight: '1.4'
                      }}>
                        <strong>ℹ️ Sandbox Test Cards (Success):</strong><br />
                        • Visa: <span style={{ fontFamily: 'monospace', fontWeight: '700' }}>4242 4242 4242 4242</span><br />
                        • Mastercard: <span style={{ fontFamily: 'monospace', fontWeight: '700' }}>5555 5555 5555 5555</span>
                      </div>

                      {/* Card Number Input */}
                      <div style={{ marginBottom: '16px' }}>
                        <label htmlFor="cardNumber" style={{ display: 'block', fontSize: '0.78rem', color: '#475569', fontWeight: '700', marginBottom: '6px', textTransform: 'uppercase' }}>Card Number</label>
                        <input
                          type="tel"
                          id="cardNumber"
                          placeholder="4242 4242 4242 4242"
                          value={cardNumber}
                          onChange={handleCardNumberChange}
                          disabled={processing}
                          style={{
                            width: '100%',
                            height: '42px',
                            border: '1px solid #cbd5e1',
                            borderRadius: '8px',
                            padding: '0 12px',
                            fontSize: '0.98rem',
                            color: '#1e293b',
                            outline: 'none',
                            letterSpacing: '0.05em'
                          }}
                        />
                      </div>

                      {/* Cardholder Name Input */}
                      <div style={{ marginBottom: '16px' }}>
                        <label htmlFor="cardName" style={{ display: 'block', fontSize: '0.78rem', color: '#475569', fontWeight: '700', marginBottom: '6px', textTransform: 'uppercase' }}>Cardholder Name</label>
                        <input
                          type="text"
                          id="cardName"
                          placeholder="e.g. John Doe"
                          value={cardName}
                          onChange={(e) => { setCardName(e.target.value); setCardError('') }}
                          disabled={processing}
                          style={{
                            width: '100%',
                            height: '42px',
                            border: '1px solid #cbd5e1',
                            borderRadius: '8px',
                            padding: '0 12px',
                            fontSize: '0.98rem',
                            color: '#1e293b',
                            outline: 'none'
                          }}
                        />
                      </div>

                      {/* Expiry & CVV Layout */}
                      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '24px' }}>
                        <div>
                          <label htmlFor="cardExpiry" style={{ display: 'block', fontSize: '0.78rem', color: '#475569', fontWeight: '700', marginBottom: '6px', textTransform: 'uppercase' }}>Expiration Date</label>
                          <input
                            type="tel"
                            id="cardExpiry"
                            placeholder="MM/YY"
                            value={cardExpiry}
                            onChange={handleCardExpiryChange}
                            disabled={processing}
                            style={{
                              width: '100%',
                              height: '42px',
                              border: '1px solid #cbd5e1',
                              borderRadius: '8px',
                              padding: '0 12px',
                              fontSize: '0.98rem',
                              color: '#1e293b',
                              outline: 'none',
                              textAlign: 'center'
                            }}
                          />
                        </div>
                        <div>
                          <label htmlFor="cardCvv" style={{ display: 'block', fontSize: '0.78rem', color: '#475569', fontWeight: '700', marginBottom: '6px', textTransform: 'uppercase' }}>CVV Code</label>
                          <input
                            type="password"
                            id="cardCvv"
                            placeholder="123"
                            maxLength="3"
                            value={cardCvv}
                            onChange={handleCardCvvChange}
                            disabled={processing}
                            style={{
                              width: '100%',
                              height: '42px',
                              border: '1px solid #cbd5e1',
                              borderRadius: '8px',
                              padding: '0 12px',
                              fontSize: '0.98rem',
                              color: '#1e293b',
                              outline: 'none',
                              textAlign: 'center',
                              letterSpacing: '0.15em'
                            }}
                          />
                        </div>
                      </div>

                      <button
                        type="submit"
                        className="primaryBtn full-width"
                        disabled={processing}
                        style={{ height: '46px', backgroundColor: '#0f172a', border: 'none', color: '#ffffff', fontWeight: '700', display: 'flex', justifyContent: 'center', alignItems: 'center' }}
                      >
                        {processing ? 'Processing Card...' : `Pay ${formattedFee} with Card`}
                      </button>
                    </form>
                  </div>
                )}
              </div>
            )}

            {/* PAY IN PERSON ACTION CONTAINER */}
            {payOption === 'IN_PERSON' && (
              <div style={{
                borderTop: '1px solid #e2e8f0',
                paddingTop: '24px',
                animation: 'fadeIn 0.25s ease-out'
              }}>
                <div style={{
                  backgroundColor: '#f8fafc',
                  border: '1px solid #cbd5e1',
                  borderRadius: '12px',
                  padding: '24px',
                  marginBottom: '24px'
                }}>
                  <h3 style={{ margin: '0 0 12px 0', fontSize: '1rem', fontWeight: '700', color: 'var(--text)' }}>
                    🏥 Pay at Reception Counter
                  </h3>
                  
                  <p style={{ fontSize: '0.88rem', color: '#475569', lineHeight: '1.5', margin: '0 0 16px 0' }}>
                    By choosing this option, you select to pay your consultation amount of <strong>{formattedFee}</strong> directly at the clinic check-in counter on your appointment date.
                  </p>

                  <ul style={{ paddingLeft: '20px', margin: '0 0 20px 0', fontSize: '0.82rem', color: '#64748b', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                    <li>Your appointment booking status remains <strong>CONFIRMED</strong> immediately.</li>
                    <li>Settle the amount using Cash, GCash QR, or Card at the clinic front desk before check-in.</li>
                    <li>Please arrive 15 minutes before your scheduled appointment time to clear details.</li>
                  </ul>

                  <button
                    type="button"
                    className="primaryBtn full-width"
                    onClick={handlePayInPersonSubmit}
                    disabled={processing}
                    style={{
                      height: '48px',
                      fontSize: '0.95rem',
                      fontWeight: '700',
                      backgroundColor: 'var(--blue)',
                      border: 'none',
                      color: '#ffffff',
                      display: 'flex',
                      justifyContent: 'center',
                      alignItems: 'center'
                    }}
                  >
                    {processing ? 'Registering Booking...' : `Confirm Pay in Person (${formattedFee})`}
                  </button>
                </div>
              </div>
            )}

            {/* ORDER SUMMARY */}
            <div style={{ borderTop: '1px solid #e2e8f0', paddingTop: '20px', marginTop: '20px' }}>
              <h4 style={{ margin: '0 0 12px 0', color: 'var(--text)', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em', fontWeight: '700' }}>Order Summary</h4>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '0.95rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-secondary)' }}>
                  <span>Consultation Fee ({appointment.doctorSpecialty})</span>
                  <span>{formattedFee}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-secondary)' }}>
                  <span>Sandbox Service Charge</span>
                  <span>₱0.00</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: '700', fontSize: '1.25rem', color: 'var(--text)', borderTop: '1px dashed #e2e8f0', paddingTop: '12px', marginTop: '4px' }}>
                  <span>Total Due</span>
                  <span style={{ color: 'var(--blue)' }}>{formattedFee}</span>
                </div>
              </div>
            </div>

          </div>
        </div>
      </div>
    </AppLayout>
  )
}
