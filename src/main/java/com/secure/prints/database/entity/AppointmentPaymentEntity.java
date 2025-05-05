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
    private String appointmentId;

    @Column(name = "svc_code")
    private String serviceCode;

    @Column(name = "svc_amt")
    private BigDecimal serviceAmount;

    @Column(name = "pymt_sts_code")
    private Integer paymentStatusCode;

    @Column(name = "pymt_method_code")
    private Integer paymentMethodCode;

    @Column(name = "pymt_dt")
    private LocalDate paymentDate;

    @Column(name = "pymt_cmt")
    private String paymentComment;

    @Column(name = "pymt_rcncl_dt")
    private LocalDate paymentReconcileDate;

}
