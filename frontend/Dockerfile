FROM node:22-alpine
WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .
# Run Vite dev server on port 80 to match docker-compose mapping (5173:80)
# We use --host 0.0.0.0 to allow external access
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0", "--port", "80"]
