package com.secure.prints.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "com_info")
public class CompanyEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "com_id")
    private Integer companyId;

    @Column(name = "com_name")
    private String companyName;

    @Column(name = "com_address_1")
    private String companyAddress1;

    @Column(name = "com_address_2")
    private String companyAddress2;

    @Column(name = "com_phone")
    private String companyPhone;

    @Column(name = "com_email")
    private String companyEmail;

    @Column(name = "com_hol_start_dt")
    private LocalDate companyHolidayStartDate;

    @Column(name = "com_hol_end_dt")
    private LocalDate companyHolidayEndDate;

}
