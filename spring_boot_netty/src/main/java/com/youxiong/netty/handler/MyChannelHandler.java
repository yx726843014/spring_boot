package com.youxiong.netty.handler;

import com.youxiong.netty.util.GlobalUserUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyChannelHandler extends SimpleChannelInboundHandler<Object> {


    private static final Logger LOGGER = LoggerFactory.getLogger(MyChannelHandler.class);

    private static final String URI = "websocket";

    private WebSocketServerHandshaker handshaker ;

    /**
     * 连接上服务器
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("【handlerAdded】====>"+ctx.channel().id());
        GlobalUserUtil.channels.add(ctx.channel());
    }

    /**
     * 断开连接
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("【handlerRemoved】====>"+ctx.channel().id());
        GlobalUserUtil.channels.remove(ctx);
    }

    /**
     * 连接异常   需要关闭相关资源
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("【系统异常】======>"+cause.toString());
        ctx.close();
        ctx.channel().close();
    }

    /**
     * 活跃的通道  也可以当作用户连接上客户端进行使用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("【channelActive】=====>"+ctx.channel());
    }

    /**
     * 不活跃的通道  就说明用户失去连接
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    }

    /**
     * 这里只要完成 flush
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 这里是保持服务器与客户端长连接  进行心跳检测 避免连接断开
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent stateEvent = (IdleStateEvent) evt;
            PingWebSocketFrame ping = new PingWebSocketFrame();
            switch (stateEvent.state()){
                //读空闲（服务器端）
                case READER_IDLE:
                    LOGGER.info("【"+ctx.channel().remoteAddress()+"】读空闲（服务器端）");
                    ctx.writeAndFlush(ping);
                    break;
                    //写空闲（客户端）
                case WRITER_IDLE:
                    LOGGER.info("【"+ctx.channel().remoteAddress()+"】写空闲（客户端）");
                    ctx.writeAndFlush(ping);
                    break;
                case ALL_IDLE:
                    LOGGER.info("【"+ctx.channel().remoteAddress()+"】读写空闲");
                    break;
            }
        }
    }

    /**
     * 收发消息处理
     * @param ctx
     * @param msg
     * @throws Exception
     */
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof HttpRequest){
            doHandlerHttpRequest(ctx,(HttpRequest) msg);
        }else if(msg instanceof WebSocketFrame){
            doHandlerWebSocketFrame(ctx,(WebSocketFrame) msg);
        }
    }

    /**
     * websocket消息处理
     * @param ctx
     * @param msg
     */
    private void doHandlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg) {
        //判断msg 是哪一种类型  分别做出不同的反应
        if(msg instanceof CloseWebSocketFrame){
            LOGGER.info("【关闭】");
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) msg);
            return ;
        }
        if(msg instanceof PingWebSocketFrame){
            LOGGER.info("【ping】");
            PongWebSocketFrame pong = new PongWebSocketFrame(msg.content().retain());
            ctx.channel().writeAndFlush(pong);
            return ;
        }
        if(msg instanceof PongWebSocketFrame){
            LOGGER.info("【pong】");
            PingWebSocketFrame ping = new PingWebSocketFrame(msg.content().retain());
            ctx.channel().writeAndFlush(ping);
            return ;
        }
        if(!(msg instanceof TextWebSocketFrame)){
            LOGGER.info("【不支持二进制】");
            throw new UnsupportedOperationException("不支持二进制");
        }
        //可以对消息进行处理
        //群发
        for (Channel channel : GlobalUserUtil.channels) {
            channel.writeAndFlush(new TextWebSocketFrame(((TextWebSocketFrame) msg).text()));
        }

    }


    /**
     * wetsocket第一次连接握手
     * @param ctx
     * @param msg
     */
    private void doHandlerHttpRequest(ChannelHandlerContext ctx, HttpRequest msg) {
        // http 解码失败
        if(!msg.getDecoderResult().isSuccess() || (!"websocket".equals(msg.headers().get("Upgrade")))){
            sendHttpResponse(ctx, (FullHttpRequest) msg,new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.BAD_REQUEST));
        }
        //可以获取msg的uri来判断
        String uri = msg.getUri();
        if(!uri.substring(1).equals(URI)){
            ctx.close();
        }
        ctx.attr(AttributeKey.valueOf("type")).set(uri);
        //可以通过url获取其他参数
        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(
                "ws://"+msg.headers().get("Host")+"/"+URI+"",null,false
        );
        handshaker = factory.newHandshaker(msg);
        if(handshaker == null){
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        }
        //进行连接
        handshaker.handshake(ctx.channel(), (FullHttpRequest) msg);
        //可以做其他处理
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        // 如果是非Keep-Alive，关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
