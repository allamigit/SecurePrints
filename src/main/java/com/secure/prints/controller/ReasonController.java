package com.secure.prints.controller;

import com.secure.prints.service.ReasonService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

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
     * @param serviceCode serviceCode
     * @return reasonList
     */
    @GetMapping(value = "reason-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public static Map<String, String> getReasonList(@RequestParam("bciOrFbi") String serviceCode) {
        return ReasonService.getReasonList(serviceCode);
    }

    /**
     * Reload rsn_list table data into bciReasonMap and fbiReasonMap
     */
    @GetMapping(value = "refresh-reason-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public static void refreshReasonList() {
        ReasonService.refreshReasonList();
    }

    /**
     * Import reason data into rsn_list table from TXT file
     * @param fileName fileName
     */
    @PostMapping(value = "import-reason-data-file", produces = MediaType.APPLICATION_JSON_VALUE)
    public void importReasonDataFile(@RequestParam("fileName") String fileName) {
        reasonService.importReasonDataFile(fileName);
    }

}
