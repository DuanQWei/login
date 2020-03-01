package com.duanqwei.login.controller;

import com.duanqwei.login.util.HttpClientUtil;
import com.duanqwei.login.util.WeChatUtil;
import com.google.gson.Gson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.UUID;

@Controller
@CrossOrigin
@RequestMapping("/WeChat")
public class WeChatController {

    //微信生成二维码
    @RequestMapping("/login")
    public String generateCode(){
        //定义微信生成二维码固定地址
        String baseUrl = "https://open.weixin.qq.com/connect/qrconnect" +
        "?appid=%s" +
        "&redirect_uri=%s" +
        "&response_type=code" +
        "&scope=snsapi_login" +
        "&state=%s" +
        "#wechat_redirect";
        //对redirect_uri进行urlEncode处理
        String redirectUrl = WeChatUtil.WX_OPEN_REDIRECT_URL;
        System.out.println(redirectUrl);
        try {
            String encodeUrl = URLEncoder.encode(redirectUrl, "utf-8");
            //一般情况下会使用一个随机数,防止csrf攻击（跨站请求伪造攻击）
            String state = UUID.randomUUID().toString().replaceAll("-", "");
            //向baseUrl中的%s占位符传值
            String formatUrl = String.format(
                    baseUrl,
                    WeChatUtil.WX_OPEN_APP_ID,
                    encodeUrl,
                    state);
            //重定向
            return "redirect:"+formatUrl;
        }catch (Exception e){
            return null;
        }
    }

    //扫描后回调的方法
    @RequestMapping("/callback")
    public String callBack(String code,String state){
        //code参数:临时票据，随机字符串，类似于手机验证码
        //state参数:生成二维码传递state值

        //向认证服务器发送请求换取access_token
        String baseAccessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token" +
        "?appid=%s" +
        "&secret=%s" +
        "&code=%s" +
        "&grant_type=authorization_code";

        String format = String.format(
                baseAccessTokenUrl,
                WeChatUtil.WX_OPEN_APP_ID,
                WeChatUtil.WX_OPEN_APP_SECRET,
                code);
       //请求这个带参数的地址，得到access_token和openid
        try {
            String accessTokenResult = HttpClientUtil.get(format);
            //从acessTokenResult获取用户信息
            Gson gson = new Gson();
            HashMap map = gson.fromJson(accessTokenResult, HashMap.class);
            String access_token = (String) map.get("access_token");
            String openid = (String) map.get("openid");

            //用access_token和openid请求一个地址才能获取到扫码人的信息
            //访问微信的资源服务器，获取用户信息
            String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                                    "?access_token=%s" +
                                    "&openid=%s";
            String format1 = String.format(userInfoUrl, access_token, openid);
            //请求地址获取扫码人信息（结果为json串）
            String userInfoResult = HttpClientUtil.get(format1);
            //将json串转换为map
            HashMap hashMap = gson.fromJson(userInfoResult, HashMap.class);
            //获取openid
            String openid1 = (String) hashMap.get("openid");
            //获取扫码人呢称
            String nickname = (String) hashMap.get("nickname");
            //获取扫码人性别
            int sex = (int) hashMap.get("sex");
            //获取到扫码人头像地址
            String headimgurl = (String) hashMap.get("headimgurl");

        }catch (Exception e){

        }

        return null;
    }
}
