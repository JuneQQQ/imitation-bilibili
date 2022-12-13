package io.juneqqq.pojo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginUserDtoResp {
    private String token;
    private String nick;
    private String userId;
}
