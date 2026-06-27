FROM golang:1.21-alpine AS build
WORKDIR /app

ENV GONOSUMDB=*
COPY go.mod go.sum ./
COPY . .

RUN CGO_ENABLED=0 GOOS=linux GOFLAGS=-mod=mod go build -o /service-b ./cmd/api

FROM alpine:3.19
WORKDIR /app
COPY --from=build /service-b .
EXPOSE 8081
ENTRYPOINT ["./service-b"]
