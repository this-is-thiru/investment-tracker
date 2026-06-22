package com.thiru.wealthlens.taxplanning.controller;

import com.thiru.wealthlens.auth.service.UserValidator;
import com.thiru.wealthlens.taxplanning.document.TaxComparisonReportGenerator;
import com.thiru.wealthlens.taxplanning.policy.entity.AllowanceCatalogueEntity;
import com.thiru.wealthlens.taxplanning.policy.entity.PerquisitePolicyEntity;
import com.thiru.wealthlens.taxplanning.policy.entity.TaxSlabPolicyEntity;
import com.thiru.wealthlens.taxplanning.policy.service.PolicyService;
import com.thiru.wealthlens.taxplanning.recommendation.RestructuringResult;
import com.thiru.wealthlens.taxplanning.salary.dto.SalaryProfileRequest;
import com.thiru.wealthlens.taxplanning.salary.dto.SalaryProfileResponse;
import com.thiru.wealthlens.taxplanning.salary.entity.SalaryProfileEntity;
import com.thiru.wealthlens.taxplanning.salary.entity.TaxComputationEntity;
import com.thiru.wealthlens.taxplanning.salary.service.SalaryProfileService;
import com.thiru.wealthlens.taxplanning.service.TaxComputationService;
import com.thiru.wealthlens.taxplanning.engine.TaxEngine;
import com.thiru.wealthlens.taxplanning.engine.TaxEngineFactory;
import com.thiru.wealthlens.taxplanning.engine.FormulaEvaluator;
import com.thiru.wealthlens.taxplanning.enums.RegimeType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tax-planning")
@Log4j2
@RequiredArgsConstructor
public class TaxPlanningController {

    private final SalaryProfileService salaryProfileService;
    private final TaxComputationService taxComputationService;
    private final PolicyService policyService;
    private final TaxComparisonReportGenerator reportGenerator;
    private final com.thiru.wealthlens.taxplanning.recommendation.RestructuringEngine restructuringEngine;

    // ========== Salary Profile Management ==========

    @PostMapping("/user/{email}/profile")
    public ResponseEntity<SalaryProfileResponse> createProfile(
            @PathVariable String email,
            @RequestBody SalaryProfileRequest request,
            Authentication authentication) {
        validateAccess(authentication, email);
        return ResponseEntity.ok(salaryProfileService.createProfile(email, request));
    }

    @GetMapping("/user/{email}/profiles")
    public ResponseEntity<List<SalaryProfileResponse>> getProfiles(
            @PathVariable String email,
            Authentication authentication) {
        validateAccess(authentication, email);
        return ResponseEntity.ok(salaryProfileService.getProfiles(email));
    }

    @GetMapping("/user/{email}/profile/{profileId}")
    public ResponseEntity<SalaryProfileResponse> getProfile(
            @PathVariable String email,
            @PathVariable String profileId,
            Authentication authentication) {
        validateAccess(authentication, email);
        return ResponseEntity.ok(salaryProfileService.getProfile(email, profileId));
    }

    @PutMapping("/user/{email}/profile/{profileId}")
    public ResponseEntity<SalaryProfileResponse> updateProfile(
            @PathVariable String email,
            @PathVariable String profileId,
            @RequestBody SalaryProfileRequest request,
            Authentication authentication) {
        validateAccess(authentication, email);
        return ResponseEntity.ok(salaryProfileService.updateProfile(email, profileId, request));
    }

    @DeleteMapping("/user/{email}/profile/{profileId}")
    public ResponseEntity<Void> deleteProfile(
            @PathVariable String email,
            @PathVariable String profileId,
            Authentication authentication) {
        validateAccess(authentication, email);
        salaryProfileService.deleteProfile(email, profileId);
        return ResponseEntity.noContent().build();
    }

    // ========== Tax Computation ==========

    @PostMapping("/user/{email}/profile/{profileId}/compute")
    public ResponseEntity<TaxComputationEntity> computeTax(
            @PathVariable String email,
            @PathVariable String profileId,
            Authentication authentication) {
        validateAccess(authentication, email);
        return ResponseEntity.ok(taxComputationService.compute(email, profileId));
    }

    // ========== Restructuring ==========

