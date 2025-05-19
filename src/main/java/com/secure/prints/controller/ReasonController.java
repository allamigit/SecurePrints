package com.secure.prints.controller;

import com.secure.prints.database.entity.ReasonEntity;
import com.secure.prints.model.ApiStatus;
import com.secure.prints.service.ReasonService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ReasonController {

    private final ReasonService reasonService;

    /**
     * Constructor of ReasonController
     * @param reasonService reasonService
     */
    public ReasonController(ReasonService reasonService) {
        this.reasonService = reasonService;
    }

    /**
     * Get reason list
     * @param listType listType
     * @return reasonList
     */
    @GetMapping(value = "reason-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public static List<ReasonEntity> getReasonList(@RequestParam("listType") String listType) {
        return ReasonService.getReasonList(listType);
    }

    /**
     * Reload rsn_list table data into bciReasonList and fbiReasonList
     * @return apiStatus
     */
    @GetMapping(value = "refresh-reason-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public static ApiStatus refreshReasonList(HttpServletResponse response) {
        ApiStatus apiStatus = ReasonService.refreshReasonList();
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * Import reason data into rsn_list table from TXT file
     * @param fileName fileName
     * @return ApiStatus
     */
    @PostMapping(value = "import-reason-data-file", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus importReasonDataFile(HttpServletResponse response,
                                          @RequestParam("fileName") String fileName) {
        ApiStatus apiStatus = reasonService.importReasonDataFile(fileName);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

}
