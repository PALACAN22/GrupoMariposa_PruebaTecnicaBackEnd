package com.biblioteca.servicea.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.biblioteca.servicea.dto.LoanRequest;
import com.biblioteca.servicea.dto.ServiceBLoanResponse;
import com.biblioteca.servicea.entity.User;
import com.biblioteca.servicea.service.LoanOrchestrationService;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
    
    private final LoanOrchestrationService loanOrchestrationService;

    @PostMapping
    public ResponseEntity<ServiceBLoanResponse> createLoan(@Valid @RequestBody LoanRequest request, @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) {
        ServiceBLoanResponse response = loanOrchestrationService.createLoan(currentUser.getId(), request.getBookId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<ServiceBLoanResponse> returnLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanOrchestrationService.returnLoan(id));
    }

    @GetMapping("/me/active")
    public ResponseEntity<List<ServiceBLoanResponse>> myActiveLoans(@Parameter(hidden = true) @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(loanOrchestrationService.getActiveLoans(currentUser.getId()));
    }

    @GetMapping("/me/history")
    public ResponseEntity<List<ServiceBLoanResponse>> myLoanHistory(@Parameter(hidden = true) @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(loanOrchestrationService.getLoanHistory(currentUser.getId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<ServiceBLoanResponse>> activeLoansByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(loanOrchestrationService.getActiveLoans(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<ServiceBLoanResponse>> historyByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(loanOrchestrationService.getLoanHistory(userId));
    }

}
