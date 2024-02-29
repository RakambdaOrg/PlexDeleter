package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeasonInfo{
	private int id;
	private int seasonNumber;
	private int status;
	private int status4k;
	private Instant createdAt;
	private Instant updatedAt;
}
