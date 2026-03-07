import type { InputHTMLAttributes } from 'react'

type Props = {
  label: string
  error?: string
} & InputHTMLAttributes<HTMLInputElement>

export default function InputField({ label, error, ...props }: Props) {
  return (
    <div className="space-y-1.5">
      <label className="text-sm font-medium text-slate-700">
        {label}
        {props.required ? <span className="text-red-500"> *</span> : null}
      </label>
      <input
        {...props}
        className={[
          'w-full rounded-xl border bg-white px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400',
          'outline-none ring-0 transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100',
          error ? 'border-red-300 focus:border-red-400 focus:ring-red-100' : 'border-slate-200',
          props.className ?? '',
        ].join(' ')}
      />
      {error ? <div className="text-xs text-red-600">{error}</div> : null}
    </div>
  )
}

