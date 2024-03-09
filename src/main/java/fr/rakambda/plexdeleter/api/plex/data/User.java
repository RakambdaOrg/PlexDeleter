package fr.rakambda.plexdeleter.api.plex.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "user")
@XmlAccessorType(XmlAccessType.FIELD)
public class User{
	@Nullable
	@XmlAttribute
	private Integer id;
}
