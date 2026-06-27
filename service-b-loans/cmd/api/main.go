package main

import (
	"fmt"

	"github.com/biblioteca/service-b-loans/internal/config"
)

func main() {
	cfg := config.Load()

	fmt.Println("Port:", cfg.Port)
	fmt.Println("DB:", cfg.DatabaseURL)
}
