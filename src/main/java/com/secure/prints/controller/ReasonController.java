package com.secure.prints.controller;

import com.secure.prints.database.entity.ReasonEntity;
import com.secure.prints.model.ApiStatus;
import com.secure.prints.service.ReasonService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
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
     * @param file file content
     * @return ApiStatus
     */
    @PostMapping(value = "import-reason-data-file", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus importReasonDataFile(HttpServletResponse response,
                                          @RequestParam("file") MultipartFile file) {
        ApiStatus apiStatus = reasonService.importReasonDataFile(file);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

}
