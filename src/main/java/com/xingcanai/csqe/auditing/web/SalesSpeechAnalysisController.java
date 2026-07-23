package com.xingcanai.csqe.auditing.web;

import com.xingcanai.csqe.auditing.entity.Employee;
import com.xingcanai.csqe.auditing.entity.EmployeeRepository;
import com.xingcanai.csqe.auditing.service.EmployeeAccessService;
import com.xingcanai.csqe.auditing.service.SalesSpeechAnalysisService;
import com.xingcanai.csqe.auditing.service.SalesSpeechAnalysisStartResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/sales-speech-analyses")
public class SalesSpeechAnalysisController {

    private final SalesSpeechAnalysisService analysisService;
    private final EmployeeAccessService employeeAccessService;
    private final EmployeeRepository employeeRepository;

    public SalesSpeechAnalysisController(
            SalesSpeechAnalysisService analysisService,
            EmployeeAccessService employeeAccessService,
            EmployeeRepository employeeRepository) {
        this.analysisService = analysisService;
        this.employeeAccessService = employeeAccessService;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping
    public ResponseEntity<SalesSpeechAnalysisResponse> start(
            @RequestBody SalesSpeechAnalysisRequest request,
            Authentication authentication) {
        if (request == null
                || request.employeeId() == null
                || request.employeeId().isBlank()
                || request.evalPeriod() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "employee_id and eval_period are required");
        }

        Employee employee = requireAccessibleEmployee(request.employeeId(), authentication.getName());
        SalesSpeechAnalysisStartResult result = analysisService.start(
                employee,
                request.evalPeriod(),
                request.shouldRegenerate(),
                authentication.getName());

        HttpStatus status = result.accepted() ? HttpStatus.ACCEPTED : HttpStatus.OK;
        return ResponseEntity
                .status(status)
                .body(SalesSpeechAnalysisResponse.from(result.analysis()));
    }

    @GetMapping
    public SalesSpeechAnalysisResponse get(
            @RequestParam String employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate evalPeriod,
            Authentication authentication) {
        Employee employee = requireAccessibleEmployee(employeeId, authentication.getName());
        return analysisService.get(employee.getId(), evalPeriod)
                .map(SalesSpeechAnalysisResponse::from)
                .orElseGet(() -> SalesSpeechAnalysisResponse.notStarted(employee.getId(), evalPeriod));
    }

    private Employee requireAccessibleEmployee(String employeeId, String username) {
        Employee employee = employeeRepository.findById(employeeId)
                .filter(item -> !Boolean.TRUE.equals(item.getIsDeleted()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Employee not found"));

        boolean allowed = employeeAccessService.canViewAllEmployees(username)
                || employeeAccessService.findManagedEmployeeIds(username).contains(employeeId);
        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No access to this employee");
        }
        return employee;
    }
}

