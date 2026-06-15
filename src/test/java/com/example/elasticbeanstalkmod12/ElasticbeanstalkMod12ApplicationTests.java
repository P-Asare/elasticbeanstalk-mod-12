package com.example.elasticbeanstalkmod12;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@SpringBootTest
class ElasticbeanstalkMod12ApplicationTests {

	@MockitoBean
	DynamoDbClient dynamoDbClient;

	@Test
	void contextLoads() {
	}

}
