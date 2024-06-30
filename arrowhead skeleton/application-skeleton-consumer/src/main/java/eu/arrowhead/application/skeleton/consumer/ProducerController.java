package eu.arrowhead.application.skeleton.consumer;

import eu.arrowhead.application.skeleton.consumer.dto.ProducerRequestDTO;
import org.springframework.web.bind.annotation.*;

/**
 * @author : Ricardo Venâncio - 1210828
 **/

@RestController
@RequestMapping("/api/producer")
public class ProducerController {

    @PostMapping
    public void addProducer(@RequestBody ProducerRequestDTO producerReq) {

    }

    @DeleteMapping
    public void deleteProducer(@RequestBody ProducerRequestDTO producerReq) {

    }
}
