package com.vassarlabs.prod.messaging.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Message {

	String to;
	List<String> toList;
	String subject;
	String message;
	List<String> attachments;
}
