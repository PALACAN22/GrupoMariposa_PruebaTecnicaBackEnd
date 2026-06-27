package client

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"github.com/biblioteca/service-b-loans/internal/apperror"
)

// Coinciden con el servicio A ya que es su respuesta
type BookAvailability struct {
	BookID          int64 `json:"bookId"`
	Exists          bool  `json:"exists"`
	Available       bool  `json:"available"`
	AvailableCopies int   `json:"availableCopies"`
}

type LibraryClient struct {
	baseURL    string
	httpClient *http.Client
}

func NewLibraryClient(baseURL string, timeout time.Duration) *LibraryClient {
	return &LibraryClient{
		baseURL: baseURL,
		httpClient: &http.Client{
			Timeout: timeout,
		},
	}
}

// copias disponibles. Usamos context.Context para poder propagar cancelaciones/timeouts desde el handler HTTP
func (c *LibraryClient) CheckAvailability(ctx context.Context, bookID int64) (*BookAvailability, error) {
	url := fmt.Sprintf("%s/api/books/%d/availability", c.baseURL, bookID)

	req, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
	if err != nil {
		return nil, fmt.Errorf("The request to the library service could not be constructed: %w", err)
	}

	resp, err := c.httpClient.Do(req)
	if err != nil {
		// Aqui caen: timeout, conexion rechazada (servicio A caido), DNS, etc.
		return nil, fmt.Errorf("%w: %v", apperror.ErrLibraryServiceDown, err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("%w: responded with status %d", apperror.ErrLibraryServiceDown, resp.StatusCode)
	}

	var availability BookAvailability
	if err := json.NewDecoder(resp.Body).Decode(&availability); err != nil {
		return nil, fmt.Errorf("Invalid response from the library service: %w", err)
	}

	return &availability, nil
}
