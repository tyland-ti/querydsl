package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.*;

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

    @Test
    public void fetchTest() {
//        List<Member> fetch = jpaQueryFactory
//                .selectFrom(member)
//                .fetch();

        QueryResults<Member> fetchResults = jpaQueryFactory.selectFrom(member)
                .fetchResults();

        fetchResults.getTotal();
        List<Member> results = fetchResults.getResults();

       // Member member = jpaQueryFactory.selectFrom(QMember.member).fetchFirst();
    }

    /**
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 오름차순(asc)
     * 3. 단, 2에서 회원이름 없으면 마지막에 출력 (null last)
     */
    @Test
    public void sortTest() {
        Member member1 = new Member(null, 100);
        Member member2 = new Member("멤버5", 100);
        Member member3 = new Member("멤버6", 100);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);

        List<Member> result = jpaQueryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.asc(), member.username.asc().nullsLast())
                .fetch();

        assertThat(result.get(0).getUsername()).isEqualTo("멤버5");
        assertThat(result.get(1).getUsername()).isEqualTo("멤버6");
        assertThat(result.get(2).getUsername()).isNull();

    }

    @Test
    public void pagingTest() {
        QueryResults<Member> memberQueryResults = jpaQueryFactory.selectFrom(member)
                .orderBy(member.username.asc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(memberQueryResults.getTotal()).isEqualTo(4);
        assertThat(memberQueryResults.getResults().size()).isEqualTo(2);

    }

    @Test
    public void aggregation() {
        List<Tuple> fetch = jpaQueryFactory.select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = fetch.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름 & 각 팀의 평균 연령을 구해사
     */
    @Test
    public void groupbyTest() {

        List<Tuple> result = jpaQueryFactory
                .select(team.teamname, member.age.sum())
                .from(member)
                .join(member.team, team)
                .groupBy(team.teamname)
                .fetch();

        Tuple tupleA = result.get(0);
        Tuple tupleB = result.get(1);

        assertThat(tupleA.get(team.teamname)).isEqualTo("TeamA");
        assertThat(tupleA.get(member.age.sum())).isEqualTo(23);

    }

    /**
     * TeamA에 소속된 멤버를 찾아라
     */
    @Test
    public void joinTest() {

        List<Member> result = jpaQueryFactory.selectFrom(member)
                .join(member.team, team)
                .where(team.teamname.eq("TeamA"))
                .fetch();

        assertThat(result.size()).isEqualTo(2);
        assertThat(result)
                .extracting("username")
                .containsExactly("멤버1", "멤버2");
    }

    /**
     * 연관관계 없는데 조인 할때
     */
    @Test
    public void theta_join() {
        em.persist(new Member("TeamA"));
        em.persist(new Member("TeamB"));
        em.persist(new Member("TeamC"));

        List<Member> result = jpaQueryFactory.select(member)
                .from(member, team)
                .where(member.username.eq(team.teamname))
                .fetch();

        assertThat(result).extracting("username")
                .containsExactly("TeamA", "TeamB");
    }

    /**
     * join on 절
     * 회원과 팀을 조인, 팀이름이 teamA팀만 조인, 회원은 모두 조회
     * JPQL : select m from Member m left join m.team t on t.name = "TeamA"
     */
    @Test
    public void join_on_filtering() {
        List<Member> teamA = jpaQueryFactory.selectFrom(member)
                .leftJoin(member.team, team)
                .on(team.teamname.eq("TeamA"))
                .fetch();

        assertThat(teamA.size()).isEqualTo(4);
    }

    /**
     * join : 연관관계 없는 entity 조인
     * 회원이름과 팀이름이 같은 대상 외부 조인
     */
    @Test
    public void join_no_relation() {
        em.persist(new Member("TeamA"));
        em.persist(new Member("TeamB"));
        em.persist(new Member("TeamC"));

        List<Tuple> result = jpaQueryFactory.select(member, team)
                .from(member)
                .leftJoin(team)
                .on(member.username.eq(team.teamname))
                .fetch();

        assertThat(result.size()).isEqualTo(7);
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    /**
     * 패치 조인
     */
    @Test
    public void fetch_join() {

        Member result = jpaQueryFactory.selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("멤버1"))
                .fetchOne();
    }

    /**
     * 서브쿼리
     * 나이가 가장 많은 회원을 조회
     */
    @Test
    public void subQuery() {

        QMember submember = new QMember("sub");

        List<Member> result = jpaQueryFactory.selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(submember.age.max())
                                .from(submember)
                ))
                .fetch();

        assertThat(result).extracting("age").containsExactly(20);
    }

    /**
     * 서브쿼리
     * 나이가 평균이상인 회원 (goe)
     */
    @Test
    public void subQueryGoe() {

        QMember sub = new QMember("sub");
        List<Member> result = jpaQueryFactory.selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(sub.age.avg())
                                .from(sub)
                )).fetch();

        assertThat(result).extracting("age").containsExactly(15, 20);
    }

    /**
     * 서브쿼리 (in)
     * 멤버나이가 10살 이상인 멤버
     */
    @Test
    public void subqueryIn() {

        QMember sub = new QMember("sub");

        List<Member> result = jpaQueryFactory.selectFrom(member)
                .where(member.age.in(
                        JPAExpressions.select(sub.age)
                                .from(sub)
                                .where(sub.age.gt(10))
                )).fetch();

        assertThat(result).extracting("age").containsExactly(13,15,20);
    }

    /**
     * 서브쿼리 (항목에 서브쿼리 사용)
     * 나이의 합을 가져옴
     */
    @Test
    public void columnSubquery() {

        QMember sub = new QMember("sub");
        jpaQueryFactory.select(member.username,
                                member.age,
                                JPAExpressions
                                .select(sub.age.sum()).from(sub))
                .from(member)
                .fetch();
    }

    /**
     * case 문 - 단순 case
     */
    @Test
    public void caseTest() {
        List<String> result = jpaQueryFactory.select(member.age
                        .when(10).then("열살")
                        .when(20).then("이십살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
    }

    /**
     * case 문 - caseBuilder()
     */
    @Test
    public void caseBuilderTest(){
        jpaQueryFactory.select(new CaseBuilder()
                .when(member.age.between(10,19)).then("삽대")
                .when(member.age.between(20,29)).then("이십대")
                .otherwise("기타"))
                .from(member)
                .fetch();
    }

    /**
     * 상수 - Expressions.constant
     */
    @Test
    public void constant() {
        List<Tuple> result = jpaQueryFactory.select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for(Tuple t:result) {
            System.out.println("tuple ="+t);
        }
    }

    /**
     * 문자 더하기 - concat
     * {username}_{age}
     */
    @Test
    public void concat() {
        List<String> result = jpaQueryFactory.select(member.username
                        .concat("_")
                        .concat(member.age.stringValue()))
                .from(member)
                .fetch();

        result.forEach(System.out::println);
    }
}
