package com.biblioteca.servicea.service;

import com.biblioteca.servicea.dto.ServiceBLoanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanOrchestrationService {
    
    private final LoanServiceBClient loanServiceBClient;
    private final BookService bookService;

    public ServiceBLoanResponse createLoan(Long userId, Long bookId) {
        // Validamos primero localmente para dar un error rapido y claro,
        // pero la validacion AUTORITATIVA sigue siendo la que hace el Servicio B
        // (es quien realmente decide si el prestamo se crea o no).
        bookService.getById(bookId);

        ServiceBLoanResponse loan = loanServiceBClient.createLoan(userId, bookId);

        try {
            bookService.decrementAvailableCopies(bookId);
        } catch (Exception e) {
            // Caso de inconsistencia: el prestamo SI se creo en B, pero A no pudo
            // descontar la copia (ej: alguien borro el libro en paralelo).
            // Lo dejamos registrado en logs para revision manual/reconciliacion,
            // en vez de fallar la respuesta al usuario con un prestamo que
            // de hecho si existe en B.
            log.error("Inconsistencia: prestamo {} creado en Servicio B pero no se pudo " +
                    "descontar la copia del libro {} en Servicio A. Requiere revision manual.", loan.getId(), bookId, e);
        }

        return loan;
    }

    public ServiceBLoanResponse returnLoan(Long loanId) {
        ServiceBLoanResponse loan = loanServiceBClient.returnLoan(loanId);
        bookService.incrementAvailableCopies(loan.getBookId());
        return loan;
    }

    public List<ServiceBLoanResponse> getActiveLoans(Long userId) {
        return loanServiceBClient.getActiveLoansByUser(userId);
    }

    public List<ServiceBLoanResponse> getLoanHistory(Long userId) {
        return loanServiceBClient.getLoanHistoryByUser(userId);
    }

}
