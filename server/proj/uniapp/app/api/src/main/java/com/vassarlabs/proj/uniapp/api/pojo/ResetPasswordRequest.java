package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
	@JsonProperty("super_app")
	UUID superAppId;

	@JsonProperty("user_id")
	String userId;

	@JsonProperty("otp")
	int otp;

	@JsonProperty("new_password")
	String newPassword;

}
