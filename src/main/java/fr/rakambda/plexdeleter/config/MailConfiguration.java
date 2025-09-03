package fr.rakambda.plexdeleter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.lang.NonNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailConfiguration{
	@NonNull
	@org.jspecify.annotations.NonNull
	private String fromAddress;
	@NonNull
	@org.jspecify.annotations.NonNull
	private String fromName;
	@Nullable
	private List<String> bccAddresses;
}
