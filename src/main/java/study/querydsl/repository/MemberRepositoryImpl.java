package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import javax.persistence.EntityManager;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * querydsl - 동적쿼리 (where 절 파라미터 사용)
     */
    @Override
    public List<MemberTeamDto> serarch(MemberSearchCondition condition) {
        return queryFactory.select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.teamname
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamnameEq(condition.getTeamname()),
                        ageGoeEq(condition.getAgeGoe()),
                        ageLoeEq(condition.getAgeLoe())
                )
                .fetch();

    }

    /**
     * querydsl - 동적쿼리 (where 절 파라미터 사용)
     * paging : 데이터 조회와 카운트 쿼리를 나눠서 사용
     */
    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pagable) {
        List<MemberTeamDto> content = queryFactory.select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.teamname
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamnameEq(condition.getTeamname()),
                        ageLoeEq(condition.getAgeLoe()),
                        ageGoeEq(condition.getAgeGoe())
                )
                .offset(pagable.getOffset())
                .limit(pagable.getPageSize())
                .fetch();

        JPAQuery<MemberTeamDto> countQuery = queryFactory.select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.teamname
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamnameEq(condition.getTeamname()),
                        ageLoeEq(condition.getAgeLoe()),
                        ageGoeEq(condition.getAgeGoe())
                )
                .offset(pagable.getOffset())
                .limit(pagable.getPageSize());

        return PageableExecutionUtils.getPage(content, pagable, countQuery::fetchCount);
        //return new PageImpl<>(content, pagable, count);
    }

    /**
     * querydsl - 동적쿼리 (where 절 파라미터 사용)
     * paging : 데이터 조회와 카운트 쿼리를 함께 사용
     */
    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pagable) {
        QueryResults<MemberTeamDto> result = queryFactory.select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.teamname
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamnameEq(condition.getTeamname()),
                        ageLoeEq(condition.getAgeLoe()),
                        ageGoeEq(condition.getAgeGoe())
                )
                .offset(pagable.getOffset())
                .limit(pagable.getPageSize())
                .fetchResults();

        List<MemberTeamDto> content = result.getResults();
        Long count = result.getTotal();

        return new PageImpl<>(content, pagable, count);
    }

    private BooleanExpression ageLoeEq(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    private BooleanExpression ageGoeEq(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression teamnameEq(String teamname) {
        return hasText(teamname) ? team.teamname.eq(teamname) : null;
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }
}
