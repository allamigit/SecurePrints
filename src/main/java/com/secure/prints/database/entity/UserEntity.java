package com.secure.prints.database.entity;

import jakarta.persistence.*;
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
@Table(name = "usr_info")
public class UserEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usr_id")
    private Integer userId;

    @Column(name = "usr_full_name")
    private String userFullName;

    @Column(name = "usr_name")
    private String userName;

    @Column(name = "usr_paswd")
    private String userPassword;

    @Column(name = "usr_sts")
    private Boolean userStatus;

}
