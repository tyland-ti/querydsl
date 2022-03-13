package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory jpaQueryFactory;

    @BeforeEach
    public void init() {
        jpaQueryFactory = new JPAQueryFactory(em);

        insertData();
    }

    public void insertData() {
        Team teamA = new Team("TeamA");
        Team teamB = new Team("TeamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("멤버1", 10, teamA);
        Member member2 = new Member("멤버2", 13, teamA);
        Member member3 = new Member("멤버3", 15, teamB);
        Member member4 = new Member("멤버4", 20, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        //초기화
        em.flush();
        em.clear();

    }

    @Test
    public void jpqlTest() {
        Member result = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "멤버1")
                .getSingleResult();

        assertThat(result.getUsername()).isEqualTo("멤버1");
        assertThat(result.getAge()).isEqualTo(10);
    }
    
    @Test
    public void querydslTest() {
        Member result = jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("멤버1"))
                .fetchOne();

        assertThat(result.getUsername()).isEqualTo("멤버1");
        assertThat(result.getTeam().getTeamname()).isEqualTo("TeamA");
    }

    @Test
    public void searchQuerydsl() {
        List<Member> results = jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq("멤버1")
                        ,member.age.eq(10))
                .fetch();

        assertThat(results.size()).isEqualTo(1);


    }
}
