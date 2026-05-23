export default function BrandHeader() {
  return (
    <div className="brand">
      <div className="brandIcon" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" className="brandSvg" xmlns="http://www.w3.org/2000/svg">
          <path
            d="M7 4v2M17 4v2M4 9h16M6.5 6h11A2.5 2.5 0 0 1 20 8.5v11A2.5 2.5 0 0 1 17.5 22h-11A2.5 2.5 0 0 1 4 19.5v-11A2.5 2.5 0 0 1 6.5 6Z"
            stroke="currentColor"
            strokeWidth="1.8"
            strokeLinecap="round"
          />
          <path
            d="M8 13h3M8 16h6"
            stroke="currentColor"
            strokeWidth="1.8"
            strokeLinecap="round"
          />
        </svg>
      </div>
      <div className="brandText">MDQueue</div>
    </div>
  )
}

