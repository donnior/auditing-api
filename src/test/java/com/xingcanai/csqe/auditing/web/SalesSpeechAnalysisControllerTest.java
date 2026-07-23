package com.xingcanai.csqe.auditing.web;

import com.xingcanai.csqe.auditing.entity.Employee;
import com.xingcanai.csqe.auditing.entity.EmployeeRepository;
import com.xingcanai.csqe.auditing.entity.SalesSpeechAnalysis;
import com.xingcanai.csqe.auditing.entity.SalesSpeechAnalysisStatus;
import com.xingcanai.csqe.auditing.service.EmployeeAccessService;
import com.xingcanai.csqe.auditing.service.SalesSpeechAnalysisService;
import com.xingcanai.csqe.auditing.service.SalesSpeechAnalysisStartResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SalesSpeechAnalysisControllerTest {

    private final SalesSpeechAnalysisService analysisService = mock(SalesSpeechAnalysisService.class);
    private final EmployeeAccessService employeeAccessService = mock(EmployeeAccessService.class);
    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);

    private SalesSpeechAnalysisController controller;
    private Employee employee;
    private LocalDate evalPeriod;

    @BeforeEach
    void setUp() {
        controller = new SalesSpeechAnalysisController(
                analysisService,
                employeeAccessService,
                employeeRepository);
        employee = new Employee();
        employee.setId("employee-1");
        employee.setQwId("qw-1");
        employee.setIsDeleted(false);
        evalPeriod = LocalDate.of(2026, 7, 18);
        when(employeeRepository.findById("employee-1")).thenReturn(Optional.of(employee));
    }

    @Test
    void adminCanReadNotStartedViewWithoutCreatingRow() {
        when(employeeAccessService.canViewAllEmployees("admin")).thenReturn(true);
        when(analysisService.get("employee-1", evalPeriod)).thenReturn(Optional.empty());

        SalesSpeechAnalysisResponse response = controller.get(
                "employee-1",
                evalPeriod,
                new TestingAuthenticationToken("admin", "password"));

        assertEquals("NOT_STARTED", response.status());
        assertEquals("employee-1", response.employeeId());
        assertNull(response.id());
    }

    @Test
    void managedLeaderCanStartAnalysis() {
        when(employeeAccessService.canViewAllEmployees("leader")).thenReturn(false);
        when(employeeAccessService.findManagedEmployeeIds("leader")).thenReturn(List.of("employee-1"));
        SalesSpeechAnalysis analysis = completedAnalysis();
        analysis.setStatus(SalesSpeechAnalysisStatus.PROCESSING);
        when(analysisService.start(employee, evalPeriod, false, "leader"))
                .thenReturn(new SalesSpeechAnalysisStartResult(analysis, true));

        ResponseEntity<SalesSpeechAnalysisResponse> response = controller.start(
                new SalesSpeechAnalysisRequest("employee-1", evalPeriod, false),
                new TestingAuthenticationToken("leader", "password"));

        assertEquals(202, response.getStatusCode().value());
        assertEquals("PROCESSING", response.getBody().status());
        verify(analysisService).start(employee, evalPeriod, false, "leader");
    }

    @Test
    void unauthorizedLeaderGetsForbidden() {
        when(employeeAccessService.canViewAllEmployees("leader")).thenReturn(false);
        when(employeeAccessService.findManagedEmployeeIds("leader")).thenReturn(List.of("employee-2"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.get(
                        "employee-1",
                        evalPeriod,
                        new TestingAuthenticationToken("leader", "password")));

        assertEquals(403, exception.getStatusCode().value());
    }

    private SalesSpeechAnalysis completedAnalysis() {
        SalesSpeechAnalysis analysis = new SalesSpeechAnalysis();
        analysis.setId("analysis-1");
        analysis.setEmployeeId("employee-1");
        analysis.setEmployeeQwId("qw-1");
        analysis.setEvalPeriod(evalPeriod);
        analysis.setStatus(SalesSpeechAnalysisStatus.COMPLETED);
        analysis.setPromptVersion("v1");
        analysis.setRequestedBy("admin");
        analysis.setCreateTime(ZonedDateTime.now());
        analysis.setUpdateTime(ZonedDateTime.now());
        return analysis;
    }
}

