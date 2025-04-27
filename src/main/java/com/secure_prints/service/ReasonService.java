package com.secure_prints.service;

import com.secure_prints.database.ReasonRepository;
import com.secure_prints.database.entity.ReasonEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReasonService {

    private static ReasonRepository reasonRepository = null;
    private static List<ReasonEntity> reasonList;
    private static Map<String, String> bciReasonMap;
    private static Map<String, String> fbiReasonMap;

    /**
     * Constructor for ReasonService
     * @param reasonRepository reasonRepository
     */
    public ReasonService(ReasonRepository reasonRepository) {
        ReasonService.reasonRepository = reasonRepository;
    }

    /**
     * Get reason code
     * @param serviceCode serviceCode
     * @param reasonText reasonText
     * @return reasonCode
     */
    public static String getReasonCode(String serviceCode, String reasonText) {
        List<ReasonEntity> result = reasonList.stream()
                .filter(r -> r.getServiceCode().equals(serviceCode) && r.getReasonText().equals(reasonText))
                .toList();
        return !result.isEmpty() ? result.get(0).getReasonCode() : null;
    }

    /**
     * Get reason list
     * @param serviceCode serviceCode
     * @return reasonList
     */
    public static Map<String, String> getReasonList(String serviceCode) {
        Map<String, String> reasonList = bciReasonMap;
        if(serviceCode.equals("FBI")) {
            reasonList = fbiReasonMap;
        }
        return reasonList;
    }

    /**
     * Reload rsn_list table data into bciReasonMap and fbiReasonMap
     */
    public static void refreshReasonList() {
        reasonList = reasonRepository.getAllReasons();
        bciReasonMap = new HashMap<>();
        fbiReasonMap = new HashMap<>();
        List<ReasonEntity> bciReasonList =
                reasonList.stream()
                .filter(r -> r.getServiceCode().equals("BCI"))
                .toList();
        List<ReasonEntity> fbiReasonList =
                reasonList.stream()
                        .filter(r -> r.getServiceCode().equals("FBI"))
                        .toList();
        for(ReasonEntity reason : bciReasonList) {
            bciReasonMap.put(reason.getReasonCode(), reason.getReasonText());
        }
        for(ReasonEntity reason : fbiReasonList) {
            fbiReasonMap.put(reason.getReasonCode(), reason.getReasonText());
        }
    }
    
}
