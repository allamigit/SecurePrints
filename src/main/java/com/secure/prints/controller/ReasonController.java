package com.secure.prints.controller;

import com.secure.prints.service.ReasonService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ReasonController {

    /**
     * Get reason list
     * @param serviceCode serviceCode
     * @return reasonList
     */
    @GetMapping(value = "reason-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public static Map<String, String> getReasonList(@RequestParam("reason-list-type") String serviceCode) {
        return ReasonService.getReasonList(serviceCode);
    }

    /**
     * Reload rsn_list table data into bciReasonMap and fbiReasonMap
     */
    @GetMapping(value = "refresh-reason-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public static void refreshReasonList() {
        ReasonService.refreshReasonList();
    }

}
