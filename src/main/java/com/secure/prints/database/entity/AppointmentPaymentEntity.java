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
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appt_pymt")
public class AppointmentPaymentEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "appt_id")
    private Long appointmentId;

    @Column(name = "svc_code")
    private String serviceCode;

    @Column(name = "svc_amt")
    private BigDecimal serviceAmount;

    @Column(name = "pymt_type")
    private Integer paymentType;

    @Column(name = "pymt_method")
    private Integer paymentMethod;

    @Column(name = "pymt_dt")
    private LocalDate paymentDate;

    @Column(name = "pymt_cmt")
    private String paymentComment;

    @Column(name = "pymt_rcncl_dt")
    private LocalDate paymentReconcileDate;

}
