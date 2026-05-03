export default function InputField({ label, error, required, className = '', ...props }) {
  return (
    <div className="field">
      <label className="label">
        {label}
        {required ? <span className="required">*</span> : null}
      </label>
      <input
        {...props}
        required={required}
        className={`input ${error ? 'inputError' : ''} ${className}`}
      />
      {error ? <div className="errorText">{error}</div> : null}
    </div>
  )
}
