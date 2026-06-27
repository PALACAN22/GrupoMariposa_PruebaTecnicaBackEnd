package repository

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"time"

	"github.com/biblioteca/service-b-loans/internal/apperror"
	"github.com/biblioteca/service-b-loans/internal/model"
)

type LoanRepository struct {
	db *sql.DB
}

func NewLoanRepository(db *sql.DB) *LoanRepository {
	return &LoanRepository{db: db}
}

func (r *LoanRepository) Create(ctx context.Context, userID, bookID int64) (*model.Loan, error) {
	now := time.Now()
	dueDate := now.AddDate(0, 0, 14)

	query := `
		INSERT INTO loans (user_id, book_id, status, loan_date, due_date)
		VALUES ($1, $2, $3, $4, $5)
		RETURNING id, user_id, book_id, status, loan_date, due_date, return_date
	`

	loan := &model.Loan{}
	var returnDate sql.NullTime

	err := r.db.QueryRowContext(ctx, query, userID, bookID, model.StatusActive, now, dueDate).
		Scan(&loan.ID, &loan.UserID, &loan.BookID, &loan.Status, &loan.LoanDate, &loan.DueDate, &returnDate)
	if err != nil {
		return nil, fmt.Errorf("Error creating the loan in the database: %w", err)
	}

	if returnDate.Valid {
		loan.ReturnDate = &returnDate.Time
	}
	return loan, nil
}

func (r *LoanRepository) FindByID(ctx context.Context, id int64) (*model.Loan, error) {
	query := `
		SELECT id, user_id, book_id, status, loan_date, due_date, return_date
		FROM loans WHERE id = $1
	`
	loan := &model.Loan{}
	var returnDate sql.NullTime

	err := r.db.QueryRowContext(ctx, query, id).
		Scan(&loan.ID, &loan.UserID, &loan.BookID, &loan.Status, &loan.LoanDate, &loan.DueDate, &returnDate)

	if errors.Is(err, sql.ErrNoRows) {
		return nil, apperror.ErrLoanNotFound
	}
	if err != nil {
		return nil, fmt.Errorf("Error searching for the loan: %w", err)
	}

	if returnDate.Valid {
		loan.ReturnDate = &returnDate.Time
	}
	return loan, nil
}

func (r *LoanRepository) MarkAsReturned(ctx context.Context, id int64) (*model.Loan, error) {
	now := time.Now()
	query := `
		UPDATE loans
		SET status = $1, return_date = $2
		WHERE id = $3 AND status = $4
		RETURNING id, user_id, book_id, status, loan_date, due_date, return_date
	`

	loan := &model.Loan{}
	var returnDate sql.NullTime

	err := r.db.QueryRowContext(ctx, query, model.StatusReturned, now, id, model.StatusActive).
		Scan(&loan.ID, &loan.UserID, &loan.BookID, &loan.Status, &loan.LoanDate, &loan.DueDate, &returnDate)

	if errors.Is(err, sql.ErrNoRows) {
		// Puede ser que el prestamo no exista o que ya estuviera devuelto
		existing, findErr := r.FindByID(ctx, id)
		if findErr != nil {
			return nil, apperror.ErrLoanNotFound
		}
		if existing.Status == model.StatusReturned {
			return nil, apperror.ErrLoanAlreadyReturned
		}
		return nil, apperror.ErrLoanNotFound
	}
	if err != nil {
		return nil, fmt.Errorf("Error marking the loan as returned: %w", err)
	}

	if returnDate.Valid {
		loan.ReturnDate = &returnDate.Time
	}
	return loan, nil
}

func (r *LoanRepository) FindActiveByUser(ctx context.Context, userID int64) ([]model.Loan, error) {
	status := model.StatusActive
	return r.findByUserAndStatus(ctx, userID, &status)
}

func (r *LoanRepository) FindHistoryByUser(ctx context.Context, userID int64) ([]model.Loan, error) {
	return r.findByUserAndStatus(ctx, userID, nil)
}

func (r *LoanRepository) findByUserAndStatus(ctx context.Context, userID int64, status *model.LoanStatus) ([]model.Loan, error) {
	query := `
		SELECT id, user_id, book_id, status, loan_date, due_date, return_date
		FROM loans WHERE user_id = $1
	`
	args := []any{userID}

	if status != nil {
		query += " AND status = $2"
		args = append(args, *status)
	}
	query += " ORDER BY loan_date DESC"

	rows, err := r.db.QueryContext(ctx, query, args...)
	if err != nil {
		return nil, fmt.Errorf("Error retrieving user loans: %w", err)
	}
	defer rows.Close()

	loans := make([]model.Loan, 0)
	for rows.Next() {
		var loan model.Loan
		var returnDate sql.NullTime
		if err := rows.Scan(&loan.ID, &loan.UserID, &loan.BookID, &loan.Status, &loan.LoanDate, &loan.DueDate, &returnDate); err != nil {
			return nil, fmt.Errorf("Error reading loan row: %w", err)
		}
		if returnDate.Valid {
			loan.ReturnDate = &returnDate.Time
		}
		loans = append(loans, loan)
	}
	return loans, rows.Err()
}
