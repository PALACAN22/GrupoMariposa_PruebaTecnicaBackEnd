package service

import (
	"context"
	"fmt"
	"log/slog"

	"github.com/biblioteca/service-b-loans/internal/apperror"
	"github.com/biblioteca/service-b-loans/internal/client"
	"github.com/biblioteca/service-b-loans/internal/model"
)

type LoanRepository interface {
	Create(ctx context.Context, userID, bookID int64) (*model.Loan, error)
	FindByID(ctx context.Context, id int64) (*model.Loan, error)
	MarkAsReturned(ctx context.Context, id int64) (*model.Loan, error)
	FindActiveByUser(ctx context.Context, userID int64) ([]model.Loan, error)
	FindHistoryByUser(ctx context.Context, userID int64) ([]model.Loan, error)
}

type LibraryClient interface {
	CheckAvailability(ctx context.Context, bookID int64) (*client.BookAvailability, error)
}

type LoanService struct {
	repo          LoanRepository
	libraryClient LibraryClient
}

func NewLoanService(repo LoanRepository, libraryClient LibraryClient) *LoanService {
	return &LoanService{repo: repo, libraryClient: libraryClient}
}

func (s *LoanService) CreateLoan(ctx context.Context, userID, bookID int64) (*model.Loan, error) {
	availability, err := s.libraryClient.CheckAvailability(ctx, bookID)
	if err != nil {
		// Si el Servicio A esta caido, el libro no exista o no tenga copias rechazar el prestamo
		slog.ErrorContext(ctx, "library service unavailable", "bookId", bookID, "error", err)
		return nil, err
	}

	if !availability.Exists {
		return nil, apperror.ErrBookNotFound
	}
	if !availability.Available {
		return nil, apperror.ErrBookNotAvailable
	}

	loan, err := s.repo.Create(ctx, userID, bookID)
	if err != nil {
		return nil, fmt.Errorf("The loan could not be recorded: %w", err)
	}
	slog.InfoContext(ctx, "loan created", "loanId", loan.ID, "userId", userID, "bookId", bookID)
	return loan, nil
}

func (s *LoanService) ReturnLoan(ctx context.Context, loanID int64) (*model.Loan, error) {
	loan, err := s.repo.MarkAsReturned(ctx, loanID)
	if err != nil {
		return nil, err
	}
	slog.InfoContext(ctx, "loan returned", "loanId", loanID)
	return loan, nil
}

func (s *LoanService) GetActiveLoansByUser(ctx context.Context, userID int64) ([]model.Loan, error) {
	return s.repo.FindActiveByUser(ctx, userID)
}

func (s *LoanService) GetLoanHistoryByUser(ctx context.Context, userID int64) ([]model.Loan, error) {
	return s.repo.FindHistoryByUser(ctx, userID)
}

func (s *LoanService) GetLoanByID(ctx context.Context, id int64) (*model.Loan, error) {
	return s.repo.FindByID(ctx, id)
}
