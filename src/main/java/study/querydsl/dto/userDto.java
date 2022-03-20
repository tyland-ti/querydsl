package study.querydsl.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class userDto {
    private String name;
    private int age;

    public userDto(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
