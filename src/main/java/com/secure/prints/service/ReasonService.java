package com.secure.prints.service;

import com.secure.prints.database.ReasonRepository;
import com.secure.prints.database.entity.ReasonEntity;
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
        List<ReasonEntity> reasonCode = reasonList.stream()
                .filter(r -> r.getReasonListType().equals(serviceCode) && r.getReasonText().equals(reasonText))
                .toList();
        return !reasonCode.isEmpty() ? reasonCode.get(0).getReasonCode() : null;
    }

    /**
     * Get reason text
     * @param serviceCode serviceCode
     * @param reasonCode serviceCode
     * @return reasonText
     */
    public static String getReasonText(String serviceCode, String reasonCode) {
        List<ReasonEntity> reasonText = reasonList.stream()
                .filter(r -> r.getReasonListType().equals(serviceCode) && r.getReasonCode().equals(reasonCode))
                .toList();
        return !reasonText.isEmpty() ? reasonText.get(0).getReasonText() : null;
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
                .filter(r -> r.getReasonListType().equals("BCI"))
                .toList();
        List<ReasonEntity> fbiReasonList =
                reasonList.stream()
                        .filter(r -> r.getReasonListType().equals("FBI"))
                        .toList();
        for(ReasonEntity reason : bciReasonList) {
            bciReasonMap.put(reason.getReasonCode(), reason.getReasonText());
        }
        for(ReasonEntity reason : fbiReasonList) {
            fbiReasonMap.put(reason.getReasonCode(), reason.getReasonText());
        }
    }
    
}
