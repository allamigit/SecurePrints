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
@Table(name = "inv_info")
public class InvoiceEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "inv_no")
    private String invoiceNumber;

    @Column(name = "inv_client_name")
    private String invoiceClientName;

    @Column(name = "inv_dt")
    private LocalDate invoiceDate;

    @Column(name = "inv_due_dt")
    private LocalDate invoiceDueDate;

    @Column(name = "inv_amt")
    private BigDecimal invoiceAmount;

    @Column(name = "inv_pymt_sts_code")
    private Integer invoicePaymentStatusCode;

    @Column(name = "inv_pymt_dt")
    private LocalDate invoicePaymentDate;

    @Column(name = "inv_pymt_method_code")
    private Integer invoicePaymentMethodCode;

    @Column(name = "inv_cmt")
    private String invoiceComments;

    @Column(name = "inv_doc_file_name")
    private String invoiceDocumentFileName;

    @Column(name = "inv_rcncl_dt")
    private LocalDate invoiceReconcileDate;

}
