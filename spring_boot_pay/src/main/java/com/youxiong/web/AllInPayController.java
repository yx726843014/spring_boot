package com.youxiong.web;

import com.alibaba.fastjson.JSON;
import com.allinpay.model.AllinPayConsumeDto;
import com.allinpay.service.AllinPayConsume;
import com.allinpay.util.RsaUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Api(description = "通灵支付")
@RestController
@RequestMapping(value = "/allInPay/")
public class AllInPayController {

    @ApiOperation(value = "消费接口")
    @RequestMapping(value = "/pay", method = RequestMethod.GET)
    public String payTest(HttpServletResponse response) throws Exception {
        AllinPayConsumeDto consumeDto = new AllinPayConsumeDto();
        consumeDto.setApp_key("testhn");
        consumeDto.setAmount("600.00");
        consumeDto.setChannel("1");
        consumeDto.setComment("商户提交备注");
        consumeDto.setDescription("产品描述或名称");
        consumeDto.setFormat("json");
        consumeDto.setMer_id("999290053990002");
        consumeDto.setMethod("allinpay.order.orderinstall.add");
        consumeDto.setNotify_url("http://119.23.104.77:8080/allInPay/notify");
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String newTime = df.format(date);
        Random random = new Random();
        int nextInt = random.nextInt(1000);
        consumeDto.setOrder_id(newTime+ nextInt);
        consumeDto.setReturn_url("http://119.23.104.77:8080/allInPay/return");
        consumeDto.setSign_v("1");
        consumeDto.setTimestamp(newTime);
        consumeDto.setTrade_date(newTime.substring(0,8));
        consumeDto.setTrade_time(newTime.substring(8));
        consumeDto.setV("1.0");
        consumeDto.setNper("6");
        consumeDto.setPdno("0200");
        consumeDto.setMer_order_id("SH"+newTime+nextInt);

        String path = AllInPayController.class.getClassLoader().getResource("") + "cert" + File.separator + "private.pfx";
        String keyPath = path.substring(6);
        String privatePwd = "123456";
        String reqUrl = "https://gateway.allinpaycard.com/asaop/rest/api";
        AllinPayConsume consume = new AllinPayConsume(consumeDto, keyPath, privatePwd, reqUrl);
        System.out.println(consume.getRequestHtml());
        response.reset();
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().print(consume.getRequestHtml());
        response.getWriter().flush();
        response.getWriter().close();
        return null;
    }

    @ApiOperation(value = "后台接收地址")
    @RequestMapping(value = "/notify",method = RequestMethod.POST)
    public Map<String,Object> notify(HttpServletRequest request){
        String path = AllInPayController.class.getClassLoader().getResource("") + "cert" + File.separator + "public.cer";
        String keyPath = path.substring(6);

        Map<String, String> param = getAllRequestParam(request);
        boolean b = RsaUtil.checkSign((HashMap<String, String>) param, keyPath);
        if(b){
            System.out.println("========验证成功========");
            System.out.println("========响应报文开始========");
            System.out.println(JSON.toJSON(param));
            System.out.println("========响应报文结束========");
        }
        return null;
    }

    @ApiOperation(value = "返回地址")
    @RequestMapping(value = "/return")
    public String returnPage(){
        return "支付成功!";
    }

    public static Map<String, String> getAllRequestParam(
            final HttpServletRequest request) {
        Map<String, String> res = new HashMap<String, String>();
        Enumeration<?> temp = request.getParameterNames();
        if (null != temp) {
            while (temp.hasMoreElements()) {
                String en = (String) temp.nextElement();
                String value = request.getParameter(en);
                res.put(en, value);
                // 在报文上送时，如果字段的值为空，则不上送<下面的处理为在获取所有参数数据时，判断若值为空，则删除这个字段>
                if (res.get(en) == null || "".equals(res.get(en))) {
                    // System.out.println("======为空的字段名===="+en);
                    res.remove(en);
                }
            }
        }
        return res;
    }


}
