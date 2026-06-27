package main

import (
	"database/sql"
	"log/slog"
	"os"
	"strconv"
	"time"

	_ "github.com/jackc/pgx/v5/stdlib" // driver de Postgres para database/sql

	"github.com/biblioteca/service-b-loans/internal/client"
	"github.com/biblioteca/service-b-loans/internal/config"
	"github.com/biblioteca/service-b-loans/internal/handler"
	"github.com/biblioteca/service-b-loans/internal/repository"
	"github.com/biblioteca/service-b-loans/internal/service"
)

func main() {
	slog.SetDefault(slog.New(slog.NewJSONHandler(os.Stdout, nil)))

	cfg := config.Load()

	db, err := connectWithRetry(cfg.DatabaseURL, 10, 3*time.Second)
	if err != nil {
		slog.Error("could not connect to database", "error", err)
		os.Exit(1)
	}
	defer db.Close()

	if err := runMigrations(db); err != nil {
		slog.Error("error applying migrations", "error", err)
		os.Exit(1)
	}

	timeoutMs, _ := strconv.Atoi(cfg.HTTPTimeoutMs)
	libraryClient := client.NewLibraryClient(cfg.LibraryServiceURL, time.Duration(timeoutMs)*time.Millisecond)

	loanRepo := repository.NewLoanRepository(db)
	loanService := service.NewLoanService(loanRepo, libraryClient)
	loanHandler := handler.NewLoanHandler(loanService)

	router := handler.NewRouter(loanHandler)

	slog.Info("service-b starting", "port", cfg.Port)
	if err := router.Run(":" + cfg.Port); err != nil {
		slog.Error("error starting server", "error", err)
		os.Exit(1)
	}
}

func connectWithRetry(dsn string, attempts int, delay time.Duration) (*sql.DB, error) {
	var db *sql.DB
	var err error

	for i := 1; i <= attempts; i++ {
		db, err = sql.Open("pgx", dsn)
		if err == nil {
			if pingErr := db.Ping(); pingErr == nil {
				return db, nil
			} else {
				err = pingErr
			}
		}
		slog.Warn("database not ready, retrying", "attempt", i, "of", attempts, "error", err)
		time.Sleep(delay)
	}
	return nil, err
}

func runMigrations(db *sql.DB) error {
	schema := `
	CREATE TABLE IF NOT EXISTS loans (
		id BIGSERIAL PRIMARY KEY,
		user_id BIGINT NOT NULL,
		book_id BIGINT NOT NULL,
		status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
		loan_date TIMESTAMP NOT NULL DEFAULT NOW(),
		due_date TIMESTAMP NOT NULL,
		return_date TIMESTAMP
	);
	CREATE INDEX IF NOT EXISTS idx_loans_user_id ON loans(user_id);
	CREATE INDEX IF NOT EXISTS idx_loans_status ON loans(status);
	`
	_, err := db.Exec(schema)
	return err
}
