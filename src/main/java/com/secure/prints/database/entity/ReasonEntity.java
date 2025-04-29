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

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rsn_list")
public class ReasonEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "rsn_id")
    private Integer reasonId;

    @Column(name = "rsn_list_type")
    private String reasonListType;

    @Column(name = "rsn_code")
    private String reasonCode;

    @Column(name = "rsn_text")
    private String reasonText;

}
