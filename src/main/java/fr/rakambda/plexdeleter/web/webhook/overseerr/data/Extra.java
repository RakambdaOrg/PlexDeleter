package fr.rakambda.plexdeleter.web.webhook.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Extra{
	@NonNull
	private String name;
	@NonNull
	private String value;
}
