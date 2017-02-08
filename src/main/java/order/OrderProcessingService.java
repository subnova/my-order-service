package order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderProcessingService {
    private final Logger logger = LoggerFactory.getLogger(OrderProcessingService.class);

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @StreamListener(Sink.INPUT)
    public void handle(Map<String, Object> order) {
        logger.info("Received order {} with id {} from Kafka", order, order.get("orderId"));
        redisTemplate.opsForHash().putAll(order.get("orderId"), order);
    }
}
