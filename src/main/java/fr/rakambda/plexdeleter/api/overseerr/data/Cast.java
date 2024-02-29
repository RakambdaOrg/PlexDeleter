package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cast{
	private int castId;
	private String character;
	private String creditId;
	private int id;
	private String name;
	private int order;
	private int gender;
	private String profilePath;
}
