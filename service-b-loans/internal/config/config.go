package config

import "os"

type Config struct {
	Port              string
	DatabaseURL       string
	LibraryServiceURL string
	HTTPTimeoutMs     string
}

func Load() Config {
	return Config{
		Port:              getEnv("PORT", "8081"),
		DatabaseURL:       buildDatabaseURL(),
		LibraryServiceURL: getEnv("LIBRARY_SERVICE_URL", "http://localhost:8080"),
		HTTPTimeoutMs:     getEnv("HTTP_TIMEOUT_MS", "5000"),
	}
}

func buildDatabaseURL() string {
	if url := os.Getenv("DATABASE_URL"); url != "" {
		return url
	}

	host := getEnv("DB_HOST", "localhost")
	port := getEnv("DB_PORT", "5433")
	user := getEnv("DB_USER", "loans_user")
	pass := getEnv("DB_PASSWORD", "loans_pass")
	name := getEnv("DB_NAME", "loans_db")
	return "postgres://" + user + ":" + pass + "@" + host + ":" + port + "/" + name + "?sslmode=disable"
}

func getEnv(key, fallback string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}

	return fallback
}
