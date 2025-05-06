package com.secure.prints.controller;

import com.secure.prints.database.entity.ReasonEntity;
import com.secure.prints.service.ReasonService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
     * @param listType listType
     * @return reasonList
     */
    @GetMapping(value = "reason-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public static List<ReasonEntity> getReasonList(@RequestParam("listType") String listType) {
        return ReasonService.getReasonList(listType);
    }

    /**
     * Reload rsn_list table data into bciReasonList and fbiReasonList
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
