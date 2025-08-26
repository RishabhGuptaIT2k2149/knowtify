// src/utils/colors.js
const palette = [
    { bg: 'bg-blue-100', text: 'text-blue-800', ring: 'ring-blue-200' },
    { bg: 'bg-green-100', text: 'text-green-800', ring: 'ring-green-200' },
    { bg: 'bg-purple-100', text: 'text-purple-800', ring: 'ring-purple-200' },
    { bg: 'bg-amber-100', text: 'text-amber-800', ring: 'ring-amber-200' },
    { bg: 'bg-pink-100', text: 'text-pink-800', ring: 'ring-pink-200' },
    { bg: 'bg-cyan-100', text: 'text-cyan-800', ring: 'ring-cyan-200' },
    { bg: 'bg-teal-100', text: 'text-teal-800', ring: 'ring-teal-200' },
  ]
  
  export const subjectColor = (name = '') => {
    let hash = 0
    for (let i = 0; i < name.length; i++) hash = (hash * 31 + name.charCodeAt(i)) >>> 0
    return palette[hash % palette.length]
  }
  