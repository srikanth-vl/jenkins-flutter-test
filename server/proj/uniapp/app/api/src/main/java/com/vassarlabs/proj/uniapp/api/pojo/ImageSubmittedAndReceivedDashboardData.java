package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageSubmittedAndReceivedDashboardData {
    EntityDetails superAppDetails;	
	EntityDetails applicationDetails;
	EntityDetails projectDetails;
	List<UUID> submittedImageIds;
	List<UUID> receivedImageIds;
	List<UUID> textDataRelayed;
	List<UUID> mediaFileRelayed;
	int submittedCount;
	int receivedCount;
	int textDataRelayCount;
	int mediaFileRelayCount;
	long insertTS;
}
