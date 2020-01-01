package com.example.springbootjpa.api;

import com.example.springbootjpa.domain.Member;
import com.example.springbootjpa.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class MemberApiController {

    private final MemberService memberService;

    /*
     * 회원가입 api
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid CreateMemberRequest memberRequest) {
        Member member = new Member();
        member.setName(memberRequest.getName());

        Long id = memberService.join(member);

        return new CreateMemberResponse(id);
    }

    /*
     * 모든 회원 조회 api
     */
    @GetMapping("/api/v1/members")
    public Result<?> getMemberV1() {
        List<Member> members = memberService.findMembers();
        List<GetMemberResponse> collect
                = members.stream()
                         .map((m) -> new GetMemberResponse(m.getId(), m.getName()))
                         .collect(Collectors.toList());

        return new Result<>(collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {    // 확장성을 위해서 객체로 json 데이터를 한번 더 감싸는 형태로 반환하기 위한 클래스
        private List<T> data;
    }

    @Data
    @AllArgsConstructor
    static class GetMemberResponse {
        private Long id;
        private String name;
    }

    /*
     * 회원수정 api
     */
    @PutMapping("/api/v1/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable Long id,
                                               @RequestBody @Valid UpdateMemberRequest memberRequest) {

        memberService.update(id, memberRequest.getName());
        Member member = memberService.findOne(id);

        return new UpdateMemberResponse(member.getId(), member.getName());
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    static class CreateMemberRequest {
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
}
