package batata.aws_api_test;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;

public class App {

	public static void main(String[] args) {
		AmazonRDSClientBuilder builderrds = AmazonRDSClientBuilder.standard();
		AmazonRDS rds = builderrds.build();
		new TesteRDS(rds);

		AmazonEC2ClientBuilder builderec2 = AmazonEC2ClientBuilder.standard();
		AmazonEC2 ec2 = builderec2.build();
		new TesteEC2(ec2);
	}

}
