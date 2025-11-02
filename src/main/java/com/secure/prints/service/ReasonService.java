package com.secure.prints.service;

import com.secure.prints.config.RequiresLogin;
import com.secure.prints.database.ReasonRepository;
import com.secure.prints.database.entity.ReasonEntity;
import com.secure.prints.model.ApiStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class ReasonService {

    private static ReasonRepository reasonRepository = null;
    private static List<ReasonEntity> reasonList;
    private static List<ReasonEntity> bciReasonList;
    private static List<ReasonEntity> fbiReasonList;

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
     * @param reasonDescription reasonDescription
     * @return reasonCode
     */
    public static String getReasonCode(String serviceCode, String reasonDescription) {
        List<ReasonEntity> reasonCode = reasonList.stream()
                .filter(r -> r.getReasonListType().equals(serviceCode) && r.getReasonDescription().equals(reasonDescription))
                .toList();
        return !reasonCode.isEmpty() ? reasonCode.get(0).getReasonCode() : null;
    }

    /**
     * Get reason text
     * @param listType listType
     * @param reasonCode listType
     * @return reasonText
     */
    public static String getReasonDescription(String listType, String reasonCode) {
        List<ReasonEntity> reasonDescription = reasonList.stream()
                .filter(r -> r.getReasonListType().equals(listType) && r.getReasonCode().equals(reasonCode))
                .toList();
        return !reasonDescription.isEmpty() ? reasonDescription.get(0).getReasonDescription() : null;
    }

    /**
     * Get reason list
     * @param listType listType
     * @return reasonList
     */
    public static List<ReasonEntity> getReasonList(String listType) {
        List<ReasonEntity> reasonList = bciReasonList;
        if(listType.equals("FBI")) {
            reasonList = fbiReasonList;
        }
        return reasonList;
    }

    /**
     * Reload rsn_list table data into bciReasonList and fbiReasonList
     * @return apiStatus
     */
    @RequiresLogin
    public static ApiStatus refreshReasonList() {
        reloadList();

        return ApiStatus.builder()
                .responseCode(200)
                .responseMessage("Reason list was successfully refreshed and cached (" + reasonList.size() + ") reasons.")
                .build();
    }

    /**
     * Import reason data into rsn_list table from CSV/TXT file
     * @param file file content
     * @return ApiStatus
     */
    @RequiresLogin
    public ApiStatus importReasonDataFile(MultipartFile file) {
        String content;
        List<String> fileLines;
        try {
            content = new String(file.getBytes());
            fileLines = Arrays.asList(content.split("\\R"));
            reasonRepository.removeAllReasonData();
            for(int id = 0; id < fileLines.size(); id++) {
                String[] eachLine = fileLines.get(id).split(", ");
                String[] lineData = eachLine[0].split("~");
                ReasonEntity reasonEntity = ReasonEntity.builder()
                        .reasonId(id + 1)
                        .reasonListType(lineData[0])
                        .reasonCode(lineData[1])
                        .reasonDescription(lineData[2].replace(",", ", "))
                        .build();
                reasonRepository.save(reasonEntity);
            }
        } catch (Exception e) {
            return ApiStatus.builder()
                    .responseCode(409)
                    .responseMessage("Reason data file failed to import.")
                    .build();
        }

        reloadList();

        return ApiStatus.builder()
                .responseCode(200)
                .responseMessage("Reason data file was successfully imported, saved and cached (" + fileLines.size() + ") lines.")
                .build();
    }

    /**
     * Reload all static lists values
     */
    private static void reloadList() {
        reasonList = reasonRepository.findAll();
        bciReasonList = reasonRepository.getAllReasonsByType("BCI");
        fbiReasonList = reasonRepository.getAllReasonsByType("FBI");
    }

}
