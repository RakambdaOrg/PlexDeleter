package fr.rakambda.plexdeleter.api.tautulli.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TautulliResponse<T>{
	private String result;
	private String message;
	private T data;
}
