package fr.rakambda.plexdeleter.api.overseerr.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatedBy{
	private int id;
	@JsonProperty("credit_id")
	private String creditId;
	private String name;
	private int gender;
	@JsonProperty("profile_path")
	private String profilePath;
}
