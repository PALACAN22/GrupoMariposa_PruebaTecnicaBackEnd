package service

import (
	"context"
	"errors"
	"testing"

	"github.com/biblioteca/service-b-loans/internal/apperror"
	"github.com/biblioteca/service-b-loans/internal/client"
	"github.com/biblioteca/service-b-loans/internal/model"
)

// fakeRepo implementa LoanRepository sin base de datos
type fakeRepo struct {
	loan *model.Loan
	err  error
}

func (f *fakeRepo) Create(_ context.Context, _, _ int64) (*model.Loan, error) {
	return f.loan, f.err
}
func (f *fakeRepo) FindByID(_ context.Context, _ int64) (*model.Loan, error) {
	return f.loan, f.err
}
func (f *fakeRepo) MarkAsReturned(_ context.Context, _ int64) (*model.Loan, error) {
	return f.loan, f.err
}
func (f *fakeRepo) FindActiveByUser(_ context.Context, _ int64) ([]model.Loan, error) {
	return nil, f.err
}
func (f *fakeRepo) FindHistoryByUser(_ context.Context, _ int64) ([]model.Loan, error) {
	return nil, f.err
}

// fakeLibraryClient implementa LibraryClient sin llamar al Servicio A
type fakeLibraryClient struct {
	availability *client.BookAvailability
	err          error
}

func (f *fakeLibraryClient) CheckAvailability(_ context.Context, _ int64) (*client.BookAvailability, error) {
	return f.availability, f.err
}

// libro existe y tiene copias el préstamo se crea sin errores
func TestCreateLoan_Success(t *testing.T) {
	expected := &model.Loan{ID: 1, UserID: 10, BookID: 5, Status: model.StatusActive}
	svc := NewLoanService(
		&fakeRepo{loan: expected},
		&fakeLibraryClient{availability: &client.BookAvailability{Exists: true, Available: true, AvailableCopies: 2}},
	)

	loan, err := svc.CreateLoan(context.Background(), 10, 5)

	if err != nil {
		t.Fatalf("no se esperaba error, se obtuvo: %v", err)
	}
	if loan.ID != expected.ID || loan.Status != model.StatusActive {
		t.Errorf("préstamo inesperado: %+v", loan)
	}
}

// El libro no existe - ErrBookNotFound
func TestCreateLoan_BookNotFound(t *testing.T) {
	svc := NewLoanService(
		&fakeRepo{},
		&fakeLibraryClient{availability: &client.BookAvailability{Exists: false, Available: false}},
	)

	_, err := svc.CreateLoan(context.Background(), 10, 99)

	if !errors.Is(err, apperror.ErrBookNotFound) {
		t.Errorf("se esperaba ErrBookNotFound, se obtuvo: %v", err)
	}
}

// El libro existe pero no tiene copias - ErrBookNotAvailable
func TestCreateLoan_BookNotAvailable(t *testing.T) {
	svc := NewLoanService(
		&fakeRepo{},
		&fakeLibraryClient{availability: &client.BookAvailability{Exists: true, Available: false, AvailableCopies: 0}},
	)

	_, err := svc.CreateLoan(context.Background(), 10, 5)

	if !errors.Is(err, apperror.ErrBookNotAvailable) {
		t.Errorf("se esperaba ErrBookNotAvailable, se obtuvo: %v", err)
	}
}

// Devolucion exitosa el préstamo queda con estado RETURNED
func TestReturnLoan_Success(t *testing.T) {
	returned := &model.Loan{ID: 1, UserID: 10, BookID: 5, Status: model.StatusReturned}
	svc := NewLoanService(
		&fakeRepo{loan: returned},
		&fakeLibraryClient{},
	)

	loan, err := svc.ReturnLoan(context.Background(), 1)

	if err != nil {
		t.Fatalf("no se esperaba error, se obtuvo: %v", err)
	}
	if loan.Status != model.StatusReturned {
		t.Errorf("se esperaba estado RETURNED, se obtuvo: %s", loan.Status)
	}
}
