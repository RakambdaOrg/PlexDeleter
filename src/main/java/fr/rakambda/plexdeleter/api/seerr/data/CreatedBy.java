package fr.rakambda.plexdeleter.api.seerr.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(CreatedBy.class)
public class CreatedBy{
	private int id;
	@JsonProperty("credit_id")
	private String creditId;
	private String name;
	private int gender;
	@JsonProperty("profile_path")
	private String profilePath;
}
