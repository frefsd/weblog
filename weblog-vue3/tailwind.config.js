/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
    "./node_modules/flowbite/**/*.js"
  ],
  theme: {
    extend: {
      fontSize: {
        'xxs': '0.625rem',     // 10px
        'xs': '0.75rem',       // 12px
        'sm': '0.875rem',      // 14px
        'base': '1rem',        // 16px
        'lg': '1.125rem',      // 18px
        'xl': '1.25rem',       // 20px
        '2xl': '1.5rem',       // 24px
        '3xl': '1.875rem',     // 30px
        '4xl': '2.25rem',      // 36px
        '5xl': '3rem',         // 48px
        '6xl': '3.75rem',      // 60px
        '7xl': '4.5rem',       // 72px
      },
      spacing: {
        '18': '4.5rem',        // 72px
        '22': '5.5rem',        // 88px
        '26': '6.5rem',        // 104px
        '30': '7.5rem',        // 120px
      },
      maxWidth: {
        'screen-2xl': '1400px',
        'screen-3xl': '1600px',
      },
      lineHeight: {
        'relaxed': '1.75',
        'loose': '2',
      },
    },
  },
  plugins: [
    require('flowbite/plugin'),
  ],
  corePlugins: {
    // 取消 tailwindcss 的默认样式
    preflight: false
  }
}

