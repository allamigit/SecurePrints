package com.secure.prints.service;

import com.secure.prints.config.RequiresLogin;
import com.secure.prints.database.ReasonRepository;
import com.secure.prints.database.entity.ReasonEntity;
import com.secure.prints.model.ApiStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@Transactional
public class ReasonService {

    private static ReasonRepository reasonRepository = null;
    private static List<ReasonEntity> reasonList;
    private static List<ReasonEntity> bciReasonList;
    private static List<ReasonEntity> fbiReasonList;
    @Value("${secure-prints.reason-data-file-path}")
    private String fileLocalPath;

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
                .filter(r -> r.getReasonListType().equals(serviceCode) && r.getReasonDescription().equals(reasonText))
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
        reasonList = reasonRepository.findAll();
        bciReasonList = reasonRepository.getAllReasonsByType("BCI");
        fbiReasonList = reasonRepository.getAllReasonsByType("FBI");

        return ApiStatus.builder()
                .responseCode(200)
                .responseMessage("Reason list was successfully refreshed and reloaded (" + reasonList.size() + ") reasons.")
                .build();
    }

    /**
     * Import reason data into rsn_list table from CSV/TXT file
     * @param fileName fileName
     * @return ApiStatus
     */
    @RequiresLogin
    public ApiStatus importReasonDataFile(String fileName) {
        Path filePath = Paths.get(fileLocalPath, fileName);
        List<String> fileLines;
        try (Stream<String> lines = Files.lines(filePath)) {
            fileLines = lines.toList();
            reasonRepository.removeAllReasonData();
            for(int id = 0; id < Objects.requireNonNull(fileLines).size(); id++) {
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
        }
        catch (IOException e) {
            return ApiStatus.builder()
                    .responseCode(409)
                    .responseMessage("Reason data file failed to import. " + e.getMessage())
                    .build();
        }

        return ApiStatus.builder()
                .responseCode(200)
                .responseMessage("Reason data file was successfully imported and saved (" + fileLines.size() + ") lines.")
                .build();
    }
    
}
