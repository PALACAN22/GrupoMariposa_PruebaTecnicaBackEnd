package main

import (
	"context"
	"fmt"
	"time"

	"github.com/biblioteca/service-b-loans/internal/apperror"
	"github.com/biblioteca/service-b-loans/internal/client"
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

	err := apperror.ErrBookNotFound
	fmt.Println("Example error:", err)

	c := client.NewLibraryClient("http://localhost:8081", 5*time.Second)

	res, _ := c.CheckAvailability(context.Background(), 10)

	fmt.Println("Availability:", res)

}
