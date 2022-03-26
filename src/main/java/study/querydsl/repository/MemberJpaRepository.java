package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    /**
     * findAll -> querydsl로 변경
     */
    public List<Member> findAll_querydsl() {
        return queryFactory.selectFrom(member).fetch();
    }

    public List<Member> findByUsername(String name) {
        return em.createQuery("select m from Member m where m.username = :name",Member.class)
                .setParameter("name",name)
                .getResultList();
    }

    /**
     * findByUsername -> querydsl 로 변경
     */
    public List<Member> findByUsername_querydsl(String name) {
        return queryFactory.selectFrom(member)
                .where(member.username.eq(name))
                .fetch();
    }

    /**
     * querydsl -> builder 사용(동적쿼리)
     */
    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {

        BooleanBuilder builder = new BooleanBuilder();
        if(hasText(condition.getUsername())) {
            builder.and(member.username.eq(condition.getUsername()));
        }
        if(hasText(condition.getTeamname())) {
            builder.and(team.teamname.eq(condition.getTeamname()));
        }
        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if(condition.getAgeLoe() !=  null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        return queryFactory.select(new QMemberTeamDto(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.teamname
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }

    /**
     * querydsl - 동적쿼리 (where 절 파라미터 사용)
     */
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
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
