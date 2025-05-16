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
@Table(name = "exp_info")
public class ExpenseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "exp_id")
    private Long expenseId;

    @Column(name = "exp_payee_name")
    private String expensePayeeName;

    @Column(name = "exp_ref_no")
    private String expenseReferenceNumber;

    @Column(name = "exp_ref_dt")
    private LocalDate expenseReferenceDate;

    @Column(name = "exp_cat_code")
    private Integer expenseCategoryCode;

    @Column(name = "exp_sub_cat_code")
    private Integer expenseSubcategoryCode;

    @Column(name = "exp_desc")
    private String expenseDescription;

    @Column(name = "exp_amt")
    private BigDecimal expenseAmount;

    @Column(name = "exp_pymt_sts_code")
    private Integer expensePaymentStatusCode;

    @Column(name = "exp_pymt_dt")
    private LocalDate expensePaymentDate;

    @Column(name = "exp_pymt_method_code")
    private Integer expensePaymentMethodCode;

    @Column(name = "exp_doc_file_name")
    private String expenseDocumentFileName;

    @Column(name = "exp_rcncl_dt")
    private LocalDate expenseReconcileDate;

    @Column(name = "exp_updt")
    private Boolean expenseUpdate;

}
