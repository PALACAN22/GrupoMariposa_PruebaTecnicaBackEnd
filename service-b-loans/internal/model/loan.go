package model

import "time"

// Tipo nuevo de dato
type LoanStatus string

const (
	StatusActive   LoanStatus = "ACTIVE"
	StatusReturned LoanStatus = "RETURNED"
)

type Loan struct {
	ID         int64      `json:"id"`
	UserID     int64      `json:"userId"`
	BookID     int64      `json:"bookId"`
	Status     LoanStatus `json:"status"`
	LoanDate   time.Time  `json:"loanDate"`
	DueDate    time.Time  `json:"dueDate"`
	ReturnDate *time.Time `json:"returnDate,omitempty"`
}

// Lo que envía servicio A para prestamos
type CreateLoanRequest struct {
	UserID int64 `json:"userId" binding:"required"`
	BookID int64 `json:"bookId" binding:"required"`
}
