package fr.rakambda.plexdeleter.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class LanguageFlagService{
	private static final Map<String, String> ISO3_TO_COUNTRY_CACHE = new HashMap<>();
	static{
		ISO3_TO_COUNTRY_CACHE.put("ara", "sa"); // Arabic -> Saudi Arabia
		ISO3_TO_COUNTRY_CACHE.put("eng", "gb"); // English -> Great Britain
		ISO3_TO_COUNTRY_CACHE.put("spa", "es"); // Spanish -> Spain
		ISO3_TO_COUNTRY_CACHE.put("eus", "es-pv"); // Basque (Regional)
		ISO3_TO_COUNTRY_CACHE.put("glg", "es-ga"); // Galician (Regional)
		ISO3_TO_COUNTRY_CACHE.put("fil", "ph"); // Filipino -> Philippines
		ISO3_TO_COUNTRY_CACHE.put("nob", "no"); // Norwegian Bokmål -> Norway
		ISO3_TO_COUNTRY_CACHE.put("zho", "cn"); // Chinese -> China
		ISO3_TO_COUNTRY_CACHE.put("cho", "cn"); // Chinese (alt) -> China
		ISO3_TO_COUNTRY_CACHE.put("ell", "gr"); // Greek -> Greece
		ISO3_TO_COUNTRY_CACHE.put("hye", "am"); // Armenian -> Armenia
		ISO3_TO_COUNTRY_CACHE.put("enm", "jp"); // English (Weeb) -> Japan (Fun mapping)
		
		for(var locale : Locale.getAvailableLocales()){
			try{
				var iso3 = locale.getISO3Language().toLowerCase();
				var country = locale.getCountry().toLowerCase();
				
				if(!ISO3_TO_COUNTRY_CACHE.containsKey(iso3) && !country.isEmpty()){
					ISO3_TO_COUNTRY_CACHE.put(iso3, country);
				}
			}
			catch(Exception ignored){
			}
		}
	}
	public String getFlagUrl(String iso3Code){
		var countryCode = ISO3_TO_COUNTRY_CACHE.getOrDefault(iso3Code.toLowerCase(), "un");
		return String.format("https://flagcdn.com/w80/%s.png", countryCode);
	}
}