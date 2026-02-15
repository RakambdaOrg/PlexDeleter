package fr.rakambda.plexdeleter.web.webhook.seerr.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request{
	@JsonProperty("request_id")
	private int requestId;
}
