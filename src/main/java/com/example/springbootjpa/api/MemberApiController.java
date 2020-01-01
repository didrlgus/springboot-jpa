package com.example.springbootjpa.api;

import com.example.springbootjpa.domain.Member;
import com.example.springbootjpa.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
public class MemberApiController {

    private final MemberService memberService;

    /*public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {

    }*/

    @Data
    static class CreateMemberResponse {
        private Long id;
    }
}
