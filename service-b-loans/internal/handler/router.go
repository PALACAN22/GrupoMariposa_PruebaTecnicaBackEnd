package handler

import (
	"log/slog"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
)

func NewRouter(loanHandler *LoanHandler) *gin.Engine {
	router := gin.New()
	router.Use(gin.Recovery(), requestLogger())

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

func requestLogger() gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()
		c.Next()
		slog.Info("request",
			"method", c.Request.Method,
			"path", c.Request.URL.Path,
			"status", c.Writer.Status(),
			"latency", time.Since(start).String(),
			"ip", c.ClientIP(),
		)
	}
}
