/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/main/resources/templates/**/*.html',
    './src/main/resources/static/js/**/*.js',
  ],

  // ↓↓↓ ★★★ これを追加 ★★★ ↓↓↓
  // スキャンが失敗しても、ここに書いたクラスは強制的にCSSに含める
  safelist: [
    'bg-green-500',
    'bg-red-500',
    'bg-yellow-400',
    'bg-blue-500',
    'text-white',
    'text-black',
    'animate-toast-in',  // ← アニメーション
    'animate-toast-out' // ← アニメーション
  ],
  // ↑↑↑ ★★★ ここまで ★★★ ↑↑↑

  theme: {
    extend: {
      keyframes: {
        fadeInRight: {
          '0%': { opacity: '0', transform: 'translateX(100%)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        },
        fadeOutRight: {
          '0%': { opacity: '1', transform: 'translateX(0)' },
          '100%': { opacity: '0', transform: 'translateX(100%)' },
        },
      },
      animation: {
        'toast-in': 'fadeInRight 0.5s forwards',
        'toast-out': 'fadeOutRight 0.5s forwards',
      },
    },
  },
  plugins: [],
};