package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

func NewRouter(loanHandler *LoanHandler) *gin.Engine {
	router := gin.New()
	router.Use(gin.Recovery(), gin.Logger())

	router.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"status": "ok"})
	})

	api := router.Group("/api/loans")
	{
		api.POST("", loanHandler.CreateLoan)
		api.PATCH("/:id/return", loanHandler.ReturnLoan)
		api.GET("/active", loanHandler.ActiveLoans)
		api.GET("/history", loanHandler.LoanHistory)
	}

	return router
}
