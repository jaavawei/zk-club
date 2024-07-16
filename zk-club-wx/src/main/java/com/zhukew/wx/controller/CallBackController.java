package com.zhukew.wx.controller;

import com.zhukew.wx.handler.WxChatMsgFactory;
import com.zhukew.wx.handler.WxChatMsgHandler;
import com.zhukew.wx.utils.MessageUtil;
import com.zhukew.wx.utils.SHA1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

/**
 * 微信回调Controller
 *
 * @author: Wei
 */
@RestController
@Slf4j
public class CallBackController {

    /**
     * 盐
     */
    private static final String token = "adwidhaidwoaid";

    @Resource
    private WxChatMsgFactory wxChatMsgFactory;

    @RequestMapping("/test")
    public String test() {
        return "hello world";
    }

    /**
     * 回调消息校验
     */
    @GetMapping("callback")
    public String callback(@RequestParam("signature") String signature,
                           @RequestParam("timestamp") String timestamp,
                           @RequestParam("nonce") String nonce,
                           @RequestParam("echostr") String echostr) {
        log.info("get验签请求参数：signature:{}，timestamp:{}，nonce:{}，echostr:{}",
                signature, timestamp, nonce, echostr);
        String shaStr = SHA1.getSHA1(token, timestamp, nonce, "");
        if (signature.equals(shaStr)) {
            // 解析成功
            return echostr;
        }
        return "unknown";
    }

    @PostMapping(value = "callback", produces = "application/xml;charset=UTF-8")
    public String callback(
            @RequestBody String requestBody,
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam(value = "msg_signature", required = false) String msgSignature) {
        log.info("接收到微信消息：requestBody：{}", requestBody);
        // 解析微信消息
        Map<String, String> messageMap = MessageUtil.parseXml(requestBody);
        String msgType = messageMap.get("MsgType");
        String event = messageMap.get("Event") == null ? "" : messageMap.get("Event");
        log.info("msgType:{},event:{}", msgType, event);

        StringBuilder sb = new StringBuilder();
        sb.append(msgType);
        if (!StringUtils.isEmpty(event)) {
            sb.append(".");
            sb.append(event);
        }
        String msgTypeKey = sb.toString();
        // 工厂 + 策略模式，根据微信消息的内容从工厂中拿到不同的 handler
        WxChatMsgHandler wxChatMsgHandler = wxChatMsgFactory.getHandlerByMsgType(msgTypeKey);
        if (Objects.isNull(wxChatMsgHandler)) {
            return "unknown";
        }
        String replyContent = wxChatMsgHandler.dealMsg(messageMap);
        log.info("replyContent:{}", replyContent);
        return replyContent;
    }


}
