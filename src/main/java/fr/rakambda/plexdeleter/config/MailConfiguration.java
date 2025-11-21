package fr.rakambda.plexdeleter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailConfiguration{
	@NonNull
	private String fromAddress;
	@NonNull
	private String fromName;
	@Nullable
	private List<String> bccAddresses;
}
