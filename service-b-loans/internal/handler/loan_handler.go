package handler

import (
	"context"
	"errors"
	"net/http"
	"strconv"

	"github.com/biblioteca/service-b-loans/internal/apperror"
	"github.com/biblioteca/service-b-loans/internal/model"
	"github.com/gin-gonic/gin"
)

type LoanService interface {
	CreateLoan(ctx context.Context, userID, bookID int64) (*model.Loan, error)
	ReturnLoan(ctx context.Context, loanID int64) (*model.Loan, error)
	GetActiveLoansByUser(ctx context.Context, userID int64) ([]model.Loan, error)
	GetLoanHistoryByUser(ctx context.Context, userID int64) ([]model.Loan, error)
}

type LoanHandler struct {
	service LoanService
}

func NewLoanHandler(service LoanService) *LoanHandler {
	return &LoanHandler{service: service}
}

type errorResponse struct {
	Status  int    `json:"status"`
	Error   string `json:"error"`
	Message string `json:"message"`
	Path    string `json:"path"`
}

func respondError(c *gin.Context, status int, message string) {
	c.JSON(status, errorResponse{
		Status:  status,
		Error:   http.StatusText(status),
		Message: message,
		Path:    c.Request.URL.Path,
	})
}

func (h *LoanHandler) CreateLoan(c *gin.Context) {
	var req model.CreateLoanRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		respondError(c, http.StatusBadRequest, "Invalid data: numeric userId and bookId are required.")
		return
	}

	loan, err := h.service.CreateLoan(c.Request.Context(), req.UserID, req.BookID)
	if err != nil {
		handleServiceError(c, err)
		return
	}

	c.JSON(http.StatusCreated, loan)
}

// ReturnLoan PATCH /api/loans/:id/return
func (h *LoanHandler) ReturnLoan(c *gin.Context) {
	id, err := parseID(c, "id")
	if err != nil {
		respondError(c, http.StatusBadRequest, "The loan ID must be numeric.")
		return
	}

	loan, err := h.service.ReturnLoan(c.Request.Context(), id)
	if err != nil {
		handleServiceError(c, err)
		return
	}

	c.JSON(http.StatusOK, loan)
}

// ActiveLoans GET /api/loans/active?userId=123
func (h *LoanHandler) ActiveLoans(c *gin.Context) {
	userID, err := parseQueryID(c, "userId")
	if err != nil {
		respondError(c, http.StatusBadRequest, "The userId parameter is mandatory and must be numeric.")
		return
	}

	loans, err := h.service.GetActiveLoansByUser(c.Request.Context(), userID)
	if err != nil {
		handleServiceError(c, err)
		return
	}

	c.JSON(http.StatusOK, loans)
}

// LoanHistory GET /api/loans/history?userId=123
func (h *LoanHandler) LoanHistory(c *gin.Context) {
	userID, err := parseQueryID(c, "userId")
	if err != nil {
		respondError(c, http.StatusBadRequest, "The userId parameter is mandatory and must be numeric.")
		return
	}

	loans, err := h.service.GetLoanHistoryByUser(c.Request.Context(), userID)
	if err != nil {
		handleServiceError(c, err)
		return
	}

	c.JSON(http.StatusOK, loans)
}

func parseID(c *gin.Context, param string) (int64, error) {
	return strconv.ParseInt(c.Param(param), 10, 64)
}

func parseQueryID(c *gin.Context, param string) (int64, error) {
	return strconv.ParseInt(c.Query(param), 10, 64)
}

func handleServiceError(c *gin.Context, err error) {
	switch {
	case errors.Is(err, apperror.ErrBookNotFound):
		respondError(c, http.StatusNotFound, "The specified book does not exist.")
	case errors.Is(err, apperror.ErrBookNotAvailable):
		respondError(c, http.StatusConflict, "There are no copies of the book available at the moment.")
	case errors.Is(err, apperror.ErrLoanNotFound):
		respondError(c, http.StatusNotFound, "The specified loan does not exist.")
	case errors.Is(err, apperror.ErrLoanAlreadyReturned):
		respondError(c, http.StatusConflict, "The loan had already been repaid.")
	case errors.Is(err, apperror.ErrLibraryServiceDown):
		respondError(c, http.StatusBadGateway, "The library service is currently unavailable. Please try again in a few minutes.")
	default:
		respondError(c, http.StatusInternalServerError, "An unexpected error occurred on the server.")
	}
}
