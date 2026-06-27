package main

import (
	"fmt"
	"time"

	"github.com/biblioteca/service-b-loans/internal/config"
	"github.com/biblioteca/service-b-loans/internal/model"
)

func main() {
	cfg := config.Load()

	fmt.Println("Port:", cfg.Port)
	fmt.Println("DB:", cfg.DatabaseURL)

	loan := model.Loan{
		ID:       1,
		UserID:   10,
		BookID:   20,
		Status:   model.StatusActive,
		LoanDate: time.Now(),
	}

	fmt.Println("Loan created:", loan)
}
