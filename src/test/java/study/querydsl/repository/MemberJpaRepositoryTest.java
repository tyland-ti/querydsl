package study.querydsl.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void basicTest() {
        Member member = new Member("member1",20);
        em.persist(member);

        Optional<Member> findOne = memberJpaRepository.findById(member.getId());

        findOne.ifPresent(value -> assertThat(value.getAge()).isEqualTo(20));

        List<Member> member1 = memberJpaRepository.findByUsername("member1");

        assertThat(member1.size()).isEqualTo(1);
    }

    @Test
    public void basicTest_querydsl() {

        Member member = new Member("member2", 30);
        em.persist(member);

        List<Member> member2 = memberJpaRepository.findByUsername_querydsl("member2");
        assertThat(member2).extracting("age").containsExactly(30);
    }

    /**
     * querydsl builder 테스트
     */
    @Test
    public void builderTest_querydsl() {

        Team teamA =  new Team("teamA");
        Team teamB =  new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 20, teamA);
        Member member2 = new Member("member2", 22, teamA);
        Member member3 = new Member("member3", 23, teamB);
        Member member4 = new Member("member4", 24, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setTeamname("teamA");
        condition.setAgeGoe(23);

        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);
        assertThat(result.size()).isEqualTo(0);

        MemberSearchCondition condition2 = new MemberSearchCondition();
        condition2.setTeamname("teamA");
        condition2.setAgeGoe(20);

        List<MemberTeamDto> result2 = memberJpaRepository.searchByBuilder(condition2);
        assertThat(result2.size()).isEqualTo(2);

        //where 절 파라미터
        List<MemberTeamDto> search = memberJpaRepository.search(condition2);
        assertThat(result2.size()).isEqualTo(2);
    }
}