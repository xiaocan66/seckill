package cn.xiaocan.seckill.mq;

import cn.xiaocan.seckill.bo.GoodsBo;
import cn.xiaocan.seckill.entity.SeckillOrder;
import cn.xiaocan.seckill.entity.User;
import cn.xiaocan.seckill.redis.RedisService;
import cn.xiaocan.seckill.service.OrderService;
import cn.xiaocan.seckill.service.SeckillGoodsService;
import cn.xiaocan.seckill.service.SeckillOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {

		private static Logger log = LoggerFactory.getLogger(MQReceiver.class);
		
		@Autowired
		RedisService redisService;
		
		@Autowired
		SeckillGoodsService goodsService;
		
		@Autowired
		OrderService orderService;
		
		@Autowired
		SeckillOrderService seckillOrderService;
		
		@RabbitListener(queues=MQConfig.MIAOSHA_QUEUE)
		public void receive(String message) {
			log.info("receive message:"+message);
			SeckillMessage mm  = RedisService.stringToBean(message, SeckillMessage.class);
			User user = mm.getUser();
			long goodsId = mm.getGoodsId();
			
			GoodsBo goods = goodsService.getseckillGoodsBoByGoodsId(goodsId);
	    	int stock = goods.getStockCount();
	    	if(stock <= 0) {
	    		return;
	    	}
	    	//判断是否已经秒杀到了
	    	SeckillOrder order = seckillOrderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
	    	if(order != null) {
	    		return;
	    	}
	    	//减库存 下订单 写入秒杀订单
			seckillOrderService.insert(user, goods);
		}
}
