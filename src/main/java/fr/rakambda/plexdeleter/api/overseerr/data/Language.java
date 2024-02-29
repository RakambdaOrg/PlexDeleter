package fr.rakambda.plexdeleter.api.overseerr.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Language{
	@JsonProperty("english_name")
	private String englishName;
	private String iso_639_1;
	private String name;
}
