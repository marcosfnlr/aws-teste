package batata.aws_api_test;

import java.util.Date;

import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.CreateDBInstanceRequest;
import com.amazonaws.services.rds.model.CreateDBSnapshotRequest;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DeleteDBInstanceRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.amazonaws.services.rds.model.RestoreDBInstanceFromDBSnapshotRequest;
import com.amazonaws.services.rds.model.StartDBInstanceRequest;
import com.amazonaws.services.rds.model.StopDBInstanceRequest;
import com.amazonaws.services.rds.model.VpcSecurityGroupMembership;

public class TesteRDS {
	private AmazonRDS rds;

	public TesteRDS(AmazonRDS rds) {
		this.rds = rds;
	}

	public void descreverInstancias(AmazonRDS rds) {
		DescribeDBInstancesRequest describeDBInstancesRequest = new DescribeDBInstancesRequest();
		DescribeDBInstancesResult response = rds.describeDBInstances(describeDBInstancesRequest);

		for (DBInstance instance : response.getDBInstances()) {
			System.out.printf(
					"Found instance with:\n" + " - id: %s;\n" + " - endpoint: address %s, port %s\n" + " - status: %s\n"
							+ " - alloc. storage: %s\n" + " - instance class: %s\n" + " - engine: %s - %s\n"
							+ " - master: %s\n" + " - license model: %s\n" + " - storage type: %s\n" + " - vpc: %s\n"
							+ " - subnet group: %s\n" + " - public access: %s\n" + " - AZ: %s\n",
					instance.getDBInstanceIdentifier(), instance.getEndpoint().getAddress(),
					instance.getEndpoint().getPort(), instance.getDBInstanceStatus(), instance.getAllocatedStorage(),
					instance.getDBInstanceClass(), instance.getEngine(), instance.getEngineVersion(),
					instance.getMasterUsername(), instance.getLicenseModel(), instance.getStorageType(),
					instance.getDBSubnetGroup().getVpcId(), instance.getDBSubnetGroup().getDBSubnetGroupName(),
					instance.getPubliclyAccessible(), instance.getAvailabilityZone());
			for (VpcSecurityGroupMembership sg : instance.getVpcSecurityGroups()) {
				System.out.println(" - sg: " + sg.getVpcSecurityGroupId());
			}
		}
	}

	public String criarInstancia(String instanceName, DBInstance instance, String securityGroupId) {
		CreateDBInstanceRequest createDBInstanceRequest = new CreateDBInstanceRequest()
				.withDBInstanceIdentifier(instanceName).withAllocatedStorage(20).withDBInstanceClass("db.t2.micro")
				.withEngine("MySQL").withMasterUsername("root").withMasterUserPassword("password").withDBName("meudb")
				.withEngineVersion("5.6.37").withLicenseModel("general-public-license").withStorageType("gp2")
				.withVpcSecurityGroupIds("sg-4b994120");
		return rds.createDBInstance(createDBInstanceRequest).getDBInstanceIdentifier();
	}

	public void pararInstancia(String id) {
		StopDBInstanceRequest stopDBInstanceRequest = new StopDBInstanceRequest().withDBInstanceIdentifier(id);
		rds.stopDBInstance(stopDBInstanceRequest);
	}

	public void iniciarInstancia(String id) {
		StartDBInstanceRequest startDBInstanceRequest = new StartDBInstanceRequest().withDBInstanceIdentifier(id);
		rds.startDBInstance(startDBInstanceRequest);
	}

	public void removerInstancia(String id) {
		DeleteDBInstanceRequest deleteDBInstanceRequest = new DeleteDBInstanceRequest(id).withSkipFinalSnapshot(true);
		rds.deleteDBInstance(deleteDBInstanceRequest);
	}

	public String criarSnapshot(String id) {
		CreateDBSnapshotRequest createDBSnapshotRequest = new CreateDBSnapshotRequest().withDBInstanceIdentifier(id)
				.withDBSnapshotIdentifier("batata-" + id + new Date().getTime());
		return rds.createDBSnapshot(createDBSnapshotRequest).getDBSnapshotIdentifier();
	}

	public String restaurarInstancia(String dBSnapshotIdentifier, String dBInstanceIdentifier) {
		RestoreDBInstanceFromDBSnapshotRequest request = new RestoreDBInstanceFromDBSnapshotRequest(
				dBInstanceIdentifier, dBSnapshotIdentifier);
		return rds.restoreDBInstanceFromDBSnapshot(request).getDBInstanceIdentifier();
	}
}
