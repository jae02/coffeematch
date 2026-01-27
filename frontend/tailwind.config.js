/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{vue,js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                creama: {
                    espresso: '#3E2723',  // Primary: Deep Espresso (Text, Headers)
                    creama: '#D4A373',    // Secondary: Golden Creama (Icons, Ratings)
                    latte: '#F5EBE0',    // Background: Soft Latte (Backgrounds)
                },
                gray: {
                    50: '#F8F9FA',
                    100: '#F1F3F5',
                    200: '#E9ECEF',
                    300: '#DEE2E6',
                    400: '#CED4DA',
                    500: '#ADB5BD',
                    600: '#868E96',
                    700: '#495057',
                    800: '#343A40',
                    900: '#212529',
                }
            }
        },
    },
    plugins: [],
}