    @PostMapping("/user/{email}/profile/{profileId}/restructure")
    public ResponseEntity<RestructuringResult> restructure(
            @PathVariable String email,
            @PathVariable String profileId,
            Authentication authentication) {
        validateAccess(authentication, email);
        SalaryProfileEntity profile = salaryProfileService.getProfileEntity(email, profileId);
        return ResponseEntity.ok(restructuringEngine.restructure(profile));
    }

    // ========== Document Generation ==========

    @GetMapping("/user/{email}/profile/{profileId}/document/tax-report")
    public ResponseEntity<byte[]> generateTaxReport(
            @PathVariable String email,
            @PathVariable String profileId,
            Authentication authentication) {
        validateAccess(authentication, email);
        TaxComputationEntity computation = taxComputationService.compute(email, profileId);
        SalaryProfileEntity profile = salaryProfileService.getProfileEntity(email, profileId);
        byte[] pdf = reportGenerator.generate(computation, profile, null);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tax-report.pdf")
                .body(pdf);
    }

    // ========== Public Allowance Catalogue ==========

    @GetMapping("/allowances")
    public ResponseEntity<List<AllowanceCatalogueEntity>> getAllowances(
            @RequestParam String taxYear,
            @RequestParam RegimeType regime) {
        return ResponseEntity.ok(policyService.getAllowanceCatalogue(taxYear));
    }

    @GetMapping("/allowances/{code}")
    public ResponseEntity<AllowanceCatalogueEntity> getAllowance(
            @PathVariable String code,
            @RequestParam String taxYear) {
        return policyService.getAllowanceCatalogue(taxYear).stream()
                .filter(a -> a.getCode().equalsIgnoreCase(code))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== Admin Endpoints (SUPER_USER only) ==========

    @PostMapping("/admin/policies/slab")
    @PreAuthorize("hasRole('SUPER_USER')")
    public ResponseEntity<TaxSlabPolicyEntity> createSlabPolicy(@RequestBody TaxSlabPolicyEntity policy) {
        return ResponseEntity.ok(policyService.createSlabPolicy(policy));
    }

    @PostMapping("/admin/policies/perquisite")
    @PreAuthorize("hasRole('SUPER_USER')")
    public ResponseEntity<PerquisitePolicyEntity> createPerquisitePolicy(@RequestBody PerquisitePolicyEntity policy) {
        return ResponseEntity.ok(policyService.createPerquisitePolicy(policy));
    }

    @PutMapping("/admin/allowances/{code}")
    @PreAuthorize("hasRole('SUPER_USER')")
    public ResponseEntity<AllowanceCatalogueEntity> updateAllowance(
            @PathVariable String code,
            @RequestParam String taxYear,
            @RequestBody AllowanceCatalogueEntity allowance) {
        return ResponseEntity.ok(policyService.updateAllowance(code, taxYear, allowance));
    }

    @GetMapping("/admin/policies")
    @PreAuthorize("hasRole('SUPER_USER')")
    public ResponseEntity<Map<String, Object>> getAllPolicies(@RequestParam String taxYear) {
        Map<String, Object> result = new HashMap<>();
        result.put("taxYear", taxYear);
        result.put("slabPolicies", policyService.getSlabPolicies(taxYear));
        result.put("perquisitePolicy", policyService.getPerquisitePolicy(taxYear));
        result.put("allowanceCatalogue", policyService.getAllowanceCatalogue(taxYear));
        return ResponseEntity.ok(result);
    }

    // ========== Helpers ==========

    private void validateAccess(Authentication authentication, String email) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        String username = authentication.getName();
        List<GrantedAuthority> authorities = new ArrayList<>(authentication.getAuthorities());
        if (UserValidator.isRestrictedUser(username, authorities)) {
            String pathEmail = extractEmailFromPath();
            if (pathEmail != null && !pathEmail.equalsIgnoreCase(username)) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied for user: " + username);
            }
        }
    }

    private String extractEmailFromPath() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String path = request.getRequestURI();
            if (!path.contains("/user/")) {
                return null;
            }
            String[] parts = path.split("/user/");
            if (parts.length > 1) {
                return parts[1].split("/")[0];
            }
        } catch (Exception e) {
            log.debug("Could not extract email from path", e);
        }
        return null;
    }
}