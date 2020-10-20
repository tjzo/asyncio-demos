FROM golang:1.15.3-alpine as build
WORKDIR /app
ADD go.mod /app
ADD go.sum /app
ADD main.go /app
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o app .

FROM scratch
COPY --from=build /app/app /app
CMD [ "/app" ]
