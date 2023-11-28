package com.example.honeyvault.data_access.markov;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class MarkovUser {

    String email;
    String realName;
    String id_card;
    String phone;
    String password;

}
