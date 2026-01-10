/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{vue,js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                daangn: {
                    50: '#FBF7F4',  // Warm off-white
                    100: '#F5EBE0', // Light beige
                    200: '#EBD5C1',
                    300: '#DDBFA2',
                    400: '#CFA883',
                    500: '#A67B5B', // Primary Wood (Warm Brown)
                    600: '#8D6345',
                    700: '#734E35',
                    800: '#5A3B28',
                    900: '#422A1C',
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
