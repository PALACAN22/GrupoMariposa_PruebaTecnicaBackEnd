package apperror

import "errors"

// Definimos errores de forma idiomatica para utilizarlos
var (
	ErrBookNotFound        = errors.New("The book does not exist.")
	ErrBookNotAvailable    = errors.New("There are no copies of the book available.")
	ErrLoanNotFound        = errors.New("The loan does not exist.")
	ErrLoanAlreadyReturned = errors.New("The loan has already been repaid.")
	ErrLibraryServiceDown  = errors.New("The library service is not available.")
	ErrInvalidInput        = errors.New("Invalid input data.")
)
