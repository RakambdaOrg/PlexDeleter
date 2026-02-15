package fr.rakambda.plexdeleter.api.seerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(ProductionCompany.class)
public class ProductionCompany{
	private int id;
	private String name;
	private String originalCountry;
	private String logoPath;
}
