FROM node:16.13.2 

EXPOSE 3000

WORKDIR /web-app

COPY package*.json ./

RUN npm install

COPY src src

CMD npm run start 