package com.secure.prints.model;

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
public class UserLoginResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String userFullName;
    private String userName;
    private String userStatus;
    private Integer responseCode;
    private String responseMessage;

}
