package fr.rakambda.plexdeleter.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.io.IOException;
import java.time.Instant;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InstantAsStringWithoutNanosSerializerTest{
	@Mock
	private JsonGenerator jsonGenerator;
	@Mock
	private SerializerProvider serializerProvider;
	
	private final InstantAsStringWithoutNanosSerializer tested = new InstantAsStringWithoutNanosSerializer();
	
	@Test
	void itShouldWriteWithSeconds() throws IOException{
		tested.serialize(Instant.parse("2024-04-05T10:15:42.123456789Z"), jsonGenerator, serializerProvider);
		
		verify(jsonGenerator).writeString("2024-04-05T10:15:42Z");
	}
}