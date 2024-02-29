package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Crew{
	private String creditId;
	private String department;
	private int id;
	private String job;
	private String name;
	private int gender;
	private String profilePath;
}
