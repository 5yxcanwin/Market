package com.hafa.market.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private Long userId;

    private String userName;

    private String userAvatar;

    private String userPhone;

    private String userEmail;

    private String userAddress;
}
