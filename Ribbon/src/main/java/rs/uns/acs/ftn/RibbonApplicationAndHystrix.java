package rs.uns.acs.ftn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import rs.uns.acs.ftn.services.HystrixService;


@Configuration
class MyConfiguration {

    @LoadBalanced
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

//@SpringBootApplication
//@EnableDiscoveryClient
//@EnableCircuitBreaker
@SpringCloudApplication
@RestController
@EnableFeignClients
public class RibbonApplicationAndHystrix {

	@Autowired
	private LoadBalancerClient lb;
	
	@Autowired
	private RestTemplate rest;
	
	@Autowired
	private Hello hello;// feign client
	
	@Autowired
	private HystrixService hystrixService;
	
	@RequestMapping("/choose")// Do load-balancing among the user-service instances
	public ServiceInstance choose(){
		return lb.choose("user-service");
	}
	
	@RequestMapping(value = "/", produces = "text/html")
	public String produce(){
		//String s = rest.getForObject("http://user-service/users/hello/?name={name}", String.class, "IGIS");//using rest template
		
		//String s = hello.hello("IGIS"); // feign REST client call
		
		String s = hystrixService.hello("IGIS"); // call fegin client hello method throught hystrix call
		
		return String.format("<html>"
								+ "<head>"
									+ "<title>TEST</title>"
								+ "</head>"
									+ "<body>"
										+ "<h1>%s</h1>"
									+ "</body>"
								+ "</html>", s);
	}
	
	@FeignClient("user-service")
	public interface Hello {
		@RequestMapping(value = "users/hello", method = RequestMethod.GET)
		String hello(@RequestParam(name = "name") String name);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(RibbonApplicationAndHystrix.class, args);
	}
}