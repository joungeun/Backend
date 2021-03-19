package com.dsm.board.controller;
import com.dsm.board.utils.form.PasswordRequestForm;
import com.dsm.board.utils.form.UserLoginForm;
import com.dsm.board.utils.form.UserForm;
import com.dsm.board.service.JwtService;
import com.dsm.board.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

//로그인과 회원가입 시
@Controller
@RequestMapping("/board")
public class UserController {

    private UserService us;
    //JwtService jwtService;

    @Autowired
    public UserController(UserService us) {
        this.us = us;
    }

    // 첫 페이지
    @GetMapping("/main")
    public String returnMain(){
        return "main";
    }

    // 회원가입, 생성(insert, create)
    @PostMapping("/join")
    @ResponseBody
    public String UserInsert(@RequestBody UserForm userinfo){
        if(userinfo.pwcheack.equals(userinfo.getPw())){
            if(us.JoinInsert(userinfo.getId(),userinfo.getPw(), userinfo.getName(),userinfo.getAge(), userinfo.getIntroduce())){ //만약 return 값이 flase면
                return "회원 가입 성공";
            } else{
                return "아이디가 중복됩니다";
            }
        }
        return "회원 가입 실패 - 패스워드 확인을 다시 해주세요";
    }

    // 로그인 전 아이디 찾기 (아이디 찾을 비밀번호 입력)
    @PostMapping("/findId")
    @ResponseBody
    public String userFindId(@RequestBody PasswordRequestForm passwordRequestForm){
        return us.findId(passwordRequestForm.getPassword());
    }

    // 비밀번호 재설정 전 계정 확인
    @GetMapping("/resetPwBefor")
    @ResponseBody
    public String idAndNameCheck(@RequestParam("id") String id, @RequestParam("name") String name){
        if(us.verifyPresenceAccount(id, name)){
            JwtService js = new JwtService();
            String token = js.creatJwt(id);
            return token; // 일치하는 계정이 있을 때 그 계정 정보를 담은 토큰 리턴
        } else{
            return "일치하는 계정을 찾을 수 없습니다.";
        }
    }

    // 비밀번호 재설정
    @PatchMapping("/resetPw")
    @ResponseBody
    public String resetPw(HttpServletRequest request, @RequestBody PasswordRequestForm changePw){
        JwtService js = new JwtService();
        String id=js.getIdFromToken(request.getHeader("Authorization"));
        System.out.println(id); // 아이디 확인
        System.out.println(changePw.getPassword()); //pw 확인

        if(us.resetPw(id, changePw.getPassword())){
            return "비밀번호가 변경되었습니다. 변경된 비밀번호를 사용해 로그인하세요.";
        } else{
            return "비밀번호 변경 실패";
        }
    }

    // 로그인, 조회(select, read)
    @PostMapping("/login")
    @ResponseBody
    public String UserLogin(@RequestBody UserLoginForm userLoginInfo){
        String check = us.loginSelect(userLoginInfo);

        return check;
    }

    //회원탈퇴
    @DeleteMapping("/deleteMember")
    @ResponseBody
    public String userDelete(HttpServletRequest request) {
        JwtService js = new JwtService();

        String token = request.getHeader("Authorization");

            try{
                if(token==null){
                    throw new NullPointerException();
                }
            }catch (NullPointerException e){
                e.printStackTrace();
                return "토큰 비어있음.";
            }

        String pwCheck = request.getHeader("pwCheck");

        String MemberId = js.getIdFromToken(token); // 토큰에서 id 가져오기
        return us.userDelete(MemberId, pwCheck);
    }
}
