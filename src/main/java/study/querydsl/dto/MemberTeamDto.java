package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberTeamDto {
    private Long memberId;
    private String username;
    private int age;
    private Long teamId;
    private String teamname;

    @QueryProjection
    public MemberTeamDto(Long memberId,
                         String username,
                         int age,
                         Long teamId,
                         String teamname) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teamname = teamname;
    }
}
