package com.youxiong.web;

import com.allinpay.model.AllinPayConsumeDto;
import com.allinpay.service.AllinPayConsume;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Map;

@Api(description = "通灵支付")
@RestController
@RequestMapping(value = "/allInPay/")
public class AllInPayController {

    @ApiOperation(value = "消费接口")
    @RequestMapping(value = "/pay", method = RequestMethod.POST)
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
        consumeDto.setNotify_url("http://localhost:8080/Notify_url");
        consumeDto.setOrder_id("20160628165423001");
        consumeDto.setReturn_url("http://localhost:8080/Return_url");
        consumeDto.setSign_v("1");
        consumeDto.setTimestamp("20160628165423");
        consumeDto.setTrade_date("20160628");
        consumeDto.setTrade_time("165214");
        consumeDto.setV("1.0");
        consumeDto.setNper("6");
        consumeDto.setPdno("0200");
        consumeDto.setMer_order_id("SH20160628165423001");

        String keyPath = "D:/cert/private.pfx";
        System.out.println(AllInPayController.class.getClassLoader().getResource("")+"cert"+ File.separator+"private.pfx");
        String privatePwd = "123456";
        String reqUrl = "https://gateway.allinpaycard.com/asaop/rest/api";
        AllinPayConsume consume = new AllinPayConsume(consumeDto, keyPath, privatePwd, reqUrl);
        System.out.println(consume.getRequestHtml());
        /*response.getWriter().print(consume.getRequestHtml());*/
        return consume.getRequestHtml();
    }
}
