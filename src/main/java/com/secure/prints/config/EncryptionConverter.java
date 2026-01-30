package com.secure.prints.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;

@Converter
public class EncryptionConverter implements AttributeConverter<String, String> {

    @Value("${secure-prints.encryption.secret-key}")
    private String secretKey;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : EncryptionUtils.encrypt(attribute, secretKey);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EncryptionUtils.decrypt(dbData, secretKey);
    }

}
