package com.example.honeyvault.data_access.path;

import lombok.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PathAndAlphaUser {
//    String pw_12306;
//    String pw_163;
//    String pw_csdn;
//    String pw_1000w;
//    String pw_duDu;
//    String pw_renRen;
    String replace_12306;
    String replace_163;
    String replace_Clix;
    String replace_BC;


    public List<String> getPswdList() {
        List<String> nonEmptyFields = new ArrayList<>();
        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true); // Ensure private fields are accessible

            try {
                // Get the value of the field using Optional
                Optional<String> value = Optional.ofNullable((String) field.get(this));

                // If the value is present and not empty, add it to the list
                value.ifPresent(val -> {
                    if (!val.isEmpty()) {
                        nonEmptyFields.add(val);
                    }
                });
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return nonEmptyFields;
    }
}
