package com.secure.prints.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLogin implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String userName;
    private String userPassword;

}
