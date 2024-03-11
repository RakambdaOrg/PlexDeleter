package fr.rakambda.plexdeleter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.lang.NonNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailConfiguration{
	@NotNull
	@NonNull
	private String fromAddress;
	@NotNull
	@NonNull
	private String fromName;
	@Nullable
	private List<String> bccAddresses;
}
