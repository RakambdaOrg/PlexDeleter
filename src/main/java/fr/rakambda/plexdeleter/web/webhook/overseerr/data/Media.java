package fr.rakambda.plexdeleter.web.webhook.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Media{
	@Nullable
	private Integer tvdbId;
}
