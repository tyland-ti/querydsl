package study.querydsl.repository;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void basicTest() {

        Member member = new Member("멤버1");
        memberRepository.save(member);

        Optional<Member> findMember = memberRepository.findById(member.getId());
        assertThat("멤버1").isEqualTo(findMember.get().getUsername());

        List<Member> findName = memberRepository.findByUsername("멤버1");
        assertThat(findName).containsExactly(member);


    }

}