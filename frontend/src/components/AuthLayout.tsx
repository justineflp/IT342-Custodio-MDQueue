import type { ReactNode } from 'react'
import BrandHeader from './BrandHeader'

export default function AuthLayout({
  children,
}: {
  children: ReactNode
}) {
  return (
    <div className="min-h-full bg-slate-50">
      <div className="mx-auto flex min-h-full max-w-md flex-col px-4 py-10">
        <BrandHeader />
        <div className="mt-8 rounded-2xl bg-white p-7 shadow-sm ring-1 ring-slate-200">
          {children}
        </div>
        <div className="mt-6 text-center text-xs text-slate-500">
          Terms&nbsp;&nbsp;&nbsp; Privacy&nbsp;&nbsp;&nbsp; Help
        </div>
      </div>
    </div>
  )
}

