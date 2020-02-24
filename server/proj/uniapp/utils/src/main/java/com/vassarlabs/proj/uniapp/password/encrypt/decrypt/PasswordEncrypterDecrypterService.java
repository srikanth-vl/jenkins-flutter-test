package com.vassarlabs.proj.uniapp.password.encrypt.decrypt;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncrypterDecrypterService {

	public String getEncryptedPassword(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt());
	}
	
	public boolean matchPassword(String textPassword, String hashedPassword) {
		return BCrypt.checkpw(textPassword, hashedPassword);
	}
}
	