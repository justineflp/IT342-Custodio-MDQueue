import BrandHeader from '../../../shared/components/BrandHeader'

export default function AuthLayout({ children }) {
  return (
    <div className="page">
      <div className="authContainer">
        <BrandHeader />
        <div className="card">{children}</div>
        <div className="footerLinks">Terms&nbsp;&nbsp;&nbsp; Privacy&nbsp;&nbsp;&nbsp; Help</div>
      </div>
    </div>
  )
}

