FROM node:14

WORKDIR /app

ADD package.json /app

RUN npm install

ADD server.js /app

EXPOSE 8080

CMD ["node", "server.js"]
