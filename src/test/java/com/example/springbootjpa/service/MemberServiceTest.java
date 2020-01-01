package com.example.springbootjpa.service;

import com.example.springbootjpa.domain.Member;
import com.example.springbootjpa.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void 회원가입() throws Exception {

        // Given -> 테스트 상황 설정
        Member member = new Member();
        member.setName("Yang");

        // When  -> 테스트 대상을 실행
        Long saveId = memberService.join(member);

        // then  -> 결과 검증
        assertThat(saveId).isGreaterThan(0L);
    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {

        // Given
        Member member1 = new Member();
        member1.setName("Yang");
        Member member2 = new Member();
        member2.setName("Yang");

        // When
        memberService.join(member1);
        memberService.join(member2);

        // Then
        fail("예외가 발생해야 한다.");
    }
}
