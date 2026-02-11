package fr.rakambda.plexdeleter.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class LanguageFlagService{
	private static final Map<String, String> FLAG_MAP = new HashMap<>();
	static{
		// Western Europe
		FLAG_MAP.put("fra", "fr"); // French -> France
		FLAG_MAP.put("por", "pt"); // Portuguese -> Portugal
		FLAG_MAP.put("deu", "de"); // German -> Germany (not 'at' Austria)
		FLAG_MAP.put("ita", "it"); // Italian -> Italy
		FLAG_MAP.put("spa", "es"); // Spanish -> Spain
		FLAG_MAP.put("nld", "nl"); // Dutch -> Netherlands (not 'aw' Aruba)
		FLAG_MAP.put("srp", "sr"); // Serbian -> Serbia
		
		// Northern Europe
		FLAG_MAP.put("dan", "dk"); // Danish -> Denmark
		FLAG_MAP.put("fin", "fi"); // Finnish -> Finland
		FLAG_MAP.put("swe", "se"); // Swedish -> Sweden
		FLAG_MAP.put("nob", "no"); // Norwegian -> Norway
		
		// Eastern Europe / Balkans
		FLAG_MAP.put("ces", "cz"); // Czech -> Czechia
		FLAG_MAP.put("pol", "pl"); // Polish -> Poland
		FLAG_MAP.put("hun", "hu"); // Hungarian -> Hungary
		FLAG_MAP.put("ukr", "ua"); // Ukrainian -> Ukraine
		FLAG_MAP.put("rus", "ru"); // Russian -> Russia (not 'ua')
		FLAG_MAP.put("hrv", "hr"); // Croatian -> Croatia
		FLAG_MAP.put("ron", "ro"); // Romanian -> Romania (not 'md')
		FLAG_MAP.put("ell", "gr"); // Greek -> Greece
		
		// Asia / Middle East
		FLAG_MAP.put("jpn", "jp"); // Japanese -> Japan
		FLAG_MAP.put("kor", "kr"); // Korean -> South Korea (not 'cn')
		FLAG_MAP.put("zho", "cn"); // Chinese -> China
		FLAG_MAP.put("tha", "th"); // Thai -> Thailand
		FLAG_MAP.put("vie", "vn"); // Vietnamese -> Vietnam
		FLAG_MAP.put("ind", "id"); // Indonesian -> Indonesia
		FLAG_MAP.put("msa", "my"); // Malay -> Malaysia (not 'bn')
		FLAG_MAP.put("ara", "sa"); // Arabic -> Saudi Arabia
		FLAG_MAP.put("heb", "il"); // Hebrew -> Israel
		FLAG_MAP.put("tur", "tr"); // Turkish -> Turkey (not 'cy')
		FLAG_MAP.put("hye", "am"); // Armenian -> Armenia
		FLAG_MAP.put("hin", "in"); // Hindi -> India
		FLAG_MAP.put("kan", "in"); // Kannada -> India
		FLAG_MAP.put("mal", "in"); // Malayalam -> India
		FLAG_MAP.put("tam", "in"); // Tamil -> India
		FLAG_MAP.put("tel", "in"); // Telugu -> India
		FLAG_MAP.put("guj", "in"); // Gujarati -> India
		FLAG_MAP.put("mar", "in"); // Marathi -> India
		FLAG_MAP.put("ben", "bn"); // Bengali -> Bengladesh
		FLAG_MAP.put("pan", "pk"); // Punjabi -> Pakistan
		FLAG_MAP.put("urd", "pk"); // Urdu -> Pakistan
		
		// English & Others
		FLAG_MAP.put("eng", "gb"); // English -> UK (standard for "English")
		FLAG_MAP.put("enm", "jp"); // English (Weeb) -> Japan
		FLAG_MAP.put("cat", "es"); // Catalan -> Catalonia
		FLAG_MAP.put("eus", "es"); // Basque -> Basque Country
		FLAG_MAP.put("glg", "es"); // Galician -> Galicia
		FLAG_MAP.put("fil", "ph"); // Filipino -> Philippines
		FLAG_MAP.put("nep", "ne"); // Nepali -> Nepal
	}
	public String getFlagUrl(String iso3Code){
		var countryCode = FLAG_MAP.getOrDefault(iso3Code.toLowerCase(), "un");
		return String.format("https://flagcdn.com/w80/%s.png", countryCode);
	}
}
