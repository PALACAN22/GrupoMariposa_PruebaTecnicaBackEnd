package com.biblioteca.servicea.service;

import com.biblioteca.servicea.dto.ServiceBLoanRequest;
import com.biblioteca.servicea.dto.ServiceBLoanResponse;
import com.biblioteca.servicea.exception.BusinessException;
import com.biblioteca.servicea.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Slf4j
public class LoanServiceBClient {
    
    private final RestTemplate restTemplate;

    private final String baseUrl;

    public LoanServiceBClient(RestTemplate restTemplate, @Value("${loans.service-b-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public ServiceBLoanResponse createLoan(Long userId, Long bookId) {
        try {
            ServiceBLoanRequest body = new ServiceBLoanRequest(userId, bookId);
            return restTemplate.postForObject(baseUrl + "/api/loans", body, ServiceBLoanResponse.class);
        } catch (HttpClientErrorException e) {
            // El Servicio B respondio pero rechazo la operacion (ej: libro sin copias).
            log.warn("Service B rejected the loan: {}", e.getResponseBodyAsString());
            throw new BusinessException(extractMessage(e));
        } catch (ResourceAccessException e) {
            // Timeout o conexion rechazada: el Servicio B esta caido o muy lento.
            log.error("Service B (loans) could not be contacted: {}", e.getMessage());
            throw new ServiceUnavailableException("The loan service is currently unavailable. Please try again in a few minutes.", e);
        }
    }

    public ServiceBLoanResponse returnLoan(Long loanId) {
        try {
            return restTemplate.patchForObject(baseUrl + "/api/loans/" + loanId + "/return", null, ServiceBLoanResponse.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new com.biblioteca.servicea.exception.ResourceNotFoundException("Prestamo no encontrado");
            }
            throw new BusinessException(extractMessage(e));
        } catch (ResourceAccessException e) {
            throw new ServiceUnavailableException("The loan service is not available at this time.", e);
        }
    }

    public List<ServiceBLoanResponse> getActiveLoansByUser(Long userId) {
        try {
            ServiceBLoanResponse[] result = restTemplate.getForObject(
                    baseUrl + "/api/loans/active?userId=" + userId, ServiceBLoanResponse[].class);
            return result == null ? List.of() : List.of(result);
        } catch (ResourceAccessException e) {
            throw new ServiceUnavailableException("The loan service is not available at this time.", e);
        }
    }

    public List<ServiceBLoanResponse> getLoanHistoryByUser(Long userId) {
        try {
            ServiceBLoanResponse[] result = restTemplate.getForObject(
                    baseUrl + "/api/loans/history?userId=" + userId, ServiceBLoanResponse[].class);
            return result == null ? List.of() : List.of(result);
        } catch (ResourceAccessException e) {
            throw new ServiceUnavailableException("The loan service is not available at this time.", e);
        }
    }

    private String extractMessage(HttpClientErrorException e) {
        try {
            return e.getResponseBodyAsString();
        } catch (Exception ex) {
            return "The loan service rejected the transaction.";
        }
    }

}
