package batata.aws_api_test;

import java.util.List;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeregisterImageRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.ImageState;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceBlockDeviceMapping;
import com.amazonaws.services.ec2.model.InstanceNetworkInterface;
import com.amazonaws.services.ec2.model.ProductCode;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class TesteEC2 {

	private AmazonEC2 ec2;

	public TesteEC2(AmazonEC2 ec2) {
		this.ec2 = ec2;
	}

	public void iniciarInstancias(List<String> ids) {
		StartInstancesRequest startRequest = new StartInstancesRequest()
				.withInstanceIds((String[]) ids.toArray(new String[ids.size()]));
		ec2.startInstances(startRequest);
	}

	public void pararInstancias(List<String> ids) {
		StopInstancesRequest stopRequest = new StopInstancesRequest().withInstanceIds(ids);
		ec2.stopInstances(stopRequest);
	}

	public void removerInstancias(List<String> ids) {
		TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest().withInstanceIds(ids);
		ec2.terminateInstances(terminateInstancesRequest);
	}

	public String criarImagem(Instance instance) {
		CreateImageRequest createImageRequest = new CreateImageRequest(instance.getInstanceId(),
				"ami-" + instance.getInstanceId());
		return ec2.createImage(createImageRequest).getImageId();
	}

	public String criarInstancia(Instance molde, String imageId) {
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

		runInstancesRequest.withImageId(imageId == null ? molde.getImageId() : imageId)
				.withInstanceType(molde.getInstanceType()).withMinCount(1).withMaxCount(1)
				.withKeyName(molde.getKeyName()).withSecurityGroupIds(molde.getSecurityGroups().get(0).getGroupId());

		RunInstancesResult result = ec2.runInstances(runInstancesRequest);
		String newImageId = "";
		for (Instance instance : result.getReservation().getInstances()) {
			criarTagName("Batata", instance.getInstanceId());
			newImageId = instance.getInstanceId();
		}
		return newImageId;
	}

	public void criarSnapshot(Instance instance) {
		List<InstanceBlockDeviceMapping> mappingList = instance.getBlockDeviceMappings();
		for (InstanceBlockDeviceMapping mapping : mappingList) {
			CreateSnapshotRequest createSnapshotRequest = new CreateSnapshotRequest()
					.withVolumeId(mapping.getEbs().getVolumeId())
					.withDescription("Batata: " + instance.getInstanceId());
			ec2.createSnapshot(createSnapshotRequest);
			System.out.println(mapping.getEbs().getVolumeId());
		}
	}

	public void criarTagName(String name, String resource) {
		CreateTagsRequest createTagsRequest = new CreateTagsRequest().withResources(resource)
				.withTags(new Tag("Name", name));
		ec2.createTags(createTagsRequest);
	}

	public void clonarInstancia(Instance instance) {
		String imageId = criarImagem(instance);
		DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest().withImageIds(imageId);
		boolean avaiable = false;
		while (!avaiable) {
			List<Image> images = ec2.describeImages(describeImagesRequest).getImages();
			if (images.get(0).getState().equalsIgnoreCase(ImageState.Available.name())) {
				avaiable = true;
			}
			try {
				System.out.println("zzZzZzZZ");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		detalharImagem(criarInstancia(instance, imageId));
		DeregisterImageRequest deregisterImageRequest = new DeregisterImageRequest(imageId);
		ec2.deregisterImage(deregisterImageRequest);
	}

	public void detalharImagem(String instanceId) {

		DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instanceId);
		boolean done = false;

		while (!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);

			for (Reservation reservation : response.getReservations()) {
				for (Instance instance : reservation.getInstances()) {
					System.out.printf(
							"Found instance with id %s, " + "AMI %s, " + "IP %s, " + "type %s, " + "state %s "
									+ ", monitoring state %s\n",
							instance.getInstanceId(), instance.getImageId(), instance.getPublicIpAddress(),
							instance.getInstanceType(), instance.getState().getName(),
							instance.getMonitoring().getState());
					for (InstanceNetworkInterface eni : instance.getNetworkInterfaces()) {
						System.out.printf(" - ENI: %s\n", eni.getNetworkInterfaceId());
					}
					for (ProductCode pc : instance.getProductCodes()) {
						System.out.printf(" - Product code: %s\n", pc.getProductCodeId());
					}
					for (Tag tag : instance.getTags()) {
						System.out.printf(" - Tag: %s - %s\n", tag.getKey(), tag.getValue());
					}
				}
			}
			request.setNextToken(response.getNextToken());
			if (response.getNextToken() == null) {
				done = true;
			}
		}
	}
}
