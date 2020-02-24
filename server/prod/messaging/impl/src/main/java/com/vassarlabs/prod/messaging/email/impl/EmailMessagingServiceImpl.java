package com.vassarlabs.prod.messaging.email.impl;

import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.vassarlabs.common.utils.err.InvalidInputParamException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.ConfigurationFileProperties;
import com.vassarlabs.prod.messaging.api.IMessagingService;
import com.vassarlabs.prod.messaging.model.Message;

@Configuration
@Component
@PropertySource(value = "classpath:email.properties")
public class EmailMessagingServiceImpl 
	implements IMessagingService {

	@Autowired
	protected IVLLogService logFactory;
	protected IVLLogger logger;
	@PostConstruct
	protected void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	@Autowired private JavaMailSender emailSender;
	@Autowired ConfigurationFileProperties configFileProperties;
	
	@Value("${spring.mail.host}") private String HOST;
	@Value("${spring.mail.port}") private int PORT;
	@Value("${spring.mail.username}") private String USERNAME;
	@Value("${spring.mail.password}") private String PASSWORD;
	@Value("${spring.mail.protocol}") private String PROTOCOL;
	@Value("${spring.mail.properties.mail.smtp.auth}") private boolean IS_AUTH_ENABLED;
	@Value("${spring.mail.properties.mail.smtp.starttls.enable}") private boolean IS_START_TLS_ENABLE;

	@Override
	public boolean sendMessage(Message msg, boolean hasAttacment) 
		throws InvalidInputParamException, MailException {
		
		validateMessageObject(msg);
		SimpleMailMessage emailMsg = prepareMailMessage(msg);
		if(hasAttacment) {
			MimeMessage mimeMessage = emailSender.createMimeMessage();
			prepareMimeMessage(mimeMessage, emailMsg, msg.getAttachments());
			emailSender.send(mimeMessage);
			return true;
		}
		emailSender.send(emailMsg);
		return true;
	}

	private void prepareMimeMessage(MimeMessage message, SimpleMailMessage simpleMailMessage, List<String> attachmentList) {
		
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(USERNAME);
			helper.setTo(simpleMailMessage.getTo());
			helper.setSubject(simpleMailMessage.getSubject());
			helper.setText(simpleMailMessage.getText());
			
			if(attachmentList == null || attachmentList.isEmpty()) {
				logger.info("No attachments found for message "+ simpleMailMessage);
				return;
			}
			
			if(attachmentList!= null && !attachmentList.isEmpty()) {
				for(String attachment : attachmentList) {
					FileSystemResource file = new FileSystemResource(attachment);
					helper.addAttachment(file.getFilename(), file);
				}
			}
			
		} catch (MessagingException e) {
			e.printStackTrace();
		}
				
	}

	private SimpleMailMessage prepareMailMessage(Message msg) {
		
		SimpleMailMessage emailMsg = new SimpleMailMessage();
		
		if (!CollectionUtils.isEmpty(msg.getToList())) {
			String[] toArr = new String[msg.getToList().size()];
			toArr = msg.getToList().toArray(toArr);
			emailMsg.setTo(toArr);
		}	else {
			emailMsg.setTo(msg.getTo());
		}	
		emailMsg.setText(msg.getMessage());
		emailMsg.setSubject(msg.getSubject());
		return emailMsg;
	}

	private void validateMessageObject(Message msg) throws InvalidInputParamException {
		
		if (msg == null) {
			throw new InvalidInputParamException("Message object is null, can't send message.");
		}
		
		if ((msg.getTo() == null && msg.getToList() == null)
				|| msg.getSubject() == null
				|| msg.getMessage() == null) {
			throw new InvalidInputParamException("Message object not defined properly, can't send message. " + msg);
		}
	}

	@Bean
	public JavaMailSender getJavaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	    mailSender.setHost("smtp.yandex.com");//HOST);
	    mailSender.setPort(465);//PORT);
	    mailSender.setUsername("donotreply@vassarlabs.com");
	    mailSender.setPassword("vA5SA6+1a85");
	     
	    Properties props = mailSender.getJavaMailProperties();
	    props.put("mail.transport.protocol", "smtp");
	    props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.ssl.trust", "smtp.yandex.com");
//	    props.put("mail.smtp.starttls.enable", "true");
	    props.put("mail.debug", "false");
	     
//		mailSender.setHost(HOST);
//	    mailSender.setPort(PORT);
//	     
//	    mailSender.setUsername(USERNAME);
//	    mailSender.setPassword(PASSWORD);
//	     
//	    Properties props = mailSender.getJavaMailProperties();
//	    props.put("mail.transport.protocol", "smtp");
//	    props.put("mail.smtp.auth", IS_AUTH_ENABLED);
//	    props.put("mail.smtp.starttls.enable", IS_START_TLS_ENABLE);
//	    props.put("mail.debug", "true");
	    return mailSender;
	}
}