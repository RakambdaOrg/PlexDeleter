package fr.rakambda.plexdeleter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import java.util.Properties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecretsUtils{
	@SneakyThrows
	public static String getSecret(String key){
		var secrets = new Properties();
		secrets.load(SecretsUtils.class.getResourceAsStream("/secrets.properties"));
		return secrets.getProperty(key);
	}
}
